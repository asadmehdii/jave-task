package com.biosteel.teams.practice.repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.biosteel.teams.practice.model.Practice;

@Repository
public interface PracticeRepository extends JpaRepository<Practice, UUID> {

        @Query(value = "SELECT p.* FROM biosteel.practice p " +
                        "JOIN biosteel.event e ON e.practice_id = p.practice_id " +
                        "WHERE e.team_id = :teamId AND p.deleted_at IS NULL", nativeQuery = true)
        List<Practice> findByTeamId(UUID teamId);

        @Query(value = "SELECT p.* FROM biosteel.practice p " +
                        "JOIN biosteel.event e ON e.practice_id = p.practice_id " +
                        "WHERE e.team_id = :teamId " +
                        "AND e.start_time >= :startTime AND e.start_time <= :endTime " +
                        "AND p.deleted_at IS NULL", nativeQuery = true)
        List<Practice> findByTeamIdAndDateRange(UUID teamId, ZonedDateTime startTime, ZonedDateTime endTime);
}