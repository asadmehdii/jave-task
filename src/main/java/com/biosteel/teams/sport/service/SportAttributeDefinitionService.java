package com.biosteel.teams.sport.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.biosteel.teams.sport.dto.SportAttributeDefinitionDTO;
import com.biosteel.teams.sport.model.SportAttributeDefinition;
import com.biosteel.teams.sport.model.SportAttributeMapping;
import com.biosteel.teams.sport.repository.SportAttributeDefinitionRepository;
import com.biosteel.teams.sport.repository.SportAttributeMappingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SportAttributeDefinitionService {

    private final SportAttributeDefinitionRepository attributeDefinitionRepository;
    private final SportAttributeMappingRepository attributeMappingRepository;

    @Transactional(readOnly = true)
    public Map<String, List<SportAttributeDefinitionDTO>> getAllAttributeDefinitionsBySportType() {
        List<SportAttributeMapping> allMappings = attributeMappingRepository.findAll();

        return allMappings.stream()
                .collect(Collectors.groupingBy(
                        mapping -> mapping.getId().getSportTypeCode(),
                        Collectors.mapping(
                                mapping -> toDTO(mapping.getAttributeDefinition(), mapping),
                                Collectors.toList())));
    }

    @Transactional(readOnly = true)
    public List<SportAttributeDefinitionDTO> getAttributeDefinitions(String sportTypeCode, String entityType) {
        List<SportAttributeMapping> mappings;

        if (entityType != null) {
            mappings = attributeMappingRepository
                    .findBySportTypeCodeAndAttributeDefinitionEntityType(sportTypeCode, entityType);
        } else {
            mappings = attributeMappingRepository.findBySportTypeCode(sportTypeCode);
        }

        return mappings.stream()
                .map(mapping -> toDTO(mapping.getAttributeDefinition(), mapping))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SportAttributeDefinitionDTO> getRequiredAttributeDefinitions(String sportTypeCode, String entityType) {
        List<SportAttributeMapping> mappings;

        if (entityType != null) {
            mappings = attributeMappingRepository
                    .findBySportTypeCodeAndAttributeDefinitionEntityType(sportTypeCode, entityType);
        } else {
            mappings = attributeMappingRepository.findBySportTypeCode(sportTypeCode);
        }

        return mappings.stream()
                .filter(SportAttributeMapping::getIsRequired)
                .map(mapping -> toDTO(mapping.getAttributeDefinition(), mapping))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public String getAttributeValidationRules(String attributeCode) {
        SportAttributeDefinition definition = attributeDefinitionRepository.findById(attributeCode)
                .orElseThrow(() -> new RuntimeException("Attribute definition not found"));
        return definition.getValidationRules();
    }

    private SportAttributeDefinitionDTO toDTO(SportAttributeDefinition definition, SportAttributeMapping mapping) {
        return SportAttributeDefinitionDTO.builder()
                .code(definition.getCode())
                .name(definition.getName())
                .description(definition.getDescription())
                .dataType(definition.getDataType())
                .isRequired(mapping.getIsRequired()) // Use the requirement from mapping
                .validationRules(definition.getValidationRules())
                .entityType(definition.getEntityType())
                .displayOrder(mapping.getDisplayOrder())
                .build();
    }
}