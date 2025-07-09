package com.biosteel.teams.notification.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.biosteel.teams.auth.dto.ApiResponse;
import com.biosteel.teams.notification.dto.DeviceTokenDTO;
import com.biosteel.teams.notification.service.NotificationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/users/{userId}/tokens")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Device Tokens", description = "APIs for managing device tokens for push notifications")
public class DeviceTokenController {

    private final NotificationService notificationService;

    @Operation(summary = "Register device token for push notifications")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<DeviceTokenDTO>> registerDeviceToken(
            @PathVariable UUID userId,
            @Valid @RequestBody DeviceTokenDTO deviceTokenDTO) {

        deviceTokenDTO.setUserId(userId);

        DeviceTokenDTO registeredToken = notificationService.registerDeviceToken(deviceTokenDTO);
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Device token registered successfully",
                registeredToken));
    }

    @Operation(summary = "Get user's device tokens")
    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<List<DeviceTokenDTO>>> getUserDeviceTokens(
            @PathVariable UUID userId) {

        List<DeviceTokenDTO> deviceTokens = notificationService.getUserDeviceTokens(userId);
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "User device tokens retrieved successfully",
                deviceTokens));
    }

    @Operation(summary = "Update device token")
    @PutMapping("/{deviceTokenId}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<DeviceTokenDTO>> updateDeviceToken(
            @PathVariable UUID userId,
            @PathVariable UUID deviceTokenId,
            @Valid @RequestBody DeviceTokenDTO deviceTokenDTO) {

        deviceTokenDTO.setUserId(userId);

        DeviceTokenDTO updatedToken = notificationService.updateDeviceToken(deviceTokenId, deviceTokenDTO);
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Device token updated successfully",
                updatedToken));
    }

    @Operation(summary = "Deactivate device token")
    @DeleteMapping("/{token}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "bearerAuth")

    public ResponseEntity<ApiResponse<Void>> deactivateDeviceToken(
            @PathVariable UUID userId,
            @PathVariable String token) {

        notificationService.deactivateDeviceToken(token);
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Device token deactivated successfully"));
    }
}