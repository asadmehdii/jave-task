package com.biosteel.teams.player.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.biosteel.teams.player.dto.PlayerDTO;
import com.biosteel.teams.player.service.PlayerService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/players")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Player Management", description = "APIs for general player management")
public class PlayerController {
    private final PlayerService playerService;

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    // @PreAuthorize("hasRole('SUPER_ADMIN') or #userId ==
    // authentication.principal.userId")
    // FIXME: Only provide this to SUPER_ADMIN, TEAM_CREATE roles
    public ResponseEntity<List<PlayerDTO>> getAllPlayers(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "firstName") String sortBy,
            @RequestParam(required = false) String q) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sortBy));
        Page<PlayerDTO> players = playerService.getAllPlayers(pageRequest, q);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(players.getTotalElements()))
                .body(players.getContent());
    }
}