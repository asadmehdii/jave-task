package com.biosteel.teams.team.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
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

import com.biosteel.teams.sport.dto.AttributeValueDTO;
import com.biosteel.teams.sport.mapper.AttributeValueMapper;
import com.biosteel.teams.sport.service.SportAttributeService;
import com.biosteel.teams.team.dto.ContactRequestDTO;
import com.biosteel.teams.team.dto.ContactResponseDTO;
import com.biosteel.teams.team.dto.TeamDTO;
import com.biosteel.teams.team.dto.TeamFeedDTO;
import com.biosteel.teams.team.dto.TeamLiveDTO;
import com.biosteel.teams.team.dto.TeamMemberMinimalDTO;
import com.biosteel.teams.team.dto.TeamMemberRequestDTO;
import com.biosteel.teams.team.dto.TeamMemberResponseDTO;
import com.biosteel.teams.team.dto.TeamMinimalDTO;
import com.biosteel.teams.team.model.TeamAttributeValue;
import com.biosteel.teams.team.model.TeamMemberAttributeValue;
import com.biosteel.teams.team.service.TeamMemberService;
import com.biosteel.teams.team.service.TeamService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/users/{userId}/teams")
@Tag(name = "Teams Management", description = "APIs for managing teams and team members")
@Validated
@RequiredArgsConstructor
@Slf4j
public class TeamController {
    private final TeamService teamService;
    private final TeamMemberService teamMemberService;
    private final SportAttributeService sportAttributeService;
    private final AttributeValueMapper attributeValueMapper;

    @Operation(summary = "Create a new team")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Team created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('SUPER_ADMIN') or #userId == authentication.principal.userId")
    public TeamDTO createTeam(
            @PathVariable UUID userId,
            @Valid @RequestBody TeamDTO teamDTO) {
        return teamService.createTeam(userId, teamDTO);
    }

    @Operation(summary = "Get all teams for a user")
    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('SUPER_ADMIN') or #userId == authentication.principal.userId")
    public List<TeamDTO> getUserTeams(@PathVariable UUID userId) {
        return teamService.getUserTeams(userId);
    }

    @Operation(summary = "Search teams by sport type")
    @GetMapping("/search")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<TeamMinimalDTO>> searchTeams(
            @RequestParam(required = true) String sportTypeCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        PageRequest pageRequest = PageRequest.of(
                page,
                size,
                Sort.Direction.fromString(sortDirection),
                sortBy);

        Page<TeamMinimalDTO> teams = teamService.searchTeams(sportTypeCode, pageRequest);

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(teams.getTotalElements()))
                .header("X-Total-Pages", String.valueOf(teams.getTotalPages()))
                .body(teams);
    }

    @Operation(summary = "Update a team")
    @PutMapping("/{teamId}")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('SUPER_ADMIN') or #userId == authentication.principal.userId")
    public TeamDTO updateTeam(
            @PathVariable UUID userId,
            @PathVariable UUID teamId,
            @Valid @RequestBody TeamDTO teamDTO) {
        return teamService.updateTeam(userId, teamId, teamDTO);
    }

    @Operation(summary = "Get Team by Team Id")
    @GetMapping("/{teamId}")
    @SecurityRequirement(name = "bearerAuth")
    public TeamDTO getTeam(
            @PathVariable UUID userId,
            @PathVariable UUID teamId) {
        return teamService.getTeam(userId, teamId);
    }

    @Operation(summary = "Delete a team")
    @DeleteMapping("/{teamId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('SUPER_ADMIN') or #userId == authentication.principal.userId")
    public void deleteTeam(
            @PathVariable UUID userId,
            @PathVariable UUID teamId) {
        teamService.deleteTeam(userId, teamId);
    }

    @Operation(summary = "Add a team member")
    @PostMapping("/{teamId}/members")
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "bearerAuth")
    public TeamMemberResponseDTO addTeamMember(
            @PathVariable UUID userId,
            @PathVariable UUID teamId,
            @Valid @RequestBody TeamMemberRequestDTO memberDTO) {
        return teamMemberService.addTeamMember(userId, teamId, memberDTO);
    }

    @Operation(summary = "Get all team members")
    @GetMapping("/{teamId}/members")
    @SecurityRequirement(name = "bearerAuth")
    public List<TeamMemberResponseDTO> getTeamMembers(
            @PathVariable UUID userId,
            @PathVariable UUID teamId) {
        return teamMemberService.getTeamMembers(userId, teamId);
    }

    @Operation(summary = "Filter team members by member role and current user's role")
    @GetMapping("/{teamId}/members/filter")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public List<TeamMemberResponseDTO> filterTeamMembers(
            @PathVariable UUID userId,
            @PathVariable UUID teamId,
            @RequestParam(required = false) String memberRole,
            @RequestParam(required = false) String currentUserRole) {
        return teamMemberService.filterTeamMembers(userId, teamId, memberRole, currentUserRole);
    }

    @Operation(summary = "Update a team member")
    @PutMapping("/{teamId}/members/{memberId}")
    @SecurityRequirement(name = "bearerAuth")
    public TeamMemberResponseDTO updateTeamMember(
            @PathVariable UUID userId,
            @PathVariable UUID teamId,
            @PathVariable UUID memberId,
            @Valid @RequestBody TeamMemberRequestDTO memberDTO) {
        return teamMemberService.updateTeamMember(userId, teamId, memberId, memberDTO);
    }

    @Operation(summary = "Remove a team member")
    @DeleteMapping("/{teamId}/members/{memberId}")
    @SecurityRequirement(name = "bearerAuth")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeTeamMember(
            @PathVariable UUID userId,
            @PathVariable UUID teamId,
            @PathVariable UUID memberId) {
        teamMemberService.removeTeamMember(userId, teamId, memberId);
    }

    @Operation(summary = "Get team's live game data")
    @GetMapping("/{teamId}/live")
    @SecurityRequirement(name = "bearerAuth")
    public TeamLiveDTO getTeamLive(
            @PathVariable UUID userId,
            @PathVariable UUID teamId) {
        return teamService.getTeamLive(userId, teamId);
    }

    @Operation(summary = "Get team's feed data")
    @GetMapping("/{teamId}/feed")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('SUPER_ADMIN') or #userId == authentication.principal.userId")
    public TeamFeedDTO getTeamFeed(
            @PathVariable UUID userId,
            @PathVariable UUID teamId) {
        return teamService.getTeamFeed(userId, teamId);
    }

    // Team Attribute endpoints
    @Operation(summary = "Create or update team attributes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Team attributes updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Team not found")
    })
    @PostMapping("/{teamId}/attributes")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('SUPER_ADMIN') or #userId == authentication.principal.userId")
    public List<AttributeValueDTO> updateTeamAttributes(
            @PathVariable UUID userId,
            @PathVariable UUID teamId,
            @Valid @RequestBody List<AttributeValueDTO> attributes) {
        List<TeamAttributeValue> reponseAttributes = sportAttributeService.updateTeamAttributes(teamId, attributes);
        return attributeValueMapper.toTeamAttributeDTOList(reponseAttributes);
    }

    @Operation(summary = "Get all team attribute values")
    @GetMapping("/{teamId}/attributes")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('SUPER_ADMIN') or #userId == authentication.principal.userId")
    public List<AttributeValueDTO> getTeamAttributes(
            @PathVariable UUID userId,
            @PathVariable UUID teamId) {
        List<TeamAttributeValue> attributes = sportAttributeService.getAllTeamAttributeValues(teamId);
        return attributeValueMapper.toTeamAttributeDTOList(attributes);
    }

    @Operation(summary = "Create or update team member attributes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Team member attributes updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Team member not found")
    })
    @PostMapping("/{teamId}/members/{memberId}/attributes")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('SUPER_ADMIN') or #userId == authentication.principal.userId")
    public List<AttributeValueDTO> updateTeamMemberAttributes(
            @PathVariable UUID userId,
            @PathVariable UUID teamId,
            @PathVariable UUID memberId,
            @Valid @RequestBody List<AttributeValueDTO> attributes) {

        List<TeamMemberAttributeValue> resoponseAttributes = sportAttributeService.updateTeamMemberAttributes(memberId,
                attributes);
        return attributeValueMapper.toTeamMemberAttributeDTOList(resoponseAttributes);
    }

    @Operation(summary = "Get all team member attribute values")
    @GetMapping("/{teamId}/members/{memberId}/attributes")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('SUPER_ADMIN') or #userId == authentication.principal.userId")
    public List<AttributeValueDTO> getTeamMemberAttributes(
            @PathVariable UUID userId,
            @PathVariable UUID teamId,
            @PathVariable UUID memberId) {
        List<TeamMemberAttributeValue> attributes = sportAttributeService.getAllTeamMemberAttributeValues(memberId);
        return attributeValueMapper.toTeamMemberAttributeDTOList(attributes);
    }

    @Operation(summary = "Search players and users for team member addition")
    @GetMapping("/member-candidates")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TeamMemberMinimalDTO>> searchTeamMemberCandidates(
            @RequestParam(defaultValue = "false") boolean isPlayerOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "firstName") String sortBy,
            @RequestParam(required = false) String q) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sortBy));
        Page<TeamMemberMinimalDTO> results = teamService.searchTeamMemberCandidates(pageRequest, q);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(results.getTotalElements()))
                .body(results.getContent());
    }

    @Operation(summary = "Get all contacts for a team member")
    @GetMapping("/{teamId}/members/{memberId}/contacts")
    @SecurityRequirement(name = "bearerAuth")
    public List<ContactResponseDTO> getMemberContacts(
            @PathVariable UUID userId,
            @PathVariable UUID teamId,
            @PathVariable UUID memberId) {
        return teamMemberService.getMemberContacts(userId, teamId, memberId);
    }

    @Operation(summary = "Add a new contact to a team member")
    @PostMapping("/{teamId}/members/{memberId}/contacts")
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "bearerAuth")
    public ContactResponseDTO addMemberContact(
            @PathVariable UUID userId,
            @PathVariable UUID teamId,
            @PathVariable UUID memberId,
            @Valid @RequestBody ContactRequestDTO contactRequest) {
        return teamMemberService.addMemberContact(userId, teamId, memberId, contactRequest);
    }

    @Operation(summary = "Update an existing contact for a team member")
    @PutMapping("/{teamId}/members/{memberId}/contacts/{contactId}")
    @SecurityRequirement(name = "bearerAuth")
    public ContactResponseDTO updateMemberContact(
            @PathVariable UUID userId,
            @PathVariable UUID teamId,
            @PathVariable UUID memberId,
            @PathVariable UUID contactId,
            @Valid @RequestBody ContactRequestDTO contactRequest) {
        return teamMemberService.updateMemberContact(userId, teamId, memberId, contactId, contactRequest);
    }

    @Operation(summary = "Remove a contact from a team member")
    @DeleteMapping("/{teamId}/members/{memberId}/contacts/{contactId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "bearerAuth")
    public void removeMemberContact(
            @PathVariable UUID userId,
            @PathVariable UUID teamId,
            @PathVariable UUID memberId,
            @PathVariable UUID contactId) {
        teamMemberService.removeMemberContact(userId, teamId, memberId, contactId);
    }
}
