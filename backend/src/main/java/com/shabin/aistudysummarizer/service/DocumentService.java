package com.shabin.aistudysummarizer.service;

import com.shabin.aistudysummarizer.dto.document.DocumentUploadResponse;
import com.shabin.aistudysummarizer.dto.document.UrlRequest;
import com.shabin.aistudysummarizer.entity.*;
import com.shabin.aistudysummarizer.repository.DocumentRepository;
import com.shabin.aistudysummarizer.repository.UserRepository;
import com.shabin.aistudysummarizer.util.SecurityUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class DocumentService {

        private final DocumentRepository documentRepository;
        private final UserRepository userRepository;
        private final PdfService pdfService;
        private final OcrService ocrService;
        private final WebScrapingService webScrapingService;

        public DocumentUploadResponse uploadDocument(MultipartFile file, String title, SourceType sourceType) {

                String email = SecurityUtil.getCurrentUserEmail();
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                String extractedText;
                if (sourceType == SourceType.IMAGE) {
                        extractedText = ocrService.extractText(file);
                } else {
                        extractedText = pdfService.extractText(file);
                }

                Document document = Document.builder()
                                .user(user)
                                .title(title == null ? file.getOriginalFilename() : title)
                                .sourceType(sourceType)
                                .originalFilename(file.getOriginalFilename())
                                .fileSizeBytes(file.getSize())
                                .extractedText(extractedText)
                                .build();

                documentRepository.save(document);

                return mapToResponse(document);
        }

        public DocumentUploadResponse processUrl(UrlRequest request) {
                String email = SecurityUtil.getCurrentUserEmail();
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                String extractedContent = webScrapingService.scrapeUrl(request.getUrl());

                Document document = Document.builder()
                                .user(user)
                                .title(request.getTitle() != null ? request.getTitle() : "Scraped: " + request.getUrl())
                                .sourceType(SourceType.URL)
                                .sourceUrl(request.getUrl())
                                .extractedText(extractedContent)
                                .build();

                documentRepository.save(document);

                return mapToResponse(document);
        }

        private DocumentUploadResponse mapToResponse(Document document) {
                return DocumentUploadResponse.builder()
                                .documentId(document.getId())
                                .title(document.getTitle())
                                .sourceType(document.getSourceType().name())
                                .sourceUrl(document.getSourceUrl())
                                .createdAt(document.getCreatedAt())
                                .build();
        }
}
