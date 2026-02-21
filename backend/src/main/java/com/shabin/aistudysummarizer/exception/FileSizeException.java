package com.shabin.aistudysummarizer.exception;

import org.springframework.http.HttpStatus;

public class FileSizeException extends AppException {
    public FileSizeException(long fileSize, long maxSize) {
        super(String.format("File size %d bytes exceeds maximum allowed size of %d bytes", fileSize, maxSize),
                HttpStatus.PAYLOAD_TOO_LARGE);
    }
}
