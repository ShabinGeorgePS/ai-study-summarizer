package com.shabin.aistudysummarizer.controller;

import com.shabin.aistudysummarizer.dto.ApiResponse;
import com.shabin.aistudysummarizer.dto.document.DocumentUploadResponse;
import com.shabin.aistudysummarizer.dto.document.PdfUploadDTO;
import com.shabin.aistudysummarizer.dto.document.UrlSummaryDTO;
import com.shabin.aistudysummarizer.service.DocumentService;
import com.shabin.aistudysummarizer.service.DocumentValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST API Controller for document management.
 * Handles document uploads and URL-based content extraction.
 */
@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Documents", description = "Document upload and management endpoints")
public class DocumentController {

        private final DocumentService documentService;
        private final DocumentValidationService validationService;

        /**
         * Upload and process a PDF file
         */
        @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @Operation(summary = "Upload and process PDF", description = "Upload a PDF file for summarization. Supports files up to 50MB.")
        public ResponseEntity<ApiResponse<DocumentUploadResponse>> uploadDocument(
                        @Parameter(description = "PDF file to upload", required = true) @RequestParam("file") MultipartFile file,
                        @Parameter(description = "Document title", required = true) @RequestParam("title") String title) {

                log.info("Document upload request: filename={}, size={} bytes", file.getOriginalFilename(),
                                file.getSize());

                // Validate file
                validationService.validateFile(file);
                validationService.validateTitle(title);

                // Process and save document
                DocumentUploadResponse response = documentService.uploadDocument(file, title, null);

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.success(response, "Document uploaded and processed successfully"));
        }

        /**
         * Process content from a URL
         */
        @PostMapping("/from-url")
        @Operation(summary = "Process URL content", description = "Extract and process content from a web URL for summarization")
        public ResponseEntity<ApiResponse<DocumentUploadResponse>> processUrl(
                        @Valid @RequestBody UrlSummaryDTO request) {

                log.info("URL processing request: url={}", request.getUrl());

                // Validate URL
                validationService.validateUrl(request.getUrl());
                if (request.getTitle() != null && !request.getTitle().isEmpty()) {
                        validationService.validateTitle(request.getTitle());
                }

                // Process and save document
                com.shabin.aistudysummarizer.dto.document.UrlRequest urlRequest = new com.shabin.aistudysummarizer.dto.document.UrlRequest(
                                request.getUrl(), request.getTitle());
                DocumentUploadResponse response = documentService.processUrl(urlRequest);

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.success(response, "URL content processed successfully"));
        }
}
