package com.biosteel.teams.user.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.biosteel.teams.auth.dto.ChangePasswordRequest;
import com.biosteel.teams.auth.dto.ResetPasswordRequest;
import com.biosteel.teams.auth.model.PasswordResetToken;
import com.biosteel.teams.auth.repository.PasswordResetTokenRepository;
import com.biosteel.teams.auth.service.UserVerificationService;
import com.biosteel.teams.common.exception.ForbiddenException;
import com.biosteel.teams.common.exception.InvalidCredentialsException;
import com.biosteel.teams.common.exception.InvalidTokenException;
import com.biosteel.teams.common.exception.ResourceNotFoundException;
import com.biosteel.teams.common.service.EmailService;
import com.biosteel.teams.common.service.SmsService;
import com.biosteel.teams.invitation.model.Invitation;
import com.biosteel.teams.invitation.service.InvitationService;
import com.biosteel.teams.media.dto.MediaDto;
import com.biosteel.teams.media.service.MediaService;
import com.biosteel.teams.notification.service.NotificationService;
import com.biosteel.teams.player.dto.PlayerCreateDTO;
import com.biosteel.teams.player.service.PlayerService;
import com.biosteel.teams.role.model.Role;
import com.biosteel.teams.role.model.UserRoleEnum;
import com.biosteel.teams.role.repository.RoleRepository;
import com.biosteel.teams.team.service.TeamMemberService;
import com.biosteel.teams.user.dto.UpdateUserRequest;
import com.biosteel.teams.user.dto.UserDto;
import com.biosteel.teams.user.dto.UserListDto;
import com.biosteel.teams.user.dto.UserRegistrationRequest;
import com.biosteel.teams.user.dto.UserRegistrationResponse;
import com.biosteel.teams.user.exception.UserAlreadyExistsException;
import com.biosteel.teams.user.mapper.UserMapper;
import com.biosteel.teams.user.model.User;
import com.biosteel.teams.user.repository.UserRepository;
import com.biosteel.teams.user.security.DSUserDetails;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final PlayerService playerService;
    private final EmailService emailService;
    private final UserVerificationService userVerificationService;
    private final PasswordResetTokenRepository passwordTokenRepository;
    private final UserMapper userMapper;
    private final MediaService mediaService;
    private final SmsService smsService;
    private final InvitationService invitationService;
    private final NotificationService notificationService;
    private final TeamMemberService teamMemberService;

    private static final String DEFAULT_ROLE = UserRoleEnum.ROLE_USER.name();

    @Value("${app.default-avatar.url-template}")
    private String defaultAvatarUrlTemplate;

    public enum TokenValidationResult {
        VALID("valid"),
        INVALID_TOKEN("invalidToken"),
        EXPIRED("expired");

        private final String value;

        TokenValidationResult(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    @Transactional
    public Page<UserListDto> getAllUsers(Pageable pageable, String query) {
        Page<User> users;
        if (query != null && !query.trim().isEmpty()) {
            users = userRepository
                    .findAllByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCaseAndDeletedAtIsNullAndEnabledIsTrue(
                            query.trim(), query.trim(), query.trim(), pageable);
        } else {
            users = userRepository.findAllByDeletedAtIsNullAndEnabledIsTrue(pageable);
        }
        return users.map(user -> {
            UserListDto dto = new UserListDto();
            dto.setUserId(user.getUserId());
            dto.setEmail(user.getEmail());
            dto.setFirstName(user.getFirstName());
            dto.setLastName(user.getLastName());
            dto.setProfilePhotoMediaId(user.getProfilePhotoMediaId());
            return dto;
        });
    }

    @Transactional
    public UserRegistrationResponse initiateRegistration(UserRegistrationRequest request) {
        User user = mapUser(request);
        Role userRole = mapUserRole(request, user);
        User savedUser = userRepository.save(user);
        if (userRole.getName().equals("ROLE_PLAYER")) {
            PlayerCreateDTO playerCreateDTO = PlayerCreateDTO.builder()
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .dateOfBirth(user.getDateOfBirth())
                    .gender(user.getGender())
                    .build();

            playerService.createPlayer(savedUser.getUserId(), playerCreateDTO);
        }

        String verificationToken = generateSixDigitCode();
        userVerificationService.createVerificationTokenForUser(savedUser, verificationToken);

        emailService.sendVerificationEmail(request.getEmail(), verificationToken);
        smsService.sendVerificationCode(request.getPhone(), verificationToken);

        UserRegistrationResponse response = new UserRegistrationResponse();
        response.setRegistrationId(savedUser.getUserId());
        response.setVerificationCode(verificationToken);

        return response;
    }

    private String generateSixDigitCode() {
        Random rnd = new Random();
        return String.valueOf(100000 + rnd.nextInt(900000));
    }

    private User mapUser(UserRegistrationRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new UserAlreadyExistsException("User already exists with email: " + request.getEmail());
        }

        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setEmail(request.getEmail());
        user.setEnabled(false);
        user.setRegistrationDate(LocalDateTime.now());
        user.setCreatedAt(LocalDateTime.now());
        user.setFailedLoginAttempts(0);
        user.setLocked(false);
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setGender(request.getGender());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setUpdatedAt(LocalDateTime.now());

        return user;
    }

    private Role mapUserRole(UserRegistrationRequest request, User user) {
        Role userRole;
        if (request.getRole() != null) {
            userRole = roleRepository.findByName("ROLE_" + request.getRole().toUpperCase())
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + request.getRole()));
        } else {
            userRole = roleRepository.findByName(DEFAULT_ROLE)
                    .orElseThrow(() -> new ResourceNotFoundException("Default role not found"));
        }
        user.setRoles(new HashSet<>(Collections.singleton(userRole)));

        return userRole;
    }

    @Transactional
    public User verifyRegistration(UUID registrationId, String token) {
        User user = userRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found"));

        if (user.isEnabled()) {
            throw new IllegalStateException("Email already verified");
        }

        final TokenValidationResult result = userVerificationService.validateVerificationToken(token);

        if (result != TokenValidationResult.VALID) {
            throw new InvalidTokenException("Invalid verification token");
        }

        userVerificationService.deleteVerificationToken(token);

        user.setEnabled(true);
        user.setRegistrationDate(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);
        emailService.sendWelcomeEmail(user.getEmail(), user.getFirstName());
        return savedUser;
    }

    @Transactional
    public User registerInvitedUser(UserRegistrationRequest request, Invitation invitation) {
        User user = mapUser(request);
        user.setEnabled(true);
        user.setRegistrationDate(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        mapUserRole(request, user);
        User savedUser = userRepository.save(user);

        if (invitation != null) {
            acceptInvitation(savedUser, invitation);
            invitationService.acceptInvitation(savedUser.getUserId(), invitation);
        }

        emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getFirstName());

        return savedUser;
    }

    @Transactional
    public User acceptInvitation(User user, Invitation invitation) {
        if (invitation != null) {
            teamMemberService.acceptInvitation(user.getUserId(), invitation);
        }

        return user;
    }

    @Transactional
    public void resendVerification(UUID registrationId) {
        User user = userRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found"));

        if (user.isEnabled()) {
            throw new IllegalStateException("Email already verified");
        }

        userVerificationService.deleteVerificationTokenForUser(user);

        String newToken = generateSixDigitCode();
        userVerificationService.createVerificationTokenForUser(user, newToken);
        emailService.sendVerificationEmail(user.getEmail(), newToken);
        smsService.sendVerificationCode(user.getPhone(), newToken);
    }

    @Transactional
    public void logout(String deviceToken) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            User user = userRepository.findByEmailIgnoreCase(authentication.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            user.setLastActivityDate(LocalDateTime.now());
            user.setAccessToken(null);
            user.setRefreshToken(null);
            userRepository.save(user);

            if (deviceToken != null && !deviceToken.trim().isEmpty()) {
                boolean deactivated = notificationService.deactivateDeviceTokenOnLogout(user.getUserId(), deviceToken);
            } else {
                int deactivatedCount = notificationService.deactivateAllUserTokensOnLogout(user.getUserId());
            }
        }
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("New passwords don't match");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String resetToken = generateSixDigitCode();
        createPasswordResetTokenForUser(user, resetToken);

        emailService.sendPasswordResetEmail(email, resetToken);
    }

    public void createPasswordResetTokenForUser(final User user, final String token) {
        final PasswordResetToken myToken = new PasswordResetToken(token, user);
        passwordTokenRepository.save(myToken);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {

        PasswordResetToken passwordResetToken = passwordTokenRepository.findByToken(request.getToken());

        if (passwordResetToken == null) {
            throw new InvalidTokenException("Invalid reset token");
        }

        if (passwordResetToken.getExpiryDate().before(new Date())) {
            throw new InvalidTokenException("Reset token has expired");
        }

        if (!request.getNewPassword().equals(request.getPasswordConfirmation())) {
            throw new IllegalArgumentException("Passwords don't match");
        }

        User user = passwordResetToken.getUser();

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        passwordTokenRepository.delete(passwordResetToken);
    }

    @Transactional
    public User getUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return user;
    }

    @Transactional
    public void validateUserExists(UUID userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional
    public UserDto getUserProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userMapper.toDto(user);
    }

    @Transactional
    public UserListDto getUserMinial(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userMapper.toUserListDto(user);
    }

    @Transactional
    public UserDto updateUserProfile(UUID userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean wasPlayer = user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_PLAYER"));

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getDateOfBirth() != null) {
            user.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getProfilePhotoMediaId() != null) {
            user.setProfilePhotoMediaId(request.getProfilePhotoMediaId());
        }

        if (request.getRole() != null) {
            Role role = roleRepository.findByName("ROLE_" + request.getRole().toUpperCase())
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + request.getRole()));
            user.setRoles(new HashSet<>(Collections.singleton(role)));
        }

        if (request.getRole() != null) {
            Role role = roleRepository.findByName("ROLE_" + request.getRole().toUpperCase())
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + request.getRole()));
            user.setRoles(new HashSet<>(Collections.singleton(role)));

            // Check if user is newly assigned ROLE_PLAYER
            boolean isNowPlayer = role.getName().equals("ROLE_PLAYER");
            if (!wasPlayer && isNowPlayer) {
                PlayerCreateDTO playerCreateDTO = PlayerCreateDTO.builder()
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .dateOfBirth(user.getDateOfBirth())
                        .gender(user.getGender())
                        .build();

                playerService.createPlayer(user.getUserId(), playerCreateDTO);
            }
        }

        user.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userRepository.save(user);
        return userMapper.toDto(updatedUser);
    }

    @Transactional
    public MediaDto uploadUserAvatar(UUID userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        MediaDto mediaDto = null;
        UUID mediaId = user.getProfilePhotoMediaId();
        String title = user.getFirstName() + " avatar";
        String body = user.getFirstName() + " " + user.getLastName();

        if (mediaId == null) {
            mediaDto = mediaService.uploadMedia(file, null, userId, title, body);
        } else {
            mediaDto = mediaService.updateMediaWithFile(mediaId, file, title, body);
        }
        user.setProfilePhotoMediaId(mediaDto.getMediaId());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return mediaDto;
    }

    public ResponseEntity<Resource> getAvatar(User user) {
        return getPublicAvatar(user.getUserId());
    }

    public User getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal != null && principal instanceof DSUserDetails) {
                return ((DSUserDetails) principal).getUser();
            }
        }
        return null;
    }

    public void validateUserAccess(UUID requestedUserId) {
        User loggedInUser = getLoggedInUser();
        if (loggedInUser == null) {
            throw new ForbiddenException("User not authorized to access this resource");
        }
        if (!loggedInUser.getUserId().equals(requestedUserId)) {
            throw new ForbiddenException("User not authorized to access this resource");
        }
    }

    public ResponseEntity<Resource> getPublicAvatar(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getProfilePhotoMediaId() != null) {
            return mediaService.streamMediaInline(user.getProfilePhotoMediaId());
        }

        String initials = generateInitials(user.getFirstName(), user.getLastName());
        String defaultAvatarUrl = defaultAvatarUrlTemplate.replace("{initials}", initials);

        return ResponseEntity.status(302)
                .header("Location", defaultAvatarUrl)
                .build();
    }

    private String generateInitials(String firstName, String lastName) {
        StringBuilder initials = new StringBuilder();
        if (firstName != null && !firstName.isEmpty()) {
            initials.append(firstName.charAt(0));
        }
        if (lastName != null && !lastName.isEmpty()) {
            initials.append(lastName.charAt(0));
        }
        return initials.toString().toUpperCase();
    }
}