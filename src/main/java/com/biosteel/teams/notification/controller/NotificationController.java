package com.biosteel.teams.notification.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.biosteel.teams.auth.dto.ApiResponse;
import com.biosteel.teams.notification.dto.NotificationDTO;
import com.biosteel.teams.notification.dto.NotificationTarget;
import com.biosteel.teams.notification.service.NotificationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/users/{userId}/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notifications", description = "APIs for managing notifications")
public class NotificationController {

        private final NotificationService notificationService;

        @Operation(summary = "Get user's notifications")
        @GetMapping
        @SecurityRequirement(name = "bearerAuth")
        public ResponseEntity<ApiResponse<Page<NotificationDTO>>> getUserNotifications(
                        @PathVariable UUID userId,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "desc") String sortDirection) {

                Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC
                                : Sort.Direction.DESC;
                PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));

                Page<NotificationDTO> notifications = notificationService.getUserNotifications(userId, pageRequest);
                return ResponseEntity.ok(new ApiResponse<>(
                                true,
                                "User notifications retrieved successfully",
                                notifications));
        }

        @Operation(summary = "Send notification")
        @PostMapping
        @ResponseStatus(HttpStatus.CREATED)
        @SecurityRequirement(name = "bearerAuth")
        public ResponseEntity<ApiResponse<Boolean>> sendNotification(
                        @PathVariable UUID userId,
                        @Valid @RequestBody NotificationDTO notificationDTO) {
                if (notificationDTO.getTarget() == null) {
                        NotificationTarget target = new NotificationTarget();
                        target.setTargetType(NotificationTarget.TargetType.USER);
                        target.setUserId(userId);
                        notificationDTO.setTarget(target);
                } else if (notificationDTO.getTarget().getTargetType() == NotificationTarget.TargetType.USER) {
                        notificationDTO.getTarget().setUserId(userId);
                }

                notificationService.sendNotification(notificationDTO);
                return ResponseEntity.ok(new ApiResponse<>(
                                true,
                                "Notification sent...",
                                true));
        }

        @Operation(summary = "Get team's notifications")
        @GetMapping("/teams/{teamId}")
        @SecurityRequirement(name = "bearerAuth")
        public ResponseEntity<ApiResponse<Page<NotificationDTO>>> getTeamNotifications(
                        @PathVariable UUID userId,
                        @PathVariable UUID teamId,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "desc") String sortDirection) {

                Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC
                                : Sort.Direction.DESC;
                PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));

                Page<NotificationDTO> notifications = notificationService.getTeamNotifications(teamId, pageRequest);
                return ResponseEntity.ok(new ApiResponse<>(
                                true,
                                "Team notifications retrieved successfully",
                                notifications));
        }

        @Operation(summary = "Subscribe to topic")
        @PostMapping("/topics/{topic}/subscribe")
        @SecurityRequirement(name = "bearerAuth")
        public ResponseEntity<ApiResponse<Boolean>> subscribeToTopic(
                        @PathVariable UUID userId,
                        @PathVariable String topic) {

                boolean subscribed = notificationService.subscribeToTopic(userId, topic);
                return ResponseEntity.ok(new ApiResponse<>(
                                subscribed,
                                subscribed ? "Subscribed to topic successfully" : "Failed to subscribe to topic",
                                subscribed));
        }

        @Operation(summary = "Unsubscribe from topic")
        @PostMapping("/topics/{topic}/unsubscribe")
        @SecurityRequirement(name = "bearerAuth")
        public ResponseEntity<ApiResponse<Boolean>> unsubscribeFromTopic(
                        @PathVariable UUID userId,
                        @PathVariable String topic) {

                boolean unsubscribed = notificationService.unsubscribeFromTopic(userId, topic);
                return ResponseEntity.ok(new ApiResponse<>(
                                unsubscribed,
                                unsubscribed ? "Unsubscribed from topic successfully"
                                                : "Failed to unsubscribe from topic",
                                unsubscribed));
        }
}