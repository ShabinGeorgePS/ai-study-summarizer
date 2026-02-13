package com.shabin.aistudysummarizer.dto.document;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UrlRequest {
    @NotBlank(message = "URL is required")
    @URL(message = "Invalid URL format")
    private String url;

    private String title;
}
