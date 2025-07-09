package com.biosteel.teams.team.controller;

import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.biosteel.teams.team.service.TeamService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/teams")
@Tag(name = "Public Team APIs", description = "Publicly accessible team endpoints")
@RequiredArgsConstructor
@Slf4j
public class PublicTeamController {

    private final TeamService teamService;

    @Operation(summary = "Get team avatar")
    @GetMapping("/{teamId}/avatar")
    public ResponseEntity<Resource> getTeamAvatar(@PathVariable UUID teamId) {
        return teamService.getPublicAvatar(teamId);
    }
}