package com.shabin.aistudysummarizer.controller;

import com.shabin.aistudysummarizer.dto.ApiResponse;
import com.shabin.aistudysummarizer.dto.summary.SummaryRequestDTO;
import com.shabin.aistudysummarizer.dto.summary.SummaryResponse;
import com.shabin.aistudysummarizer.service.ISummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST API Controller for summary management.
 * Handles all summary-related operations with v1 API versioning.
 */
@RestController
@RequestMapping("/api/v1/summaries")
@RequiredArgsConstructor
@Tag(name = "Summaries", description = "Summary generation and management endpoints")
public class SummaryController {

    private final ISummaryService summaryService;

    /**
     * Generate a new summary for a document
     */
    @PostMapping("/generate")
    @Operation(summary = "Generate a new summary", description = "Creates a new summary for the specified document")
    public ResponseEntity<ApiResponse<SummaryResponse>> generateSummary(
            @Valid @RequestBody SummaryRequestDTO request) {
        SummaryResponse response = summaryService.generateSummary(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Summary generated successfully"));
    }

    /**
     * Get user's summaries with pagination
     */
    @GetMapping
    @Operation(summary = "Get user's summaries", description = "Retrieve all summaries for the authenticated user with pagination")
    public ResponseEntity<ApiResponse<Page<SummaryResponse>>> getUserSummaries(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (ASC or DESC)", example = "DESC")
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<SummaryResponse> summaries = summaryService.getUserSummaries(pageable);

        return ResponseEntity.ok(ApiResponse.success(summaries, "Summaries retrieved successfully"));
    }

    /**
     * Get a specific summary by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get summary by ID", description = "Retrieve a specific summary by its unique identifier")
    public ResponseEntity<ApiResponse<SummaryResponse>> getSummaryById(
            @Parameter(description = "Summary ID (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id) {
        SummaryResponse summary = summaryService.getSummaryById(id);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    /**
     * Generate additional MCQs for a summary
     */
    @PostMapping("/{id}/mcqs")
    @Operation(summary = "Generate more MCQs", description = "Generate additional multiple choice questions for an existing summary")
    public ResponseEntity<ApiResponse<SummaryResponse>> generateMoreMcqs(
            @Parameter(description = "Summary ID (UUID)")
            @PathVariable UUID id) {
        SummaryResponse response = summaryService.generateMoreMcqs(id);
        return ResponseEntity.ok(ApiResponse.success(response, "MCQs generated successfully"));
    }

    /**
     * Generate additional flashcards for a summary
     */
    @PostMapping("/{id}/flashcards")
    @Operation(summary = "Generate more flashcards", description = "Generate additional flashcards for an existing summary")
    public ResponseEntity<ApiResponse<SummaryResponse>> generateMoreFlashcards(
            @Parameter(description = "Summary ID (UUID)")
            @PathVariable UUID id) {
        SummaryResponse response = summaryService.generateMoreFlashcards(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Flashcards generated successfully"));
    }

    /**
     * Generate additional summary content
     */
    @PostMapping("/{id}/content")
    @Operation(summary = "Generate more summary content", description = "Generate additional summary content for an existing summary")
    public ResponseEntity<ApiResponse<SummaryResponse>> generateMoreSummary(
            @Parameter(description = "Summary ID (UUID)")
            @PathVariable UUID id) {
        SummaryResponse response = summaryService.generateMoreSummary(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Summary content generated successfully"));
    }

    /**
     * Delete a summary
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete summary", description = "Permanently delete a summary")
    public ResponseEntity<Void> deleteSummary(
            @Parameter(description = "Summary ID (UUID)")
            @PathVariable UUID id) {
        summaryService.deleteSummary(id);
        return ResponseEntity.noContent().build();
    }
}
