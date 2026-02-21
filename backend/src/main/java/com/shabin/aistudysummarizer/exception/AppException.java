package com.shabin.aistudysummarizer.exception;

import org.springframework.http.HttpStatus;

/**
 * Base exception class for application-specific errors.
 * All custom exceptions extend this to provide consistent error handling.
 */
public abstract class AppException extends RuntimeException {
    private final HttpStatus status;

    public AppException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public AppException(String message, Throwable cause, HttpStatus status) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
