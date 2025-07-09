package com.biosteel.teams.player.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.biosteel.teams.player.dto.PlayerCreateDTO;
import com.biosteel.teams.player.dto.PlayerDTO;
import com.biosteel.teams.player.service.PlayerService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/users/{userId}/players")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Player Management", description = "APIs for user management")
public class UserPlayerController {

    private final PlayerService playerService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('SUPER_ADMIN') or #userId == authentication.principal.userId")
    public ResponseEntity<PlayerDTO> createPlayer(
            @PathVariable UUID userId,
            @Valid @RequestBody PlayerCreateDTO playerCreateDTO) {
        log.info("Creating new player for user {}: {}", userId, playerCreateDTO);
        PlayerDTO createdPlayer = playerService.createPlayer(userId, playerCreateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPlayer);
    }

    @GetMapping("/{playerId}")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('SUPER_ADMIN') or #userId == authentication.principal.userId")
    public ResponseEntity<PlayerDTO> getPlayer(
            @PathVariable UUID userId,
            @PathVariable UUID playerId) {
        log.info("Fetching player {} for user {}", playerId, userId);
        PlayerDTO player = playerService.getPlayerByIdAndUserId(playerId, userId);
        return ResponseEntity.ok(player);
    }

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('SUPER_ADMIN') or #userId == authentication.principal.userId")
    public ResponseEntity<List<PlayerDTO>> getAllUserPlayers(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "firstName") String sortBy) {
        log.info("Fetching all players for user {}", userId);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sortBy));
        Page<PlayerDTO> players = playerService.getAllPlayersByUserId(userId, pageRequest);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(players.getTotalElements()))
                .body(players.getContent());
    }

    @PutMapping("/{playerId}")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('SUPER_ADMIN') or #userId == authentication.principal.userId")
    public ResponseEntity<PlayerDTO> updatePlayer(
            @PathVariable UUID userId,
            @PathVariable UUID playerId,
            @Valid @RequestBody PlayerCreateDTO playerUpdateDTO) {
        log.info("Updating player {} for user {}", playerId, userId);
        PlayerDTO updatedPlayer = playerService.updatePlayer(userId, playerId, playerUpdateDTO);
        return ResponseEntity.ok(updatedPlayer);
    }

    @DeleteMapping("/{playerId}")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('SUPER_ADMIN') or #userId == authentication.principal.userId")
    public ResponseEntity<Void> deletePlayer(
            @PathVariable UUID userId,
            @PathVariable UUID playerId) {
        log.info("Deleting player {} for user {}", playerId, userId);
        playerService.deletePlayer(userId, playerId);
        return ResponseEntity.noContent().build();
    }
}