package com.shabin.aistudysummarizer.service;

import com.shabin.aistudysummarizer.dto.document.DocumentUploadResponse;
import com.shabin.aistudysummarizer.dto.document.UrlRequest;
import com.shabin.aistudysummarizer.entity.*;
import com.shabin.aistudysummarizer.repository.DocumentRepository;
import com.shabin.aistudysummarizer.repository.UserRepository;
import com.shabin.aistudysummarizer.util.SecurityUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

        private final DocumentRepository documentRepository;
        private final UserRepository userRepository;
        private final PdfService pdfService;
        private final OcrService ocrService;
        private final WebScrapingService webScrapingService;
        private final DocxService docxService;
        private final PptxService pptxService;
        private final TextFileService textFileService;

        public DocumentUploadResponse uploadDocument(MultipartFile file, String title, SourceType sourceType) {

                String email = SecurityUtil.getCurrentUserEmail();
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                String extractedText;
                SourceType detectedSourceType = sourceType;

                // If sourceType is not explicitly provided, detect it from filename
                if (sourceType == null) {
                        detectedSourceType = detectSourceType(file.getOriginalFilename());
                }

                // Extract text based on file type
                if (detectedSourceType == SourceType.IMAGE) {
                        extractedText = ocrService.extractText(file);
                } else if (detectedSourceType == SourceType.DOCX) {
                        extractedText = docxService.extractText(file);
                } else if (detectedSourceType == SourceType.PPTX) {
                        extractedText = pptxService.extractText(file);
                } else if (detectedSourceType == SourceType.TEXT || detectedSourceType == SourceType.MARKDOWN) {
                        extractedText = textFileService.extractText(file);
                } else {
                        // Default to PDF
                        extractedText = pdfService.extractText(file);
                }

                Document document = Document.builder()
                                .user(user)
                                .title(title == null ? file.getOriginalFilename() : title)
                                .sourceType(detectedSourceType)
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

        private SourceType detectSourceType(String filename) {
                if (filename == null) {
                        return SourceType.PDF; // Default
                }

                String lowerFilename = filename.toLowerCase();

                if (lowerFilename.endsWith(".pdf")) {
                        return SourceType.PDF;
                } else if (lowerFilename.endsWith(".docx") || lowerFilename.endsWith(".doc")) {
                        return SourceType.DOCX;
                } else if (lowerFilename.endsWith(".pptx") || lowerFilename.endsWith(".ppt")) {
                        return SourceType.PPTX;
                } else if (lowerFilename.endsWith(".txt")) {
                        return SourceType.TEXT;
                } else if (lowerFilename.endsWith(".md") || lowerFilename.endsWith(".markdown")) {
                        return SourceType.MARKDOWN;
                } else if (isImageFile(lowerFilename)) {
                        return SourceType.IMAGE;
                } else {
                        return SourceType.PDF; // Default fallback
                }
        }

        private boolean isImageFile(String filename) {
                return filename.matches(".*\\.(jpg|jpeg|png|gif|bmp|webp)$");
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
