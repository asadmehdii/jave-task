package com.biosteel.teams.game.mapper;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.biosteel.teams.game.dto.GameScoreResponseDTO;
import com.biosteel.teams.game.model.GameScore;
import com.biosteel.teams.sport.mapper.ScoreTypeMapper;

@Component
public class GameScoreMapper {

    /**
     * Convert GameScore entity to response DTO
     * 
     * @param gameScore GameScore entity
     * @return GameScoreResponseDTO
     */
    public GameScoreResponseDTO toResponseDTO(GameScore gameScore) {
        if (gameScore == null) {
            return null;
        }

        GameScoreResponseDTO dto = new GameScoreResponseDTO();

        dto.setScoreId(gameScore.getScoreId());
        dto.setGameId(gameScore.getGameId());
        dto.setTeamId(gameScore.getTeamId());
        dto.setPlayerId(gameScore.getPlayerId());
        dto.setUserId(gameScore.getUserId());
        dto.setScoreValue(gameScore.getScoreValue());
        dto.setHomeTeam(gameScore.isHomeTeam());
        dto.setPlayerName(gameScore.getPlayerName());
        dto.setTeamName(gameScore.getTeamName());
        dto.setRecordedAt(gameScore.getRecordedAt());
        dto.setCreatedAt(gameScore.getCreatedAt());

        // Set score type name if available
        if (gameScore.getScoreType() != null) {
            ScoreTypeMapper scoreTypeMapper = new ScoreTypeMapper();
            dto.setScoreType(scoreTypeMapper.toDto(gameScore.getScoreType()));
        }

        // Set period information if available
        if (gameScore.getPeriodDefinition() != null) {
            PeriodMapper periodMapper = new PeriodMapper();
            dto.setPeriodDefinition(periodMapper.toSportPeriodDefinitionDTO(gameScore.getPeriodDefinition()));
        }

        return dto;
    }

    /**
     * Convert a list of GameScore entities to response DTOs
     * 
     * @param gameScores List of GameScore entities
     * @return List of GameScoreResponseDTO objects
     */
    public List<GameScoreResponseDTO> toResponseDTOList(List<GameScore> gameScores) {
        if (gameScores == null) {
            return null;
        }

        List<GameScoreResponseDTO> dtos = new ArrayList<>(gameScores.size());
        for (GameScore gameScore : gameScores) {
            dtos.add(toResponseDTO(gameScore));
        }

        return dtos;
    }
}