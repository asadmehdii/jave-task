package com.biosteel.teams.game.mapper;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.biosteel.teams.game.dto.PeriodTypeDTO;
import com.biosteel.teams.game.dto.SportPeriodDefinitionDTO;
import com.biosteel.teams.game.model.PeriodType;
import com.biosteel.teams.game.model.SportPeriodDefinition;

@Component
public class PeriodMapper {

    /**
     * Convert PeriodType entity to DTO
     * 
     * @param periodType PeriodType entity
     * @return PeriodTypeDTO
     */
    public PeriodTypeDTO toPeriodTypeDTO(PeriodType periodType) {
        if (periodType == null) {
            return null;
        }

        PeriodTypeDTO dto = new PeriodTypeDTO();
        dto.setCode(periodType.getCode());
        dto.setDescription(periodType.getDescription());

        return dto;
    }

    /**
     * Convert a list of PeriodType entities to DTOs
     * 
     * @param periodTypes List of PeriodType entities
     * @return List of PeriodTypeDTO objects
     */
    public List<PeriodTypeDTO> toPeriodTypeDTOList(List<PeriodType> periodTypes) {
        if (periodTypes == null) {
            return null;
        }

        List<PeriodTypeDTO> dtos = new ArrayList<>(periodTypes.size());
        for (PeriodType periodType : periodTypes) {
            dtos.add(toPeriodTypeDTO(periodType));
        }

        return dtos;
    }

    /**
     * Convert SportPeriodDefinition entity to DTO
     * 
     * @param periodDefinition SportPeriodDefinition entity
     * @return SportPeriodDefinitionDTO
     */
    public SportPeriodDefinitionDTO toSportPeriodDefinitionDTO(SportPeriodDefinition periodDefinition) {
        if (periodDefinition == null) {
            return null;
        }

        SportPeriodDefinitionDTO dto = new SportPeriodDefinitionDTO();
        dto.setSportPeriodDefinitionId(periodDefinition.getSportPeriodDefinitionId());
        dto.setSportTypeCode(periodDefinition.getSportTypeCode());
        dto.setPeriodTypeCode(periodDefinition.getPeriodTypeCode());
        dto.setPeriodNumber(periodDefinition.getPeriodNumber());
        dto.setPeriodName(periodDefinition.getPeriodName());
        dto.setDefaultDurationMinutes(periodDefinition.getDefaultDurationMinutes());
        dto.setDisplayOrder(periodDefinition.getDisplayOrder());
        dto.setIsScoringPeriod(periodDefinition.getIsScoringPeriod());

        // Set period type name if available
        if (periodDefinition.getPeriodType() != null) {
            dto.setPeriodTypeName(periodDefinition.getPeriodType().getCode());
        }

        return dto;
    }

    /**
     * Convert a list of SportPeriodDefinition entities to DTOs
     * 
     * @param periodDefinitions List of SportPeriodDefinition entities
     * @return List of SportPeriodDefinitionDTO objects
     */
    public List<SportPeriodDefinitionDTO> toSportPeriodDefinitionDTOList(
            List<SportPeriodDefinition> periodDefinitions) {
        if (periodDefinitions == null) {
            return null;
        }

        List<SportPeriodDefinitionDTO> dtos = new ArrayList<>(periodDefinitions.size());
        for (SportPeriodDefinition periodDefinition : periodDefinitions) {
            dtos.add(toSportPeriodDefinitionDTO(periodDefinition));
        }

        return dtos;
    }
}