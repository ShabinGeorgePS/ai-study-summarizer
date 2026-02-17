package com.shabin.aistudysummarizer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PptxService {

    public String extractText(MultipartFile file) {
        try {
            XMLSlideShow presentation = new XMLSlideShow(file.getInputStream());
            StringBuilder textBuilder = new StringBuilder();

            List<XSLFSlide> slides = presentation.getSlides();
            for (int slideNum = 0; slideNum < slides.size(); slideNum++) {
                XSLFSlide slide = slides.get(slideNum);
                textBuilder.append("--- Slide ").append(slideNum + 1).append(" ---\n");

                for (XSLFShape shape : slide.getShapes()) {
                    if (shape instanceof XSLFTextShape) {
                        XSLFTextShape textShape = (XSLFTextShape) shape;
                        String text = textShape.getText();
                        if (text != null && !text.isEmpty()) {
                            textBuilder.append(text).append("\n");
                        }
                    }
                }
                textBuilder.append("\n");
            }

            presentation.close();

            String extractedText = textBuilder.toString().trim();
            if (extractedText.isEmpty()) {
                throw new RuntimeException("No text could be extracted from the PPTX file");
            }

            return extractedText;
        } catch (IOException e) {
            log.error("Error extracting text from PPTX file: {}", e.getMessage());
            throw new RuntimeException("Failed to extract text from PPTX file: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error processing PPTX file: {}", e.getMessage());
            throw new RuntimeException("Failed to process PPTX file: " + e.getMessage());
        }
    }
}
