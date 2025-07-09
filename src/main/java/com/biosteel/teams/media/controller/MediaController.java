package com.biosteel.teams.media.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.biosteel.teams.auth.dto.ApiResponse;
import com.biosteel.teams.media.dto.MediaDto;
import com.biosteel.teams.media.service.MediaService;
import com.biosteel.teams.user.model.User;
import com.biosteel.teams.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
@Tag(name = "Media Management", description = "APIs for media management")
public class MediaController {

    private final MediaService mediaService;
    private final UserService userService;

    @Operation(summary = "Upload media file")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MediaDto>> uploadMedia(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "teamId", required = false) UUID teamId,
            @RequestParam(value = "userId", required = false) UUID userId,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "body", required = false) String body) {

        MediaDto uploadedMedia = mediaService.uploadMedia(file, teamId, userId, title, body);
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Media uploaded successfully",
                uploadedMedia));
    }

    @Operation(summary = "Get media by ID", description = "Streams the media file content. Returns the file for download or inline display depending on the media type.")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{mediaId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> getMedia(@PathVariable UUID mediaId) {
        return mediaService.streamMedia(mediaId);
    }

    @Operation(summary = "Get all media for a team")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/team/{teamId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<MediaDto>>> getTeamMedia(@PathVariable UUID teamId) {
        List<MediaDto> mediaList = mediaService.getTeamMedia(teamId);
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Team media retrieved successfully",
                mediaList));
    }

    @Operation(summary = "Update media metadata and file")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping(value = "/{mediaId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MediaDto>> updateMedia(
            @PathVariable UUID mediaId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String body,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        User user = userService.getLoggedInUser();
        MediaDto updatedMedia = mediaService.updateMedia(user, mediaId, title, body, file);
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Media updated successfully",
                updatedMedia));
    }

    @Operation(summary = "Delete media")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{mediaId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteMedia(@PathVariable UUID mediaId) {
        mediaService.deleteMedia(mediaId);
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Media deleted successfully"));
    }
}