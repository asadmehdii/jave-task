package com.biosteel.teams.notification.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private String title;
    private String body;
    private String imageUrl;
    private Map<String, String> data;
    private NotificationTarget target;
}