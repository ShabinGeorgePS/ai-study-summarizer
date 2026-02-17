package com.shabin.aistudysummarizer.controller;

import com.shabin.aistudysummarizer.dto.summary.SummaryResponse;
import com.shabin.aistudysummarizer.service.GeminiService;
import com.shabin.aistudysummarizer.service.SummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class SummaryController {

    private final SummaryService summaryService;
    private final GeminiService geminiService;

    @PostMapping("/api/summarize/{documentId}")
    public ResponseEntity<SummaryResponse> generateSummary(@PathVariable UUID documentId, @RequestBody(required = false) Map<String, Integer> body) {
        Integer mcqCount = (body != null && body.containsKey("mcqCount")) ? body.get("mcqCount") : 5;
        return ResponseEntity.ok(summaryService.generateSummary(documentId, mcqCount));
    }

    @GetMapping("/api/summaries")
    public ResponseEntity<List<SummaryResponse>> getUserSummaries() {
        return ResponseEntity.ok(summaryService.getUserSummaries());
    }

    @GetMapping("/api/summaries/{id}")
    public ResponseEntity<SummaryResponse> getSummary(@PathVariable UUID id) {
        return ResponseEntity.ok(summaryService.getSummary(id));
    }

    @GetMapping("/api/models")
    public ResponseEntity<List<String>> listGeminiModels() {
        return ResponseEntity.ok(geminiService.listAvailableModels());
    }

    @PostMapping("/api/summaries/{summaryId}/generate-more/mcqs")
    public ResponseEntity<SummaryResponse> generateMoreMcqs(@PathVariable UUID summaryId) {
        return ResponseEntity.ok(summaryService.generateMoreMcqs(summaryId));
    }

    @PostMapping("/api/summaries/{summaryId}/generate-more/flashcards")
    public ResponseEntity<SummaryResponse> generateMoreFlashcards(@PathVariable UUID summaryId) {
        return ResponseEntity.ok(summaryService.generateMoreFlashcards(summaryId));
    }

    @PostMapping("/api/summaries/{summaryId}/generate-more/summary")
    public ResponseEntity<SummaryResponse> generateMoreSummary(@PathVariable UUID summaryId) {
        return ResponseEntity.ok(summaryService.generateMoreSummary(summaryId));
    }

    @DeleteMapping("/api/summaries/{id}")
    public ResponseEntity<Void> deleteSummary(@PathVariable UUID id) {
        summaryService.deleteSummary(id);
        return ResponseEntity.noContent().build();
    }
}
