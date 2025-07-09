package com.biosteel.teams.advertisement.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.biosteel.teams.advertisement.dto.AdvertisementDTO;
import com.biosteel.teams.advertisement.model.Advertisement;
import com.biosteel.teams.media.service.MediaService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AdvertisementMapper {

    private final MediaService mediaService;

    public AdvertisementDTO toDTO(Advertisement advertisement) {
        if (advertisement == null) {
            return null;
        }

        AdvertisementDTO dto = new AdvertisementDTO();
        dto.setAdId(advertisement.getAdId());
        dto.setTitle(advertisement.getTitle());
        dto.setDescription(advertisement.getDescription());
        dto.setTargetUrl(advertisement.getTargetUrl());
        dto.setAnalyticEvent(advertisement.getAnalyticEvent());
        dto.setType(advertisement.getType());
        dto.setSettingsJson(advertisement.getSettingsJson());
        dto.setPlacement(advertisement.getPlacement());
        dto.setIsActive(advertisement.getIsActive());
        dto.setStartDate(advertisement.getStartDate());
        dto.setEndDate(advertisement.getEndDate());

        // Handle image URL - prefer external URL, fallback to media service
        if (advertisement.getImageUrl() != null && !advertisement.getImageUrl().isEmpty()) {
            // External URL stored directly
            dto.setImageUrl(advertisement.getImageUrl());
        } else if (advertisement.getImageMediaId() != null) {
            // Resolve internal media URL
            try {
                String mediaUrl = mediaService.getMediaUrl(advertisement.getImageMediaId());
                dto.setImageUrl(mediaUrl);
            } catch (Exception e) {
                // Log error but don't fail the mapping
                dto.setImageUrl(null);
            }
        }

        return dto;
    }

    public List<AdvertisementDTO> toDTOList(List<Advertisement> advertisements) {
        if (advertisements == null || advertisements.isEmpty()) {
            return List.of();
        }
        return advertisements.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}