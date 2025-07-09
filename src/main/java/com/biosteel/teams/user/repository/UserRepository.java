package com.biosteel.teams.user.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.biosteel.teams.user.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
        Optional<User> findByEmailIgnoreCase(String email);

        Optional<User> findByAccessToken(String accessToken);

        boolean existsByEmailIgnoreCase(String email);

        List<User> findAllByDeletedAtIsNull();

        Page<User> findAllByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCaseAndDeletedAtIsNull(
                        String firstName,
                        String lastName,
                        String email,
                        Pageable pageable);

        Page<User> findAllByDeletedAtIsNull(Pageable pageable);

        Page<User> findAllByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCaseAndDeletedAtIsNullAndEnabledIsTrue(
                        String firstName,
                        String lastName,
                        String email,
                        Pageable pageable);

        Page<User> findAllByDeletedAtIsNullAndEnabledIsTrue(Pageable pageable);

        @Modifying
        @Query("UPDATE User u SET u.failedLoginAttempts = ?1 WHERE u.userId = ?2")
        void updateFailedLoginAttempts(Integer attempts, UUID userId);

        @Modifying
        @Query("UPDATE User u SET u.locked = ?1, u.lockedDate = ?2 WHERE u.userId = ?3")
        void updateAccountLocked(boolean locked, LocalDateTime lockedDate, UUID userId);

        @Modifying
        @Query("UPDATE User u SET u.lastActivityDate = ?1 WHERE u.userId = ?2")
        void updateLastActivityDate(LocalDateTime lastActivityDate, UUID userId);

        Page<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        String firstName, String lastName, String email, Pageable pageable);
}
