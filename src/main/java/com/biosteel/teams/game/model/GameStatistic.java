package com.biosteel.teams.game.model;

import java.time.ZonedDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "game_statistic", schema = "biosteel")
@Data
@NoArgsConstructor
public class GameStatistic {
    @Id
    @Column(name = "stat_id")
    private UUID statId = UUID.randomUUID();

    @Column(name = "game_id")
    private UUID gameId;

    @Column(name = "team_id")
    private UUID teamId;

    @Column(name = "player_id")
    private UUID playerId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "stat_type")
    private String statType;

    @Column(name = "stat_value")
    private Integer statValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sport_period_definition_id")
    private SportPeriodDefinition sportPeriodDefinition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_score_id")
    private GameScore gameScore;

    @Column(name = "home_score_total")
    private Integer homeScoreTotal;

    @Column(name = "away_score_total")
    private Integer awayScoreTotal;

    @Column(name = "metadata")
    private String metadata;

    @Column(name = "recorded_at")
    private ZonedDateTime recordedAt;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @Column(name = "deleted_at")
    private ZonedDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = ZonedDateTime.now();
        recordedAt = ZonedDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = ZonedDateTime.now();
    }
}