package com.shabin.aistudysummarizer.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetResponse {

    private String message;
    private String resetToken;
    private long expiresIn; // milliseconds until token expiry
}
