package com.biosteel.teams.team.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class TeamMinimalDTO {
    private UUID teamId;
    private String name;
    private String description;
    private String sportTypeCode;
    private String ageGroup;
    private String division;
    private Integer seasonYear;
    private String logoMediaId;
}