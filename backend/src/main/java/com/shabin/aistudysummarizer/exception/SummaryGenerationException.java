package com.shabin.aistudysummarizer.exception;

import org.springframework.http.HttpStatus;

public class SummaryGenerationException extends AppException {
    public SummaryGenerationException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public SummaryGenerationException(String message, Throwable cause) {
        super(message, cause, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static SummaryGenerationException apiError(String wrappedMessage) {
        return new SummaryGenerationException("Summary generation API failed: " + wrappedMessage);
    }

    public static SummaryGenerationException retryFailed(int attempts) {
        return new SummaryGenerationException(
                "Summary generation failed after " + attempts + " attempts. Please try again later."
        );
    }
}
