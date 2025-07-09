package com.biosteel.teams.advertisement.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class AdvertisementDTO {
    private UUID adId;
    private String title;
    private String description;
    private String imageUrl;
    private String targetUrl;
    private String settingsJson;
    private String type;
    private String placement;
    private String analyticEvent;
    private Boolean isActive;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
}