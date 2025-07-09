package com.biosteel.teams.dashboard.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.biosteel.teams.event.model.Event;

/**
 * Dashboard Repository with complete native SQL implementation
 * Includes support for events, games, practices, and comprehensive filtering
 */
@Repository
public interface DashboardRepository extends JpaRepository<Event, UUID> {

    /**
     * Find today's events for a user with optional filters
     * Returns: event_id, team_id, team_name, event_type, event_status, title,
     * description,
     * location, start_time, end_time, sport_type_code, game_id, home_team_name,
     * away_team_name, home_score, away_score, game_status, is_home_game,
     * current_period, period_count, practice_id, instructions
     */
    @Query(value = """
            SELECT DISTINCT
                e.event_id,
                e.team_id,
                t.name as team_name,
                e.event_type,
                e.event_status,
                e.title,
                e.description,
                COALESCE(l.place_name, l.address_line_1, e.location) as location,
                e.start_time,
                e.end_time,
                t.sport_type_code,
                g.game_id,
                COALESCE(ht.name, g.away_team_name) as home_team_name,
                COALESCE(at.name, g.away_team_name) as away_team_name,
                g.home_score,
                g.away_score,
                g.status as game_status,
                g.is_home_game,
                g.current_period,
                g.period_count,
                p.practice_id,
                p.instructions
            FROM biosteel.event e
            INNER JOIN biosteel.team t ON e.team_id = t.team_id
            INNER JOIN biosteel.team_member tm ON t.team_id = tm.team_id
            LEFT JOIN biosteel.game g ON e.game_id = g.game_id
            LEFT JOIN biosteel.team ht ON g.home_team_id = ht.team_id
            LEFT JOIN biosteel.team at ON g.away_team_id = at.team_id
            LEFT JOIN biosteel.location l ON e.location_id = l.location_id
            LEFT JOIN biosteel.practice p ON e.practice_id = p.practice_id
            WHERE tm.user_id = :userId
                AND e.start_time BETWEEN :startOfDay AND :endOfDay
                AND e.deleted_at IS NULL
                AND t.deleted_at IS NULL
                AND tm.left_at IS NULL
                AND (:teamId IS NULL OR t.team_id = :teamId)
                AND (:playerId IS NULL OR tm.player_id = :playerId OR
                     EXISTS (SELECT 1 FROM biosteel.team_member tm2
                            WHERE tm2.team_id = t.team_id
                            AND tm2.player_id = :playerId
                            AND tm2.left_at IS NULL))
            ORDER BY e.start_time ASC
            """, nativeQuery = true)
    List<Object[]> findTodaysEvents(
            @Param("userId") UUID userId,
            @Param("teamId") UUID teamId,
            @Param("playerId") UUID playerId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);

    /**
     * Find upcoming events for a user with optional filters and limit
     */
    @Query(value = """
            SELECT DISTINCT
                e.event_id,
                e.team_id,
                t.name as team_name,
                e.event_type,
                e.event_status,
                e.title,
                e.description,
                COALESCE(l.place_name, l.address_line_1, e.location) as location,
                e.start_time,
                e.end_time,
                t.sport_type_code,
                g.game_id,
                COALESCE(ht.name, g.away_team_name) as home_team_name,
                COALESCE(at.name, g.away_team_name) as away_team_name,
                g.home_score,
                g.away_score,
                g.status as game_status,
                g.is_home_game,
                g.current_period,
                g.period_count,
                p.practice_id,
                p.instructions
            FROM biosteel.event e
            INNER JOIN biosteel.team t ON e.team_id = t.team_id
            INNER JOIN biosteel.team_member tm ON t.team_id = tm.team_id
            LEFT JOIN biosteel.game g ON e.game_id = g.game_id
            LEFT JOIN biosteel.team ht ON g.home_team_id = ht.team_id
            LEFT JOIN biosteel.team at ON g.away_team_id = at.team_id
            LEFT JOIN biosteel.location l ON e.location_id = l.location_id
            LEFT JOIN biosteel.practice p ON e.practice_id = p.practice_id
            WHERE tm.user_id = :userId
                AND e.start_time > :fromDateTime
                AND e.deleted_at IS NULL
                AND t.deleted_at IS NULL
                AND tm.left_at IS NULL
                AND (:teamId IS NULL OR t.team_id = :teamId)
                AND (:playerId IS NULL OR tm.player_id = :playerId OR
                     EXISTS (SELECT 1 FROM biosteel.team_member tm2
                            WHERE tm2.team_id = t.team_id
                            AND tm2.player_id = :playerId
                            AND tm2.left_at IS NULL))
            ORDER BY e.start_time ASC
            LIMIT :limit
            """, nativeQuery = true)
    List<Object[]> findUpcomingEvents(
            @Param("userId") UUID userId,
            @Param("teamId") UUID teamId,
            @Param("playerId") UUID playerId,
            @Param("fromDateTime") LocalDateTime fromDateTime,
            @Param("limit") int limit);

    /**
     * Find recent scored games within a date range
     */
    @Query(value = """
            SELECT DISTINCT
                g.game_id,
                e.event_id,
                e.title as event_title,
                t.team_id,
                t.name as team_name,
                COALESCE(ht.name, g.away_team_name) as home_team_name,
                COALESCE(at.name, g.away_team_name) as away_team_name,
                g.home_score,
                g.away_score,
                g.status as game_status,
                g.is_home_game,
                t.sport_type_code,
                e.start_time as game_datetime
            FROM biosteel.game g
            INNER JOIN biosteel.event e ON g.game_id = e.game_id
            INNER JOIN biosteel.team t ON e.team_id = t.team_id
            INNER JOIN biosteel.team_member tm ON t.team_id = tm.team_id
            LEFT JOIN biosteel.team ht ON g.home_team_id = ht.team_id
            LEFT JOIN biosteel.team at ON g.away_team_id = at.team_id
            WHERE tm.user_id = :userId
                AND e.start_time BETWEEN :startOfWeek AND :endOfPeriod
                AND g.status IN ('COMPLETED', 'FINAL', 'FINISHED')
                AND EXISTS (SELECT 1 FROM biosteel.game_score gs
                           WHERE gs.game_id = g.game_id
                           AND gs.deleted_at IS NULL)
                AND e.deleted_at IS NULL
                AND t.deleted_at IS NULL
                AND g.deleted_at IS NULL
                AND tm.left_at IS NULL
                AND (:teamId IS NULL OR t.team_id = :teamId)
                AND (:playerId IS NULL OR tm.player_id = :playerId OR
                     EXISTS (SELECT 1 FROM biosteel.team_member tm2
                            WHERE tm2.team_id = t.team_id
                            AND tm2.player_id = :playerId
                            AND tm2.left_at IS NULL))
            ORDER BY e.start_time DESC
            """, nativeQuery = true)
    List<Object[]> findRecentScoredGames(
            @Param("userId") UUID userId,
            @Param("teamId") UUID teamId,
            @Param("playerId") UUID playerId,
            @Param("startOfWeek") LocalDateTime startOfWeek,
            @Param("endOfPeriod") LocalDateTime endOfPeriod);

    /**
     * Find player scores for a specific game
     */
    @Query(value = """
            SELECT
                COALESCE(gs.player_id, gs.user_id) as player_id,
                COALESCE(
                    CONCAT(p.first_name, ' ', p.last_name),
                    CONCAT(u.first_name, ' ', u.last_name),
                    gs.player_name,
                    'Unknown Player'
                ) as player_name,
                st.name as score_type_name,
                st.points,
                gs.period,
                gs.recorded_at
            FROM biosteel.game_score gs
            INNER JOIN biosteel.score_type st ON gs.score_type_id = st.score_type_id
            LEFT JOIN biosteel.player p ON gs.player_id = p.player_id AND p.deleted_at IS NULL
            LEFT JOIN biosteel.user_account u ON gs.user_id = u.user_id AND u.deleted_at IS NULL
            WHERE gs.game_id = :gameId
                AND gs.deleted_at IS NULL
                AND (:teamId IS NULL OR gs.team_id = :teamId)
                AND (:playerId IS NULL OR gs.player_id = :playerId OR gs.user_id = :playerId)
            ORDER BY gs.recorded_at ASC, gs.period ASC
            """, nativeQuery = true)
    List<Object[]> findPlayerScoresForGame(
            @Param("gameId") UUID gameId,
            @Param("teamId") UUID teamId,
            @Param("playerId") UUID playerId);

    /**
     * Get combined dashboard statistics in one query for performance
     */
    @Query(value = """
            WITH user_teams AS (
                SELECT DISTINCT t.team_id, t.name, t.sport_type_code
                FROM biosteel.team t
                INNER JOIN biosteel.team_member tm ON t.team_id = tm.team_id
                WHERE tm.user_id = :userId
                    AND t.deleted_at IS NULL
                    AND tm.left_at IS NULL
                    AND (:teamId IS NULL OR t.team_id = :teamId)
            )
            SELECT
                (SELECT COUNT(*) FROM user_teams) as total_teams,
                (SELECT COUNT(DISTINCT e.event_id)
                 FROM biosteel.event e
                 INNER JOIN user_teams ut ON e.team_id = ut.team_id
                 WHERE e.start_time > :currentDateTime
                    AND e.deleted_at IS NULL) as upcoming_events,
                (SELECT COUNT(DISTINCT g.game_id)
                 FROM biosteel.game g
                 INNER JOIN biosteel.event e ON g.game_id = e.game_id
                 INNER JOIN user_teams ut ON e.team_id = ut.team_id
                 WHERE g.status IN ('COMPLETED', 'FINAL', 'FINISHED')
                    AND e.deleted_at IS NULL
                    AND g.deleted_at IS NULL) as completed_games,
                (SELECT COUNT(DISTINCT g.game_id)
                 FROM biosteel.game g
                 INNER JOIN biosteel.event e ON g.game_id = e.game_id
                 INNER JOIN user_teams ut ON e.team_id = ut.team_id
                 WHERE g.status IN ('IN_PROGRESS', 'LIVE', 'ACTIVE', 'STARTED')
                    AND e.deleted_at IS NULL
                    AND g.deleted_at IS NULL) as active_games,
                (SELECT COUNT(DISTINCT e.event_id)
                 FROM biosteel.event e
                 INNER JOIN user_teams ut ON e.team_id = ut.team_id
                 WHERE e.start_time BETWEEN :startOfWeek AND :endOfWeek
                    AND e.deleted_at IS NULL) as games_this_week,
                (SELECT COUNT(DISTINCT e.event_id)
                 FROM biosteel.event e
                 INNER JOIN user_teams ut ON e.team_id = ut.team_id
                 WHERE e.start_time BETWEEN :startOfToday AND :endOfToday
                    AND e.deleted_at IS NULL) as events_today,
                (SELECT COUNT(DISTINCT tm.team_member_id)
                 FROM biosteel.team_member tm
                 INNER JOIN user_teams ut ON tm.team_id = ut.team_id
                 WHERE tm.role IN ('PLAYER', 'ROLE_PLAYER')
                    AND tm.left_at IS NULL) as total_players
            """, nativeQuery = true)
    List<Object[]> getCombinedDashboardStats(
            @Param("userId") UUID userId,
            @Param("teamId") UUID teamId,
            @Param("currentDateTime") LocalDateTime currentDateTime,
            @Param("startOfWeek") LocalDateTime startOfWeek,
            @Param("endOfWeek") LocalDateTime endOfWeek,
            @Param("startOfToday") LocalDateTime startOfToday,
            @Param("endOfToday") LocalDateTime endOfToday);

    /**
     * Count total teams for a user with optional team filter
     */
    @Query(value = """
            SELECT COUNT(DISTINCT t.team_id)
            FROM biosteel.team t
            INNER JOIN biosteel.team_member tm ON t.team_id = tm.team_id
            WHERE tm.user_id = :userId
                AND t.deleted_at IS NULL
                AND tm.left_at IS NULL
                AND (:teamId IS NULL OR t.team_id = :teamId)
            """, nativeQuery = true)
    Integer countUserTeams(@Param("userId") UUID userId, @Param("teamId") UUID teamId);

    /**
     * Count upcoming events for a user
     */
    @Query(value = """
            SELECT COUNT(DISTINCT e.event_id)
            FROM biosteel.event e
            INNER JOIN biosteel.team t ON e.team_id = t.team_id
            INNER JOIN biosteel.team_member tm ON t.team_id = tm.team_id
            WHERE tm.user_id = :userId
                AND e.start_time > :fromDateTime
                AND e.deleted_at IS NULL
                AND t.deleted_at IS NULL
                AND tm.left_at IS NULL
                AND (:teamId IS NULL OR t.team_id = :teamId)
                AND (:playerId IS NULL OR tm.player_id = :playerId OR
                     EXISTS (SELECT 1 FROM biosteel.team_member tm2
                            WHERE tm2.team_id = t.team_id
                            AND tm2.player_id = :playerId
                            AND tm2.left_at IS NULL))
            """, nativeQuery = true)
    Integer countUpcomingEvents(
            @Param("userId") UUID userId,
            @Param("teamId") UUID teamId,
            @Param("playerId") UUID playerId,
            @Param("fromDateTime") LocalDateTime fromDateTime);

    /**
     * Count completed games for a user
     */
    @Query(value = """
            SELECT COUNT(DISTINCT g.game_id)
            FROM biosteel.game g
            INNER JOIN biosteel.event e ON g.game_id = e.game_id
            INNER JOIN biosteel.team t ON e.team_id = t.team_id
            INNER JOIN biosteel.team_member tm ON t.team_id = tm.team_id
            WHERE tm.user_id = :userId
                AND g.status IN ('COMPLETED', 'FINAL', 'FINISHED')
                AND e.deleted_at IS NULL
                AND t.deleted_at IS NULL
                AND g.deleted_at IS NULL
                AND tm.left_at IS NULL
                AND (:teamId IS NULL OR t.team_id = :teamId)
                AND (:playerId IS NULL OR tm.player_id = :playerId OR
                     EXISTS (SELECT 1 FROM biosteel.team_member tm2
                            WHERE tm2.team_id = t.team_id
                            AND tm2.player_id = :playerId
                            AND tm2.left_at IS NULL))
            """, nativeQuery = true)
    Integer countCompletedGames(
            @Param("userId") UUID userId,
            @Param("teamId") UUID teamId,
            @Param("playerId") UUID playerId);

    /**
     * Count active/live games for a user
     */
    @Query(value = """
            SELECT COUNT(DISTINCT g.game_id)
            FROM biosteel.game g
            INNER JOIN biosteel.event e ON g.game_id = e.game_id
            INNER JOIN biosteel.team t ON e.team_id = t.team_id
            INNER JOIN biosteel.team_member tm ON t.team_id = tm.team_id
            WHERE tm.user_id = :userId
                AND g.status IN ('IN_PROGRESS', 'LIVE', 'ACTIVE', 'STARTED')
                AND e.deleted_at IS NULL
                AND t.deleted_at IS NULL
                AND g.deleted_at IS NULL
                AND tm.left_at IS NULL
                AND (:teamId IS NULL OR t.team_id = :teamId)
                AND (:playerId IS NULL OR tm.player_id = :playerId OR
                     EXISTS (SELECT 1 FROM biosteel.team_member tm2
                            WHERE tm2.team_id = t.team_id
                            AND tm2.player_id = :playerId
                            AND tm2.left_at IS NULL))
            """, nativeQuery = true)
    Integer countActiveGames(
            @Param("userId") UUID userId,
            @Param("teamId") UUID teamId,
            @Param("playerId") UUID playerId);

    /**
     * Count games within a specific time period
     */
    @Query(value = """
            SELECT COUNT(DISTINCT g.game_id)
            FROM biosteel.game g
            INNER JOIN biosteel.event e ON g.game_id = e.game_id
            INNER JOIN biosteel.team t ON e.team_id = t.team_id
            INNER JOIN biosteel.team_member tm ON t.team_id = tm.team_id
            WHERE tm.user_id = :userId
                AND e.start_time BETWEEN :startDateTime AND :endDateTime
                AND e.deleted_at IS NULL
                AND t.deleted_at IS NULL
                AND g.deleted_at IS NULL
                AND tm.left_at IS NULL
                AND (:teamId IS NULL OR t.team_id = :teamId)
                AND (:playerId IS NULL OR tm.player_id = :playerId OR
                     EXISTS (SELECT 1 FROM biosteel.team_member tm2
                            WHERE tm2.team_id = t.team_id
                            AND tm2.player_id = :playerId
                            AND tm2.left_at IS NULL))
            """, nativeQuery = true)
    Integer countGamesInPeriod(
            @Param("userId") UUID userId,
            @Param("teamId") UUID teamId,
            @Param("playerId") UUID playerId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);

    /**
     * Count events within a specific time period
     */
    @Query(value = """
            SELECT COUNT(DISTINCT e.event_id)
            FROM biosteel.event e
            INNER JOIN biosteel.team t ON e.team_id = t.team_id
            INNER JOIN biosteel.team_member tm ON t.team_id = tm.team_id
            WHERE tm.user_id = :userId
                AND e.start_time BETWEEN :startDateTime AND :endDateTime
                AND e.deleted_at IS NULL
                AND t.deleted_at IS NULL
                AND tm.left_at IS NULL
                AND (:teamId IS NULL OR t.team_id = :teamId)
                AND (:playerId IS NULL OR tm.player_id = :playerId OR
                     EXISTS (SELECT 1 FROM biosteel.team_member tm2
                            WHERE tm2.team_id = t.team_id
                            AND tm2.player_id = :playerId
                            AND tm2.left_at IS NULL))
            """, nativeQuery = true)
    Integer countEventsInPeriod(
            @Param("userId") UUID userId,
            @Param("teamId") UUID teamId,
            @Param("playerId") UUID playerId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);

    /**
     * Count total players in teams that the user manages/owns
     */
    @Query(value = """
            SELECT COUNT(DISTINCT tm_player.team_member_id)
            FROM biosteel.team_member tm_player
            INNER JOIN biosteel.team t ON tm_player.team_id = t.team_id
            INNER JOIN biosteel.team_member tm_owner ON t.team_id = tm_owner.team_id
            WHERE tm_owner.user_id = :userId
                AND tm_owner.role IN ('OWNER', 'MANAGER', 'COACH', 'ROLE_MANAGER', 'ROLE_COACH')
                AND tm_player.role IN ('PLAYER', 'ROLE_PLAYER')
                AND t.deleted_at IS NULL
                AND tm_player.left_at IS NULL
                AND tm_owner.left_at IS NULL
                AND (:teamId IS NULL OR t.team_id = :teamId)
            """, nativeQuery = true)
    Integer countPlayersInUserTeams(@Param("userId") UUID userId, @Param("teamId") UUID teamId);

    /**
     * Get practice sessions for a specific period
     */
    @Query(value = """
            SELECT DISTINCT
                e.event_id,
                e.team_id,
                t.name as team_name,
                e.title,
                e.start_time,
                e.end_time,
                p.practice_id,
                p.instructions,
                COALESCE(l.place_name, l.address_line_1, e.location) as location
            FROM biosteel.event e
            INNER JOIN biosteel.team t ON e.team_id = t.team_id
            INNER JOIN biosteel.team_member tm ON t.team_id = tm.team_id
            INNER JOIN biosteel.practice p ON e.practice_id = p.practice_id
            LEFT JOIN biosteel.location l ON e.location_id = l.location_id
            WHERE tm.user_id = :userId
                AND e.start_time BETWEEN :startDateTime AND :endDateTime
                AND e.event_type = 'PRACTICE'
                AND e.deleted_at IS NULL
                AND t.deleted_at IS NULL
                AND p.deleted_at IS NULL
                AND tm.left_at IS NULL
                AND (:teamId IS NULL OR t.team_id = :teamId)
            ORDER BY e.start_time ASC
            """, nativeQuery = true)
    List<Object[]> findPracticeSessionsInPeriod(
            @Param("userId") UUID userId,
            @Param("teamId") UUID teamId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);

    /**
     * Get recent activity feed for dashboard
     */
    @Query(value = """
            SELECT
                'GAME_COMPLETED' as activity_type,
                g.game_id as entity_id,
                t.team_id,
                t.name as team_name,
                CONCAT('Game completed: ', COALESCE(ht.name, g.away_team_name), ' vs ',
                       COALESCE(at.name, g.away_team_name)) as description,
                e.start_time as activity_time
            FROM biosteel.game g
            INNER JOIN biosteel.event e ON g.game_id = e.game_id
            INNER JOIN biosteel.team t ON e.team_id = t.team_id
            INNER JOIN biosteel.team_member tm ON t.team_id = tm.team_id
            LEFT JOIN biosteel.team ht ON g.home_team_id = ht.team_id
            LEFT JOIN biosteel.team at ON g.away_team_id = at.team_id
            WHERE tm.user_id = :userId
                AND g.status IN ('COMPLETED', 'FINAL', 'FINISHED')
                AND e.start_time >= :sinceDateTime
                AND e.deleted_at IS NULL
                AND t.deleted_at IS NULL
                AND g.deleted_at IS NULL
                AND tm.left_at IS NULL
                AND (:teamId IS NULL OR t.team_id = :teamId)

            UNION ALL

            SELECT
                'EVENT_CREATED' as activity_type,
                e.event_id as entity_id,
                t.team_id,
                t.name as team_name,
                CONCAT('New event scheduled: ', e.title) as description,
                e.created_at as activity_time
            FROM biosteel.event e
            INNER JOIN biosteel.team t ON e.team_id = t.team_id
            INNER JOIN biosteel.team_member tm ON t.team_id = tm.team_id
            WHERE tm.user_id = :userId
                AND e.created_at >= :sinceDateTime
                AND e.deleted_at IS NULL
                AND t.deleted_at IS NULL
                AND tm.left_at IS NULL
                AND (:teamId IS NULL OR t.team_id = :teamId)

            ORDER BY activity_time DESC
            LIMIT 10
            """, nativeQuery = true)
    List<Object[]> findRecentActivity(
            @Param("userId") UUID userId,
            @Param("teamId") UUID teamId,
            @Param("sinceDateTime") LocalDateTime sinceDateTime);
}