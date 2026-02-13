package com.shabin.aistudysummarizer.dto.summary;

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
public class SummaryResponse {
    private UUID id;
    private UUID documentId;
    private String documentTitle;
    private SummaryContent content;
    private String modelUsed;
    private Integer tokensUsed;
    private LocalDateTime createdAt;
}
