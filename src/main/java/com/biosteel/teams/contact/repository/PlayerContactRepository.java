package com.biosteel.teams.contact.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.biosteel.teams.contact.model.PlayerContact;

@Repository
public interface PlayerContactRepository extends JpaRepository<PlayerContact, UUID> {

    List<PlayerContact> findByPlayerMemberIdAndDeletedAtIsNull(UUID playerMemberId);

    List<PlayerContact> findByContactMemberIdAndDeletedAtIsNull(UUID contactMemberId);

    @Query("SELECT pc FROM PlayerContact pc WHERE pc.playerMemberId = :playerMemberId " +
            "AND pc.isPrimaryContact = true AND pc.deletedAt IS NULL")
    List<PlayerContact> findPrimaryContactsByPlayerMemberId(UUID playerMemberId);

    @Query("SELECT pc FROM PlayerContact pc WHERE pc.playerMemberId = :playerMemberId " +
            "AND pc.isEmergencyContact = true AND pc.deletedAt IS NULL")
    List<PlayerContact> findEmergencyContactsByPlayerMemberId(UUID playerMemberId);

    boolean existsByPlayerMemberIdAndContactMemberIdAndDeletedAtIsNull(UUID playerMemberId, UUID contactMemberId);
}