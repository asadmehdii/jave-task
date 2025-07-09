package com.biosteel.teams.sport.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.biosteel.teams.sport.dto.ScoreTypeDTO;
import com.biosteel.teams.sport.dto.SportAttributeDefinitionDTO;
import com.biosteel.teams.sport.dto.SportTypeDTO;
import com.biosteel.teams.sport.mapper.SportTypeMapper;
import com.biosteel.teams.sport.repository.SportTypeRepository;
import com.biosteel.teams.sport.service.ScoreTypeService;
import com.biosteel.teams.sport.service.SportAttributeDefinitionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/sports")
@Tag(name = "Sport and Attributes", description = "APIs for retrieving sport related and attribute definitions")
@Validated
@RequiredArgsConstructor
public class SportAttributeDefinitionController {

    private final SportAttributeDefinitionService attributeDefinitionService;
    private final SportTypeRepository sportTypeRepository;
    private final SportTypeMapper sportTypeMapper;
    private final ScoreTypeService scoreTypeService;

    @Operation(summary = "Get all sport types")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retrieved all sport types successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SportTypeDTO.class)))
    })
    @GetMapping(produces = "application/json")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<SportTypeDTO>> getAllSportTypes() {
        var sportTypes = sportTypeRepository.findAll();
        var sportTypeDTOs = sportTypeMapper.toDTOList(sportTypes);
        return ResponseEntity.ok(sportTypeDTOs);
    }

    @Operation(summary = "Get all attribute definitions grouped by sport type")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retrieved all attribute definitions successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class)))
    })
    @GetMapping("/attributes")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, List<SportAttributeDefinitionDTO>>> getAllAttributeDefinitions() {
        return ResponseEntity.ok(attributeDefinitionService.getAllAttributeDefinitionsBySportType());
    }

    @Operation(summary = "Get all attribute definitions for a sport type")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retrieved attribute definitions successfully"),
            @ApiResponse(responseCode = "404", description = "Sport type not found")
    })
    @GetMapping("/{sportTypeCode}/attributes")
    @SecurityRequirement(name = "bearerAuth")
    // @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('COACH') or
    // hasRole('MANAGER')")
    public List<SportAttributeDefinitionDTO> getAttributeDefinitions(
            @PathVariable String sportTypeCode,
            @RequestParam(required = false) String entityType) {
        return attributeDefinitionService.getAttributeDefinitions(sportTypeCode, entityType);
    }

    @Operation(summary = "Get required attribute definitions for a sport type")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retrieved required attribute definitions successfully"),
            @ApiResponse(responseCode = "404", description = "Sport type not found")
    })
    @GetMapping("/{sportTypeCode}/attributes/required")
    @SecurityRequirement(name = "bearerAuth")
    // @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('COACH') or
    // hasRole('MANAGER')")
    public List<SportAttributeDefinitionDTO> getRequiredAttributeDefinitions(
            @PathVariable String sportTypeCode,
            @RequestParam(required = false) String entityType) {
        return attributeDefinitionService.getRequiredAttributeDefinitions(sportTypeCode, entityType);
    }

    @Operation(summary = "Get validation rules for an attribute")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retrieved validation rules successfully"),
            @ApiResponse(responseCode = "404", description = "Attribute definition not found")
    })
    @GetMapping("/attributes/{attributeCode}/validation")
    @SecurityRequirement(name = "bearerAuth")
    // @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('COACH') or
    // hasRole('MANAGER')")
    public String getAttributeValidationRules(@PathVariable String attributeCode) {
        return attributeDefinitionService.getAttributeValidationRules(attributeCode);
    }

    @Operation(summary = "Get all score types")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retrieved all score types successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ScoreTypeDTO.class)))
    })
    @GetMapping("/score-types")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<ScoreTypeDTO>> getAllScoreTypes() {
        return ResponseEntity.ok(scoreTypeService.getAllScoreTypes());
    }

    @Operation(summary = "Get score types for a specific sport")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retrieved score types successfully"),
            @ApiResponse(responseCode = "404", description = "Sport type not found")
    })
    @GetMapping("/{sportTypeCode}/score-types")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<ScoreTypeDTO>> getScoreTypesForSport(
            @PathVariable String sportTypeCode) {
        return ResponseEntity.ok(scoreTypeService.getScoreTypesForSport(sportTypeCode));
    }
}