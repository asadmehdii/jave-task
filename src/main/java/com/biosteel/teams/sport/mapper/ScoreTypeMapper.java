package com.biosteel.teams.sport.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.biosteel.teams.sport.dto.ScoreTypeDTO;
import com.biosteel.teams.sport.model.ScoreType;

@Component
public class ScoreTypeMapper {

    public ScoreTypeDTO toDto(ScoreType entity) {
        if (entity == null) {
            return null;
        }

        ScoreTypeDTO dto = new ScoreTypeDTO();
        dto.setScoreTypeId(entity.getScoreTypeId());
        dto.setSportType(entity.getSportType());
        dto.setName(entity.getName());
        dto.setPoints(entity.getPoints());
        dto.setDescription(entity.getDescription());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        return dto;
    }

    public List<ScoreTypeDTO> toDtoList(List<ScoreType> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}