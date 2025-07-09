package com.biosteel.teams.sport.model;

import java.time.ZonedDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "score_type", schema = "biosteel")
@Data
public class ScoreType {
    @Id
    @Column(name = "score_type_id")
    private UUID scoreTypeId;

    @Column(name = "sport_type", nullable = false)
    private String sportType;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "points", nullable = false)
    private Integer points;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @Column(name = "deleted_at")
    private ZonedDateTime deletedAt;
}