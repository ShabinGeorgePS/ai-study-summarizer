package com.shabin.aistudysummarizer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shabin.aistudysummarizer.dto.summary.SummaryRequestDTO;
import com.shabin.aistudysummarizer.dto.summary.SummaryResponse;
import com.shabin.aistudysummarizer.entity.Document;
import com.shabin.aistudysummarizer.entity.Summary;
import com.shabin.aistudysummarizer.exception.*;
import com.shabin.aistudysummarizer.repository.DocumentRepository;
import com.shabin.aistudysummarizer.repository.SummaryRepository;
import com.shabin.aistudysummarizer.util.RetryUtil;
import com.shabin.aistudysummarizer.util.SecurityUtil;
import com.shabin.aistudysummarizer.util.TextChunkingUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;
import java.util.UUID;

/**
 * Production-grade summary service with chunking, retry logic, and error
 * handling.
 * Implements ISummaryService interface.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SummaryService implements ISummaryService {

    private final SummaryRepository summaryRepository;
    private final DocumentRepository documentRepository;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.model:gemini-2.5-flash}")
    private String modelName;

    /**
     * Generate summary synchronously - for direct API calls
     */
    @Override
    @Transactional
    public SummaryResponse generateSummary(SummaryRequestDTO request) {
        String email = SecurityUtil.getCurrentUserEmail();
        log.info("Generating summary for document {} by user {}", request.getDocumentId(), email);

        // Retrieve and validate document
        Document document = retrieveDocument(request.getDocumentId(), email);
        String extractedText = validateAndPrepareText(document);

        try {
            // Chunk text if needed and generate summary
            String summaryJson = generateSummaryWithChunking(extractedText, request);

            // Save and return summary
            Summary summary = Summary.builder()
                    .user(document.getUser())
                    .document(document)
                    .summaryJson(summaryJson)
                    .modelUsed(modelName)
                    .tokensUsed(estimateTokens(extractedText))
                    .build();

            summaryRepository.save(summary);
            log.info("Summary generated successfully for document {}", request.getDocumentId());

            return mapToResponse(summary);
        } catch (Exception e) {
            log.error("Failed to generate summary for document {}: {}", request.getDocumentId(), e.getMessage(), e);
            throw handleSummaryGenerationError(e);
        }
    }

    /**
     * Generate summary asynchronously - for background processing
     */
    @Override
    @Async
    public CompletableFuture<SummaryResponse> generateSummaryAsync(SummaryRequestDTO request) {
        try {
            SummaryResponse response = generateSummary(request);
            return CompletableFuture.completedFuture(response);
        } catch (Exception e) {
            log.error("Async summary generation failed: {}", e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Get user's summaries with pagination
     */
    @Override
    @Cacheable(value = "userSummaries", key = "#pageable")
    public Page<SummaryResponse> getUserSummaries(Pageable pageable) {
        String email = SecurityUtil.getCurrentUserEmail();
        log.debug("Fetching summaries for user {}, page {}, size {}",
                email, pageable.getPageNumber(), pageable.getPageSize());

        return summaryRepository.findByUserEmail(email, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get specific summary by ID
     */
    @Override
    @Cacheable(value = "summaryById", key = "#id")
    public SummaryResponse getSummaryById(UUID id) {
        String email = SecurityUtil.getCurrentUserEmail();
        log.debug("Fetching summary {} for user {}", id, email);

        Summary summary = summaryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Summary", id.toString()));

        // Verify ownership
        if (!summary.getUser().getEmail().equals(email)) {
            log.warn("Unauthorized access attempt to summary {} by user {}", id, email);
            throw UnauthorizedException.accessDenied();
        }

        return mapToResponse(summary);
    }

    /**
     * Generate more MCQs for existing summary
     */
    @Override
    @Transactional
    @CacheEvict(value = "summaryById", key = "#summaryId")
    public SummaryResponse generateMoreMcqs(UUID summaryId) {
        String email = SecurityUtil.getCurrentUserEmail();
        log.info("Generating more MCQs for summary {} by user {}", summaryId, email);

        Summary summary = retrieveSummary(summaryId, email);
        String extractedText = validateAndPrepareText(summary.getDocument());

        try {
            String newMcqsJson = RetryUtil.executeWithRetry(
                    () -> geminiService.generateMoreMcqs(extractedText),
                    "MCQ Generation");

            JSONObject summaryJson = new JSONObject(summary.getSummaryJson());
            JSONArray existingMcqs = summaryJson.optJSONArray("mcqs");
            JSONArray newMcqs = new JSONArray(newMcqsJson);

            // Append new MCQs
            if (existingMcqs != null) {
                for (int i = 0; i < newMcqs.length(); i++) {
                    existingMcqs.put(newMcqs.get(i));
                }
            } else {
                summaryJson.put("mcqs", newMcqs);
            }

            summary.setSummaryJson(summaryJson.toString());
            summaryRepository.save(summary);

            log.info("Added MCQs to summary {}", summaryId);
            return mapToResponse(summary);
        } catch (SummaryGenerationException e) {
            throw e;
        } catch (Exception e) {
            log.error("MCQ generation failed: {}", e.getMessage(), e);
            throw handleSummaryGenerationError(e);
        }
    }

    /**
     * Generate more flashcards for existing summary
     */
    @Override
    @Transactional
    @CacheEvict(value = "summaryById", key = "#summaryId")
    public SummaryResponse generateMoreFlashcards(UUID summaryId) {
        String email = SecurityUtil.getCurrentUserEmail();
        log.info("Generating more flashcards for summary {} by user {}", summaryId, email);

        Summary summary = retrieveSummary(summaryId, email);
        String extractedText = validateAndPrepareText(summary.getDocument());

        try {
            String newFlashcardsJson = RetryUtil.executeWithRetry(
                    () -> geminiService.generateMoreFlashcards(extractedText),
                    "Flashcard Generation");

            JSONObject summaryJson = new JSONObject(summary.getSummaryJson());
            JSONArray existingFlashcards = summaryJson.optJSONArray("flashcards");
            JSONArray newFlashcards = new JSONArray(newFlashcardsJson);

            // Append new flashcards
            if (existingFlashcards != null) {
                for (int i = 0; i < newFlashcards.length(); i++) {
                    existingFlashcards.put(newFlashcards.get(i));
                }
            } else {
                summaryJson.put("flashcards", newFlashcards);
            }

            summary.setSummaryJson(summaryJson.toString());
            summaryRepository.save(summary);

            log.info("Added flashcards to summary {}", summaryId);
            return mapToResponse(summary);
        } catch (SummaryGenerationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Flashcard generation failed: {}", e.getMessage(), e);
            throw handleSummaryGenerationError(e);
        }
    }

    /**
     * Generate more summary content
     */
    @Override
    @Transactional
    @CacheEvict(value = "summaryById", key = "#summaryId")
    public SummaryResponse generateMoreSummary(UUID summaryId) {
        String email = SecurityUtil.getCurrentUserEmail();
        log.info("Generating more summary content for {} by user {}", summaryId, email);

        Summary summary = retrieveSummary(summaryId, email);
        String extractedText = validateAndPrepareText(summary.getDocument());

        try {
            String newSummaryJson = RetryUtil.executeWithRetry(
                    () -> geminiService.generateMoreSummary(extractedText),
                    "Summary Generation");

            JSONObject summaryJson = new JSONObject(summary.getSummaryJson());
            JSONObject newSummary = new JSONObject(newSummaryJson);

            // Merge summaries
            summaryJson.put("detailed_summary", newSummary.optString("detailed_summary"));
            summaryJson.put("bullet_points", newSummary.optJSONArray("bullet_points"));

            summary.setSummaryJson(summaryJson.toString());
            summaryRepository.save(summary);

            log.info("Updated summary content for {}", summaryId);
            return mapToResponse(summary);
        } catch (SummaryGenerationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Summary content generation failed: {}", e.getMessage(), e);
            throw handleSummaryGenerationError(e);
        }
    }

    /**
     * Delete a summary
     */
    @Override
    @Transactional
    @CacheEvict(value = "summaryById", key = "#id")
    public void deleteSummary(UUID id) {
        String email = SecurityUtil.getCurrentUserEmail();
        log.info("Deleting summary {} by user {}", id, email);

        Summary summary = retrieveSummary(id, email);
        summaryRepository.delete(summary);

        log.info("Summary {} deleted", id);
    }

    // ================== Private Helper Methods ==================

    /**
     * Retrieve document with ownership validation
     */
    private Document retrieveDocument(UUID documentId, String email) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Document", documentId.toString()));

        if (!document.getUser().getEmail().equals(email)) {
            log.warn("Unauthorized access to document {} by user {}", documentId, email);
            throw UnauthorizedException.accessDenied();
        }

        return document;
    }

    /**
     * Retrieve summary with ownership validation
     */
    private Summary retrieveSummary(UUID summaryId, String email) {
        Summary summary = summaryRepository.findById(summaryId)
                .orElseThrow(() -> new EntityNotFoundException("Summary", summaryId.toString()));

        if (!summary.getUser().getEmail().equals(email)) {
            log.warn("Unauthorized access to summary {} by user {}", summaryId, email);
            throw UnauthorizedException.accessDenied();
        }

        return summary;
    }

    /**
     * Validate and prepare extracted text for summarization
     */
    private String validateAndPrepareText(Document document) {
        String extractedText = document.getExtractedText();

        if (extractedText == null || extractedText.isBlank()) {
            throw InvalidFileException.emptyContent();
        }

        // Normalize text
        extractedText = TextChunkingUtil.normalizeText(extractedText);

        if (extractedText.isBlank()) {
            throw InvalidFileException.emptyContent();
        }

        log.debug("Text prepared for summarization, length: {}", extractedText.length());
        return extractedText;
    }

    /**
     * Generate summary with automatic chunking for long documents
     */
    private String generateSummaryWithChunking(String text, SummaryRequestDTO request) {
        // Check if text exceeds token limit
        if (TextChunkingUtil.exceedsTokenLimit(text)) {
            log.warn("Text exceeds token limit, using chunked approach");
            return generateSummaryWithChunks(text, request);
        }

        // Direct summary for shorter text
        return RetryUtil.executeWithRetry(
                () -> geminiService.generateSummary(text, request.getMcqCount()),
                "Summary Generation");
    }

    /**
     * Generate summary by chunking large documents
     */
    private String generateSummaryWithChunks(String text, SummaryRequestDTO request) {
        var chunks = TextChunkingUtil.chunkText(text);
        log.info("Processing {} chunks", chunks.size());

        // Generate summary for each chunk
       int totalMcqs = request.getMcqCount();
int perChunkMcq = Math.max(3, totalMcqs / chunks.size());

StringBuilder combinedSummary = new StringBuilder();

for (int i = 0; i < chunks.size(); i++) {
    final int index = i;

    log.debug("Processing chunk {}/{}", index + 1, chunks.size());

    String chunkSummary = RetryUtil.executeWithRetry(
            () -> geminiService.generateSummary(
                    chunks.get(index),
                    perChunkMcq  // <-- FIXED
            ),
            "Chunk " + (index + 1) + " Summary Generation"
    );

    combinedSummary.append(chunkSummary).append("\n\n");
}

        // Create final comprehensive summary from chunk summaries
        return RetryUtil.executeWithRetry(
                () -> geminiService.generateSummary(combinedSummary.toString(), request.getMcqCount()),
                "Final Summary Generation");
    }

    /**
     * Estimate token count from text (rough estimation)
     */
    private Integer estimateTokens(String text) {
        return (int) (text.length() / 4); // Rough estimation: 4 chars ≈ 1 token
    }

    /**
     * Map Summary entity to response DTO
     */
    private SummaryResponse mapToResponse(Summary summary) {
        try {
            com.shabin.aistudysummarizer.dto.summary.SummaryContent content = objectMapper
                    .readValue(summary.getSummaryJson(), com.shabin.aistudysummarizer.dto.summary.SummaryContent.class);

            return SummaryResponse.builder()
                    .id(summary.getId())
                    .documentId(summary.getDocument().getId())
                    .documentTitle(summary.getDocument().getTitle())
                    .content(content)
                    .modelUsed(summary.getModelUsed())
                    .tokensUsed(summary.getTokensUsed())
                    .createdAt(summary.getCreatedAt())
                    .build();
        } catch (Exception e) {
            log.error("Error mapping summary to response: {}", e.getMessage(), e);
            throw new SummaryGenerationException("Failed to map summary response: " + e.getMessage());
        }
    }

    /**
     * Handle and convert summary generation errors
     */
    private RuntimeException handleSummaryGenerationError(Exception e) {
        if (e instanceof AppException) {
            return (AppException) e;
        }

        if (e instanceof SummaryGenerationException) {
            return (SummaryGenerationException) e;
        }

        if (e.getMessage() != null && e.getMessage().contains("rate limit")) {
            return new SummaryGenerationException("API rate limit exceeded. Please try again later.");
        }

        if (e.getMessage() != null && e.getMessage().contains("timeout")) {
            return new SummaryGenerationException(
                    "Summary generation timed out. Please try again or use a smaller document.");
        }

        return new SummaryGenerationException("Summary generation failed: " + e.getMessage(), e);
    }
}
