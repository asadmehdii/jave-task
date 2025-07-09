package com.biosteel.teams.notification.dto;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTarget {
    private TargetType targetType;
    private UUID userId;
    private UUID teamId;
    private List<UUID> userIds;
    private String topic;
    private String token;

    public enum TargetType {
        USER, // Send to a specific user
        TEAM, // Send to all members of a team
        MULTI_USER, // Send to multiple specific users
        TOPIC, // Send to a topic subscribers
        TOKEN // Send directly to a device token
    }
}