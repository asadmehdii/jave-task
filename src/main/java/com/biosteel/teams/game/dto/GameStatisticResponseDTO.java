package com.biosteel.teams.game.dto;

import java.time.ZonedDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class GameStatisticResponseDTO {
    private UUID statId;
    private UUID gameId;
    private UUID teamId;
    private UUID playerId;
    private UUID userId;
    private SportPeriodDefinitionDTO sportPeriodDefinition;
    private GameScoreResponseDTO gameScore;
    private Integer homeScoreTotal;
    private Integer awayScoreTotal;
    private String metadata;
    private String statType;
    private Integer statValue;
    private ZonedDateTime recordedAt;
    private ZonedDateTime createdAt;
}