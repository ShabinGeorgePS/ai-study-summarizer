package com.shabin.aistudysummarizer.dto.document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for URL-based summarization
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UrlSummaryDTO {

    @NotBlank(message = "URL is required")
    @Pattern(
            regexp = "^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})([/\\w .-]*)*/?$",
            message = "Please provide a valid URL (e.g., https://example.com)"
    )
    private String url;

    @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
    private String title;
}
