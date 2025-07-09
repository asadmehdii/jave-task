package com.biosteel.teams.dashboard.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.biosteel.teams.dashboard.dto.DashboardResponseDTO;
import com.biosteel.teams.dashboard.dto.DashboardResponseDTO.DashboardStatsDTO;
import com.biosteel.teams.dashboard.dto.DashboardResponseDTO.EventSummaryDTO;
import com.biosteel.teams.dashboard.dto.DashboardResponseDTO.GameInfoDTO;
import com.biosteel.teams.dashboard.dto.DashboardResponseDTO.GameSummaryDTO;
import com.biosteel.teams.dashboard.dto.DashboardResponseDTO.PlayerScoreDTO;
import com.biosteel.teams.dashboard.dto.DashboardResponseDTO.PracticeInfoDTO;
import com.biosteel.teams.dashboard.repository.DashboardRepository;
import com.biosteel.teams.user.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardService {

        private final DashboardRepository dashboardRepository;
        private final UserService userService;

        public DashboardResponseDTO getDashboardData(UUID userId, UUID teamId, UUID playerId, LocalDate date) {
                // Validate user exists
                userService.validateUserExists(userId);

                LocalDate targetDate = date != null ? date : LocalDate.now();
                LocalDateTime startOfDay = targetDate.atStartOfDay();
                LocalDateTime endOfDay = targetDate.atTime(LocalTime.MAX);

                // Get today's events
                List<EventSummaryDTO> todaysEvents = getTodaysEvents(userId, teamId, playerId, startOfDay, endOfDay);

                // Get upcoming events if today's events are less than 5
                List<EventSummaryDTO> upcomingEvents = List.of();
                if (todaysEvents.size() < 5) {
                        int limit = 5 - todaysEvents.size();
                        upcomingEvents = getUpcomingEvents(userId, teamId, playerId, endOfDay, limit);
                }

                // Get past scored games from this week
                LocalDate startOfWeek = targetDate.minusDays(targetDate.getDayOfWeek().getValue() - 1);
                List<GameSummaryDTO> recentScoredGames = getRecentScoredGames(
                                userId, teamId, playerId, startOfWeek.atStartOfDay(), targetDate.atTime(LocalTime.MAX));

                // Get dashboard statistics
                DashboardStatsDTO stats = getDashboardStats(userId, teamId, playerId, targetDate);

                return DashboardResponseDTO.builder()
                                .todaysEvents(todaysEvents)
                                .upcomingEvents(upcomingEvents)
                                .recentScoredGames(recentScoredGames)
                                .stats(stats)
                                .filterDate(targetDate)
                                .filterTeamId(teamId)
                                .filterPlayerId(playerId)
                                .build();
        }

        private List<EventSummaryDTO> getTodaysEvents(UUID userId, UUID teamId, UUID playerId,
                        LocalDateTime startOfDay, LocalDateTime endOfDay) {
                log.debug("Getting today's events for user: {}, team: {}, player: {} between {} and {}",
                                userId, teamId, playerId, startOfDay, endOfDay);

                return dashboardRepository.findTodaysEvents(userId, teamId, playerId, startOfDay, endOfDay)
                                .stream()
                                .map(this::mapToEventSummaryDTO)
                                .collect(Collectors.toList());
        }

        private List<EventSummaryDTO> getUpcomingEvents(UUID userId, UUID teamId, UUID playerId,
                        LocalDateTime fromDateTime, int limit) {
                log.debug("Getting {} upcoming events for user: {}, team: {}, player: {} from {}",
                                limit, userId, teamId, playerId, fromDateTime);

                return dashboardRepository.findUpcomingEvents(userId, teamId, playerId, fromDateTime, limit)
                                .stream()
                                .map(this::mapToEventSummaryDTO)
                                .collect(Collectors.toList());
        }

        private List<GameSummaryDTO> getRecentScoredGames(UUID userId, UUID teamId, UUID playerId,
                        LocalDateTime startOfWeek, LocalDateTime endOfPeriod) {
                log.debug("Getting recent scored games for user: {}, team: {}, player: {} between {} and {}",
                                userId, teamId, playerId, startOfWeek, endOfPeriod);

                List<GameSummaryDTO> games = dashboardRepository
                                .findRecentScoredGames(userId, teamId, playerId, startOfWeek, endOfPeriod)
                                .stream()
                                .map(this::mapToGameSummaryDTO)
                                .collect(Collectors.toList());

                // Enhance with player scores
                for (GameSummaryDTO game : games) {
                        List<PlayerScoreDTO> playerScores = dashboardRepository.findPlayerScoresForGame(
                                        game.getGameId(), teamId, playerId).stream()
                                        .map(this::mapToPlayerScoreDTO)
                                        .collect(Collectors.toList());

                        game.setPlayerScores(playerScores);
                        game.setTotalTeamScore(playerScores.stream()
                                        .mapToInt(PlayerScoreDTO::getPoints)
                                        .sum());
                }

                return games;
        }

        private DashboardStatsDTO getDashboardStats(UUID userId, UUID teamId, UUID playerId, LocalDate targetDate) {
                LocalDateTime startOfDay = targetDate.atStartOfDay();
                LocalDateTime endOfDay = targetDate.atTime(LocalTime.MAX);
                LocalDate startOfWeek = targetDate.minusDays(targetDate.getDayOfWeek().getValue() - 1);
                LocalDateTime startOfWeekDateTime = startOfWeek.atStartOfDay();

                return DashboardStatsDTO.builder()
                                .totalTeams(dashboardRepository.countUserTeams(userId, teamId))
                                .totalUpcomingEvents(dashboardRepository.countUpcomingEvents(userId, teamId, playerId,
                                                endOfDay))
                                .totalCompletedGames(dashboardRepository.countCompletedGames(userId, teamId, playerId))
                                .totalActiveGames(dashboardRepository.countActiveGames(userId, teamId, playerId))
                                .gamesThisWeek(dashboardRepository.countGamesInPeriod(userId, teamId, playerId,
                                                startOfWeekDateTime, endOfDay))
                                .eventsToday(dashboardRepository.countEventsInPeriod(userId, teamId, playerId,
                                                startOfDay, endOfDay))
                                .totalPlayersInTeams(dashboardRepository.countPlayersInUserTeams(userId, teamId))
                                .build();
        }

        private EventSummaryDTO mapToEventSummaryDTO(Object[] row) {
                return EventSummaryDTO.builder()
                                .eventId((UUID) row[0])
                                .teamId((UUID) row[1])
                                .teamName((String) row[2])
                                .eventType((String) row[3])
                                .eventStatus((String) row[4])
                                .title((String) row[5])
                                .description((String) row[6])
                                .location((String) row[7])
                                .startTime(((Instant) row[8]).atZone(ZoneId.systemDefault()).toLocalDateTime())
                                .endTime(((Instant) row[9]).atZone(ZoneId.systemDefault()).toLocalDateTime())
                                .sportType((String) row[10])
                                .gameInfo(row[11] != null ? GameInfoDTO.builder()
                                                .gameId((UUID) row[11])
                                                .homeTeamName((String) row[12])
                                                .awayTeamName((String) row[13])
                                                .homeScore((Integer) row[14])
                                                .awayScore((Integer) row[15])
                                                .gameStatus((String) row[16])
                                                .isHomeGame((Boolean) row[17])
                                                .currentPeriod((Integer) row[18])
                                                .periodCount((Integer) row[19])
                                                .build() : null)
                                .practiceInfo(row[20] != null ? PracticeInfoDTO.builder()
                                                .practiceId((UUID) row[20])
                                                .instructions((String) row[21])
                                                .build() : null)
                                .build();
        }

        private GameSummaryDTO mapToGameSummaryDTO(Object[] row) {
                return GameSummaryDTO.builder()
                                .gameId((UUID) row[0])
                                .eventId((UUID) row[1])
                                .eventTitle((String) row[2])
                                .teamId((UUID) row[3])
                                .teamName((String) row[4])
                                .homeTeamName((String) row[5])
                                .awayTeamName((String) row[6])
                                .homeScore((Integer) row[7])
                                .awayScore((Integer) row[8])
                                .gameStatus((String) row[9])
                                .isHomeGame((Boolean) row[10])
                                .sportType((String) row[11])
                                .gameDateTime(((Instant) row[12]).atZone(ZoneId.systemDefault()).toLocalDateTime())
                                .build();
        }

        private PlayerScoreDTO mapToPlayerScoreDTO(Object[] row) {
                return PlayerScoreDTO.builder()
                                .playerId((UUID) row[0])
                                .playerName((String) row[1])
                                .scoreTypeName((String) row[2])
                                .points((Integer) row[3])
                                .period((Integer) row[4])
                                .recordedAt(((Instant) row[5]).atZone(ZoneId.systemDefault()).toLocalDateTime())
                                .build();
        }
}