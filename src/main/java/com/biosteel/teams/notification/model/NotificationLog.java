package com.biosteel.teams.notification.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notification_log", schema = "biosteel")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationLog {

    @Id
    @Column(name = "notification_log_id")
    private UUID notificationLogId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "team_id")
    private UUID teamId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "body", nullable = false)
    private String body;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "data", columnDefinition = "jsonb")
    private String data;

    @Column(name = "notification_type")
    private String notificationType;

    @Column(name = "target_type")
    private String targetType;

    @Column(name = "delivery_status")
    private String deliveryStatus;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        notificationLogId = UUID.randomUUID();
        createdAt = LocalDateTime.now();
    }
}