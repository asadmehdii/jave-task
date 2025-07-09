package com.biosteel.teams.game.service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.biosteel.teams.game.dto.GamePeriodUpdateDTO;
import com.biosteel.teams.game.model.Game;
import com.biosteel.teams.game.model.PeriodType;
import com.biosteel.teams.game.model.SportPeriodDefinition;
import com.biosteel.teams.game.repository.GameRepository;
import com.biosteel.teams.game.repository.PeriodTypeRepository;
import com.biosteel.teams.game.repository.SportPeriodDefinitionRepository;
import com.biosteel.teams.team.model.Team;
import com.biosteel.teams.team.repository.TeamRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PeriodService {

        private final SportPeriodDefinitionRepository sportPeriodDefinitionRepository;
        private final PeriodTypeRepository periodTypeRepository;
        private final GameRepository gameRepository;
        private final TeamRepository teamRepository;
        private final GameStatisticService gameStatisticService;

        /**
         * Get all period types
         * 
         * @return List of all period types
         */
        @Transactional(readOnly = true)
        public List<PeriodType> getAllPeriodTypes() {
                return periodTypeRepository.findAllByDeletedAtIsNull();
        }

        /**
         * Get all period definitions for a sport
         * 
         * @param sportTypeCode The sport type code
         * @return List of period definitions
         */
        @Transactional(readOnly = true)
        public List<SportPeriodDefinition> getPeriodDefinitionsForSport(String sportTypeCode) {
                return sportPeriodDefinitionRepository
                                .findBySportTypeCodeAndDeletedAtIsNullOrderByDisplayOrder(sportTypeCode);
        }

        /**
         * Get a period definition by ID
         * 
         * @param sportPeriodDefinitionId The period definition ID
         * @return Optional containing the period definition if found
         */
        @Transactional(readOnly = true)
        public Optional<SportPeriodDefinition> getPeriodDefinitionById(UUID sportPeriodDefinitionId) {
                return sportPeriodDefinitionRepository
                                .findBySportPeriodDefinitionIdAndDeletedAtIsNull(sportPeriodDefinitionId);
        }

        /**
         * Initialize a game's period information
         * 
         * @param gameId The game ID
         * @return The updated game
         */
        @Transactional
        public Game initializeGamePeriods(UUID userId, UUID gameId) {
                Game game = gameRepository.findById(gameId)
                                .orElseThrow(() -> new IllegalArgumentException("Game not found with ID: " + gameId));

                if (game.getCurrentPeriodDefinitionId() != null) {
                        // Already initialized
                        return game;
                }

                Team homeTeam = teamRepository.findById(game.getHomeTeamId())
                                .orElseThrow(
                                                () -> new IllegalArgumentException("Home team not found with ID: "
                                                                + game.getHomeTeamId()));

                String sportTypeCode = homeTeam.getSportType() != null ? homeTeam.getSportType().getCode() : "";
                if (sportTypeCode == null) {
                        throw new IllegalStateException("Sport type code not found for team: " + homeTeam.getName());
                }

                // Get the first period definition for this sport
                SportPeriodDefinition firstPeriod = sportPeriodDefinitionRepository
                                .findFirstBySportTypeCodeAndDeletedAtIsNullOrderByPeriodNumber(sportTypeCode)
                                .orElseThrow(() -> new IllegalStateException(
                                                "No period definitions found for sport type: " + sportTypeCode));

                game.setCurrentPeriodDefinitionId(firstPeriod.getSportPeriodDefinitionId());
                game.setUpdatedAt(ZonedDateTime.now());
                game.setCurrentPeriodDefinition(firstPeriod);
                Game savedGame = gameRepository.save(game);

                gameStatisticService.recordPeriodStarted(userId, game.getHomeTeamId(), gameId,
                                savedGame.getCurrentPeriodDefinitionId());
                return savedGame;
        }

        /**
         * Update a game's current period
         * 
         * @param gameId          The game ID
         * @param periodUpdateDTO The period update data
         * @return The updated game
         */
        @Transactional
        public Game updateGamePeriod(UUID userId, UUID gameId, GamePeriodUpdateDTO periodUpdateDTO) {
                Game game = gameRepository.findById(gameId)
                                .orElseThrow(() -> new IllegalArgumentException("Game not found with ID: " + gameId));

                UUID previousPeriodId = game.getCurrentPeriodDefinitionId();

                SportPeriodDefinition periodDefinition = sportPeriodDefinitionRepository
                                .findById(periodUpdateDTO.getPeriodDefinitionId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Period definition not found with ID: " +
                                                                periodUpdateDTO.getPeriodDefinitionId()));

                game.setCurrentPeriodDefinitionId(periodDefinition.getSportPeriodDefinitionId());
                game.setUpdatedAt(ZonedDateTime.now());

                Game savedGame = gameRepository.save(game);

                // If we're changing periods, record appropriate period ended/started statistics
                if (previousPeriodId != null && !previousPeriodId.equals(periodUpdateDTO.getPeriodDefinitionId())) {
                        // End the previous period
                        gameStatisticService.recordPeriodEnded(userId, game.getHomeTeamId(), gameId, previousPeriodId);

                        // Start the new period
                        gameStatisticService.recordPeriodStarted(userId, game.getHomeTeamId(), gameId,
                                        periodUpdateDTO.getPeriodDefinitionId());
                }

                return savedGame;
        }

        /**
         * Advance to the next period of the game
         * 
         * @param gameId The game ID
         * @return The updated game
         */
        @Transactional
        public Game advanceToNextPeriod(UUID userId, UUID teamId, UUID gameId) {
                Game game = gameRepository.findById(gameId)
                                .orElseThrow(() -> new IllegalArgumentException("Game not found with ID: " + gameId));

                // Use home team ID from the game itself to ensure it exists in the database
                UUID homeTeamId = game.getHomeTeamId();

                if (game.getCurrentPeriodDefinitionId() == null) {
                        Game initializedGame = initializeGamePeriods(userId, gameId);
                        gameStatisticService.recordPeriodStarted(userId, homeTeamId, gameId,
                                        initializedGame.getCurrentPeriodDefinitionId());
                        return initializedGame;
                }

                // Store the current period ID before advancing to record the end statistic
                UUID currentPeriodId = game.getCurrentPeriodDefinitionId();

                SportPeriodDefinition currentPeriod = sportPeriodDefinitionRepository
                                .findById(game.getCurrentPeriodDefinitionId())
                                .orElseThrow(() -> new IllegalStateException(
                                                "Current period definition not found with ID: " +
                                                                game.getCurrentPeriodDefinitionId()));

                Team homeTeam = teamRepository.findById(homeTeamId)
                                .orElseThrow(
                                                () -> new IllegalArgumentException("Home team not found with ID: "
                                                                + homeTeamId));

                String sportTypeCode = homeTeam.getSportType() != null ? homeTeam.getSportType().getCode() : "";

                // Get the next period definition
                Optional<SportPeriodDefinition> nextPeriodOpt = sportPeriodDefinitionRepository
                                .findFirstBySportTypeCodeAndPeriodNumberGreaterThanAndDeletedAtIsNullOrderByPeriodNumber(
                                                sportTypeCode, currentPeriod.getPeriodNumber());

                // First, record that the current period has ended
                gameStatisticService.recordPeriodEnded(userId, homeTeamId, gameId, currentPeriodId);

                if (nextPeriodOpt.isPresent()) {
                        SportPeriodDefinition nextPeriod = nextPeriodOpt.get();
                        game.setCurrentPeriodDefinitionId(nextPeriod.getSportPeriodDefinitionId());
                        game.setUpdatedAt(ZonedDateTime.now());
                        Game savedGame = gameRepository.save(game);

                        // Record that the next period has started
                        gameStatisticService.recordPeriodStarted(userId, homeTeamId, gameId,
                                        nextPeriod.getSportPeriodDefinitionId());

                        return savedGame;
                } else {
                        // No more periods available, game should be completed
                        game.setStatus("COMPLETED");
                        game.setUpdatedAt(ZonedDateTime.now());
                        Game savedGame = gameRepository.save(game);

                        // Record game ended statistic since we've reached the end of all periods
                        gameStatisticService.recordGameEnded(userId, homeTeamId, gameId);

                        return savedGame;
                }
        }

        /**
         * Get the current period definition for a game
         * 
         * @param gameId The game ID
         * @return The current period definition
         */
        @Transactional(readOnly = true)
        public SportPeriodDefinition getCurrentPeriodDefinition(UUID gameId) {
                Game game = gameRepository.findById(gameId)
                                .orElseThrow(() -> new IllegalArgumentException("Game not found with ID: " + gameId));

                if (game.getCurrentPeriodDefinitionId() == null) {
                        Team homeTeam = teamRepository.findById(game.getHomeTeamId())
                                        .orElseThrow(
                                                        () -> new IllegalArgumentException(
                                                                        "Home team not found with ID: "
                                                                                        + game.getHomeTeamId()));

                        String sportTypeCode = homeTeam.getSportType() != null ? homeTeam.getSportType().getCode() : "";

                        return sportPeriodDefinitionRepository
                                        .findFirstBySportTypeCodeAndDeletedAtIsNullOrderByPeriodNumber(sportTypeCode)
                                        .orElseThrow(() -> new IllegalStateException(
                                                        "No period definitions found for sport type: "
                                                                        + sportTypeCode));
                }

                return sportPeriodDefinitionRepository
                                .findById(game.getCurrentPeriodDefinitionId())
                                .orElseThrow(() -> new IllegalStateException(
                                                "Current period definition not found with ID: " +
                                                                game.getCurrentPeriodDefinitionId()));
        }

}