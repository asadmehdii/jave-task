package com.biosteel.teams.game.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GameStatisticRequestDTO {
    @NotNull
    private UUID teamId;
    private UUID playerId;
    private UUID sportPeriodDefinitionId;
    private UUID gameScoreId;
    private Integer homeScoreTotal;
    private Integer awayScoreTotal;
    private String metadata;

    @NotNull
    private String statType;
    private Integer statValue;
}