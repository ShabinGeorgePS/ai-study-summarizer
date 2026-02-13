package com.shabin.aistudysummarizer.controller;

import com.shabin.aistudysummarizer.dto.summary.SummaryResponse;
import com.shabin.aistudysummarizer.service.SummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class SummaryController {

    private final SummaryService summaryService;

    @PostMapping("/api/summarize/{documentId}")
    public ResponseEntity<SummaryResponse> generateSummary(@PathVariable UUID documentId) {
        return ResponseEntity.ok(summaryService.generateSummary(documentId));
    }

    @GetMapping("/api/summaries")
    public ResponseEntity<List<SummaryResponse>> getUserSummaries() {
        return ResponseEntity.ok(summaryService.getUserSummaries());
    }

    @GetMapping("/api/summaries/{id}")
    public ResponseEntity<SummaryResponse> getSummary(@PathVariable UUID id) {
        return ResponseEntity.ok(summaryService.getSummary(id));
    }
}
