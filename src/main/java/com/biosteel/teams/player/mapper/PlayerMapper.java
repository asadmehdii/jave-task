package com.biosteel.teams.player.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.biosteel.teams.player.dto.PlayerCreateDTO;
import com.biosteel.teams.player.dto.PlayerDTO;
import com.biosteel.teams.player.model.Player;

import lombok.RequiredArgsConstructor;

// CustomModelMapper.java
@Component
@RequiredArgsConstructor
public class PlayerMapper {

    public Player toEntity(PlayerCreateDTO dto) {
        if (dto == null)
            return null;

        return Player.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .dateOfBirth(dto.getDateOfBirth())
                .logoMediaId(dto.getLogoMediaId())
                .gender(dto.getGender())
                .jerseyNumber(dto.getJerseyNumber())
                .heightCm(dto.getHeightCm())
                .weightKg(dto.getWeightKg())
                .primaryPosition(dto.getPrimaryPosition())
                .secondaryPosition(dto.getSecondaryPosition())
                .medicalNotes(dto.getMedicalNotes())
                .build();
    }

    public void updateEntityFromDto(PlayerCreateDTO dto, Player player) {
        if (dto == null || player == null)
            return;

        player.setFirstName(dto.getFirstName());
        player.setLastName(dto.getLastName());
        player.setDateOfBirth(dto.getDateOfBirth());
        player.setGender(dto.getGender());
        player.setJerseyNumber(dto.getJerseyNumber());
        player.setHeightCm(dto.getHeightCm());
        player.setWeightKg(dto.getWeightKg());
        player.setPrimaryPosition(dto.getPrimaryPosition());
        player.setSecondaryPosition(dto.getSecondaryPosition());
        player.setMedicalNotes(dto.getMedicalNotes());
        player.setLogoMediaId(dto.getLogoMediaId());

    }

    public PlayerDTO toDto(Player entity) {
        if (entity == null)
            return null;

        return PlayerDTO.builder()
                .playerId(entity.getPlayerId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .dateOfBirth(entity.getDateOfBirth())
                .gender(entity.getGender())
                .jerseyNumber(entity.getJerseyNumber())
                .heightCm(entity.getHeightCm())
                .weightKg(entity.getWeightKg())
                .primaryPosition(entity.getPrimaryPosition())
                .secondaryPosition(entity.getSecondaryPosition())
                .medicalNotes(entity.getMedicalNotes())
                .logoMediaId(entity.getLogoMediaId())
                .build();
    }

    public List<PlayerDTO> toDtoList(List<Player> entities) {
        if (entities == null)
            return Collections.emptyList();
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}