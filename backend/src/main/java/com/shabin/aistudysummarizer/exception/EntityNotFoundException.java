package com.shabin.aistudysummarizer.exception;

import org.springframework.http.HttpStatus;

public class EntityNotFoundException extends AppException {
    public EntityNotFoundException(String entityType, String identifier) {
        super(entityType + " not found: " + identifier, HttpStatus.NOT_FOUND);
    }

    public EntityNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
