package com.shabin.aistudysummarizer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocxService {

    public String extractText(MultipartFile file) {
        try {
            XWPFDocument document = new XWPFDocument(file.getInputStream());
            StringBuilder textBuilder = new StringBuilder();

            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String text = paragraph.getText();
                if (text != null && !text.isEmpty()) {
                    textBuilder.append(text).append("\n");
                }
            }

            document.close();

            String extractedText = textBuilder.toString().trim();
            if (extractedText.isEmpty()) {
                throw new RuntimeException("No text could be extracted from the DOCX file");
            }

            return extractedText;
        } catch (IOException e) {
            log.error("Error extracting text from DOCX file: {}", e.getMessage());
            throw new RuntimeException("Failed to extract text from DOCX file: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error processing DOCX file: {}", e.getMessage());
            throw new RuntimeException("Failed to process DOCX file: " + e.getMessage());
        }
    }
}
