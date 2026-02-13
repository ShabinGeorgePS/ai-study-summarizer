package com.shabin.aistudysummarizer.service;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class WebScrapingService {

    public String scrapeUrl(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .timeout(10000)
                    .userAgent(
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .get();

            // Extract title
            String title = doc.title();

            // Remove scripts and styles
            doc.select("script, style, nav, footer, header").remove();

            // Get body text or specific article content
            Element body = doc.body();
            if (body == null) {
                throw new RuntimeException("Could not find body element in the URL");
            }

            // Attempt to find main content areas if they exist (common in modern sites)
            Elements mainContent = doc.select("article, main, .content, #content");
            String content;
            if (!mainContent.isEmpty()) {
                content = mainContent.text();
            } else {
                content = body.text();
            }

            if (content.isBlank()) {
                throw new RuntimeException("No readable content found at URL");
            }

            return cleanText(title + "\n\n" + content);

        } catch (IOException e) {
            log.error("Error scraping URL: {}", url, e);
            throw new RuntimeException("Failed to scrape URL: " + e.getMessage());
        }
    }

    private String cleanText(String text) {
        return text.replaceAll("\\s+", " ").trim();
    }
}
