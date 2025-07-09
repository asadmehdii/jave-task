package com.biosteel.teams.game.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.biosteel.teams.auth.dto.ApiResponse;
import com.biosteel.teams.game.dto.GameRequestDTO;
import com.biosteel.teams.game.dto.GameResponseDTO;
import com.biosteel.teams.game.dto.GameScoreRequestDTO;
import com.biosteel.teams.game.dto.GameScoreResponseDTO;
import com.biosteel.teams.game.dto.GameStatisticRequestDTO;
import com.biosteel.teams.game.dto.GameStatisticResponseDTO;
import com.biosteel.teams.game.dto.UpdateGameStatusRequestDTO;
import com.biosteel.teams.game.service.GameService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/users/{userId}/teams/{teamId}/games")
@Tag(name = "Game Management", description = "APIs for managing team games and scoring")
@Validated
@RequiredArgsConstructor
@Slf4j
public class GameController {
    private final GameService gameService;

    @Operation(summary = "Create a new game")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('SUPER_ADMIN') or #userId == authentication.principal.userId")
    public GameResponseDTO createGame(
            @PathVariable UUID userId,
            @PathVariable UUID teamId,
            @Valid @RequestBody GameRequestDTO gameDTO) {
        return gameService.createGame(userId, teamId, gameDTO);
    }

    @Operation(summary = "Get all team games")
    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('SUPER_ADMIN') or #userId == authentication.principal.userId")
    public List<GameResponseDTO> getTeamGames(
            @PathVariable UUID userId,
            @PathVariable UUID teamId) {
        return gameService.getTeamGames(userId, teamId);
    }

    @Operation(summary = "Get game details")
    @GetMapping("/{gameId}")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('SUPER_ADMIN') or #userId == authentication.principal.userId")
    public GameResponseDTO getGame(
            @PathVariable UUID userId,
            @PathVariable UUID teamId,
            @PathVariable UUID gameId) {
        return gameService.getGame(userId, teamId, gameId);
    }

    @Operation(summary = "Update game details")
    @PutMapping("/{gameId}")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('SUPER_ADMIN') or #userId == authentication.principal.userId")
    public GameResponseDTO updateGame(
            @PathVariable UUID userId,
            @PathVariable UUID teamId,
            @PathVariable UUID gameId,
            @Valid @RequestBody GameRequestDTO gameDTO) {
        return gameService.updateGame(userId, teamId, gameId, gameDTO);
    }

    @Operation(summary = "Start a game")
    @PutMapping("/{gameId}/start")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('SUPER_ADMIN') or #userId == authentication.principal.userId")
    public ResponseEntity<ApiResponse<GameResponseDTO>> startGame(
            @PathVariable UUID userId,
            @PathVariable UUID teamId,
            @PathVariable UUID gameId) {
        GameResponseDTO game = gameService.startGame(userId, teamId, gameId);
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Game started successfully",
                game));
    }

    @Operation(summary = "End a game")
    @PutMapping("/{gameId}/end")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('SUPER_ADMIN') or #userId == authentication.principal.userId")
    public ResponseEntity<ApiResponse<GameResponseDTO>> endGame(
            @PathVariable UUID userId,
            @PathVariable UUID teamId,
            @PathVariable UUID gameId) {
        GameResponseDTO game = gameService.endGame(userId, teamId, gameId);
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Game ended successfully",
                game));
    }

    @Operation(summary = "Record game score")
    @PostMapping("/{gameId}/scores")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('SUPER_ADMIN') or #userId == authentication.principal.userId")
    public GameScoreResponseDTO recordScore(
            @PathVariable UUID userId,
            @PathVariable UUID teamId,
            @PathVariable UUID gameId,
            @Valid @RequestBody GameScoreRequestDTO scoreDTO) {
        return gameService.recordScore(userId, teamId, gameId, scoreDTO);
    }

    @Operation(summary = "Get game scores")
    @GetMapping("/{gameId}/scores")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('SUPER_ADMIN') or #userId == authentication.principal.userId")
    public List<GameScoreResponseDTO> getGameScores(
            @PathVariable UUID userId,
            @PathVariable UUID teamId,
            @PathVariable UUID gameId) {
        return gameService.getGameScores(userId, teamId, gameId);
    }

    @Operation(summary = "Record game statistics")
    @PostMapping("/{gameId}/statistics")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('SUPER_ADMIN') or #userId == authentication.principal.userId")
    public GameStatisticResponseDTO recordStatistic(
            @PathVariable UUID userId,
            @PathVariable UUID teamId,
            @PathVariable UUID gameId,
            @Valid @RequestBody GameStatisticRequestDTO statisticDTO) {
        return gameService.recordStatistic(userId, teamId, gameId, statisticDTO);
    }

    @Operation(summary = "Get game statistics")
    @GetMapping("/{gameId}/statistics")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('SUPER_ADMIN') or #userId == authentication.principal.userId")
    public List<GameStatisticResponseDTO> getGameStatistics(
            @PathVariable UUID userId,
            @PathVariable UUID teamId,
            @PathVariable UUID gameId) {
        return gameService.getGameStatistics(userId, teamId, gameId);
    }

    @Operation(summary = "Update game status")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{gameId}/status")
    @PreAuthorize("hasRole('SUPER_ADMIN') or #userId == authentication.principal.userId")
    public ResponseEntity<ApiResponse<Void>> updateGameStatus(
            @PathVariable UUID userId,
            @PathVariable UUID teamId,
            @PathVariable UUID gameId,
            @Valid @RequestBody UpdateGameStatusRequestDTO request) {

        gameService.updateGameStatus(userId, teamId, gameId, request.getStatus());
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Game status updated successfully"));
    }
}