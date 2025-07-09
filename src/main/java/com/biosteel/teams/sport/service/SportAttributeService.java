package com.biosteel.teams.sport.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.biosteel.teams.sport.dto.AttributeValueDTO;
import com.biosteel.teams.sport.model.SportAttributeDefinition;
import com.biosteel.teams.sport.repository.SportAttributeDefinitionRepository;
import com.biosteel.teams.sport.repository.TeamAttributeValueRepository;
import com.biosteel.teams.sport.repository.TeamMemberAttributeValueRepository;
import com.biosteel.teams.team.model.Team;
import com.biosteel.teams.team.model.TeamAttributeValue;
import com.biosteel.teams.team.model.TeamAttributeValueId;
import com.biosteel.teams.team.model.TeamMember;
import com.biosteel.teams.team.model.TeamMemberAttributeValue;
import com.biosteel.teams.team.model.TeamMemberAttributeValueId;
import com.biosteel.teams.team.repository.TeamMemberRepository;
import com.biosteel.teams.team.repository.TeamRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SportAttributeService {
    private final SportAttributeDefinitionRepository attributeDefinitionRepository;
    private final TeamAttributeValueRepository teamAttributeValueRepository;
    private final TeamMemberAttributeValueRepository teamMemberAttributeValueRepository;
    private final TeamRepository teamRepository;
    private final ObjectMapper objectMapper;
    private final AttributeValidator attributeValidator;
    private final TeamMemberRepository teamMemberRepository;

    @Transactional
    public List<TeamAttributeValue> updateTeamAttributes(UUID teamId, List<AttributeValueDTO> attributes) {
        log.debug("Updating attributes for team {}: {}", teamId, attributes);
        List<TeamAttributeValue> updated = attributes.stream()
                .map(attr -> updateSingleTeamAttribute(teamId, attr))
                .collect(Collectors.toList());
        log.debug("Updated {} team attributes", updated.size());
        return updated;
    }

    @Transactional
    public List<TeamMemberAttributeValue> updateTeamMemberAttributes(UUID teamMemberId,
            List<AttributeValueDTO> attributes) {
        log.debug("Updating attributes for team member {}: {}", teamMemberId, attributes);
        List<TeamMemberAttributeValue> updated = attributes.stream()
                .map(attr -> updateSingleTeamMemberAttribute(teamMemberId, attr))
                .collect(Collectors.toList());
        log.debug("Updated {} team member attributes", updated.size());
        return updated;
    }

    private TeamAttributeValue updateSingleTeamAttribute(UUID teamId, AttributeValueDTO dto) {
        SportAttributeDefinition definition = getAttributeDefinition(dto.getAttributeCode());
        TeamAttributeValue attributeValue = getOrCreateTeamAttributeValue(teamId, dto.getAttributeCode());

        Object value = dto.getValue();
        attributeValidator.validate(definition, value);
        attributeValue.setValue(serializeValue(value));

        return teamAttributeValueRepository.save(attributeValue);
    }

    private TeamMemberAttributeValue updateSingleTeamMemberAttribute(UUID teamMemberId, AttributeValueDTO dto) {
        SportAttributeDefinition definition = getAttributeDefinition(dto.getAttributeCode());
        TeamMemberAttributeValue attributeValue = getOrCreateTeamMemberAttributeValue(teamMemberId,
                dto.getAttributeCode());

        Object value = dto.getValue();
        attributeValidator.validate(definition, value);
        attributeValue.setValue(serializeValue(value));

        return teamMemberAttributeValueRepository.save(attributeValue);
    }

    private String serializeValue(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing value", e);
        }
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

    private TeamAttributeValue getOrCreateTeamAttributeValue(UUID teamId, String attributeCode) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found with id: " + teamId));

        TeamAttributeValueId id = new TeamAttributeValueId();
        id.setTeamId(teamId);
        id.setAttributeCode(attributeCode);

        return teamAttributeValueRepository.findById(id)
                .orElse(TeamAttributeValue.builder()
                        .id(id)
                        .team(team)
                        .build());
    }

    private TeamMemberAttributeValue getOrCreateTeamMemberAttributeValue(UUID teamMemberId, String attributeCode) {
        // First, fetch the TeamMember entity
        TeamMember teamMember = teamMemberRepository.findById(teamMemberId)
                .orElseThrow(() -> new RuntimeException("Team member not found with id: " + teamMemberId));

        TeamMemberAttributeValueId id = new TeamMemberAttributeValueId();
        id.setTeamMemberId(teamMemberId);
        id.setAttributeCode(attributeCode);

        return teamMemberAttributeValueRepository.findById(id)
                .orElse(TeamMemberAttributeValue.builder()
                        .id(id)
                        .teamMember(teamMember) // Set the TeamMember reference
                        .build());
    }

    private SportAttributeDefinition getAttributeDefinition(String attributeCode) {
        return attributeDefinitionRepository.findById(attributeCode)
                .orElseThrow(() -> new RuntimeException("Attribute definition not found: " + attributeCode));
    }

    @Transactional(readOnly = true)
    public List<TeamAttributeValue> getAllTeamAttributeValues(UUID teamId) {
        log.debug("Fetching all attributes for team {}", teamId);
        List<TeamAttributeValue> attributeValues = teamAttributeValueRepository.findAllByIdTeamId(teamId);

        // Deserialize values based on their attribute definitions
        attributeValues.forEach(attributeValue -> {
            SportAttributeDefinition definition = getAttributeDefinition(attributeValue.getId().getAttributeCode());
            Object deserializedValue = deserializeValue(attributeValue.getValue(), definition.getDataType());
            attributeValue.setValue(serializeValue(deserializedValue)); // Re-serialize to maintain consistency
        });

        return attributeValues;
    }

    @Transactional(readOnly = true)
    public List<TeamMemberAttributeValue> getAllTeamMemberAttributeValues(UUID teamMemberId) {
        log.debug("Fetching all attributes for team member {}", teamMemberId);
        List<TeamMemberAttributeValue> attributeValues = teamMemberAttributeValueRepository
                .findAllByIdTeamMemberId(teamMemberId);

        // Deserialize values based on their attribute definitions
        attributeValues.forEach(attributeValue -> {
            SportAttributeDefinition definition = getAttributeDefinition(attributeValue.getId().getAttributeCode());
            Object deserializedValue = deserializeValue(attributeValue.getValue(), definition.getDataType());
            attributeValue.setValue(serializeValue(deserializedValue)); // Re-serialize to maintain consistency
        });

        return attributeValues;
    }
}