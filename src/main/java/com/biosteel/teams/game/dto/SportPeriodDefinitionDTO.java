package com.biosteel.teams.game.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SportPeriodDefinitionDTO {
    private UUID sportPeriodDefinitionId;
    private String sportTypeCode;
    private String periodTypeCode;
    private String periodTypeName;
    private Integer periodNumber;
    private String periodName;
    private Integer defaultDurationMinutes;
    private Integer displayOrder;
    private Boolean isScoringPeriod;
}
