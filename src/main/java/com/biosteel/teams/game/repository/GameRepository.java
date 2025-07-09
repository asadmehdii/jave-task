package com.biosteel.teams.game.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.biosteel.teams.game.model.Game;

@Repository
public interface GameRepository extends JpaRepository<Game, UUID> {
    List<Game> findByHomeTeamIdOrAwayTeamId(UUID homeTeamId, UUID awayTeamId);

    @Query(value = "SELECT * FROM biosteel.game g WHERE g.deleted_at IS NULL " +
            "AND (g.home_team_id = :teamId OR g.away_team_id = :teamId)", nativeQuery = true)
    List<Game> findActiveGamesByTeamId(UUID teamId);
}