package com.shabin.aistudysummarizer.service;

import com.shabin.aistudysummarizer.dto.summary.SummaryRequestDTO;
import com.shabin.aistudysummarizer.dto.summary.SummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Service interface for summary operations.
 * Defines contract for generating and managing summaries.
 */
public interface ISummaryService {

    /**
     * Generate a summary for a document (blocking call)
     */
    SummaryResponse generateSummary(SummaryRequestDTO request);

    /**
     * Generate a summary asynchronously
     */
    @Async
    CompletableFuture<SummaryResponse> generateSummaryAsync(SummaryRequestDTO request);

    /**
     * Get user's summaries with pagination
     */
    Page<SummaryResponse> getUserSummaries(Pageable pageable);

    /**
     * Get specific summary by ID
     */
    SummaryResponse getSummaryById(UUID id);

    /**
     * Generate additional MCQs for existing summary
     */
    SummaryResponse generateMoreMcqs(UUID summaryId);

    /**
     * Generate additional flashcards for existing summary
     */
    SummaryResponse generateMoreFlashcards(UUID summaryId);

    /**
     * Generate additional summary content
     */
    SummaryResponse generateMoreSummary(UUID summaryId);

    /**
     * Delete a summary
     */
    void deleteSummary(UUID id);
}
