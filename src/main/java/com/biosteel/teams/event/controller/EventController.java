package com.biosteel.teams.event.controller;

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

import com.biosteel.teams.event.dto.EventAttendanceRequestDTO;
import com.biosteel.teams.event.dto.EventAttendanceResponseDTO;
import com.biosteel.teams.event.dto.EventAttendanceUpdateRequestDTO;
import com.biosteel.teams.event.dto.EventRequestDTO;
import com.biosteel.teams.event.dto.EventResponseDTO;
import com.biosteel.teams.event.service.EventService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/users/{userId}/teams/{teamId}/events")
@Tag(name = "Event Management", description = "APIs for managing team events and attendance")
@Validated
@Slf4j
public class EventController {
    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @Operation(summary = "Create a new event")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Event created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Team not found")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('SUPER_ADMIN') or #userId == authentication.principal.userId")
    public EventResponseDTO createEvent(
            @PathVariable UUID userId,
            @PathVariable UUID teamId,
            @Valid @RequestBody EventRequestDTO eventDTO) {
        return eventService.createEvent(userId, teamId, eventDTO);
    }

    @Operation(summary = "Get all team events")
    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('SUPER_ADMIN') or #userId == authentication.principal.userId")
    public List<EventResponseDTO> getTeamEvents(
            @PathVariable UUID userId,
            @PathVariable UUID teamId) {
        return eventService.getTeamEvents(userId, teamId);
    }

    @Operation(summary = "Update an event")
    @PutMapping("/{eventId}")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('SUPER_ADMIN') or #userId == authentication.principal.userId")
    public EventResponseDTO updateEvent(
            @PathVariable UUID userId,
            @PathVariable UUID teamId,
            @PathVariable UUID eventId,
            @Valid @RequestBody EventRequestDTO eventDTO) {
        return eventService.updateEvent(userId, teamId, eventId, eventDTO);
    }

    @Operation(summary = "Delete an event")
    @DeleteMapping("/{eventId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('SUPER_ADMIN') or #userId == authentication.principal.userId")
    public void deleteEvent(
            @PathVariable UUID userId,
            @PathVariable UUID teamId,
            @PathVariable UUID eventId) {
        eventService.deleteEvent(userId, teamId, eventId);
    }

    @Operation(summary = "Record event attendance")
    @PostMapping("/{eventId}/attendance")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('SUPER_ADMIN') or #userId == authentication.principal.userId")
    public EventAttendanceResponseDTO recordAttendance(
            @PathVariable UUID userId,
            @PathVariable UUID teamId,
            @PathVariable UUID eventId,
            @Valid @RequestBody EventAttendanceRequestDTO attendanceDTO) {
        return eventService.recordAttendance(userId, teamId, eventId, attendanceDTO);
    }

    @Operation(summary = "Update event attendance")
    @PutMapping("/{eventId}/attendance/{eventAttendanceId}")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('SUPER_ADMIN') or #userId == authentication.principal.userId")
    public EventAttendanceResponseDTO updateAttendance(
            @PathVariable UUID userId,
            @PathVariable UUID teamId,
            @PathVariable UUID eventId,
            @PathVariable UUID eventAttendanceId,
            @Valid @RequestBody EventAttendanceUpdateRequestDTO attendanceDTO) {
        return eventService.updateAttendance(userId, teamId, eventId, eventAttendanceId, attendanceDTO);
    }

    @Operation(summary = "Get event attendance")
    @GetMapping("/{eventId}/attendance")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('SUPER_ADMIN') or #userId == authentication.principal.userId")
    public List<EventAttendanceResponseDTO> getEventAttendance(
            @PathVariable UUID userId,
            @PathVariable UUID teamId,
            @PathVariable UUID eventId) {
        return eventService.getEventAttendance(userId, teamId, eventId);
    }
}