package com.biosteel.teams.event.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.biosteel.teams.event.model.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {
        List<Event> findByTeamId(UUID teamId);

        Optional<Event> findByGame_GameId(UUID gameId);

        Optional<Event> findByPractice_PracticeId(UUID practiceId);

        List<Event> findByGame_GameIdIn(List<UUID> gameIds);

        @Query(value = """
                        SELECT * FROM biosteel.event e
                        WHERE e.user_id = :userId
                        AND (cast(:startDate as timestamp) IS NULL OR e.start_time >= cast(:startDate as timestamp))
                        AND (cast(:endDate as timestamp) IS NULL OR e.start_time <= cast(:endDate as timestamp))
                        AND e.deleted_at IS NULL
                        ORDER BY e.start_time
                        LIMIT :size OFFSET :offset
                        """, nativeQuery = true)
        List<Event> findUserEventsByDateRange(
                        @Param("userId") UUID userId,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        @Param("offset") int offset,
                        @Param("size") int size);

        @Query(value = """
                        SELECT * FROM biosteel.event e
                        WHERE e.user_id = :userId
                        AND e.event_type = :eventType
                        AND (cast(:startDate as timestamp) IS NULL OR e.start_time >= cast(:startDate as timestamp))
                        AND (cast(:endDate as timestamp) IS NULL OR e.start_time <= cast(:endDate as timestamp))
                        AND e.deleted_at IS NULL
                        ORDER BY e.start_time
                        LIMIT :size OFFSET :offset
                        """, nativeQuery = true)
        List<Event> findUserEventsByTypeAndDateRange(
                        @Param("userId") UUID userId,
                        @Param("eventType") String eventType,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        @Param("offset") int offset,
                        @Param("size") int size);

        @Query(value = """
                        SELECT COUNT(*) FROM biosteel.event e
                        WHERE e.user_id = :userId
                        AND (cast(:startDate as timestamp) IS NULL OR e.start_time >= cast(:startDate as timestamp))
                        AND (cast(:endDate as timestamp) IS NULL OR e.start_time <= cast(:endDate as timestamp))
                        AND e.deleted_at IS NULL
                        """, nativeQuery = true)
        long countUserEvents(
                        @Param("userId") UUID userId,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        @Query(value = """
                        SELECT COUNT(*) FROM biosteel.event e
                        WHERE e.user_id = :userId
                        AND e.event_type = :eventType
                        AND (cast(:startDate as timestamp) IS NULL OR e.start_time >= cast(:startDate as timestamp))
                        AND (cast(:endDate as timestamp) IS NULL OR e.start_time <= cast(:endDate as timestamp))
                        AND e.deleted_at IS NULL
                        """, nativeQuery = true)
        long countUserEventsByType(
                        @Param("userId") UUID userId,
                        @Param("eventType") String eventType,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        @Query(value = """
                        SELECT * FROM biosteel.event e
                        WHERE e.team_id = :teamId
                        AND e.event_type = :eventType
                        AND e.deleted_at IS NULL
                        ORDER BY e.start_time
                        """, nativeQuery = true)
        List<Event> findByTeamIdAndEventType(
                        @Param("teamId") UUID teamId,
                        @Param("eventType") String eventType);

        @Query(value = """
                        SELECT DISTINCT e.* FROM biosteel.event e
                        INNER JOIN biosteel.team t ON e.team_id = t.team_id
                        INNER JOIN biosteel.team_member tm ON t.team_id = tm.team_id
                        WHERE tm.user_id = :userId
                        AND tm.left_at IS NULL
                        AND (CAST(:startDate AS TIMESTAMP) IS NULL OR e.start_time >= CAST(:startDate AS TIMESTAMP))
                        AND (CAST(:endDate AS TIMESTAMP) IS NULL OR e.start_time <= CAST(:endDate AS TIMESTAMP))
                        AND e.deleted_at IS NULL
                        AND t.deleted_at IS NULL
                        ORDER BY e.start_time
                        LIMIT :size OFFSET :offset
                        """, nativeQuery = true)
        List<Event> findEventsForUserTeams(
                        @Param("userId") UUID userId,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        @Param("offset") int offset,
                        @Param("size") int size);

        @Query(value = """
                        SELECT COUNT(DISTINCT e.event_id) FROM biosteel.event e
                        INNER JOIN biosteel.team t ON e.team_id = t.team_id
                        INNER JOIN biosteel.team_member tm ON t.team_id = tm.team_id
                        WHERE tm.user_id = :userId
                        AND tm.left_at IS NULL
                        AND (CAST(:startDate AS TIMESTAMP) IS NULL OR e.start_time >= CAST(:startDate AS TIMESTAMP))
                        AND (CAST(:endDate AS TIMESTAMP) IS NULL OR e.start_time <= CAST(:endDate AS TIMESTAMP))
                        AND e.deleted_at IS NULL
                        AND t.deleted_at IS NULL
                        """, nativeQuery = true)
        long countEventsForUserTeams(
                        @Param("userId") UUID userId,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        @Query(value = """
                        SELECT DISTINCT e.* FROM biosteel.event e
                        INNER JOIN biosteel.team t ON e.team_id = t.team_id
                        INNER JOIN biosteel.team_member tm ON t.team_id = tm.team_id
                        WHERE tm.user_id = :userId
                        AND tm.left_at IS NULL
                        AND e.event_type = :eventType
                        AND (CAST(:startDate AS TIMESTAMP) IS NULL OR e.start_time >= CAST(:startDate AS TIMESTAMP))
                        AND (CAST(:endDate AS TIMESTAMP) IS NULL OR e.start_time <= CAST(:endDate AS TIMESTAMP))
                        AND e.deleted_at IS NULL
                        AND t.deleted_at IS NULL
                        ORDER BY e.start_time
                        LIMIT :size OFFSET :offset
                        """, nativeQuery = true)
        List<Event> findEventsForUserTeamsByType(
                        @Param("userId") UUID userId,
                        @Param("eventType") String eventType,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        @Param("offset") int offset,
                        @Param("size") int size);

        @Query(value = """
                        SELECT COUNT(*) FROM biosteel.event e
                        JOIN biosteel.team_member tm ON e.team_id = tm.team_id
                        WHERE tm.user_id = :userId
                        AND e.event_type = :eventType
                        AND (cast(:startDate as timestamp) IS NULL OR e.start_time >= cast(:startDate as timestamp))
                        AND (cast(:endDate as timestamp) IS NULL OR e.start_time <= cast(:endDate as timestamp))
                        AND e.deleted_at IS NULL
                        """, nativeQuery = true)
        long countEventsForUserTeamsByType(
                        @Param("userId") UUID userId,
                        @Param("eventType") String eventType,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);
}