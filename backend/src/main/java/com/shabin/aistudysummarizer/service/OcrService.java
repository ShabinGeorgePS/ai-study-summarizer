package com.shabin.aistudysummarizer.service;

import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

@Service
@Slf4j
public class OcrService {

    @Value("${tesseract.datapath:}")
    private String tesseractDataPath;

    public String extractText(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("Image file is empty");
        }

        try (InputStream inputStream = file.getInputStream()) {
            BufferedImage image = ImageIO.read(inputStream);
            if (image == null) {
                throw new RuntimeException("Could not read image from file");
            }

            ITesseract tesseract = new Tesseract();

            // If datapath is provided in config, use it. Otherwise, it uses default.
            if (tesseractDataPath != null && !tesseractDataPath.isBlank()) {
                tesseract.setDatapath(tesseractDataPath);
            }

            String result = tesseract.doOCR(image);

            if (result == null || result.isBlank()) {
                throw new RuntimeException("No text could be extracted from image");
            }

            return cleanText(result);

        } catch (IOException | TesseractException e) {
            log.error("Error during OCR processing", e);
            throw new RuntimeException("Failed to perform OCR on image: " + e.getMessage());
        }
    }

    private String cleanText(String text) {
        return text.replaceAll("\\s+", " ").trim();
    }
}
