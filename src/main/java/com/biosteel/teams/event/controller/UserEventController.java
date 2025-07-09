package com.biosteel.teams.event.controller;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.biosteel.teams.common.dto.PageResponse;
import com.biosteel.teams.event.dto.EventResponseDTO;
import com.biosteel.teams.event.service.EventService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/users/{userId}/events")
@Tag(name = "User Events", description = "APIs for managing user's events across all teams")
@Validated
@Slf4j
public class UserEventController {
    private final EventService eventService;

    public UserEventController(EventService eventService) {
        this.eventService = eventService;
    }

    @Operation(summary = "Get Event by Event Id")
    @GetMapping("/{eventId}")
    @SecurityRequirement(name = "bearerAuth")
    public EventResponseDTO getEvent(
            @PathVariable UUID userId,
            @PathVariable UUID eventId) {
        return eventService.getEvent(eventId);
    }

    @Operation(summary = "Get user events with filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Events retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input parameters"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('SUPER_ADMIN') or #userId == authentication.principal.userId")
    public PageResponse<EventResponseDTO> getUserEvents(
            @PathVariable UUID userId,
            @RequestParam(required = false, defaultValue = "UPCOMING") String timeFilter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {

        Page<EventResponseDTO> eventPage = eventService.getUserEvents(userId, timeFilter, startDate, endDate, eventType,
                page, size);
        return PageResponse.from(eventPage);
    }
}