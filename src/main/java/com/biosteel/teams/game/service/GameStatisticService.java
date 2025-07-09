package com.biosteel.teams.game.service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.biosteel.teams.common.exception.ResourceNotFoundException;
import com.biosteel.teams.game.dto.GameStatisticRequestDTO;
import com.biosteel.teams.game.dto.GameStatisticResponseDTO;
import com.biosteel.teams.game.mapper.GameStatisticMapper;
import com.biosteel.teams.game.model.Game;
import com.biosteel.teams.game.model.GameScore;
import com.biosteel.teams.game.model.GameStatistic;
import com.biosteel.teams.game.model.SportPeriodDefinition;
import com.biosteel.teams.game.repository.GameRepository;
import com.biosteel.teams.game.repository.GameScoreRepository;
import com.biosteel.teams.game.repository.GameStatisticRepository;
import com.biosteel.teams.game.repository.SportPeriodDefinitionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameStatisticService {
    private final GameRepository gameRepository;
    private final GameScoreRepository gameScoreRepository;
    private final GameStatisticRepository gameStatisticRepository;
    private final SportPeriodDefinitionRepository sportPeriodDefinitionRepository;
    private final GameStatisticMapper gameStatisticMapper;

    @Transactional
    public GameStatisticResponseDTO recordStatistic(UUID userId, UUID teamId, UUID gameId,
            GameStatisticRequestDTO statisticDTO) {
        gameRepository.findById(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found"));

        GameStatistic statistic = gameStatisticMapper.toEntity(statisticDTO);
        statistic.setGameId(gameId);
        statistic.setUserId(userId);
        statistic.setTeamId(teamId);
        statistic.setRecordedAt(ZonedDateTime.now());

        if (statisticDTO.getSportPeriodDefinitionId() != null) {
            SportPeriodDefinition periodDefinition = sportPeriodDefinitionRepository
                    .findBySportPeriodDefinitionIdAndDeletedAtIsNull(statisticDTO.getSportPeriodDefinitionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Period definition not found"));

            statistic.setSportPeriodDefinition(periodDefinition);
        }

        if (statisticDTO.getGameScoreId() != null) {
            GameScore gameScore = gameScoreRepository.findById(statisticDTO.getGameScoreId())
                    .orElseThrow(() -> new ResourceNotFoundException("Game score not found"));
            statistic.setGameScore(gameScore);
        }

        GameStatistic savedStatistic = gameStatisticRepository.save(statistic);
        return gameStatisticMapper.toDto(savedStatistic);
    }

    @Transactional
    public GameStatisticResponseDTO recordGameScheduled(UUID userId, UUID teamId, UUID gameId, String metadata) {
        GameStatisticRequestDTO statDTO = new GameStatisticRequestDTO();
        statDTO.setStatType("GAME_SCHEDULED");
        statDTO.setStatValue(1);
        statDTO.setMetadata(metadata);

        return recordStatistic(userId, teamId, gameId, statDTO);
    }

    @Transactional
    public GameStatisticResponseDTO recordGameStarted(UUID userId, UUID teamId, UUID gameId) {
        GameStatisticRequestDTO statDTO = new GameStatisticRequestDTO();
        statDTO.setStatType("GAME_STARTED");
        statDTO.setStatValue(1);

        return recordStatistic(userId, teamId, gameId, statDTO);
    }

    @Transactional
    public GameStatisticResponseDTO recordGameEnded(UUID userId, UUID teamId, UUID gameId) {
        GameStatisticRequestDTO statDTO = new GameStatisticRequestDTO();
        statDTO.setStatType("GAME_ENDED");
        statDTO.setStatValue(1);

        return recordStatistic(userId, teamId, gameId, statDTO);
    }

    @Transactional
    public GameStatisticResponseDTO recordPeriodStarted(UUID userId, UUID teamId, UUID gameId,
            UUID periodDefinitionId) {
        sportPeriodDefinitionRepository
                .findBySportPeriodDefinitionIdAndDeletedAtIsNull(periodDefinitionId)
                .orElseThrow(() -> new ResourceNotFoundException("Period definition not found"));

        GameStatisticRequestDTO statDTO = new GameStatisticRequestDTO();
        statDTO.setStatType("PERIOD_STARTED");
        statDTO.setStatValue(1);
        statDTO.setSportPeriodDefinitionId(periodDefinitionId);

        return recordStatistic(userId, teamId, gameId, statDTO);
    }

    @Transactional
    public GameStatisticResponseDTO recordPeriodEnded(UUID userId, UUID teamId, UUID gameId, UUID periodDefinitionId) {
        sportPeriodDefinitionRepository
                .findBySportPeriodDefinitionIdAndDeletedAtIsNull(periodDefinitionId)
                .orElseThrow(() -> new ResourceNotFoundException("Period definition not found"));

        GameStatisticRequestDTO statDTO = new GameStatisticRequestDTO();
        statDTO.setStatType("PERIOD_ENDED");
        statDTO.setStatValue(1);
        statDTO.setSportPeriodDefinitionId(periodDefinitionId);

        return recordStatistic(userId, teamId, gameId, statDTO);
    }

    @Transactional
    public GameStatisticResponseDTO recordScoreStatistic(
            UUID userId,
            UUID teamId,
            UUID gameId,
            UUID gameScoreId,
            String metadata) {

        gameScoreRepository.findById(gameScoreId)
                .orElseThrow(() -> new ResourceNotFoundException("Game score not found"));

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found"));

        Integer homeScoreTotal = calculateTeamScore(gameId, true);
        Integer awayScoreTotal = calculateTeamScore(gameId, false);

        game.setHomeScore(homeScoreTotal);
        game.setAwayScore(awayScoreTotal);
        gameRepository.save(game);

        GameStatisticRequestDTO statDTO = new GameStatisticRequestDTO();
        statDTO.setStatType("SCORE");
        statDTO.setStatValue(1);
        statDTO.setGameScoreId(gameScoreId);
        statDTO.setHomeScoreTotal(homeScoreTotal);
        statDTO.setAwayScoreTotal(awayScoreTotal);
        statDTO.setMetadata(metadata);

        return recordStatistic(userId, teamId, gameId, statDTO);
    }

    @Transactional
    public GameStatisticResponseDTO recordCustomStatistic(
            UUID userId,
            UUID teamId,
            UUID gameId,
            String statType,
            Integer statValue,
            String metadata) {

        GameStatisticRequestDTO statDTO = new GameStatisticRequestDTO();
        statDTO.setStatType(statType);
        statDTO.setStatValue(statValue);
        statDTO.setMetadata(metadata);

        return recordStatistic(userId, teamId, gameId, statDTO);
    }

    public Integer calculateTeamScore(UUID gameId, Boolean isHomeTeam) {
        List<GameScore> allGameScores = gameScoreRepository.findByGameId(gameId);

        int totalScore = 0;
        for (GameScore score : allGameScores) {
            if (isHomeTeam.equals(score.isHomeTeam())) {
                try {
                    if (score.getScoreValue() != null) {
                        totalScore += Integer.parseInt(score.getScoreValue());
                    } else if (score.getScoreType() != null) {
                        totalScore += score.getScoreType().getPoints();
                    }
                } catch (NumberFormatException e) {
                    log.warn("Could not parse score value for score {}: {}", score.getScoreId(), e.getMessage());
                }
            }
        }

        return totalScore;
    }

    @Transactional(readOnly = true)
    public List<GameStatisticResponseDTO> getGameStatistics(UUID gameId) {
        List<GameStatistic> statistics = gameStatisticRepository.findByGameIdOrderByRecordedAtDesc(gameId);
        return gameStatisticMapper.toDtoList(statistics);
    }

    @Transactional(readOnly = true)
    public List<GameStatisticResponseDTO> getGameStatisticsByType(UUID gameId, String statType) {
        List<GameStatistic> statistics = gameStatisticRepository.findByGameIdAndStatType(gameId, statType);
        return gameStatisticMapper.toDtoList(statistics);
    }

    @Transactional(readOnly = true)
    public List<GameStatisticResponseDTO> getGameStatisticsByPeriod(UUID gameId, UUID periodDefinitionId) {
        List<GameStatistic> statistics = gameStatisticRepository
                .findByGameIdAndSportPeriodDefinition_SportPeriodDefinitionId(
                        gameId, periodDefinitionId);
        return gameStatisticMapper.toDtoList(statistics);
    }
}