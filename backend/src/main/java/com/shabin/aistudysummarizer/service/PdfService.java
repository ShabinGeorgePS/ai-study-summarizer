package com.shabin.aistudysummarizer.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Service
@Slf4j
public class PdfService {

    public String extractText(MultipartFile file) {

        if (file.isEmpty()) {
            throw new RuntimeException("PDF file is empty");
        }

        try (InputStream inputStream = file.getInputStream();
             PDDocument document = PDDocument.load(inputStream)) {

            if (document.isEncrypted()) {
                throw new RuntimeException("Encrypted PDFs are not supported");
            }

            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);

            String text = stripper.getText(document);

            if (text == null || text.isBlank()) {
                throw new RuntimeException("No readable text found in PDF");
            }

            return cleanText(text);

        } catch (IOException e) {
            log.error("Error parsing PDF", e);
            throw new RuntimeException("Failed to parse PDF file");
        }
    }

    private String cleanText(String text) {
        return text
                .replaceAll("\\s+", " ")
                .trim();
    }
}
