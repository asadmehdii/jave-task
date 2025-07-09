package com.biosteel.teams.game.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GameScoreRequestDTO {
    private UUID teamId;
    @NotNull
    private UUID scoreTypeId;
    private UUID playerId;
    private boolean isHomeTeam;
    private String playerName;
    private String teamName;
    private String scoreValue;
}