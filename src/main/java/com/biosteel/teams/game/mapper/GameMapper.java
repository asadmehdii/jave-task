package com.biosteel.teams.game.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.biosteel.teams.event.dto.LocationDTO;
import com.biosteel.teams.event.model.Event;
import com.biosteel.teams.event.model.Location;
import com.biosteel.teams.game.dto.GameRequestDTO;
import com.biosteel.teams.game.dto.GameResponseDTO;
import com.biosteel.teams.game.dto.GameScoreRequestDTO;
import com.biosteel.teams.game.dto.GameScoreResponseDTO;
import com.biosteel.teams.game.dto.GameStatisticRequestDTO;
import com.biosteel.teams.game.dto.GameStatisticResponseDTO;
import com.biosteel.teams.game.model.Game;
import com.biosteel.teams.game.model.GameScore;
import com.biosteel.teams.game.model.GameStatistic;
import com.biosteel.teams.sport.mapper.ScoreTypeMapper;

@Component
public class GameMapper {

    public Game toEntity(GameRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Game game = new Game();
        game.setHomeTeamId(dto.getHomeTeamId());
        game.setIsHomeGame(dto.getIsHomeGame());
        game.setAwayTeamId(dto.getAwayTeamId());
        game.setAwayTeamName(dto.getAwayTeamName());
        game.setHomeScore(0);
        game.setAwayScore(0);
        game.setCurrentPeriod(1);
        game.setStatus(dto.getStatus());
        return game;
    }

    public GameResponseDTO toDto(Game entity, Event event) {
        if (entity == null) {
            return null;
        }

        GameResponseDTO dto = new GameResponseDTO();
        dto.setGameId(entity.getGameId());
        dto.setHomeTeamId(entity.getHomeTeamId());
        dto.setIsHomeGame(entity.getIsHomeGame());
        dto.setAwayTeamId(entity.getAwayTeamId());
        dto.setAwayTeamName(entity.getAwayTeamName());
        dto.setHomeScore(entity.getHomeScore());
        dto.setAwayScore(entity.getAwayScore());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setStatus(entity.getStatus());

        if (event != null) {
            dto.setTitle(event.getTitle());
            dto.setDescription(event.getDescription());
            dto.setStartTime(event.getStartTime());
            dto.setEndTime(event.getEndTime());
            dto.setEventStatus(event.getEventStatus());
            dto.setArrivalTime(event.getArrivalTime());

            // Map Location
            if (event.getLocation() != null) {
                dto.setLocation(toLocationDto(event.getLocation()));
            }
        }
        if (entity.getCurrentPeriodDefinition() != null) {
            PeriodMapper periodMapper = new PeriodMapper();
            dto.setCurrentPeriod(periodMapper.toSportPeriodDefinitionDTO((entity.getCurrentPeriodDefinition())));
        }
        return dto;
    }

    public LocationDTO toLocationDto(Location location) {
        if (location == null) {
            return null;
        }

        LocationDTO dto = new LocationDTO();
        dto.setLocationId(location.getLocationId());
        dto.setAddressLine1(location.getAddressLine1());
        dto.setAddressLine2(location.getAddressLine2());
        dto.setMajorIntersection(location.getMajorIntersection());
        dto.setPostalZip(location.getPostalZip());
        dto.setCity(location.getCity());
        dto.setProvince(location.getProvince());
        dto.setCountry(location.getCountry());
        dto.setPlaceName(location.getPlaceName());
        dto.setPlaceGuid(location.getPlaceGuid());
        dto.setLongitude(location.getLongitude());
        dto.setLatitude(location.getLatitude());
        return dto;
    }

    public Location toLocationEntity(LocationDTO dto) {
        if (dto == null) {
            return null;
        }

        Location entity = new Location();
        entity.setLocationId(dto.getLocationId());
        entity.setAddressLine1(dto.getAddressLine1());
        entity.setAddressLine2(dto.getAddressLine2());
        entity.setMajorIntersection(dto.getMajorIntersection());
        entity.setPostalZip(dto.getPostalZip());
        entity.setCity(dto.getCity());
        entity.setProvince(dto.getProvince());
        entity.setCountry(dto.getCountry());
        entity.setPlaceName(dto.getPlaceName());
        entity.setPlaceGuid(dto.getPlaceGuid());
        entity.setLongitude(dto.getLongitude());
        entity.setLatitude(dto.getLatitude());
        return entity;
    }

    public List<GameResponseDTO> toDtoList(List<Game> entities, List<Event> events) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(game -> {
                    Event matchingEvent = events.stream()
                            .filter(event -> event.getGameId().equals(game.getGameId()))
                            .findFirst()
                            .orElse(null);
                    return toDto(game, matchingEvent);
                })
                .collect(Collectors.toList());
    }

    public void updateEntityFromDto(GameRequestDTO dto, Game entity) {
        if (dto == null || entity == null) {
            return;
        }

        entity.setHomeTeamId(dto.getHomeTeamId());
        entity.setIsHomeGame(dto.getIsHomeGame());
        entity.setAwayTeamId(dto.getAwayTeamId());
        entity.setAwayTeamName(dto.getAwayTeamName());
        entity.setHomeScore(dto.getHomeScore());
        entity.setAwayScore(dto.getAwayScore());
    }

    public GameScore toEntity(GameScoreRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        GameScore score = new GameScore();
        score.setTeamId(dto.getTeamId());
        score.setScoreTypeId(dto.getScoreTypeId());
        score.setScoreValue(dto.getScoreValue());
        score.setPlayerId(dto.getPlayerId());
        score.setPlayerName(dto.getPlayerName());
        score.setTeamName(dto.getTeamName());
        score.setHomeTeam(dto.isHomeTeam());
        return score;
    }

    public GameScoreResponseDTO toDto(GameScore entity) {
        if (entity == null) {
            return null;
        }

        GameScoreResponseDTO dto = new GameScoreResponseDTO();
        dto.setScoreId(entity.getScoreId());
        dto.setGameId(entity.getGameId());
        dto.setTeamId(entity.getTeamId());
        dto.setPlayerId(entity.getPlayerId());
        dto.setUserId(entity.getUserId());
        if (entity.getScoreType() != null) {
            ScoreTypeMapper scoreTypeMapper = new ScoreTypeMapper();
            dto.setScoreType(scoreTypeMapper.toDto(entity.getScoreType()));
        }

        dto.setScoreValue(entity.getScoreValue());
        dto.setRecordedAt(entity.getRecordedAt());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setPlayerName(entity.getPlayerName());
        dto.setTeamName(entity.getTeamName());
        dto.setHomeTeam(entity.isHomeTeam());

        if (entity.getPeriodDefinition() != null) {
            PeriodMapper periodMapper = new PeriodMapper();
            dto.setPeriodDefinition(periodMapper.toSportPeriodDefinitionDTO(entity.getPeriodDefinition()));
        }
        return dto;
    }

    public List<GameScoreResponseDTO> toScoreDtoList(List<GameScore> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public GameStatistic toEntity(GameStatisticRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        GameStatistic statistic = new GameStatistic();
        statistic.setTeamId(dto.getTeamId());
        statistic.setPlayerId(dto.getPlayerId());
        statistic.setStatType(dto.getStatType());
        statistic.setStatValue(dto.getStatValue());
        return statistic;
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
        dto.setRecordedAt(entity.getRecordedAt());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    public List<GameStatisticResponseDTO> toStatisticDtoList(List<GameStatistic> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}