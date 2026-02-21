package com.shabin.aistudysummarizer.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends AppException {
    public UnauthorizedException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }

    public static UnauthorizedException accessDenied() {
        return new UnauthorizedException("You don't have permission to access this resource.");
    }
}
