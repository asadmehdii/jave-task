package com.biosteel.teams.game.dto;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.biosteel.teams.event.dto.LocationDTO;

import lombok.Data;

@Data
public class GameResponseDTO {
    private UUID gameId;
    private Boolean isHomeGame;
    private UUID homeTeamId;
    private UUID awayTeamId;
    private String awayTeamName;
    private Integer homeScore;
    private Integer awayScore;
    private SportPeriodDefinitionDTO currentPeriod;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private ZonedDateTime arrivalTime;
    private String title;
    private String description;
    private LocationDTO location;
    private ZonedDateTime startTime;
    private ZonedDateTime endTime;
    private String eventStatus;
    private String status;
}
