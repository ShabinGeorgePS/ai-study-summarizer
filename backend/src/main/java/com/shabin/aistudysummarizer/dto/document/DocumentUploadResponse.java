package com.shabin.aistudysummarizer.dto.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUploadResponse {
    private UUID documentId;
    private String title;
    private String sourceType;
    private String sourceUrl;
    private LocalDateTime createdAt;
}
