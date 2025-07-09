package com.biosteel.teams.notification.mapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.biosteel.teams.notification.dto.DeviceTokenDTO;
import com.biosteel.teams.notification.model.DeviceToken;

@Component
public class DeviceTokenMapper {

    public DeviceTokenDTO toDto(DeviceToken entity) {
        if (entity == null) {
            return null;
        }

        DeviceTokenDTO dto = new DeviceTokenDTO();
        dto.setDeviceTokenId(entity.getDeviceTokenId());
        dto.setUserId(entity.getUserId());
        dto.setToken(entity.getToken());
        dto.setDeviceType(entity.getDeviceType());
        dto.setAppVersion(entity.getAppVersion());
        dto.setIsActive(entity.getIsActive());

        return dto;
    }

    public DeviceToken toEntity(DeviceTokenDTO dto) {
        if (dto == null) {
            return null;
        }

        DeviceToken entity = new DeviceToken();

        // Generate a new ID if not provided
        if (dto.getDeviceTokenId() == null) {
            entity.setDeviceTokenId(UUID.randomUUID());
        } else {
            entity.setDeviceTokenId(dto.getDeviceTokenId());
        }

        entity.setUserId(dto.getUserId());
        entity.setToken(dto.getToken());
        entity.setDeviceType(dto.getDeviceType());
        entity.setAppVersion(dto.getAppVersion());
        entity.setIsActive(dto.getIsActive());
        // entity.setLastUsedDate(LocalDateTime.now());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        return entity;
    }

    public List<DeviceTokenDTO> toDtoList(List<DeviceToken> entities) {
        if (entities == null) {
            return null;
        }

        List<DeviceTokenDTO> dtoList = new ArrayList<>(entities.size());
        for (DeviceToken entity : entities) {
            dtoList.add(toDto(entity));
        }

        return dtoList;
    }

    public void updateEntityFromDto(DeviceTokenDTO dto, DeviceToken entity) {
        if (dto == null || entity == null) {
            return;
        }

        if (dto.getUserId() != null) {
            entity.setUserId(dto.getUserId());
        }

        if (dto.getToken() != null) {
            entity.setToken(dto.getToken());
        }

        if (dto.getDeviceType() != null) {
            entity.setDeviceType(dto.getDeviceType());
        }

        if (dto.getAppVersion() != null) {
            entity.setAppVersion(dto.getAppVersion());
        }

        if (dto.getIsActive() != null) {
            entity.setIsActive(dto.getIsActive());
        }

        entity.setUpdatedAt(LocalDateTime.now());
    }
}