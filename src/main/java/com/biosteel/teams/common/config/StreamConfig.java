package com.biosteel.teams.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "stream")
public class StreamConfig {

    @NotBlank
    private String apiKey;

    @NotBlank
    private String apiSecret;

    private int tokenExpiryHours = 24;

    @Bean
    @ConfigurationProperties(prefix = "stream.retry")
    public StreamRetryConfig streamRetryConfig() {
        return new StreamRetryConfig();
    }
}

@Data
class StreamRetryConfig {
    private int maxAttempts = 3;
    private long initialDelay = 1000;
    private double multiplier = 2.0;
    private long maxDelay = 10000;
}