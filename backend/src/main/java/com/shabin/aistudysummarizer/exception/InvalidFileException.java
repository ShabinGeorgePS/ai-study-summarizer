package com.shabin.aistudysummarizer.exception;

import org.springframework.http.HttpStatus;

public class InvalidFileException extends AppException {
    public InvalidFileException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }

    public static InvalidFileException invalidFileType(String fileName, String contentType) {
        return new InvalidFileException(
                String.format("Invalid file type '%s' for file '%s'. Only PDF files are supported.", contentType, fileName)
        );
    }

    public static InvalidFileException emptyContent() {
        return new InvalidFileException("Uploaded file is empty or contains no extractable text.");
    }

    public static InvalidFileException corruptedFile(String fileName) {
        return new InvalidFileException(String.format("File '%s' is corrupted or cannot be processed.", fileName));
    }
}
