package com.shabin.aistudysummarizer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shabin.aistudysummarizer.dto.summary.*;
import com.shabin.aistudysummarizer.entity.*;
import com.shabin.aistudysummarizer.repository.DocumentRepository;
import com.shabin.aistudysummarizer.repository.SummaryRepository;
import com.shabin.aistudysummarizer.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SummaryService {

    private final SummaryRepository summaryRepository;
    private final DocumentRepository documentRepository;
    private final OpenAIService openAIService;
    private final ObjectMapper objectMapper;

    public SummaryResponse generateSummary(UUID documentId) {
        String email = SecurityUtil.getCurrentUserEmail();
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        if (!document.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized access to document");
        }

        String summaryJson = openAIService.generateSummary(document.getExtractedText());

        Summary summary = Summary.builder()
                .user(document.getUser())
                .document(document)
                .summaryJson(summaryJson)
                .modelUsed("gpt-3.5-turbo-0125")
                .build();

        summaryRepository.save(summary);

        return mapToResponse(summary);
    }

    public List<SummaryResponse> getUserSummaries() {
        String email = SecurityUtil.getCurrentUserEmail();
        // This is a simple implementation, in a real service we would use a more direct
        // query
        return summaryRepository.findAll().stream()
                .filter(s -> s.getUser().getEmail().equals(email))
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

    private SummaryResponse mapToResponse(Summary summary) {
        SummaryContent content = null;
        try {
            content = objectMapper.readValue(summary.getSummaryJson(), SummaryContent.class);
        } catch (Exception e) {
            log.error("Error deserializing summary JSON", e);
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
}
