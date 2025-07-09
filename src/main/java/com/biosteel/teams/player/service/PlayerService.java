package com.biosteel.teams.player.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.biosteel.teams.common.exception.ResourceNotFoundException;
import com.biosteel.teams.media.repository.MediaRepository;
import com.biosteel.teams.player.dto.PlayerCreateDTO;
import com.biosteel.teams.player.dto.PlayerDTO;
import com.biosteel.teams.player.mapper.PlayerMapper;
import com.biosteel.teams.player.model.Player;
import com.biosteel.teams.player.repository.PlayerRepository;
import com.biosteel.teams.user.model.User;
import com.biosteel.teams.user.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final UserRepository userRepository;
    private final PlayerMapper playerMapper;
    private final MediaRepository mediaRepository;

    @Transactional
    public PlayerDTO createPlayer(UUID userId, PlayerCreateDTO playerCreateDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Player player = playerMapper.toEntity(playerCreateDTO);
        player.setParentUser(user);
        Player savedPlayer = playerRepository.save(player);

        return playerMapper.toDto(savedPlayer);
    }

    @Transactional
    public PlayerDTO getPlayerByIdAndUserId(UUID playerId, UUID userId) {
        Player player = playerRepository.findByPlayerIdAndParentUserUserIdAndDeletedAtIsNull(playerId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Player not found with id: %s for user: %s", playerId, userId)));

        return playerMapper.toDto(player);
    }

    @Transactional
    public PlayerDTO getPlayerById(UUID playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Player not found with id: %s for user: %s", playerId)));

        return playerMapper.toDto(player);
    }

    @Transactional
    public Page<PlayerDTO> getAllPlayersByUserId(UUID userId, Pageable pageable) {
        // Verify user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        Page<Player> players = playerRepository.findAllByParentUserUserIdAndDeletedAtIsNull(userId, pageable);
        return players.map(playerMapper::toDto);
    }

    @Transactional
    public Page<PlayerDTO> getAllPlayers(Pageable pageable, String query) {
        Page<Player> players;
        if (query != null && !query.trim().isEmpty()) {
            players = playerRepository
                    .findAllByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseAndDeletedAtIsNull(
                            query.trim(), query.trim(), pageable);
        } else {
            players = playerRepository.findAllByDeletedAtIsNull(pageable);
        }
        return players.map(playerMapper::toDto);
    }

    @Transactional
    public PlayerDTO updatePlayer(UUID userId, UUID playerId, PlayerCreateDTO playerUpdateDTO) {
        Player player = playerRepository.findByPlayerIdAndParentUserUserIdAndDeletedAtIsNull(playerId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Player not found with id: %s for user: %s", playerId, userId)));

        playerMapper.updateEntityFromDto(playerUpdateDTO, player);
        Player updatedPlayer = playerRepository.save(player);
        return playerMapper.toDto(updatedPlayer);
    }

    @Transactional
    public void deletePlayer(UUID userId, UUID playerId) {
        Player player = playerRepository.findByPlayerIdAndParentUserUserIdAndDeletedAtIsNull(playerId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Player not found with id: %s for user: %s", playerId, userId)));

        if (player.getLogoMediaId() != null) {
            mediaRepository.deleteById(player.getLogoMediaId());
        }

        playerRepository.delete(player);
    }
}