package com.biosteel.teams.dashboard.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponseDTO {
    private List<EventSummaryDTO> todaysEvents;
    private List<EventSummaryDTO> upcomingEvents;
    private List<GameSummaryDTO> recentScoredGames;
    private DashboardStatsDTO stats;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate filterDate;
    private UUID filterTeamId;
    private UUID filterPlayerId;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PracticeInfoDTO {
        private UUID practiceId;
        private String instructions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventSummaryDTO {
        private UUID eventId;
        private UUID teamId;
        private String teamName;
        private String eventType;
        private String eventStatus;
        private String title;
        private String description;
        private String location;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime startTime;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime endTime;

        private GameInfoDTO gameInfo;
        private PracticeInfoDTO practiceInfo;
        private String sportType;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GameInfoDTO {
        private UUID gameId;
        private String homeTeamName;
        private String awayTeamName;
        private Integer homeScore;
        private Integer awayScore;
        private String gameStatus;
        private Boolean isHomeGame;
        private Integer currentPeriod;
        private Integer periodCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GameSummaryDTO {
        private UUID gameId;
        private UUID eventId;
        private String eventTitle;
        private UUID teamId;
        private String teamName;
        private String homeTeamName;
        private String awayTeamName;
        private Integer homeScore;
        private Integer awayScore;
        private String gameStatus;
        private Boolean isHomeGame;
        private String sportType;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime gameDateTime;

        private List<PlayerScoreDTO> playerScores;
        private Integer totalTeamScore;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlayerScoreDTO {
        private UUID playerId;
        private String playerName;
        private String scoreTypeName;
        private Integer points;
        private Integer period;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime recordedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardStatsDTO {
        private Integer totalTeams;
        private Integer totalUpcomingEvents;
        private Integer totalCompletedGames;
        private Integer totalActiveGames;
        private Integer gamesThisWeek;
        private Integer eventsToday;
        private Integer totalPlayersInTeams;
    }
}