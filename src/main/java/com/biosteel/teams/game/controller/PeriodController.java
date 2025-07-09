package com.biosteel.teams.game.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.biosteel.teams.game.dto.GamePeriodUpdateDTO;
import com.biosteel.teams.game.dto.PeriodTypeDTO;
import com.biosteel.teams.game.dto.SportPeriodDefinitionDTO;
import com.biosteel.teams.game.mapper.PeriodMapper;
import com.biosteel.teams.game.model.PeriodType;
import com.biosteel.teams.game.model.SportPeriodDefinition;
import com.biosteel.teams.game.service.PeriodService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Game Period Management", description = "APIs for managing game periods")
public class PeriodController {

    private final PeriodService periodService;
    private final PeriodMapper periodMapper;

    @Operation(summary = "Get all period types")
    @GetMapping("/period-types")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PeriodTypeDTO>> getAllPeriodTypes() {
        List<PeriodType> periodTypes = periodService.getAllPeriodTypes();
        return ResponseEntity.ok(periodMapper.toPeriodTypeDTOList(periodTypes));
    }

    @Operation(summary = "Get period definitions for a sport")
    @GetMapping("/sports/{sportTypeCode}/period-definitions")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SportPeriodDefinitionDTO>> getPeriodDefinitionsForSport(
            @PathVariable String sportTypeCode) {
        List<SportPeriodDefinition> periodDefinitions = periodService.getPeriodDefinitionsForSport(sportTypeCode);
        return ResponseEntity.ok(periodMapper.toSportPeriodDefinitionDTOList(periodDefinitions));
    }

    @Operation(summary = "Initialize game periods")
    @PostMapping("/users/{userId}/teams/{teamId}/games/{gameId}/periods/initialize")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SportPeriodDefinitionDTO> initializeGamePeriods(
            @PathVariable UUID userId,
            @PathVariable UUID teamId,
            @PathVariable UUID gameId) {

        periodService.initializeGamePeriods(userId, gameId);
        SportPeriodDefinition currentPeriod = periodService.getCurrentPeriodDefinition(gameId);
        return ResponseEntity.ok(periodMapper.toSportPeriodDefinitionDTO(currentPeriod));
    }

    @Operation(summary = "Update game period")
    @PutMapping("/users/{userId}/teams/{teamId}/games/{gameId}/periods")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SportPeriodDefinitionDTO> updateGamePeriod(
            @PathVariable UUID userId,
            @PathVariable UUID teamId,
            @PathVariable UUID gameId,
            @Valid @RequestBody GamePeriodUpdateDTO periodUpdateDTO) {

        periodService.updateGamePeriod(userId, gameId, periodUpdateDTO);
        SportPeriodDefinition currentPeriod = periodService.getCurrentPeriodDefinition(gameId);
        return ResponseEntity.ok(periodMapper.toSportPeriodDefinitionDTO(currentPeriod));
    }

    @Operation(summary = "Advance game to next period")
    @PostMapping("/users/{userId}/teams/{teamId}/games/{gameId}/periods/advance")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SportPeriodDefinitionDTO> advanceGamePeriod(
            @PathVariable UUID userId,
            @PathVariable UUID teamId,
            @PathVariable UUID gameId) {

        periodService.advanceToNextPeriod(userId, teamId, gameId);

        SportPeriodDefinition currentPeriod = periodService.getCurrentPeriodDefinition(gameId);
        return ResponseEntity.ok(periodMapper.toSportPeriodDefinitionDTO(currentPeriod));
    }

    @Operation(summary = "Get current period definition for game")
    @GetMapping("/users/{userId}/teams/{teamId}/games/{gameId}/periods/current")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SportPeriodDefinitionDTO> getCurrentPeriodDefinition(
            @PathVariable UUID userId,
            @PathVariable UUID teamId,
            @PathVariable UUID gameId) {

        SportPeriodDefinition currentPeriod = periodService.getCurrentPeriodDefinition(gameId);
        return ResponseEntity.ok(periodMapper.toSportPeriodDefinitionDTO(currentPeriod));
    }
}