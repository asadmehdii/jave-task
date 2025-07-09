package com.biosteel.teams.dashboard.controller;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.biosteel.teams.dashboard.dto.DashboardResponseDTO;
import com.biosteel.teams.dashboard.service.DashboardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/users/{userId}/dashboard")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Dashboard", description = "APIs for dashboard data aggregation")
public class DashboardController {

        private final DashboardService dashboardService;

        @Operation(summary = "Get user dashboard data", description = "Retrieves comprehensive dashboard data including today's events (or 5 upcoming if less than 5), "
                        +
                        "past scored games in the current week, with optional filtering by team, player, or specific date")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Dashboard data retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "User not found"),
                        @ApiResponse(responseCode = "403", description = "Access denied")
        })
        @GetMapping
        @SecurityRequirement(name = "bearerAuth")
        @PreAuthorize("hasRole('SUPER_ADMIN') or #userId == authentication.principal.userId")
        public DashboardResponseDTO getDashboard(
                        @Parameter(description = "User ID", required = true) @PathVariable UUID userId,

                        @Parameter(description = "Filter by specific team ID") @RequestParam(required = false) UUID teamId,

                        @Parameter(description = "Filter by specific player ID") @RequestParam(required = false) UUID playerId,

                        @Parameter(description = "Filter by specific date (YYYY-MM-DD). If not provided, uses current date") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

                log.info("Getting dashboard data for user: {}, teamId: {}, playerId: {}, date: {}",
                                userId, teamId, playerId, date);

                return dashboardService.getDashboardData(userId, teamId, playerId, date);
        }
}