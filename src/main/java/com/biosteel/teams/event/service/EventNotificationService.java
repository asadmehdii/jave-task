package com.biosteel.teams.event.service;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.biosteel.teams.common.exception.ResourceNotFoundException;
import com.biosteel.teams.event.model.Event;
import com.biosteel.teams.event.model.Location;
import com.biosteel.teams.game.model.Game;
import com.biosteel.teams.notification.dto.NotificationDTO;
import com.biosteel.teams.notification.dto.NotificationTarget;
import com.biosteel.teams.notification.service.NotificationService;
import com.biosteel.teams.team.model.Team;
import com.biosteel.teams.team.model.TeamMember;
import com.biosteel.teams.team.repository.TeamMemberRepository;
import com.biosteel.teams.team.repository.TeamRepository;
import com.biosteel.teams.user.model.User;
import com.biosteel.teams.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class EventNotificationService {
    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamRepository teamRepository;
    private final NotificationService notificationService;

    @Transactional
    public void sendNewEventNotification(UUID createdByUserId, UUID teamId, Event event) {
        User creator = userRepository.findById(createdByUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found for: " + createdByUserId));

        String createdByName = creator.getUserName();
        String eventType = event.getEventType().toLowerCase();

        ZonedDateTime startTime = event.getStartTime();
        String eventDate = startTime.format(DateTimeFormatter.ofPattern("EEE, MMM d"));
        String eventTime = startTime.format(DateTimeFormatter.ofPattern("h:mm a"));

        List<TeamMember> teamMembers = teamMemberRepository.findByTeamIdAndLeftAtIsNull(teamId);
        if (teamMembers.isEmpty()) {
            log.warn("No members found for team: {}", teamId);
            return;
        }

        List<UUID> teamMemberUserIds = teamMembers.stream()
                .map(TeamMember::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Map<String, String> data = new HashMap<>();
        data.put("action", "new_event_added");
        data.put("teamId", teamId.toString());
        data.put("eventId", event.getEventId().toString());
        data.put("eventType", event.getEventType());

        String title = buildNotificationTitle(event, teamId);
        String body = buildNotificationBody(createdByName, event, eventType, eventDate, eventTime, teamId);

        for (UUID userId : teamMemberUserIds) {
            if (userId.equals(createdByUserId)) {
                continue;
            }

            NotificationTarget target = new NotificationTarget();
            target.setTargetType(NotificationTarget.TargetType.USER);
            target.setUserId(userId);

            NotificationDTO notification = new NotificationDTO();
            notification.setTitle(title);
            notification.setBody(body);
            notification.setData(data);
            notification.setTarget(target);

            try {
                notificationService.sendNotification(notification);
                log.debug("Sent notification to user: {} for event: {}", userId, event.getEventId());
            } catch (Exception e) {
                log.error("Failed to send notification to user: {} for event: {}", userId, event.getEventId(), e);
            }
        }

        log.info("Sent event notifications to {} team members for event: {}",
                teamMemberUserIds.size() - 1, event.getEventId()); // -1 to exclude creator
    }

    private String buildNotificationTitle(Event event, UUID teamId) {
        String eventType = event.getEventType();

        if ("GAME".equalsIgnoreCase(eventType)) {
            Game game = event.getGame();
            if (game != null) {
                GameInfo gameInfo = getGameInfo(game, teamId);
                if (gameInfo.homeTeam != null && gameInfo.opponent != null) {
                    return gameInfo.isHomeGame ? gameInfo.homeTeam + " vs. " + gameInfo.opponent
                            : gameInfo.homeTeam + " @ " + gameInfo.opponent;
                }
            }
            return "New game scheduled";
        } else if ("PRACTICE".equalsIgnoreCase(eventType)) {
            return "New practice scheduled";
        } else {
            return "New " + eventType.toLowerCase() + " scheduled";
        }
    }

    private String buildNotificationBody(String createdByName, Event event, String eventType,
            String eventDate, String eventTime, UUID teamId) {
        StringBuilder body = new StringBuilder();
        body.append(createdByName).append(" scheduled a ");

        if ("GAME".equalsIgnoreCase(event.getEventType())) {
            Game game = event.getGame();
            if (game != null) {
                GameInfo gameInfo = getGameInfo(game, teamId);
                if (gameInfo.homeTeam != null && gameInfo.opponent != null) {
                    if (gameInfo.isHomeGame) {
                        body.append("game - ").append(gameInfo.homeTeam).append(" vs. ").append(gameInfo.opponent);
                    } else {
                        body.append("game - ").append(gameInfo.homeTeam).append(" @ ").append(gameInfo.opponent);
                    }
                } else {
                    body.append("new game");
                }
            } else {
                body.append("new game");
            }
        } else {
            body.append("new ").append(eventType);
        }

        body.append(" on ").append(eventDate).append(" at ").append(eventTime);

        String locationInfo = getLocationInfo(event);
        if (locationInfo != null && !locationInfo.isEmpty()) {
            body.append(" at ").append(locationInfo);
        }

        return body.toString();
    }

    private static class GameInfo {
        String homeTeam;
        String opponent;
        boolean isHomeGame;

        GameInfo(String homeTeam, String opponent, boolean isHomeGame) {
            this.homeTeam = homeTeam;
            this.opponent = opponent;
            this.isHomeGame = isHomeGame;
        }
    }

    private GameInfo getGameInfo(Game game, UUID teamId) {
        if (game == null || teamId == null) {
            return new GameInfo(null, null, true);
        }

        boolean isHomeGame = teamId.equals(game.getHomeTeamId());
        String homeTeamName = getTeamName(game.getHomeTeamId());
        String awayTeamName = getTeamName(game.getAwayTeamId(), game.getAwayTeamName());

        if (isHomeGame) {
            return new GameInfo(homeTeamName, awayTeamName, true);
        } else {
            return new GameInfo(homeTeamName, awayTeamName, false);
        }
    }

    private String getTeamName(UUID teamId) {
        return getTeamName(teamId, null);
    }

    private String getTeamName(UUID teamId, String fallbackName) {
        if (teamId != null) {
            try {
                Team team = teamRepository.findById(teamId).orElse(null);
                if (team != null && team.getName() != null && !team.getName().trim().isEmpty()) {
                    return team.getName().trim();
                }
            } catch (Exception e) {
                log.warn("Could not fetch team details for teamId: {}", teamId, e);
            }
        }

        if (fallbackName != null && !fallbackName.trim().isEmpty()) {
            return fallbackName.trim();
        }

        return null;
    }

    private String getLocationInfo(Event event) {
        try {
            Location location = event.getLocation();
            if (location != null) {
                return formatLocationName(location);
            }
        } catch (Exception e) {
            log.warn("Could not fetch location details for event: {}", event.getEventId(), e);
        }

        return null;
    }

    private String formatLocationName(Location location) {
        if (location == null) {
            return null;
        }

        if (location.getPlaceName() != null && !location.getPlaceName().trim().isEmpty()) {
            return location.getPlaceName().trim();
        }

        StringBuilder locationName = new StringBuilder();

        if (location.getAddressLine1() != null && !location.getAddressLine1().trim().isEmpty()) {
            locationName.append(location.getAddressLine1().trim());
        }

        if (location.getCity() != null && !location.getCity().trim().isEmpty()) {
            if (locationName.length() > 0) {
                locationName.append(", ");
            }
            locationName.append(location.getCity().trim());
        }

        return locationName.length() > 0 ? locationName.toString() : null;
    }
}