package com.biosteel.teams.sport.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.biosteel.teams.sport.dto.SportTypeDTO;
import com.biosteel.teams.sport.model.SportType;

@Component
public class SportTypeMapper {

    public SportTypeDTO toDTO(SportType entity) {
        if (entity == null) {
            return null;
        }

        return SportTypeDTO.builder()
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deletedAt(entity.getDeletedAt())
                .build();
    }

    public List<SportTypeDTO> toDTOList(List<SportType> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}