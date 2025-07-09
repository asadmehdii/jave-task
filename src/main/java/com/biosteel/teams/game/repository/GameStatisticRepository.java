package com.biosteel.teams.game.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.biosteel.teams.game.model.GameStatistic;

@Repository
public interface GameStatisticRepository extends JpaRepository<GameStatistic, UUID> {
        List<GameStatistic> findByGameIdOrderByRecordedAtDesc(UUID gameId);

        List<GameStatistic> findByGameIdAndTeamId(UUID gameId, UUID teamId);

        List<GameStatistic> findByGameIdAndPlayerId(UUID gameId, UUID playerId);

        @Query(value = "SELECT * FROM biosteel.game_statistic gs " +
                        "WHERE gs.game_id = :gameId AND gs.stat_type = :statType", nativeQuery = true)
        List<GameStatistic> findByGameIdAndStatType(UUID gameId, String statType);

        List<GameStatistic> findByGameIdAndSportPeriodDefinition_SportPeriodDefinitionId(
                        UUID gameId, UUID sportPeriodDefinitionId);
}