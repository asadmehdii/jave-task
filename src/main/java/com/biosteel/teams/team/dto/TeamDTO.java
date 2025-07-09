package com.biosteel.teams.team.dto;

import java.util.List;
import java.util.UUID;

import com.biosteel.teams.sport.dto.AttributeValueDTO;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TeamDTO {
    private UUID id;
    private String name;
    private String description;
    private String sportType;
    private String ageGroup;
    private String division;
    private Integer seasonYear;
    private UUID logoMediaId;
    private Boolean isAdmin;
    private List<AttributeValueDTO> attributes;
}