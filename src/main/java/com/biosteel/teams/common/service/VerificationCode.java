package com.biosteel.teams.common.service;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificationCode {
    private String code;
    private LocalDateTime timestamp;
    private int attempts;

    public VerificationCode(String code) {
        this.code = code;
        this.timestamp = LocalDateTime.now();
        this.attempts = 0;
    }
}