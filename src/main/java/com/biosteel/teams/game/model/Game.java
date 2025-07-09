package com.biosteel.teams.game.model;

import java.time.ZonedDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "game", schema = "biosteel")
@Data
@NoArgsConstructor
public class Game {
    @Id
    @Column(name = "game_id")
    private UUID gameId = UUID.randomUUID();

    @Column(name = "home_team_id")
    private UUID homeTeamId;

    @Column(name = "away_team_id")
    private UUID awayTeamId;

    @Column(name = "home_score")
    private Integer homeScore = 0;

    @Column(name = "away_score")
    private Integer awayScore = 0;

    @Column(name = "is_home_game")
    private Boolean isHomeGame = true;

    @Column(name = "away_team_name")
    private String awayTeamName;

    @Column(name = "status")
    private String status;

    @Column(name = "current_period")
    private Integer currentPeriod;

    @Column(name = "current_period_definition_id")
    private UUID currentPeriodDefinitionId;

    @ManyToOne
    @JoinColumn(name = "current_period_definition_id", referencedColumnName = "sport_period_definition_id", insertable = false, updatable = false)
    private SportPeriodDefinition currentPeriodDefinition;

    @Column(name = "period_count")
    private Integer periodCount;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @Column(name = "deleted_at")
    private ZonedDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = ZonedDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = ZonedDateTime.now();
    }
}
