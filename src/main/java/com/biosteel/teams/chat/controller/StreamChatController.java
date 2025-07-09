package com.biosteel.teams.chat.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.biosteel.teams.auth.dto.ApiResponse;
import com.biosteel.teams.chat.dto.ChatTokenDTO;
import com.biosteel.teams.chat.dto.ChatUserDTO;
import com.biosteel.teams.chat.service.StreamChatService;
import com.biosteel.teams.user.model.User;
import com.biosteel.teams.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/stream-chat")
@RequiredArgsConstructor
@Tag(name = "Stream Chat", description = "APIs for Stream Chat operations")
public class StreamChatController {

    private final StreamChatService streamChatService;
    private final UserService userService;

    @Operation(summary = "Get Stream Chat token for authenticated user")
    @GetMapping("/token")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ChatTokenDTO>> getStreamToken() {
        User user = userService.getLoggedInUser();
        ChatTokenDTO token = streamChatService.getAccessToken(user.getUserId());

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Stream Chat token generated successfully",
                token));
    }

    @Operation(summary = "Get members of a team channel")
    @GetMapping("/teams/{teamId}/members")
    @SecurityRequirement(name = "bearerAuth")
    // @PreAuthorize("hasRole('SUPER_ADMIN') or
    // @teamSecurityService.isTeamMember(#teamId)")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ChatUserDTO>>> getChannelMembers(
            @Parameter(description = "Team ID", required = true) @PathVariable UUID teamId) {

        List<ChatUserDTO> members = streamChatService.getChannelMembers(teamId);

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Team channel members retrieved successfully",
                members));
    }

    @Operation(summary = "Get livestream recording URLs")
    @GetMapping("/livestreams/{livestreamId}/recordings")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRecordings(
            @Parameter(description = "Livestream ID (Event ID)", required = true) @PathVariable String livestreamId) {

        Map<String, Object> recordings = streamChatService.getRecordings(livestreamId);

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Livestream recordings retrieved successfully",
                recordings));
    }
}