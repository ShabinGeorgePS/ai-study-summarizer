package com.shabin.aistudysummarizer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shabin.aistudysummarizer.dto.summary.*;
import com.shabin.aistudysummarizer.entity.*;
import com.shabin.aistudysummarizer.repository.DocumentRepository;
import com.shabin.aistudysummarizer.repository.SummaryRepository;
import com.shabin.aistudysummarizer.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SummaryService {

    private final SummaryRepository summaryRepository;
    private final DocumentRepository documentRepository;
    private final GeminiService geminiService;

    @Value("${gemini.api.model:gemini-2.5-flash}")
    private String modelName;
    private final ObjectMapper objectMapper;

    @Transactional
    public SummaryResponse generateSummary(UUID documentId, int mcqCount) {
        String email = SecurityUtil.getCurrentUserEmail();
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        if (!document.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized access to document");
        }

        String extractedText = document.getExtractedText();
        if (extractedText == null || extractedText.isBlank()) {
            throw new RuntimeException(
                    "Document has no extractable text. Please upload a PDF with selectable text or an image with clear text.");
        }
        if (extractedText.length() > 900_000) {
            extractedText = extractedText.substring(0, 900_000) + "\n\n[Text truncated due to length...]";
        }

        String summaryJson = geminiService.generateSummary(extractedText, mcqCount);

        Summary summary = Summary.builder()
                .user(document.getUser())
                .document(document)
                .summaryJson(summaryJson)
                .modelUsed(modelName)
                .build();

        summaryRepository.save(summary);

        return mapToResponse(summary);
    }

    public List<SummaryResponse> getUserSummaries() {
        String email = SecurityUtil.getCurrentUserEmail();
        return summaryRepository.findByUserEmail(email).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public SummaryResponse getSummary(UUID id) {
        String email = SecurityUtil.getCurrentUserEmail();
        Summary summary = summaryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Summary not found"));

        if (!summary.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized access to summary");
        }

        return mapToResponse(summary);
    }

    @Transactional
    public SummaryResponse generateMoreMcqs(UUID summaryId) {
        String email = SecurityUtil.getCurrentUserEmail();
        Summary summary = summaryRepository.findById(summaryId)
                .orElseThrow(() -> new RuntimeException("Summary not found"));

        if (!summary.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized access to summary");
        }

        Document document = summary.getDocument();
        String extractedText = document.getExtractedText();
        if (extractedText == null || extractedText.isBlank()) {
            throw new RuntimeException("Document has no extractable text");
        }
        if (extractedText.length() > 900_000) {
            extractedText = extractedText.substring(0, 900_000) + "\n\n[Text truncated due to length...]";
        }

        String newMcqsJson = geminiService.generateMoreMcqs(extractedText);
        updateSummaryWithMoreMcqs(summary, newMcqsJson);
        summaryRepository.save(summary);

        return mapToResponse(summary);
    }

    @Transactional
    public SummaryResponse generateMoreFlashcards(UUID summaryId) {
        String email = SecurityUtil.getCurrentUserEmail();
        Summary summary = summaryRepository.findById(summaryId)
                .orElseThrow(() -> new RuntimeException("Summary not found"));

        if (!summary.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized access to summary");
        }

        Document document = summary.getDocument();
        String extractedText = document.getExtractedText();
        if (extractedText == null || extractedText.isBlank()) {
            throw new RuntimeException("Document has no extractable text");
        }
        if (extractedText.length() > 900_000) {
            extractedText = extractedText.substring(0, 900_000) + "\n\n[Text truncated due to length...]";
        }

        String newFlashcardsJson = geminiService.generateMoreFlashcards(extractedText);
        updateSummaryWithMoreFlashcards(summary, newFlashcardsJson);
        summaryRepository.save(summary);

        return mapToResponse(summary);
    }

    @Transactional
    public SummaryResponse generateMoreSummary(UUID summaryId) {
        String email = SecurityUtil.getCurrentUserEmail();
        Summary summary = summaryRepository.findById(summaryId)
                .orElseThrow(() -> new RuntimeException("Summary not found"));

        if (!summary.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized access to summary");
        }

        Document document = summary.getDocument();
        String extractedText = document.getExtractedText();
        if (extractedText == null || extractedText.isBlank()) {
            throw new RuntimeException("Document has no extractable text");
        }
        if (extractedText.length() > 900_000) {
            extractedText = extractedText.substring(0, 900_000) + "\n\n[Text truncated due to length...]";
        }

        String newSummary = geminiService.generateMoreSummary(extractedText);
        updateSummaryWithMoreSummary(summary, newSummary);
        summaryRepository.save(summary);

        return mapToResponse(summary);
    }

    private void updateSummaryWithMoreMcqs(Summary summary, String newMcqsJson) {
        try {
            SummaryContent content = objectMapper.readValue(summary.getSummaryJson(), SummaryContent.class);
            JSONArray newMcqsArray = new JSONArray(newMcqsJson);

            for (int i = 0; i < newMcqsArray.length(); i++) {
                JSONObject mcqObj = newMcqsArray.getJSONObject(i);

                // Convert JSONArray to List<String>
                List<String> options = new java.util.ArrayList<>();
                JSONArray optionsArray = mcqObj.getJSONArray("options");
                for (int j = 0; j < optionsArray.length(); j++) {
                    options.add(optionsArray.getString(j));
                }

                SummaryContent.Mcq mcq = new SummaryContent.Mcq(
                        mcqObj.getString("question"),
                        options,
                        mcqObj.getString("answer"),
                        mcqObj.getString("explanation")
                );
                content.getMcqs().add(mcq);
            }

            String updatedJson = objectMapper.writeValueAsString(content);
            summary.setSummaryJson(updatedJson);
        } catch (Exception e) {
            log.error("Error updating summary with more MCQs", e);
            throw new RuntimeException("Failed to add more MCQs: " + e.getMessage());
        }
    }

    private void updateSummaryWithMoreFlashcards(Summary summary, String newFlashcardsJson) {
        try {
            SummaryContent content = objectMapper.readValue(summary.getSummaryJson(), SummaryContent.class);
            JSONArray newFlashcardsArray = new JSONArray(newFlashcardsJson);

            for (int i = 0; i < newFlashcardsArray.length(); i++) {
                JSONObject flashcardObj = newFlashcardsArray.getJSONObject(i);
                SummaryContent.Flashcard flashcard = new SummaryContent.Flashcard(
                        flashcardObj.getString("front"),
                        flashcardObj.getString("back")
                );
                content.getFlashcards().add(flashcard);
            }

            String updatedJson = objectMapper.writeValueAsString(content);
            summary.setSummaryJson(updatedJson);
        } catch (Exception e) {
            log.error("Error updating summary with more flashcards", e);
            throw new RuntimeException("Failed to add more flashcards: " + e.getMessage());
        }
    }

    private void updateSummaryWithMoreSummary(Summary summary, String newSummaryText) {
        try {
            SummaryContent content = objectMapper.readValue(summary.getSummaryJson(), SummaryContent.class);
            // Append new summary to existing one
            String updatedSummary = content.getExecutiveSummary() + "\n\n--- Alternative Summary ---\n\n" + newSummaryText;
            content.setExecutiveSummary(updatedSummary);

            String updatedJson = objectMapper.writeValueAsString(content);
            summary.setSummaryJson(updatedJson);
        } catch (Exception e) {
            log.error("Error updating summary with more summary", e);
            throw new RuntimeException("Failed to add more summary: " + e.getMessage());
        }
    }

    private SummaryResponse mapToResponse(Summary summary) {
        SummaryContent content = null;
        try {
            String jsonString = summary.getSummaryJson();
            if (jsonString != null && !jsonString.isEmpty()) {
                // Always use readValue with String - handles all cases
                content = objectMapper.readValue(jsonString, SummaryContent.class);
            }
        } catch (Exception e) {
            log.error("Error deserializing summary JSON: {}", e.getMessage());
            // Return response with null content on deserialization error
        }

        return SummaryResponse.builder()
                .id(summary.getId())
                .documentId(summary.getDocument().getId())
                .documentTitle(summary.getDocument().getTitle())
                .content(content)
                .modelUsed(summary.getModelUsed())
                .tokensUsed(summary.getTokensUsed())
                .createdAt(summary.getCreatedAt())
                .build();
    }

    @Transactional
    public void deleteSummary(UUID id) {
        String email = SecurityUtil.getCurrentUserEmail();
        Summary summary = summaryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Summary not found"));

        if (!summary.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized access to summary");
        }

        summaryRepository.delete(summary);
    }
}
