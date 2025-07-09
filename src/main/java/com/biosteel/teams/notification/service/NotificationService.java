package com.biosteel.teams.notification.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.biosteel.teams.common.exception.ResourceNotFoundException;
import com.biosteel.teams.notification.dto.DeviceTokenDTO;
import com.biosteel.teams.notification.dto.NotificationDTO;
import com.biosteel.teams.notification.dto.NotificationTarget;
import com.biosteel.teams.notification.mapper.DeviceTokenMapper;
import com.biosteel.teams.notification.model.DeviceToken;
import com.biosteel.teams.notification.model.NotificationLog;
import com.biosteel.teams.notification.repository.DeviceTokenRepository;
import com.biosteel.teams.notification.repository.NotificationLogRepository;
import com.biosteel.teams.team.model.TeamMember;
import com.biosteel.teams.team.repository.TeamMemberRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.TopicManagementResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final NotificationLogRepository notificationLogRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final DeviceTokenMapper deviceTokenMapper;
    private final FirebaseMessaging firebaseMessaging;
    private final ObjectMapper objectMapper;

    @Transactional
    public DeviceTokenDTO registerDeviceToken(DeviceTokenDTO deviceTokenDTO) {
        Optional<DeviceToken> existingToken = deviceTokenRepository.findByToken(deviceTokenDTO.getToken());

        if (existingToken.isPresent()) {
            DeviceToken token = existingToken.get();

            if (!token.getUserId().equals(deviceTokenDTO.getUserId())) {
                token.setIsActive(false);
                deviceTokenRepository.save(token);

                DeviceToken newToken = deviceTokenMapper.toEntity(deviceTokenDTO);
                // newToken.setLastUsedDate(LocalDateTime.now());
                return deviceTokenMapper.toDto(deviceTokenRepository.save(newToken));
            } else {
                // token.setLastUsedDate(LocalDateTime.now());
                token.setIsActive(true);
                token.setDeviceType(deviceTokenDTO.getDeviceType());
                token.setAppVersion(deviceTokenDTO.getAppVersion());
                return deviceTokenMapper.toDto(deviceTokenRepository.save(token));
            }
        } else {
            DeviceToken newToken = deviceTokenMapper.toEntity(deviceTokenDTO);
            // newToken.setLastUsedDate(LocalDateTime.now());
            return deviceTokenMapper.toDto(deviceTokenRepository.save(newToken));
        }
    }

    @Transactional
    public DeviceTokenDTO updateDeviceToken(UUID deviceTokenId, DeviceTokenDTO deviceTokenDTO) {
        DeviceToken deviceToken = deviceTokenRepository.findById(deviceTokenId)
                .orElseThrow(() -> new ResourceNotFoundException("Device token not found: " + deviceTokenId));

        deviceTokenMapper.updateEntityFromDto(deviceTokenDTO, deviceToken);
        // deviceToken.setLastUsedDate(LocalDateTime.now());

        return deviceTokenMapper.toDto(deviceTokenRepository.save(deviceToken));
    }

    @Transactional
    public void deactivateDeviceToken(String token) {
        DeviceToken deviceToken = deviceTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Device token not found: " + token));

        deviceToken.setIsActive(false);
        deviceTokenRepository.save(deviceToken);
    }

    public List<DeviceTokenDTO> getUserDeviceTokens(UUID userId) {
        List<DeviceToken> deviceTokens = deviceTokenRepository.findByUserIdAndIsActiveTrue(userId);
        return deviceTokenMapper.toDtoList(deviceTokens);
    }

    @Transactional
    public int deactivateAllUserTokensOnLogout(UUID userId) {
        List<DeviceToken> activeTokens = deviceTokenRepository.findByUserIdAndIsActiveTrue(userId);

        if (activeTokens.isEmpty()) {
            log.info("No active device tokens found for user: {} during logout", userId);
            return 0;
        }

        activeTokens.forEach(token -> {
            token.setIsActive(false);
        });

        deviceTokenRepository.saveAll(activeTokens);

        log.info("Deactivated {} device tokens for user: {} on logout", activeTokens.size(), userId);
        return activeTokens.size();
    }

    @Transactional
    public boolean deactivateDeviceTokenOnLogout(UUID userId, String deviceToken) {
        Optional<DeviceToken> tokenOpt = deviceTokenRepository.findByTokenAndUserId(deviceToken, userId);

        if (tokenOpt.isEmpty()) {
            log.warn("Device token not found for user: {} and token: {} during logout", userId, deviceToken);
            return false;
        }

        DeviceToken token = tokenOpt.get();
        if (!token.getIsActive()) {
            log.info("Device token already inactive for user: {} during logout", userId);
            return true;
        }

        token.setIsActive(false);
        deviceTokenRepository.save(token);

        log.info("Deactivated device token for user: {} on logout", userId);
        return true;
    }

    @Async
    @Transactional
    public void sendNotification(NotificationDTO notificationDTO) {
        try {
            NotificationTarget target = notificationDTO.getTarget();

            if (target == null) {
                log.error("Notification target is missing");
                return;
            }

            Message.Builder messageBuilder = Message.builder()
                    .setNotification(com.google.firebase.messaging.Notification.builder()
                            .setTitle(notificationDTO.getTitle())
                            .setBody(notificationDTO.getBody())
                            .setImage(notificationDTO.getImageUrl())
                            .build());

            if (notificationDTO.getData() != null && !notificationDTO.getData().isEmpty()) {
                messageBuilder.putAllData(notificationDTO.getData());
            }

            switch (target.getTargetType()) {
                case USER:
                    sendToUser(target.getUserId(), messageBuilder, notificationDTO);
                    break;

                case TEAM:
                    sendToTeam(target.getTeamId(), messageBuilder, notificationDTO);
                    break;
                case MULTI_USER:
                    sendToMultipleUsers(target.getUserIds(), messageBuilder, notificationDTO);

                case TOPIC:
                    sendToTopic(target.getTopic(), messageBuilder, notificationDTO);
                    break;

                case TOKEN:
                    sendToToken(target.getToken(), messageBuilder, notificationDTO);
                    break;

                default:
                    log.error("Unknown notification target type: {}", target.getTargetType());
                    break;
            }
        } catch (Exception e) {
            log.error("Error sending notification: {}", e.getMessage(), e);
            logNotificationError(notificationDTO, e.getMessage());
        }
    }

    private void sendToUser(UUID userId, Message.Builder messageBuilder, NotificationDTO notification) {
        List<DeviceToken> deviceTokens = deviceTokenRepository.findByUserIdAndIsActiveTrue(userId);

        if (deviceTokens.isEmpty()) {
            log.warn("No active device tokens found for user: {}", userId);
            return;
        }

        for (DeviceToken deviceToken : deviceTokens) {
            try {
                Message message = messageBuilder.setToken(deviceToken.getToken()).build();
                String response = firebaseMessaging.send(message);
                logNotificationSuccess(notification, userId, null, "USER");
                log.info("Successfully sent notification to user {}: {}", userId, response);
            } catch (FirebaseMessagingException e) {
                handleFirebaseException(e, deviceToken);
                logNotificationError(notification, e.getMessage());
            }
        }
    }

    private boolean sendToTeam(UUID teamId, Message.Builder messageBuilder, NotificationDTO notification) {
        List<TeamMember> teamMembers = teamMemberRepository.findByTeamIdAndLeftAtIsNull(teamId);

        if (teamMembers.isEmpty()) {
            log.warn("No members found for team: {}", teamId);
            return false;
        }

        List<UUID> teamMemberUserIds = teamMembers.stream()
                .map(TeamMember::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<DeviceToken> deviceTokens = deviceTokenRepository.findByUserIdInAndIsActiveTrue(teamMemberUserIds);

        if (deviceTokens.isEmpty()) {
            log.warn("No active device tokens found for team members: {}", teamId);
            return false;
        }

        boolean anySuccess = false;

        for (DeviceToken deviceToken : deviceTokens) {
            try {
                Message message = messageBuilder.setToken(deviceToken.getToken()).build();
                String response = firebaseMessaging.send(message);
                anySuccess = true;
                logNotificationSuccess(notification, deviceToken.getUserId(), teamId, "TEAM");
                log.info("Successfully sent notification to team member {}: {}", deviceToken.getUserId(), response);
            } catch (FirebaseMessagingException e) {
                handleFirebaseException(e, deviceToken);
                logNotificationError(notification, e.getMessage());
            }
        }

        return anySuccess;
    }

    private boolean sendToMultipleUsers(List<UUID> userIds, Message.Builder messageBuilder,
            NotificationDTO notification) {
        if (userIds == null || userIds.isEmpty()) {
            log.warn("No user IDs provided for multi-user notification");
            return false;
        }

        List<DeviceToken> deviceTokens = deviceTokenRepository.findByUserIdInAndIsActiveTrue(userIds);

        if (deviceTokens.isEmpty()) {
            log.warn("No active device tokens found for users: {}", userIds);
            return false;
        }

        boolean anySuccess = false;

        Map<UUID, List<DeviceToken>> tokensByUser = deviceTokens.stream()
                .collect(Collectors.groupingBy(DeviceToken::getUserId));

        for (Map.Entry<UUID, List<DeviceToken>> entry : tokensByUser.entrySet()) {
            UUID userId = entry.getKey();
            List<DeviceToken> userTokens = entry.getValue();

            for (DeviceToken deviceToken : userTokens) {
                try {
                    Message message = messageBuilder.setToken(deviceToken.getToken()).build();
                    String response = firebaseMessaging.send(message);
                    anySuccess = true;
                    logNotificationSuccess(notification, userId, null, "MULTI_USER");
                    log.info("Successfully sent notification to user {}: {}", userId, response);
                    break;
                } catch (FirebaseMessagingException e) {
                    handleFirebaseException(e, deviceToken);
                    logNotificationError(notification, e.getMessage());
                }
            }
        }

        return anySuccess;
    }

    private boolean sendToTopic(String topic, Message.Builder messageBuilder, NotificationDTO notification) {
        try {
            String normalizedTopic = topic.replaceAll("[^a-zA-Z0-9-_.~%]", "_");

            Message message = messageBuilder.setTopic(normalizedTopic).build();
            String response = firebaseMessaging.send(message);
            logNotificationSuccess(notification, null, null, "TOPIC");
            log.info("Successfully sent notification to topic {}: {}", topic, response);
            return true;
        } catch (FirebaseMessagingException e) {
            log.error("Error sending notification to topic {}: {}", topic, e.getMessage(), e);
            logNotificationError(notification, e.getMessage());
            return false;
        }
    }

    private boolean sendToToken(String token, Message.Builder messageBuilder, NotificationDTO notification) {
        try {
            Message message = messageBuilder.setToken(token).build();
            String response = firebaseMessaging.send(message);

            DeviceToken deviceToken = deviceTokenRepository.findByToken(token).orElse(null);
            UUID userId = deviceToken != null ? deviceToken.getUserId() : null;

            logNotificationSuccess(notification, userId, null, "TOKEN");
            log.info("Successfully sent notification to token: {}", response);
            return true;
        } catch (FirebaseMessagingException e) {
            log.error("Error sending notification to token {}: {}", token, e.getMessage(), e);

            if (e.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT ||
                    e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                Optional<DeviceToken> deviceToken = deviceTokenRepository.findByToken(token);
                deviceToken.ifPresent(dt -> {
                    dt.setIsActive(false);
                    deviceTokenRepository.save(dt);
                    log.info("Deactivated invalid token: {}", token);
                });
            }

            logNotificationError(notification, e.getMessage());
            return false;
        }
    }

    private void handleFirebaseException(FirebaseMessagingException e, DeviceToken deviceToken) {
        log.error("Error sending notification to token {}: {}", deviceToken.getToken(), e.getMessage());

        if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED ||
                e.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT) {
            deviceToken.setIsActive(false);
            deviceTokenRepository.save(deviceToken);
            log.info("Deactivated invalid token: {}", deviceToken.getToken());
        }
    }

    private void logNotificationSuccess(NotificationDTO notification, UUID userId, UUID teamId, String targetType) {
        try {
            NotificationLog log = NotificationLog.builder()
                    .userId(userId)
                    .teamId(teamId)
                    .title(notification.getTitle())
                    .body(notification.getBody())
                    .imageUrl(notification.getImageUrl())
                    .targetType(targetType)
                    .deliveryStatus("SUCCESS")
                    .build();

            if (notification.getData() != null) {
                log.setData(objectMapper.writeValueAsString(notification.getData()));
            }

            notificationLogRepository.save(log);
        } catch (JsonProcessingException e) {
            log.error("Error logging notification: {}", e.getMessage());
        }
    }

    private void logNotificationError(NotificationDTO notification, String errorMessage) {
        try {
            NotificationLog log = NotificationLog.builder()
                    .title(notification.getTitle())
                    .body(notification.getBody())
                    .imageUrl(notification.getImageUrl())
                    .deliveryStatus("FAILED")
                    .errorMessage(errorMessage)
                    .build();

            if (notification.getTarget() != null) {
                switch (notification.getTarget().getTargetType()) {
                    case USER:
                        log.setUserId(notification.getTarget().getUserId());
                        log.setTargetType("USER");
                        break;
                    case TEAM:
                        log.setTeamId(notification.getTarget().getTeamId());
                        log.setTargetType("TEAM");
                        break;
                    case MULTI_USER:
                        log.setTargetType("MULTI_USER");
                        break;
                    case TOPIC:
                        log.setTargetType("TOPIC");
                        break;
                    case TOKEN:
                        log.setTargetType("TOKEN");
                        break;
                }
            }

            if (notification.getData() != null) {
                log.setData(objectMapper.writeValueAsString(notification.getData()));
            }

            notificationLogRepository.save(log);
        } catch (JsonProcessingException e) {
            log.error("Error logging notification error: {}", e.getMessage());
        }
    }

    public boolean subscribeToTopic(UUID userId, String topic) {
        List<DeviceToken> deviceTokens = deviceTokenRepository.findByUserIdAndIsActiveTrue(userId);

        if (deviceTokens.isEmpty()) {
            log.warn("No active device tokens found for user: {}", userId);
            return false;
        }

        String normalizedTopic = topic.replaceAll("[^a-zA-Z0-9-_.~%]", "_");

        try {
            List<String> tokens = deviceTokens.stream()
                    .map(DeviceToken::getToken)
                    .collect(Collectors.toList());

            TopicManagementResponse response = firebaseMessaging.subscribeToTopic(tokens, normalizedTopic);

            if (response.getFailureCount() > 0) {
                List<TopicManagementResponse.Error> errors = response.getErrors();
                for (int i = 0; i < errors.size(); i++) {
                    if (errors.get(i) != null) {
                        log.error("Failed to subscribe token: {} to topic: {}, error: {}",
                                tokens.get(i), normalizedTopic, errors.get(i).getReason());
                    }
                }
            }

            return response.getSuccessCount() > 0;
        } catch (FirebaseMessagingException e) {
            log.error("Error subscribing to topic: {}", e.getMessage(), e);
            return false;
        }
    }

    public boolean unsubscribeFromTopic(UUID userId, String topic) {
        List<DeviceToken> deviceTokens = deviceTokenRepository.findByUserIdAndIsActiveTrue(userId);

        if (deviceTokens.isEmpty()) {
            log.warn("No active device tokens found for user: {}", userId);
            return false;
        }

        String normalizedTopic = topic.replaceAll("[^a-zA-Z0-9-_.~%]", "_");

        try {
            List<String> tokens = deviceTokens.stream()
                    .map(DeviceToken::getToken)
                    .collect(Collectors.toList());

            TopicManagementResponse response = firebaseMessaging.unsubscribeFromTopic(tokens, normalizedTopic);

            if (response.getFailureCount() > 0) {
                List<TopicManagementResponse.Error> errors = response.getErrors();
                for (int i = 0; i < errors.size(); i++) {
                    if (errors.get(i) != null) {
                        log.error("Failed to unsubscribe token: {} from topic: {}, error: {}",
                                tokens.get(i), normalizedTopic, errors.get(i).getReason());
                    }
                }
            }

            return response.getSuccessCount() > 0;
        } catch (FirebaseMessagingException e) {
            log.error("Error unsubscribing from topic: {}", e.getMessage(), e);
            return false;
        }
    }

    public Page<NotificationDTO> getUserNotifications(UUID userId, Pageable pageable) {
        Page<NotificationLog> notificationLogs = notificationLogRepository.findByUserIdOrderByCreatedAtDesc(userId,
                pageable);
        return notificationLogs.map(this::convertToNotificationDTO);
    }

    public Page<NotificationDTO> getTeamNotifications(UUID teamId, Pageable pageable) {
        Page<NotificationLog> notificationLogs = notificationLogRepository.findByTeamIdOrderByCreatedAtDesc(teamId,
                pageable);
        return notificationLogs.map(this::convertToNotificationDTO);
    }

    private NotificationDTO convertToNotificationDTO(NotificationLog notificationLog) {
        NotificationDTO dto = new NotificationDTO();
        dto.setTitle(notificationLog.getTitle());
        dto.setBody(notificationLog.getBody());
        dto.setImageUrl(notificationLog.getImageUrl());

        try {
            if (notificationLog.getData() != null && !notificationLog.getData().isEmpty()) {
                Map<String, String> data = objectMapper.readValue(notificationLog.getData(),
                        objectMapper.getTypeFactory().constructMapType(Map.class, String.class, String.class));
                dto.setData(data);
            }
        } catch (Exception e) {
            log.error("Error parsing notification data: {}", e.getMessage(), e);
        }

        return dto;
    }
}