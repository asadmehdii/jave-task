package com.biosteel.teams.user.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.biosteel.teams.auth.dto.ApiResponse;
import com.biosteel.teams.auth.dto.ChangePasswordRequest;
import com.biosteel.teams.auth.dto.ResetPasswordRequest;
import com.biosteel.teams.auth.dto.VerificationRequest;
import com.biosteel.teams.auth.service.LoginAuthenticationSuccessHandler;
import com.biosteel.teams.common.config.DevelopmentProperties;
import com.biosteel.teams.common.exception.ResourceNotFoundException;
import com.biosteel.teams.invitation.dto.InvitationResponseDTO;
import com.biosteel.teams.invitation.mapper.InvitationMapper;
import com.biosteel.teams.invitation.model.Invitation;
import com.biosteel.teams.invitation.service.InvitationService;
import com.biosteel.teams.media.dto.MediaDto;
import com.biosteel.teams.team.dto.TeamDTO;
import com.biosteel.teams.team.mapper.TeamMapper;
import com.biosteel.teams.team.model.Team;
import com.biosteel.teams.team.repository.TeamRepository;
import com.biosteel.teams.user.dto.UpdateUserRequest;
import com.biosteel.teams.user.dto.UserDto;
import com.biosteel.teams.user.dto.UserListDto;
import com.biosteel.teams.user.dto.UserRegistrationRequest;
import com.biosteel.teams.user.dto.UserRegistrationResponse;
import com.biosteel.teams.user.mapper.UserMapper;
import com.biosteel.teams.user.model.User;
import com.biosteel.teams.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for user management")
public class UserController {

        private final UserService userService;
        private final LoginAuthenticationSuccessHandler loginAuthenticationSuccessHandler;
        private final UserMapper userMapper;
        private final InvitationService invitationService;
        private final InvitationMapper invitationMapper;
        private final TeamRepository teamRepository;
        private final TeamMapper teamMapper;
        private final DevelopmentProperties developmentProperties;

        @Operation(summary = "Get all users")
        @SecurityRequirement(name = "bearerAuth")
        @GetMapping
        @PreAuthorize("isAuthenticated()")
        // @PreAuthorize("hasRole('SUPER_ADMIN') or #userId ==
        // authentication.principal.userId")
        // FIXME: Only provide this to SUPER_ADMIN, TEAM_CREATE roles
        public ResponseEntity<List<UserListDto>> getAllUsers(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "firstName") String sortBy,
                        @RequestParam(required = false) String q) {
                PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sortBy));
                Page<UserListDto> users = userService.getAllUsers(pageRequest, q);
                return ResponseEntity.ok()
                                .header("X-Total-Count", String.valueOf(users.getTotalElements()))
                                .body(users.getContent());
        }

        @Operation(summary = "Register new user with details")
        @PostMapping("/register")
        public ResponseEntity<ApiResponse<UUID>> registerEmail(
                        @Valid @RequestBody UserRegistrationRequest request) {
                UserRegistrationResponse initResponse = userService.initiateRegistration(request);

                ApiResponse<UUID> response = new ApiResponse<>(
                                true,
                                "Registration initiated. Please check your phone text for verification.",
                                initResponse.getRegistrationId());

                if (developmentProperties.isEnabled() && developmentProperties.isIncludeVerificationCode()) {
                        response.withDevelopmentData(Map.of("verificationCode", initResponse.getVerificationCode()));
                }

                return ResponseEntity.ok(response);
        }

        @Operation(summary = "Verify email registration")
        @PostMapping("/register/{registrationId}/verify")
        public ResponseEntity<ApiResponse<UserDto>> verifyRegistration(
                        HttpServletRequest request, HttpServletResponse response,
                        @PathVariable UUID registrationId,
                        @RequestBody VerificationRequest verificationRequest) {
                User user = userService.verifyRegistration(registrationId, verificationRequest.getToken());
                if (user != null) {
                        loginAuthenticationSuccessHandler.setUserBearerToken(response, user);

                }
                return ResponseEntity.ok(new ApiResponse<>(
                                true,
                                "Email verified successfully",
                                userMapper.toDto(user)));
        }

        @Operation(summary = "Resend verification email")
        @PostMapping("/register/{registrationId}/verify/resend")
        public ResponseEntity<ApiResponse<Void>> resendVerification(
                        @PathVariable UUID registrationId) {
                userService.resendVerification(registrationId);
                return ResponseEntity.ok(new ApiResponse<>(
                                true,
                                "Verification email resent successfully"));
        }

        @Operation(summary = "Validate an invitation")
        @PostMapping("/invitation/validate/{invitationCode}")
        public ResponseEntity<ApiResponse<InvitationResponseDTO>> validateInvitation(
                        @PathVariable String invitationCode) {
                Invitation invitation = invitationService.verifyInvitation(invitationCode);
                Team team = teamRepository.findById(invitation.getTeamId())
                                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));
                TeamDTO teamDTO = teamMapper.toDTO(team);
                return ResponseEntity.ok(new ApiResponse<>(true, "Invitation validated successfully",
                                invitationMapper.toDto(invitation, teamDTO)));
        }

        @Operation(summary = "Register invited user")
        @PostMapping("/invitation/register/{invitationCode}")
        public ResponseEntity<ApiResponse<UserDto>> registerInvitedUser(
                        HttpServletRequest request,
                        HttpServletResponse response,
                        @PathVariable String invitationCode,
                        @Valid @RequestBody UserRegistrationRequest registrationRequest) {

                Invitation invitation = invitationService.verifyInvitation(invitationCode);
                User user = userService.registerInvitedUser(registrationRequest, invitation);

                // Set authentication tokens for immediate login
                loginAuthenticationSuccessHandler.setUserBearerToken(response, user);

                // Return the user details
                UserDto userDto = userMapper.toDto(user);
                return ResponseEntity.ok(new ApiResponse<>(
                                true,
                                "Registration successful and added to team",
                                userDto));
        }

        @Operation(summary = "Accept an invitation")
        @SecurityRequirement(name = "bearerAuth")
        @PostMapping("/invitation/accept/{invitationCode}")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<ApiResponse<UserDto>> acceptInvitation(
                        @PathVariable String invitationCode) {

                User user = userService.getLoggedInUser();
                Invitation invitation = invitationService.verifyInvitation(invitationCode);

                userService.acceptInvitation(user, invitation);
                invitationService.acceptInvitation(user.getUserId(), invitation);

                UserDto userDto = userMapper.toDto(user);
                return ResponseEntity.ok(new ApiResponse<>(
                                true,
                                "Invitation accepted successfully",
                                userDto));
        }

        @Operation(summary = "Get current user's pending invitations")
        @SecurityRequirement(name = "bearerAuth")
        @GetMapping("/me/invitations")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<ApiResponse<List<InvitationResponseDTO>>> getUserPendingInvitations() {
                User user = userService.getLoggedInUser();
                List<InvitationResponseDTO> invitations = invitationService.getUserPendingInvitations(user.getEmail());
                return ResponseEntity.ok(new ApiResponse<>(
                                true,
                                "Pending invitations retrieved successfully",
                                invitations));
        }

        @Operation(summary = "User logout")
        @SecurityRequirement(name = "bearerAuth")
        @PostMapping("/logout")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<ApiResponse<Void>> logout(@RequestParam(required = false) String deviceToken) {
                userService.logout(deviceToken);
                return ResponseEntity.ok(new ApiResponse<>(
                                true,
                                "Logged out successfully"));
        }

        @Operation(summary = "Change password")
        @SecurityRequirement(name = "bearerAuth")
        @PostMapping("/changePassword")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<ApiResponse<Void>> changePassword(
                        @Valid @RequestBody ChangePasswordRequest request) {
                userService.changePassword(request);
                return ResponseEntity.ok(new ApiResponse<>(
                                true,
                                "Password changed successfully"));
        }

        @Operation(summary = "Request password reset")
        @PostMapping("/resetPassword/request")
        public ResponseEntity<ApiResponse<Void>> requestPasswordReset(
                        @RequestParam String email) {
                userService.requestPasswordReset(email);
                return ResponseEntity.ok(new ApiResponse<>(
                                true,
                                "Password reset email sent successfully"));
        }

        @Operation(summary = "Reset password")
        @PostMapping("/resetPassword/confirm")
        public ResponseEntity<ApiResponse<Void>> resetPassword(
                        @Valid @RequestBody ResetPasswordRequest request) {
                userService.resetPassword(request);
                return ResponseEntity.ok(new ApiResponse<>(
                                true,
                                "Password reset successfully"));
        }

        @Operation(summary = "Get current user's profile")
        @SecurityRequirement(name = "bearerAuth")
        @PreAuthorize("isAuthenticated()")
        @GetMapping("/me")
        public ResponseEntity<ApiResponse<UserDto>> getCurrentUser() {
                User user = userService.getLoggedInUser();
                UserDto userDto = userService.getUserProfile(user.getUserId());
                return ResponseEntity.ok(new ApiResponse<>(
                                true,
                                "Current user profile retrieved successfully",
                                userDto));
        }

        @Operation(summary = "Get user profile")
        @SecurityRequirement(name = "bearerAuth")
        @GetMapping("/{userId}")
        @PreAuthorize("hasRole('SUPER_ADMIN') or #userId == authentication.principal.userId")
        public ResponseEntity<ApiResponse<UserDto>> getUser(
                        @PathVariable UUID userId) {
                UserDto user = userService.getUserProfile(userId);
                return ResponseEntity.ok(new ApiResponse<>(
                                true,
                                "User profile retrieved successfully",
                                user));
        }

        @Operation(summary = "Upload user profile avatar", description = "Uploads a user's profile avatar image file. Supports common image formats (JPEG, PNG, etc).")
        @SecurityRequirement(name = "bearerAuth")
        @PostMapping(value = "/{userId}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @PreAuthorize("hasRole('SUPER_ADMIN') or #userId == authentication.principal.userId")
        public ResponseEntity<ApiResponse<MediaDto>> uploadUserAvatar(
                        @Parameter(description = "ID of the user", required = true) @PathVariable UUID userId,
                        @RequestParam(value = "file", required = false) MultipartFile file) {
                MediaDto mediaDto = userService.uploadUserAvatar(userId, file);
                return ResponseEntity.ok(new ApiResponse<>(
                                true,
                                "User profile avatar uploaded successfully",
                                mediaDto));
        }

        @Operation(summary = "Get user avatar")
        @GetMapping("/{userId}/avatar")
        public ResponseEntity<Resource> getUserAvatar(
                        @PathVariable UUID userId) {
                return userService.getPublicAvatar(userId);
        }

        @Operation(summary = "Get logged in user avatar", description = "Streams the media file content. Returns the file for download or inline display depending on the media type.")
        @SecurityRequirement(name = "bearerAuth")
        @GetMapping("/avatar")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<Resource> getAvatar() {
                User user = userService.getLoggedInUser();
                return userService.getPublicAvatar(user.getUserId());
        }

        @Operation(summary = "Update user profile")
        @SecurityRequirement(name = "bearerAuth")
        @PutMapping("/{userId}")
        @PreAuthorize("hasRole('SUPER_ADMIN') or #userId == authentication.principal.userId")
        public ResponseEntity<ApiResponse<UserDto>> updateUser(
                        @PathVariable UUID userId,
                        @Valid @RequestBody UpdateUserRequest request) {
                UserDto updatedUser = userService.updateUserProfile(userId, request);
                return ResponseEntity.ok(new ApiResponse<>(
                                true,
                                "User profile updated successfully",
                                updatedUser));
        }
}