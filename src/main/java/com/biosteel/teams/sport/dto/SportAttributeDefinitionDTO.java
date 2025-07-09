package com.biosteel.teams.sport.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SportAttributeDefinitionDTO {
    private String code;
    private String name;
    private String description;
    private String dataType;
    private Boolean isRequired;
    private String validationRules;
    private String entityType;
    private Integer displayOrder; // From mapping
}