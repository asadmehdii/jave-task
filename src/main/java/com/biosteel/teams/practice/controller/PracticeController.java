package com.biosteel.teams.practice.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.biosteel.teams.practice.dto.PracticeRequestDTO;
import com.biosteel.teams.practice.dto.PracticeResponseDTO;
import com.biosteel.teams.practice.service.PracticeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/users/{userId}/teams/{teamId}/practices")
@Tag(name = "Practice Management", description = "APIs for managing team practices")
@Validated
@RequiredArgsConstructor
@Slf4j
public class PracticeController {
    private final PracticeService practiceService;

    @Operation(summary = "Create a new practice")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('SUPER_ADMIN') or #userId == authentication.principal.userId")
    public PracticeResponseDTO createPractice(
            @PathVariable UUID userId,
            @PathVariable UUID teamId,
            @Valid @RequestBody PracticeRequestDTO practiceDTO) {
        return practiceService.createPractice(userId, teamId, practiceDTO);
    }

    @Operation(summary = "Get all team practices")
    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('SUPER_ADMIN') or #userId == authentication.principal.userId")
    public List<PracticeResponseDTO> getTeamPractices(
            @PathVariable UUID userId,
            @PathVariable UUID teamId) {
        return practiceService.getTeamPractices(userId, teamId);
    }

    @Operation(summary = "Get practice details")
    @GetMapping("/{practiceId}")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('SUPER_ADMIN') or #userId == authentication.principal.userId")
    public PracticeResponseDTO getPractice(
            @PathVariable UUID userId,
            @PathVariable UUID teamId,
            @PathVariable UUID practiceId) {
        return practiceService.getPractice(userId, teamId, practiceId);
    }

    @Operation(summary = "Update practice details")
    @PutMapping("/{practiceId}")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('SUPER_ADMIN') or #userId == authentication.principal.userId")
    public PracticeResponseDTO updatePractice(
            @PathVariable UUID userId,
            @PathVariable UUID teamId,
            @PathVariable UUID practiceId,
            @Valid @RequestBody PracticeRequestDTO practiceDTO) {
        return practiceService.updatePractice(userId, teamId, practiceId, practiceDTO);
    }

    @Operation(summary = "Delete a practice")
    @DeleteMapping("/{practiceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('SUPER_ADMIN') or #userId == authentication.principal.userId")
    public void deletePractice(
            @PathVariable UUID userId,
            @PathVariable UUID teamId,
            @PathVariable UUID practiceId) {
        practiceService.deletePractice(userId, teamId, practiceId);
    }
}