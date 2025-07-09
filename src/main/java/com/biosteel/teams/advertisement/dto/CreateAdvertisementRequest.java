package com.biosteel.teams.advertisement.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateAdvertisementRequest {
    private String title;
    private String description;
    private UUID imageMediaId;
    private String imageUrl;
    @NotBlank(message = "Target URL is required")
    private String targetUrl;
    private String analyticEvent;
    private String settingsJson;
    private String type = "CARD";
    private String placement = "EVENTS";
    private Boolean isActive = true;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
}