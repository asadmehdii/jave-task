package com.biosteel.teams.notification.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceTokenDTO {

    private UUID deviceTokenId;
    private UUID userId;

    @NotBlank(message = "Token is required")
    private String token;

    private String deviceType;

    private String appVersion;

    private Boolean isActive;
}