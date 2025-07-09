package com.biosteel.teams.sport.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.biosteel.teams.sport.dto.AttributeValueDTO;
import com.biosteel.teams.sport.model.SportAttributeDefinition;
import com.biosteel.teams.sport.repository.SportAttributeDefinitionRepository;
import com.biosteel.teams.team.model.TeamAttributeValue;
import com.biosteel.teams.team.model.TeamMemberAttributeValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AttributeValueMapper {

    private final SportAttributeDefinitionRepository sportAttributeDefinitionRepository;
    private final ObjectMapper objectMapper;

    public AttributeValueDTO toDTO(TeamAttributeValue entity) {
        if (entity == null) {
            return null;
        }

        SportAttributeDefinition definition = sportAttributeDefinitionRepository
                .findByCode(entity.getId().getAttributeCode())
                .orElseThrow(() -> new IllegalStateException("Attribute definition not found"));

        return AttributeValueDTO.builder()
                .attributeCode(entity.getId().getAttributeCode())
                .value(getValueByDataType(definition.getDataType(), entity))
                .build();
    }

    public AttributeValueDTO toDTO(TeamMemberAttributeValue entity) {
        if (entity == null) {
            return null;
        }

        SportAttributeDefinition definition = sportAttributeDefinitionRepository
                .findByCode(entity.getId().getAttributeCode())
                .orElseThrow(() -> new IllegalStateException("Attribute definition not found"));

        return AttributeValueDTO.builder()
                .attributeCode(entity.getId().getAttributeCode())
                .value(getValueByDataType(definition.getDataType(), entity))
                .build();
    }

    private Object getValueByDataType(String dataType, TeamAttributeValue value) {
        return deserializeValue(value.getValue(), dataType);
    }

    private Object getValueByDataType(String dataType, TeamMemberAttributeValue value) {
        return deserializeValue(value.getValue(), dataType);
    }

    private Object deserializeValue(String value, String dataType) {
        if (value == null) {
            return null;
        }
        try {
            switch (dataType) {
                case "STRING":
                case "ENUM":
                    return objectMapper.readValue(value, String.class);
                case "NUMBER":
                    return objectMapper.readValue(value, Double.class);
                case "BOOLEAN":
                    return objectMapper.readValue(value, Boolean.class);
                default:
                    throw new IllegalArgumentException("Unsupported data type: " + dataType);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error deserializing value", e);
        }
    }

    public List<AttributeValueDTO> toTeamAttributeDTOList(List<TeamAttributeValue> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }

        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<AttributeValueDTO> toTeamMemberAttributeDTOList(List<TeamMemberAttributeValue> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }

        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}