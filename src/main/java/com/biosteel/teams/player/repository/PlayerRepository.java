package com.biosteel.teams.player.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.biosteel.teams.player.model.Player;

@Repository
public interface PlayerRepository extends JpaRepository<Player, UUID> {
    Optional<Player> findByPlayerIdAndParentUserUserIdAndDeletedAtIsNull(UUID playerId, UUID userId);

    Page<Player> findAllByParentUserUserIdAndDeletedAtIsNull(UUID userId, Pageable pageable);

    Page<Player> findAllByDeletedAtIsNull(Pageable pageable);

    boolean existsByPlayerIdAndParentUserUserId(UUID playerId, UUID userId);

    Page<Player> findAllByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseAndDeletedAtIsNull(
            String firstName, String lastName, Pageable pageable);
}