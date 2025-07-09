package com.biosteel.teams.advertisement.model;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "advertisement", schema = "biosteel")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Advertisement {

    @Id
    @Column(name = "ad_id")
    private UUID adId;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "image_media_id")
    private UUID imageMediaId;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "target_url")
    private String targetUrl;

    @Column(name = "analytic_event")
    private String analyticEvent;

    @Column(name = "settings_json")
    private String settingsJson;

    @Column(name = "type")
    private String type;

    @Column(name = "placement")
    private String placement;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "start_date")
    private OffsetDateTime startDate;

    @Column(name = "end_date")
    private OffsetDateTime endDate;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;
}