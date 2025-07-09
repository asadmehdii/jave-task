package com.biosteel.teams.invitation.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.biosteel.teams.auth.dto.ApiResponse;
import com.biosteel.teams.invitation.dto.BulkInvitationRequestDTO;
import com.biosteel.teams.invitation.dto.InvitationRequestDTO;
import com.biosteel.teams.invitation.dto.InvitationResponseDTO;
import com.biosteel.teams.invitation.service.InvitationService;
import com.biosteel.teams.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/users/{userId}/teams/{teamId}/invitations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Invitations", description = "APIs for managing team invitations")
public class InvitationController {

        private final InvitationService invitationService;
        private final UserService userService;

        @Operation(summary = "Send a single invitation")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Invitation created successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Unauthorized access")
        })
        @PostMapping
        @ResponseStatus(HttpStatus.CREATED)
        @SecurityRequirement(name = "bearerAuth")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<ApiResponse<InvitationResponseDTO>> createInvitation(
                        @Valid @RequestBody InvitationRequestDTO request) {

                UUID currentUserId = userService.getLoggedInUser().getUserId();
                InvitationResponseDTO invitation = invitationService.createInvitation(currentUserId, request);

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(new ApiResponse<>(true, "Invitation created successfully", invitation));
        }

        @Operation(summary = "Send bulk invitations")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Invitations created successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Unauthorized access")
        })
        @PostMapping("/bulk")
        @ResponseStatus(HttpStatus.CREATED)
        @SecurityRequirement(name = "bearerAuth")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<ApiResponse<List<InvitationResponseDTO>>> createBulkInvitations(
                        @Valid @RequestBody BulkInvitationRequestDTO request) {

                UUID currentUserId = userService.getLoggedInUser().getUserId();
                List<InvitationResponseDTO> invitations = invitationService.createBulkInvitations(currentUserId,
                                request);

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(new ApiResponse<>(true, "Bulk invitations created successfully", invitations));
        }

        @Operation(summary = "Get all invitations for a team")
        @GetMapping("")
        @SecurityRequirement(name = "bearerAuth")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<ApiResponse<List<InvitationResponseDTO>>> getTeamInvitations(
                        @PathVariable UUID teamId) {

                UUID currentUserId = userService.getLoggedInUser().getUserId();
                List<InvitationResponseDTO> invitations = invitationService.getTeamInvitations(currentUserId, teamId);

                return ResponseEntity
                                .ok(new ApiResponse<>(true, "Team invitations retrieved successfully", invitations));
        }

        @Operation(summary = "Cancel an invitation")
        @DeleteMapping("/{invitationId}")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        @SecurityRequirement(name = "bearerAuth")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<ApiResponse<Void>> cancelInvitation(
                        @PathVariable UUID invitationId) {

                UUID currentUserId = userService.getLoggedInUser().getUserId();
                invitationService.cancelInvitation(currentUserId, invitationId);

                return ResponseEntity.ok(new ApiResponse<>(true, "Invitation cancelled successfully"));
        }
}