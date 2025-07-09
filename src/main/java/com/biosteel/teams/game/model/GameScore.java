package com.biosteel.teams.game.model;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.biosteel.teams.sport.model.ScoreType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "game_score", schema = "biosteel")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameScore {
    @Id
    @Column(name = "score_id")
    private UUID scoreId = UUID.randomUUID();

    @Column(name = "game_id")
    private UUID gameId;

    @Column(name = "team_id")
    private UUID teamId;

    @Column(name = "player_id")
    private UUID playerId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "score_type_id")
    private UUID scoreTypeId;

    @ManyToOne
    @JoinColumn(name = "score_type_id", referencedColumnName = "score_type_id", insertable = false, updatable = false)
    private ScoreType scoreType;

    @Column(name = "period")
    private Integer period;

    @Column(name = "period_definition_id")
    private UUID periodDefinitionId;

    @ManyToOne
    @JoinColumn(name = "period_definition_id", referencedColumnName = "sport_period_definition_id", insertable = false, updatable = false)
    private SportPeriodDefinition periodDefinition;

    @Column(name = "score_value")
    private String scoreValue;

    @Column(name = "is_home_team")
    private boolean isHomeTeam;

    @Column(name = "player_name")
    private String playerName;

    @Column(name = "team_name")
    private String teamName;

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