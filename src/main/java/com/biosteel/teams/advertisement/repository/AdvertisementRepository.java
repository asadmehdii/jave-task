package com.biosteel.teams.advertisement.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.biosteel.teams.advertisement.model.Advertisement;

@Repository
public interface AdvertisementRepository extends JpaRepository<Advertisement, UUID> {

        List<Advertisement> findAllByDeletedAtIsNullOrderByCreatedAtDesc();

        Optional<Advertisement> findByAdIdAndDeletedAtIsNull(UUID adId);

        @Query(value = """
                        SELECT * FROM biosteel.advertisement a
                        WHERE a.deleted_at IS NULL
                        AND a.is_active = true
                        AND (a.start_date IS NULL OR a.start_date <= :now)
                        AND (a.end_date IS NULL OR a.end_date >= :now)
                        AND (:placement IS NULL OR a.placement = :placement)
                        ORDER BY a.created_at DESC
                        """, nativeQuery = true)
        List<Advertisement> findActiveAdsByPlacement(
                        @Param("placement") String placement,
                        @Param("now") OffsetDateTime now);

        @Query(value = """
                        SELECT * FROM biosteel.advertisement a
                        WHERE a.deleted_at IS NULL
                        AND a.is_active = true
                        AND (a.start_date IS NULL OR a.start_date <= :now)
                        AND (a.end_date IS NULL OR a.end_date >= :now)
                        ORDER BY a.created_at DESC
                        """, nativeQuery = true)
        List<Advertisement> findAllActiveAds(@Param("now") OffsetDateTime now);
}