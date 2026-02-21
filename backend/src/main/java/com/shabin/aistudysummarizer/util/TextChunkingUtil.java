package com.shabin.aistudysummarizer.util;

import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for chunking large documents into manageable pieces for AI processing.
 * Helps manage token limits and improves summary quality for long documents.
 */
@Slf4j
public class TextChunkingUtil {

    private static final int CHUNK_SIZE = 4000;  // Characters per chunk
    private static final int CHUNK_OVERLAP = 200;  // Characters overlap between chunks
    private static final int MAX_CHUNKS = 15;  // Maximum chunks to process

    /**
     * Split text into overlapping chunks for processing
     * @param text The text to chunk
     * @return List of text chunks
     */
    public static List<String> chunkText(String text) {
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> chunks = new ArrayList<>();
        int totalChunks = (int) Math.ceil((double) text.length() / (CHUNK_SIZE - CHUNK_OVERLAP));

        if (totalChunks > MAX_CHUNKS) {
            log.warn("Document has {} chunks, limiting to {} chunks", totalChunks, MAX_CHUNKS);
            text = text.substring(0, MAX_CHUNKS * (CHUNK_SIZE - CHUNK_OVERLAP) + CHUNK_OVERLAP);
        }

        int startIndex = 0;
        while (startIndex < text.length()) {
            int endIndex = Math.min(startIndex + CHUNK_SIZE, text.length());
            String chunk = text.substring(startIndex, endIndex).trim();

            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }

            // Move start index for next chunk with overlap
            startIndex = endIndex - CHUNK_OVERLAP;

            // Avoid infinite loop if text is small
            if (startIndex <= 0) {
                break;
            }
        }

        log.info("Split text into {} chunks of ~{} characters each", chunks.size(), CHUNK_SIZE);
        return chunks;
    }

    /**
     * Normalize text by removing extra whitespace and cleaning special characters
     * @param text The text to normalize
     * @return Normalized text
     */
    public static String normalizeText(String text) {
        if (text == null) {
            return "";
        }

        // Remove multiple spaces
        text = text.replaceAll("\\s+", " ");

        // Remove special characters but keep basic punctuation
        text = text.replaceAll("[^\\w\\s.?!,;:\"-]", "");

        // Remove BOM if present
        if (text.startsWith("\uFEFF")) {
            text = text.substring(1);
        }

        return text.trim();
    }

    /**
     * Get the total length of text for validation
     * @param text The text to measure
     * @return Length in characters
     */
    public static long getTextLength(String text) {
        return text == null ? 0 : text.length();
    }

    /**
     * Check if text exceeds maximum token limit (rough estimation)
     * @param text The text to check
     * @return true if text is likely too long for API
     */
    public static boolean exceedsTokenLimit(String text) {
        // Rough estimation: 4 characters = 1 token
        long estimatedTokens = getTextLength(text) / 4;
        return estimatedTokens > 100000;  // Token limit for Gemini
    }
}
