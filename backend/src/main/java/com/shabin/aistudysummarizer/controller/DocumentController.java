package com.shabin.aistudysummarizer.controller;

import com.shabin.aistudysummarizer.dto.document.DocumentUploadResponse;
import com.shabin.aistudysummarizer.dto.document.UrlRequest;
import com.shabin.aistudysummarizer.entity.SourceType;
import com.shabin.aistudysummarizer.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/upload")
    public ResponseEntity<DocumentUploadResponse> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "sourceType", required = false) SourceType sourceType) {
        return ResponseEntity.ok(
                documentService.uploadDocument(file, title, sourceType));
    }

    @PostMapping("/url")
    public ResponseEntity<DocumentUploadResponse> processUrl(@Valid @RequestBody UrlRequest request) {
        return ResponseEntity.ok(documentService.processUrl(request));
    }
}
