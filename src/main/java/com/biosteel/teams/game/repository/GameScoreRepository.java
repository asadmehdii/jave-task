package com.biosteel.teams.game.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.biosteel.teams.game.model.GameScore;

@Repository
public interface GameScoreRepository extends JpaRepository<GameScore, UUID> {
    List<GameScore> findByGameId(UUID gameId);

    @Query(value = "SELECT COALESCE(SUM(st.points), 0) FROM biosteel.game_score s " +
            "JOIN biosteel.score_type st ON s.score_type_id = st.score_type_id " +
            "WHERE s.game_id = :gameId AND s.team_id = :teamId", nativeQuery = true)
    int calculateTeamScore(UUID gameId, UUID teamId);

    List<GameScore> findByGameIdAndTeamId(UUID gameId, UUID teamId);

    List<GameScore> findByGameIdAndPlayerId(UUID gameId, UUID playerId);
}