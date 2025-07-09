package com.biosteel.teams.sport.dto;

import java.time.ZonedDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class ScoreTypeDTO {
    private UUID scoreTypeId;
    private String sportType;
    private String name;
    private Integer points;
    private String description;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}