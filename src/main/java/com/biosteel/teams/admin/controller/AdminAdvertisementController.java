package com.biosteel.teams.admin.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.biosteel.teams.advertisement.dto.AdvertisementDTO;
import com.biosteel.teams.advertisement.dto.CreateAdvertisementRequest;
import com.biosteel.teams.advertisement.service.AdvertisementService;
import com.biosteel.teams.auth.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/advertisements")
@RequiredArgsConstructor
@Tag(name = "Admin - Advertisement Management", description = "Admin APIs for managing advertisements")
public class AdminAdvertisementController {

    private final AdvertisementService advertisementService;

    @Operation(summary = "Get all advertisements")
    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<AdvertisementDTO>>> getAllAds() {
        List<AdvertisementDTO> ads = advertisementService.getAllAds();
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Advertisements retrieved successfully",
                ads));
    }

    @Operation(summary = "Get advertisement by ID")
    @GetMapping("/{adId}")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AdvertisementDTO>> getAdById(@PathVariable UUID adId) {
        AdvertisementDTO ad = advertisementService.getAdById(adId);
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Advertisement retrieved successfully",
                ad));
    }

    @Operation(summary = "Create new advertisement")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AdvertisementDTO>> createAd(
            @Valid @RequestBody CreateAdvertisementRequest request) {
        AdvertisementDTO ad = advertisementService.createAd(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(
                true,
                "Advertisement created successfully",
                ad));
    }

    @Operation(summary = "Update advertisement")
    @PutMapping("/{adId}")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AdvertisementDTO>> updateAd(
            @PathVariable UUID adId,
            @Valid @RequestBody CreateAdvertisementRequest request) {
        AdvertisementDTO ad = advertisementService.updateAd(adId, request);
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Advertisement updated successfully",
                ad));
    }

    @Operation(summary = "Delete advertisement")
    @DeleteMapping("/{adId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteAd(@PathVariable UUID adId) {
        advertisementService.deleteAd(adId);
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Advertisement deleted successfully"));
    }

    @Operation(summary = "Toggle advertisement active status")
    @PutMapping("/{adId}/toggle")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> toggleAdStatus(
            @PathVariable UUID adId,
            @RequestBody boolean isActive) {
        advertisementService.toggleAdStatus(adId, isActive);
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Advertisement status updated successfully"));
    }
}