package com.shabin.aistudysummarizer.service;

import com.shabin.aistudysummarizer.exception.FileSizeException;
import com.shabin.aistudysummarizer.exception.InvalidFileException;
import com.shabin.aistudysummarizer.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

/**
 * Service for validating uploaded documents.
 * Ensures files meet security and size requirements before processing.
 */
@Service
@Slf4j
public class DocumentValidationService {

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "application/pdf",
            "image/jpeg", "image/png", "image/gif",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",  // DOCX
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",  // PPTX
            "text/plain",
            "text/markdown"
    );

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "jpg", "jpeg", "png", "gif",
            "docx", "pptx", "txt", "md", "markdown"
    );

    @Value("${file.max-size:52428800}")  // 50MB default
    private long maxFileSize;

    @Value("${file.max-text-length:1000000}")  // 1 million characters
    private long maxTextLength;

    /**
     * Validate uploaded file
     * @param file The file to validate
     * @throws InvalidFileException if file is invalid
     * @throws FileSizeException if file is too large
     * @throws ValidationException if file type is not allowed
     */
    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw InvalidFileException.emptyContent();
        }

        String fileName = file.getOriginalFilename();
        validateFileName(fileName);
        validateFileType(fileName, file.getContentType());
        validateFileSize(file.getSize());
    }

    /**
     * Validate extracted text length
     * @param text The extracted text
     * @throws ValidationException if text is too long
     */
    public void validateExtractedText(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw InvalidFileException.emptyContent();
        }

        if (text.length() > maxTextLength) {
            log.warn("Extracted text length {} exceeds maximum {}", text.length(), maxTextLength);
            // Truncate instead of throwing exception
        }
    }

    /**
     * Validate title for document
     * @param title The title to validate
     * @throws ValidationException if title is invalid
     */
    public void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new ValidationException("Document title is required");
        }

        if (title.length() < 3) {
            throw new ValidationException("Document title must be at least 3 characters");
        }

        if (title.length() > 255) {
            throw new ValidationException("Document title cannot exceed 255 characters");
        }
    }

    /**
     * Validate URL format
     * @param url The URL to validate
     * @ throws ValidationException if URL is invalid
     */
    public void validateUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new ValidationException("URL is required");
        }

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw new ValidationException("URL must start with http:// or https://");
        }

        if (url.length() > 2048) {
            throw new ValidationException("URL is too long");
        }

        try {
            new java.net.URL(url);
        } catch (java.net.MalformedURLException e) {
            throw new ValidationException("Invalid URL format: " + e.getMessage());
        }
    }

    // Private helper methods

    private void validateFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            throw new ValidationException("File name is required");
        }

        // Check for path traversal attempts
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            throw new ValidationException("Invalid file name: contains path traversal characters");
        }

        // Check file extension
        String extension = getFileExtension(fileName).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw InvalidFileException.invalidFileType(fileName,
                    "File type ." + extension + " is not supported. Allowed types: " + ALLOWED_EXTENSIONS);
        }
    }

    private void validateFileType(String fileName, String contentType) {
        if (contentType != null && !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            log.warn("Unsupported MIME type for file {}: {}", fileName, contentType);
            // Note: Some browsers may send incorrect MIME types
            // We rely more on file extension validation
        }
    }

    private void validateFileSize(long fileSize) {
        if (fileSize == 0) {
            throw InvalidFileException.emptyContent();
        }

        if (fileSize > maxFileSize) {
            throw new FileSizeException(fileSize, maxFileSize);
        }

        log.debug("File size validation passed: {} bytes", fileSize);
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            return fileName.substring(lastDot + 1);
        }
        return "";
    }
}
