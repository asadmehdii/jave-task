package com.biosteel.teams.game.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.biosteel.teams.game.dto.GameScoreResponseDTO;
import com.biosteel.teams.game.dto.GameStatisticRequestDTO;
import com.biosteel.teams.game.dto.GameStatisticResponseDTO;
import com.biosteel.teams.game.dto.SportPeriodDefinitionDTO;
import com.biosteel.teams.game.model.GameStatistic;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GameStatisticMapper {

    private final PeriodMapper periodMapper;
    private final GameScoreMapper gameScoreMapper;

    public GameStatistic toEntity(GameStatisticRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        GameStatistic entity = new GameStatistic();
        entity.setStatId(java.util.UUID.randomUUID());
        entity.setStatType(dto.getStatType());
        entity.setStatValue(dto.getStatValue());
        entity.setTeamId(dto.getTeamId());
        entity.setPlayerId(dto.getPlayerId());
        entity.setHomeScoreTotal(dto.getHomeScoreTotal());
        entity.setAwayScoreTotal(dto.getAwayScoreTotal());
        entity.setMetadata(dto.getMetadata());

        // Note: The related entities (SportPeriodDefinition and GameScore)
        // should be set separately after retrieving them from repositories

        return entity;
    }

    public GameStatisticResponseDTO toDto(GameStatistic entity) {
        if (entity == null) {
            return null;
        }

        GameStatisticResponseDTO dto = new GameStatisticResponseDTO();
        dto.setStatId(entity.getStatId());
        dto.setGameId(entity.getGameId());
        dto.setTeamId(entity.getTeamId());
        dto.setPlayerId(entity.getPlayerId());
        dto.setUserId(entity.getUserId());
        dto.setStatType(entity.getStatType());
        dto.setStatValue(entity.getStatValue());
        dto.setHomeScoreTotal(entity.getHomeScoreTotal());
        dto.setAwayScoreTotal(entity.getAwayScoreTotal());
        dto.setMetadata(entity.getMetadata());
        dto.setRecordedAt(entity.getRecordedAt());
        dto.setCreatedAt(entity.getCreatedAt());

        if (entity.getSportPeriodDefinition() != null) {
            SportPeriodDefinitionDTO periodDto = periodMapper.toSportPeriodDefinitionDTO(
                    entity.getSportPeriodDefinition());
            dto.setSportPeriodDefinition(periodDto);
        }

        if (entity.getGameScore() != null) {
            GameScoreResponseDTO scoreDto = gameScoreMapper.toResponseDTO(entity.getGameScore());
            dto.setGameScore(scoreDto);
        }

        return dto;
    }

    public List<GameStatisticResponseDTO> toDtoList(List<GameStatistic> entities) {
        if (entities == null) {
            return java.util.Collections.emptyList();
        }

        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public GameStatistic updateEntityFromDto(GameStatisticRequestDTO dto, GameStatistic entity) {
        if (dto == null || entity == null) {
            return entity;
        }

        if (dto.getStatType() != null) {
            entity.setStatType(dto.getStatType());
        }

        if (dto.getStatValue() != null) {
            entity.setStatValue(dto.getStatValue());
        }

        if (dto.getTeamId() != null) {
            entity.setTeamId(dto.getTeamId());
        }

        if (dto.getPlayerId() != null) {
            entity.setPlayerId(dto.getPlayerId());
        }

        if (dto.getHomeScoreTotal() != null) {
            entity.setHomeScoreTotal(dto.getHomeScoreTotal());
        }

        if (dto.getAwayScoreTotal() != null) {
            entity.setAwayScoreTotal(dto.getAwayScoreTotal());
        }

        if (dto.getMetadata() != null) {
            entity.setMetadata(dto.getMetadata());
        }

        // Note: Related entities should be updated separately

        return entity;
    }
}