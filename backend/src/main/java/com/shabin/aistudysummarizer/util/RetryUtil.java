package com.shabin.aistudysummarizer.util;

import com.shabin.aistudysummarizer.exception.SummaryGenerationException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.Callable;

/**
 * Retry utility for handling transient API failures with exponential backoff.
 * Useful for calls to external APIs like Gemini that may temporarily fail.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RetryUtil {

    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_DELAY_MS = 1000;
    private static final double BACKOFF_MULTIPLIER = 2.0;

    /**
     * Execute a callable with automatic retry on failure
     * @param <T> Return type
     * @param callable The operation to execute
     * @param operationName Name of operation for logging
     * @return Result of the callable
     * @throws SummaryGenerationException if all retries fail
     */
    public static <T> T executeWithRetry(Callable<T> callable, String operationName) {
        return executeWithRetry(callable, operationName, MAX_RETRIES);
    }

    /**
     * Execute a callable with automatic retry on failure
     * @param <T> Return type
     * @param callable The operation to execute
     * @param operationName Name of operation for logging
     * @param maxRetries Maximum number of retries
     * @return Result of the callable
     * @throws SummaryGenerationException if all retries fail
     */
    public static <T> T executeWithRetry(Callable<T> callable, String operationName, int maxRetries) {
        int attempts = 0;
        long delayMs = INITIAL_DELAY_MS;

        while (attempts < maxRetries) {
            try {
                attempts++;
                log.debug("Executing '{}', attempt {}/{}", operationName, attempts, maxRetries);
                return callable.call();
            } catch (Exception e) {
                if (attempts >= maxRetries) {
                    log.error("Operation '{}' failed after {} attempts", operationName, maxRetries, e);
                    throw SummaryGenerationException.retryFailed(maxRetries);
                }

                log.warn("Operation '{}' failed (attempt {}/{}), retrying in {}ms: {}",
                        operationName, attempts, maxRetries, delayMs, e.getMessage());

                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("Retry interrupted", ie);
                    throw new SummaryGenerationException("Operation interrupted during retry");
                }

                delayMs = (long) (delayMs * BACKOFF_MULTIPLIER);
            }
        }

        throw SummaryGenerationException.retryFailed(maxRetries);
    }

    /**
     * Check if exception is retryable
     * Some exceptions (like validation errors) should not be retried
     * @param e The exception to check
     * @return true if the exception is likely transient and should be retried
     */
    public static boolean isRetryable(Exception e) {
        String message = e.getMessage().toLowerCase();

        // Don't retry validation/auth errors
        if (message.contains("validation") || message.contains("unauthorized")
                || message.contains("forbidden") || message.contains("invalid")) {
            return false;
        }

        // Retry network, timeout, and server errors
        return message.contains("timeout") || message.contains("connection")
                || message.contains("unavailable") || message.contains("500")
                || message.contains("502") || message.contains("503");
    }
}
