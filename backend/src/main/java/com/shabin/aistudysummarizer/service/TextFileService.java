package com.shabin.aistudysummarizer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class TextFileService {

    public String extractText(MultipartFile file) {
        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);

            if (content.trim().isEmpty()) {
                throw new RuntimeException("The text file is empty");
            }

            return content.trim();
        } catch (IOException e) {
            log.error("Error extracting text from text file: {}", e.getMessage());
            throw new RuntimeException("Failed to extract text from file: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error processing text file: {}", e.getMessage());
            throw new RuntimeException("Failed to process text file: " + e.getMessage());
        }
    }
}
