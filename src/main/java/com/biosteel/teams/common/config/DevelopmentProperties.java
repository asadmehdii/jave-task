package com.biosteel.teams.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "app.development")
public class DevelopmentProperties {
    private boolean enabled = false;
    private boolean includeVerificationCode = false;
}