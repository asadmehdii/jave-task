package com.biosteel.teams.user.dto;

import java.util.Map;

import lombok.Data;

@Data
public class UpdateUserPreferencesRequest {
    private Map<String, Boolean> notificationPreferences;
    private String timezone;
    private String language;
}