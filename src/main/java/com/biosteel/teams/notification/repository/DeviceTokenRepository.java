package com.biosteel.teams.notification.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.biosteel.teams.notification.model.DeviceToken;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, UUID> {

    List<DeviceToken> findByUserIdAndIsActiveTrue(UUID userId);

    Optional<DeviceToken> findByToken(String token);

    Optional<DeviceToken> findByTokenAndUserId(String token, UUID userId);

    List<DeviceToken> findByUserIdInAndIsActiveTrue(List<UUID> userIds);

    boolean existsByToken(String token);
}