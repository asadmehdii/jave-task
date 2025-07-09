package com.biosteel.teams.auth.service;

import java.util.Calendar;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.biosteel.teams.auth.model.VerificationToken;
import com.biosteel.teams.auth.repository.VerificationTokenRepository;
import com.biosteel.teams.user.model.User;
import com.biosteel.teams.user.service.UserService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserVerificationService {

    /** The token repository. */
    private final VerificationTokenRepository tokenRepository;

    /**
     * Gets verification token by user.
     *
     * @param User the user
     * @return the verification token by user
     */
    public VerificationToken getVerificationTokenByUser(final User user) {
        log.debug("UserVerificationService.getVerificationTokenByUser: called with user: {}",
                user == null ? "UNDEFINED" : user.getUserId());
        final VerificationToken token = tokenRepository.findByUser(user);
        return token;
    }

    /**
     * Gets the verification token.
     *
     * @param VerificationToken the verification token
     * @return the verification token
     */
    public VerificationToken getVerificationToken(final String VerificationToken) {
        return tokenRepository.findByToken(VerificationToken);
    }

    /**
     * Generate new verification token.
     *
     * @param existingVerificationToken the existing verification token
     * @return the verification token
     */
    public VerificationToken generateNewVerificationToken(final String existingVerificationToken) {
        VerificationToken vToken = tokenRepository.findByToken(existingVerificationToken);
        vToken.updateToken(UUID.randomUUID().toString());
        vToken = tokenRepository.save(vToken);
        return vToken;
    }

    /**
     * Creates the verification token for user.
     *
     * @param user  the user
     * @param token the token
     */
    @Transactional
    public void createVerificationTokenForUser(final User user, final String token) {
        final VerificationToken myToken = new VerificationToken(token, user);
        tokenRepository.save(myToken);
    }

    @Transactional
    public void deleteVerificationTokenForUser(final User user) {
        tokenRepository.deleteByUserId(user.getUserId());
    }

    /**
     * Validate verification token.
     *
     * @param token the token
     * @return the string
     */
    public UserService.TokenValidationResult validateVerificationToken(String token) {
        final VerificationToken verificationToken = tokenRepository.findByToken(token);
        if (verificationToken == null) {
            return UserService.TokenValidationResult.INVALID_TOKEN;
        }

        final Calendar cal = Calendar.getInstance();
        if (verificationToken.getExpiryDate().before(cal.getTime())) {
            tokenRepository.delete(verificationToken);
            return UserService.TokenValidationResult.EXPIRED;
        }

        return UserService.TokenValidationResult.VALID;
    }

    /**
     * Delete verification token.
     *
     * @param token the token
     */
    public void deleteVerificationToken(final String token) {
        log.debug("UserVerificationService.deleteVerificationToken: called with token: {}", token);
        final VerificationToken verificationToken = tokenRepository.findByToken(token);
        if (verificationToken != null) {
            tokenRepository.delete(verificationToken);
            log.debug("UserVerificationService.deleteVerificationToken: token deleted.");
        } else {
            log.debug("UserVerificationService.deleteVerificationToken: token not found.");
        }
    }

}
