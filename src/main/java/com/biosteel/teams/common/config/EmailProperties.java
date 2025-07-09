package com.biosteel.teams.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "email")
@Getter
@Setter
public class EmailProperties {
    private String fromEmail;
    private String supportEmail;
    private String verificationBaseUrl;
    private String passwordResetBaseUrl;
}
