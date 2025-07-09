package com.biosteel.teams.user.security;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.biosteel.teams.auth.service.LoginAttemptService;
import com.biosteel.teams.role.model.Role;
import com.biosteel.teams.user.model.User;
import com.biosteel.teams.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
/**
 * DSUserDetailsService is an implementation of Spring Security's
 * UserDetailsService. It is responsible for loading user-specific data during
 * authentication.
 */
public class DSUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final LoginAttemptService loginAttemptService;

    @Override
    public DSUserDetails loadUserByUsername(final String email) throws UsernameNotFoundException {
        log.debug("DSUserDetailsService.loadUserByUsername: called with email: {}", email);

        try {
            // Validate email input
            if (email == null || email.trim().isEmpty()) {
                throw new UsernameNotFoundException("Email address cannot be empty");
            }

            String trimmedEmail = email.trim().toLowerCase();
            User user = userRepository.findByEmailIgnoreCase(trimmedEmail)
                    .orElseThrow(() -> new UsernameNotFoundException(
                            String.format("No user found with email: '%s'", trimmedEmail)));

            user.setLastActivityDate(LocalDateTime.now());
            user = loginAttemptService.checkIfUserShouldBeUnlocked(user);

            Collection<? extends GrantedAuthority> authorities = getAuthorities(user.getRoles());
            return new DSUserDetails(user, authorities);
        } catch (UsernameNotFoundException e) {
            log.debug("User not found for email: {}", email);
            throw e;
        } catch (final Exception e) {
            log.error("DSUserDetailsService.loadUserByUsername: Exception!", e);
            throw new RuntimeException("An unexpected error occurred during authentication", e);
        }
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Collection<Role> roles) {
        return roles.stream()
                .map(Role::getName)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }

}
