package com.biosteel.teams.user.dto;

import java.util.Map;
import java.util.UUID;

import lombok.Data;

@Data
public class UserPreferencesDto {
    private UUID userPreferenceId;
    private UUID userId;
    private Map<String, Boolean> notificationPreferences;
    private String timezone;
    private String language;
}