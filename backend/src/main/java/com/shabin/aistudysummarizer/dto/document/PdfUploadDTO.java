package com.shabin.aistudysummarizer.dto.document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for PDF file upload
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PdfUploadDTO {

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
    private String title;

    // Note: The actual file is handled by Spring's MultipartFile
    // This DTO is for metadata validation
}
