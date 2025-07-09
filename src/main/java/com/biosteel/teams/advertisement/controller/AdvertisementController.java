package com.biosteel.teams.advertisement.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.biosteel.teams.advertisement.dto.AdvertisementDTO;
import com.biosteel.teams.advertisement.service.AdvertisementService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/advertisements")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Advertisement Management", description = "APIs for managing advertisements")
public class AdvertisementController {

    private final AdvertisementService advertisementService;

    @Operation(summary = "Get advertisements by placement")
    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AdvertisementDTO>> getAdsByPlacement(
            @RequestParam(required = false) String placement) {

        List<AdvertisementDTO> ads = advertisementService.getActiveAdsByPlacement(placement);
        return ResponseEntity.ok(ads);
    }

    @Operation(summary = "Get advertisement by ID")
    @GetMapping("/{adId}")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AdvertisementDTO> getAdById(@PathVariable UUID adId) {
        AdvertisementDTO ad = advertisementService.getAdById(adId);
        return ResponseEntity.ok(ad);
    }
}