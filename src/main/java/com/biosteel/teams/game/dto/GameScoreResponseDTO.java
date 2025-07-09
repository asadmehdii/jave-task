package com.biosteel.teams.game.dto;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.biosteel.teams.sport.dto.ScoreTypeDTO;

import lombok.Data;

@Data
public class GameScoreResponseDTO {
    private UUID scoreId;
    private UUID gameId;
    private UUID teamId;
    private UUID playerId;
    private UUID userId;
    private ZonedDateTime recordedAt;
    private ZonedDateTime createdAt;
    private boolean isHomeTeam;
    private String playerName;
    private String teamName;
    private String scoreValue;
    private ScoreTypeDTO scoreType;
    private SportPeriodDefinitionDTO periodDefinition;
}
