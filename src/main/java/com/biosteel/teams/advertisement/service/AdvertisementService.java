package com.biosteel.teams.advertisement.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.biosteel.teams.advertisement.dto.AdvertisementDTO;
import com.biosteel.teams.advertisement.dto.CreateAdvertisementRequest;
import com.biosteel.teams.advertisement.mapper.AdvertisementMapper;
import com.biosteel.teams.advertisement.model.Advertisement;
import com.biosteel.teams.advertisement.repository.AdvertisementRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdvertisementService {

    private final AdvertisementRepository advertisementRepository;
    private final AdvertisementMapper advertisementMapper;

    @Transactional(readOnly = true)
    public List<AdvertisementDTO> getActiveAdsByPlacement(String placement) {
        log.debug("Fetching active ads for placement: {}", placement);

        OffsetDateTime now = OffsetDateTime.now();
        List<Advertisement> ads;

        if (placement != null && !placement.isEmpty()) {
            ads = advertisementRepository.findActiveAdsByPlacement(placement, now);
        } else {
            ads = advertisementRepository.findAllActiveAds(now);
        }

        return advertisementMapper.toDTOList(ads);
    }

    @Transactional(readOnly = true)
    public List<AdvertisementDTO> getAllAds() {
        log.debug("Fetching all advertisements for admin");
        List<Advertisement> ads = advertisementRepository.findAllByDeletedAtIsNullOrderByCreatedAtDesc();
        return advertisementMapper.toDTOList(ads);
    }

    @Transactional(readOnly = true)
    public AdvertisementDTO getAdById(UUID adId) {
        log.debug("Fetching advertisement by ID: {}", adId);
        Advertisement ad = advertisementRepository.findByAdIdAndDeletedAtIsNull(adId)
                .orElseThrow(() -> new RuntimeException("Advertisement not found: " + adId));
        return advertisementMapper.toDTO(ad);
    }

    public AdvertisementDTO createAd(CreateAdvertisementRequest request) {
        log.info("Creating new advertisement: {}", request.getTitle());

        Advertisement ad = Advertisement.builder()
                .adId(UUID.randomUUID())
                .title(request.getTitle())
                .description(request.getDescription())
                .imageMediaId(request.getImageMediaId())
                .imageUrl(request.getImageUrl())
                .targetUrl(request.getTargetUrl())
                .settingsJson(request.getSettingsJson())
                .analyticEvent(request.getAnalyticEvent())
                .type(request.getType() != null ? request.getType() : "CARD")
                .placement(request.getPlacement() != null ? request.getPlacement() : "EVENTS")
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .createdAt(OffsetDateTime.now())
                .build();

        Advertisement savedAd = advertisementRepository.save(ad);
        return advertisementMapper.toDTO(savedAd);
    }

    public AdvertisementDTO updateAd(UUID adId, CreateAdvertisementRequest request) {
        log.info("Updating advertisement: {}", adId);

        Advertisement ad = advertisementRepository.findByAdIdAndDeletedAtIsNull(adId)
                .orElseThrow(() -> new RuntimeException("Advertisement not found: " + adId));

        ad.setTitle(request.getTitle());
        ad.setDescription(request.getDescription());
        ad.setImageMediaId(request.getImageMediaId());
        ad.setImageUrl(request.getImageUrl());
        ad.setTargetUrl(request.getTargetUrl());
        ad.setAnalyticEvent(request.getAnalyticEvent());
        ad.setType(request.getType() != null ? request.getType() : ad.getType());
        ad.setPlacement(request.getPlacement() != null ? request.getPlacement() : ad.getPlacement());
        ad.setIsActive(request.getIsActive() != null ? request.getIsActive() : ad.getIsActive());
        ad.setStartDate(request.getStartDate());
        ad.setEndDate(request.getEndDate());
        ad.setUpdatedAt(OffsetDateTime.now());

        Advertisement savedAd = advertisementRepository.save(ad);
        return advertisementMapper.toDTO(savedAd);
    }

    public void deleteAd(UUID adId) {
        log.info("Deleting advertisement: {}", adId);

        Advertisement ad = advertisementRepository.findByAdIdAndDeletedAtIsNull(adId)
                .orElseThrow(() -> new RuntimeException("Advertisement not found: " + adId));

        ad.setDeletedAt(OffsetDateTime.now());
        advertisementRepository.save(ad);
    }

    public void toggleAdStatus(UUID adId, boolean isActive) {
        log.info("Toggling advertisement status: {} to {}", adId, isActive);

        Advertisement ad = advertisementRepository.findByAdIdAndDeletedAtIsNull(adId)
                .orElseThrow(() -> new RuntimeException("Advertisement not found: " + adId));

        ad.setIsActive(isActive);
        ad.setUpdatedAt(OffsetDateTime.now());
        advertisementRepository.save(ad);
    }
}