package com.biosteel.teams.game.service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.biosteel.teams.common.exception.ResourceNotFoundException;
import com.biosteel.teams.event.model.Event;
import com.biosteel.teams.event.model.Location;
import com.biosteel.teams.event.repository.EventRepository;
import com.biosteel.teams.event.repository.LocationRepository;
import com.biosteel.teams.event.service.EventNotificationService;
import com.biosteel.teams.game.dto.GameRequestDTO;
import com.biosteel.teams.game.dto.GameResponseDTO;
import com.biosteel.teams.game.dto.GameScoreRequestDTO;
import com.biosteel.teams.game.dto.GameScoreResponseDTO;
import com.biosteel.teams.game.dto.GameStatisticRequestDTO;
import com.biosteel.teams.game.dto.GameStatisticResponseDTO;
import com.biosteel.teams.game.mapper.GameMapper;
import com.biosteel.teams.game.mapper.PeriodMapper;
import com.biosteel.teams.game.model.Game;
import com.biosteel.teams.game.model.GameScore;
import com.biosteel.teams.game.model.SportPeriodDefinition;
import com.biosteel.teams.game.repository.GameRepository;
import com.biosteel.teams.game.repository.GameScoreRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameService {
        private final GameRepository gameRepository;
        private final GameScoreRepository gameScoreRepository;
        private final EventRepository eventRepository;
        private final LocationRepository locationRepository;
        private final PeriodService periodService;
        private final GameMapper gameMapper;
        private final GameStatisticService gameStatisticService;
        private final EventNotificationService eventNotificationService;

        @Transactional
        public GameResponseDTO createGame(UUID userId, UUID teamId, GameRequestDTO gameDTO) {
                // Create the game record
                Game game = gameMapper.toEntity(gameDTO);
                Game savedGame = gameRepository.save(game);

                // Handle location if provided
                Location location = null;
                if (gameDTO.getLocation() != null) {
                        location = gameMapper.toLocationEntity(gameDTO.getLocation());
                        if (location.getLocationId() == null) {
                                location.setLocationId(UUID.randomUUID());
                        }
                        location = locationRepository.save(location);
                }

                // Create associated event
                Event event = new Event();
                event.setEventId(UUID.randomUUID());
                event.setTeamId(teamId);
                event.setUserId(userId);
                event.setEventType("GAME");
                event.setEventStatus("SCHEDULED");
                event.setGame(savedGame);
                event.setArrivalTime(gameDTO.getArrivalTime());
                event.setTitle(gameDTO.getTitle());
                event.setDescription(gameDTO.getDescription());
                event.setStartTime(gameDTO.getStartTime());
                event.setEndTime(gameDTO.getEndTime());
                event.setCreatedAt(ZonedDateTime.now());
                event.setIsRecurring(false);
                if (location != null) {
                        event.setLocation(location);
                        event.setLocationId(location.getLocationId());
                }

                Event savedEvent = eventRepository.save(event);
                String metadata = String.format("Scheduled at %s",
                                savedEvent.getStartTime());
                gameStatisticService.recordGameScheduled(userId, teamId, savedGame.getGameId(), metadata);

                eventNotificationService.sendNewEventNotification(userId, teamId, savedEvent);

                return gameMapper.toDto(savedGame, savedEvent);
        }

        @Transactional
        public GameResponseDTO startGame(UUID userId, UUID teamId, UUID gameId) {
                Game game = gameRepository.findById(gameId)
                                .orElseThrow(() -> new ResourceNotFoundException("Game not found"));

                // Update game status
                game.setCurrentPeriod(1);
                Game savedGame = gameRepository.save(game);

                // Update event status
                Event event = eventRepository.findByGame_GameId(gameId)
                                .orElseThrow(() -> new ResourceNotFoundException("Associated event not found"));
                event.setEventStatus("IN_PROGRESS");
                event.setUpdatedAt(ZonedDateTime.now());
                eventRepository.save(event);

                gameStatisticService.recordGameStarted(userId, teamId, gameId);
                savedGame = periodService.initializeGamePeriods(userId, savedGame.getGameId());

                return gameMapper.toDto(savedGame, event);
        }

        @Transactional
        public GameResponseDTO endGame(UUID userId, UUID teamId, UUID gameId) {
                Game game = gameRepository.findById(gameId)
                                .orElseThrow(() -> new ResourceNotFoundException("Game not found"));

                // Update event status
                Event event = eventRepository.findByGame_GameId(gameId)
                                .orElseThrow(() -> new ResourceNotFoundException("Associated event not found"));
                event.setEventStatus("COMPLETED");
                event.setUpdatedAt(ZonedDateTime.now());
                eventRepository.save(event);

                gameStatisticService.recordGameEnded(userId, teamId, gameId);

                return gameMapper.toDto(game, event);
        }

        @Transactional(readOnly = true)
        public List<GameResponseDTO> getTeamGames(UUID userId, UUID teamId) {
                List<Game> games = gameRepository.findByHomeTeamIdOrAwayTeamId(teamId, teamId);

                // Get the associated events for these games
                List<UUID> gameIds = games.stream()
                                .map(Game::getGameId)
                                .collect(Collectors.toList());

                List<Event> events = eventRepository.findByGame_GameIdIn(gameIds);

                return gameMapper.toDtoList(games, events);
        }

        @Transactional(readOnly = true)
        public GameResponseDTO getGame(UUID userId, UUID teamId, UUID gameId) {
                Game game = gameRepository.findById(gameId)
                                .orElseThrow(() -> new ResourceNotFoundException("Game not found"));
                Event event = eventRepository.findByGame_GameId(gameId)
                                .orElseThrow(() -> new ResourceNotFoundException("Associated event not found"));
                return gameMapper.toDto(game, event);
        }

        @Transactional
        public GameResponseDTO updateGame(UUID userId, UUID teamId, UUID gameId, GameRequestDTO gameDTO) {
                Game game = gameRepository.findById(gameId)
                                .orElseThrow(() -> new ResourceNotFoundException("Game not found"));

                // Update game details
                gameMapper.updateEntityFromDto(gameDTO, game);
                game = gameRepository.save(game);

                // Update associated event
                Event event = eventRepository.findByGame_GameId(gameId)
                                .orElseThrow(() -> new ResourceNotFoundException("Associated event not found"));

                // Handle location update if provided
                if (gameDTO.getLocation() != null) {
                        Location location = event.getLocation();
                        if (location == null) {
                                location = new Location();
                                location.setLocationId(UUID.randomUUID());
                        }
                        Location updatedLocation = gameMapper.toLocationEntity(gameDTO.getLocation());
                        updatedLocation.setLocationId(location.getLocationId());
                        location = locationRepository.save(updatedLocation);

                        event.setLocation(location);
                        event.setLocationId(location.getLocationId());
                }
                event.setTitle(gameDTO.getTitle());
                event.setDescription(gameDTO.getDescription());
                event.setStartTime(gameDTO.getStartTime());
                event.setEndTime(gameDTO.getEndTime());
                event.setArrivalTime(gameDTO.getArrivalTime());
                event.setUpdatedAt(ZonedDateTime.now());
                eventRepository.save(event);

                GameResponseDTO gameResponseDTO = gameMapper.toDto(game, event);

                try {
                        SportPeriodDefinition currentPeriod = periodService.getCurrentPeriodDefinition(gameId);
                        if (currentPeriod != null) {
                                PeriodMapper periodMapper = new PeriodMapper();
                                gameResponseDTO.setCurrentPeriod(
                                                periodMapper.toSportPeriodDefinitionDTO(currentPeriod));
                        }
                } catch (Exception e) {
                        log.warn("Could not get current period for game {}: {}", gameId, e.getMessage());
                }

                return gameResponseDTO;
        }

        @Transactional
        public GameScoreResponseDTO recordScore(UUID userId, UUID teamId, UUID gameId, GameScoreRequestDTO scoreDTO) {
                gameRepository.findById(gameId)
                                .orElseThrow(() -> new ResourceNotFoundException("Game not found"));

                eventRepository.findByGame_GameId(gameId)
                                .orElseThrow(() -> new ResourceNotFoundException("Associated event not found"));

                // FIXME: Uncomment this block after implementing game status management
                // if (!"IN_PROGRESS".equals(event.getEventStatus())) {
                // throw new IllegalStateException("Cannot record score - game is not in
                // progress");
                // }
                SportPeriodDefinition currentPeriod = periodService.getCurrentPeriodDefinition(gameId);

                GameScore score = gameMapper.toEntity(scoreDTO);
                if (currentPeriod != null) {
                        score.setPeriodDefinitionId(currentPeriod.getSportPeriodDefinitionId());
                }
                score.setGameId(gameId);
                score.setUserId(userId);
                GameScore savedScore = gameScoreRepository.save(score);

                // Update game total score
                updateGameScore(gameId);

                // Record a SCORE statistic
                String metadata = String.format("Score by %s, Type: %s",
                                score.getPlayerName() != null ? score.getPlayerName() : "Unknown",
                                score.getScoreType() != null ? score.getScoreType().getName() : "Unknown");
                gameStatisticService.recordScoreStatistic(userId, teamId, gameId, savedScore.getScoreId(), metadata);

                return gameMapper.toDto(savedScore);
        }

        private void updateGameScore(UUID gameId) {
                Game game = gameRepository.findById(gameId)
                                .orElseThrow(() -> new ResourceNotFoundException("Game not found"));

                // Calculate totals from GameScore records
                int homeScore = gameScoreRepository.calculateTeamScore(gameId, game.getHomeTeamId());
                int awayScore = gameScoreRepository.calculateTeamScore(gameId, game.getAwayTeamId());

                game.setHomeScore(homeScore);
                game.setAwayScore(awayScore);
                gameRepository.save(game);
        }

        @Transactional(readOnly = true)
        public List<GameScoreResponseDTO> getGameScores(UUID userId, UUID teamId, UUID gameId) {
                List<GameScore> scores = gameScoreRepository.findByGameId(gameId);
                return gameMapper.toScoreDtoList(scores);
        }

        @Transactional
        public GameStatisticResponseDTO recordStatistic(UUID userId, UUID teamId, UUID gameId,
                        GameStatisticRequestDTO statisticDTO) {
                return gameStatisticService.recordStatistic(userId, teamId, gameId, statisticDTO);
        }

        @Transactional(readOnly = true)
        public List<GameStatisticResponseDTO> getGameStatistics(UUID userId, UUID teamId, UUID gameId) {
                return gameStatisticService.getGameStatistics(gameId);
        }

        @Transactional
        public void cancelGame(UUID userId, UUID teamId, UUID gameId) {
                Game game = gameRepository.findById(gameId)
                                .orElseThrow(() -> new ResourceNotFoundException("Game not found"));

                // Update event status
                Event event = eventRepository.findByGame_GameId(gameId)
                                .orElseThrow(() -> new ResourceNotFoundException("Associated event not found"));
                event.setEventStatus("CANCELLED");
                event.setUpdatedAt(ZonedDateTime.now());
                event.setDeletedAt(ZonedDateTime.now());
                eventRepository.save(event);

                // Soft delete game
                game.setDeletedAt(ZonedDateTime.now());
                gameRepository.save(game);
        }

        @Transactional
        public void updateGameStatus(UUID userId, UUID teamId, UUID gameId, String status) {
                Game game = gameRepository.findById(gameId)
                                .orElseThrow(() -> new ResourceNotFoundException("Game not found"));
                game.setStatus(status);
                Game savedGame = gameRepository.save(game);

                // If the game is starting, make sure periods are initialized
                if ("STARTING".equals(status)
                                || "IN_PROGRESS".equals(status)) {
                        if (savedGame.getCurrentPeriodDefinitionId() == null) {
                                savedGame = periodService.initializeGamePeriods(userId, savedGame.getGameId());
                        }
                }
        }

}