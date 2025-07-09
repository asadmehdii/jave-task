package com.biosteel.teams.game.dto;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.biosteel.teams.event.dto.LocationDTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GameRequestDTO {
    @NotNull
    private UUID homeTeamId;
    private Boolean isHomeGame = true;
    private UUID awayTeamId;
    private String awayTeamName;
    private String title;
    private String description;
    private LocationDTO location;
    private String status;
    @NotNull
    private ZonedDateTime startTime;
    @NotNull
    private ZonedDateTime endTime;
    private ZonedDateTime arrivalTime;
    private Integer homeScore;
    private Integer awayScore;
    private UUID currentPeriodDefinitionId;
}
