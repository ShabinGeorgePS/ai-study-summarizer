package com.shabin.aistudysummarizer.dto.summary;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for generating a summary
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SummaryRequestDTO {

    @NotNull(message = "Document ID is required")
    private java.util.UUID documentId;

    @Min(value = 1, message = "MCQ count must be at least 1")
    @Max(value = 20, message = "MCQ count cannot exceed 20")
    @Builder.Default
    private Integer mcqCount = 5;

    @Pattern(regexp = "simple|detailed|exam", message = "Summary mode must be: simple, detailed, or exam")
    @Builder.Default
    private String summaryMode = "detailed";

    @Min(value = 1, message = "Minimum bullet points is 1")
    @Max(value = 50, message = "Maximum bullet points is 50")
    @Builder.Default
    private Integer bulletPointCount = 10;
}
