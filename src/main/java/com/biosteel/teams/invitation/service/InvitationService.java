package com.biosteel.teams.invitation.service;

import java.security.SecureRandom;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.biosteel.teams.common.exception.ResourceNotFoundException;
import com.biosteel.teams.common.exception.ValidationException;
import com.biosteel.teams.common.service.EmailService;
import com.biosteel.teams.common.service.SmsService;
import com.biosteel.teams.invitation.dto.BulkInvitationRequestDTO;
import com.biosteel.teams.invitation.dto.InvitationRecipientDTO;
import com.biosteel.teams.invitation.dto.InvitationRequestDTO;
import com.biosteel.teams.invitation.dto.InvitationResponseDTO;
import com.biosteel.teams.invitation.mapper.InvitationMapper;
import com.biosteel.teams.invitation.model.Invitation;
import com.biosteel.teams.invitation.model.Invitation.InvitationStatus;
import com.biosteel.teams.invitation.repository.InvitationRepository;
import com.biosteel.teams.team.dto.TeamDTO;
import com.biosteel.teams.team.mapper.TeamMapper;
import com.biosteel.teams.team.model.Team;
import com.biosteel.teams.team.service.TeamService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final InvitationMapper invitationMapper;
    private final TeamService teamService;
    private final TeamMapper teamMapper;
    private final EmailService emailService;
    private final SmsService smsService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${invitation.expiration.hours:48}")
    private int invitationExpirationHours;

    @Transactional
    public InvitationResponseDTO createInvitation(UUID requestingUserId, InvitationRequestDTO request) {
        Team team = teamService.validateTeamAccess(requestingUserId, request.getTeamId());

        TeamDTO teamDTO = teamMapper.toDTO(team);

        Optional<Invitation> existingInvitation = invitationRepository
                .findActiveInvitationForEmailAndTeam(team.getId(), request.getEmail(),
                        InvitationStatus.PENDING.toString());

        if (existingInvitation.isPresent()) {
            return invitationMapper.toDto(existingInvitation.get(), teamDTO);
        }

        Invitation invitation = invitationMapper.toEntity(request);
        invitation.setInvitationCode(generateInvitationCode());
        invitation.setExpiresAt(ZonedDateTime.now().plusHours(invitationExpirationHours));

        Invitation savedInvitation = invitationRepository.save(invitation);

        sendInvitationEmail(savedInvitation, team.getName(), request.getPhone());

        return invitationMapper.toDto(savedInvitation, teamDTO);
    }

    @Transactional
    public List<InvitationResponseDTO> createBulkInvitations(UUID requestingUserId, BulkInvitationRequestDTO request) {
        Team team = teamService.validateTeamAccess(requestingUserId, request.getTeamId());

        List<Invitation> invitationsToSave = new ArrayList<>();
        List<InvitationResponseDTO> results = new ArrayList<>();

        TeamDTO teamDTO = teamMapper.toDTO(team);

        for (InvitationRecipientDTO recipient : request.getRecipients()) {
            Optional<Invitation> existingInvitation = invitationRepository
                    .findActiveInvitationForEmailAndTeam(team.getId(), recipient.getEmail(),
                            InvitationStatus.PENDING.toString());

            if (existingInvitation.isPresent()) {
                results.add(invitationMapper.toDto(existingInvitation.get(), teamDTO));
                continue;
            }

            Invitation invitation = Invitation.builder()
                    .invitationId(UUID.randomUUID())
                    .teamId(request.getTeamId())
                    .eventId(request.getEventId())
                    .email(recipient.getEmail())
                    .firstName(recipient.getFirstName())
                    .lastName(recipient.getLastName())
                    .invitationCode(generateInvitationCode())
                    .invitationStatus(InvitationStatus.PENDING)
                    .teamRole(recipient.getTeamRole())
                    .expiresAt(ZonedDateTime.now().plusHours(invitationExpirationHours))
                    .createdAt(ZonedDateTime.now())
                    .build();

            invitationsToSave.add(invitation);
        }

        if (!invitationsToSave.isEmpty()) {
            List<Invitation> savedInvitations = invitationRepository.saveAll(invitationsToSave);

            for (Invitation invitation : savedInvitations) {
                sendInvitationEmail(invitation, team.getName(), null); // FIXME: Bulk is not sending SMS

                results.add(invitationMapper.toDto(invitation, teamDTO));
            }
        }

        return results;
    }

    @Transactional(readOnly = true)
    public List<InvitationResponseDTO> getTeamInvitations(UUID requestingUserId, UUID teamId) {
        Team team = teamService.validateTeamAccess(requestingUserId, teamId);
        teamService.validateTeamAccess(requestingUserId, teamId);
        TeamDTO teamDTO = teamMapper.toDTO(team);
        List<Invitation> invitations = invitationRepository.findByTeamId(teamId);
        return invitationMapper.toDtoList(invitations, teamDTO);
    }

    @Transactional
    public Invitation verifyInvitation(String invitationCode) {
        Invitation invitation = invitationRepository.findByInvitationCode(invitationCode)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid invitation code"));

        if (!invitation.isValid()) {
            if (invitation.isExpired()) {
                throw new ValidationException("Invitation has expired");
            } else if (invitation.isUsed()) {
                throw new ValidationException("Invitation has already been used");
            } else {
                throw new ValidationException("Invalid invitation status");
            }
        }

        return invitation;
    }

    @Transactional
    public InvitationResponseDTO acceptInvitation(UUID userId, Invitation invitation) {
        if (invitation == null) {
            throw new ValidationException("Invitation not found!");
        }
        invitation.setInvitationStatus(InvitationStatus.ACCEPTED);
        invitation.setUsedAt(ZonedDateTime.now());
        invitation.setUserId(userId);
        invitation.setUpdatedAt(ZonedDateTime.now());
        Invitation savedInvitation = invitationRepository.save(invitation);
        TeamDTO team = teamService.getTeam(invitation.getTeamId());
        return invitationMapper.toDto(savedInvitation, team);
    }

    @Transactional
    public void cancelInvitation(UUID requestingUserId, UUID invitationId) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

        teamService.validateTeamAccess(requestingUserId, invitation.getTeamId());

        if (invitation.isUsed()) {
            throw new ValidationException("Cannot cancel an invitation that has already been used");
        }

        invitation.setInvitationStatus(InvitationStatus.CANCELLED);
        invitation.setUpdatedAt(ZonedDateTime.now());
        invitationRepository.save(invitation);
    }

    @Transactional(readOnly = true)
    public List<InvitationResponseDTO> getUserPendingInvitations(String email) {
        List<Invitation> invitations = invitationRepository.findByEmailAndInvitationStatus(email,
                InvitationStatus.PENDING);

        List<InvitationResponseDTO> results = new ArrayList<>();

        for (Invitation invitation : invitations) {
            TeamDTO teamDTO = teamService.getTeam(invitation.getTeamId());
            results.add(invitationMapper.toDto(invitation, teamDTO));
        }

        return results;
    }

    private String generateInvitationCode() {
        int code;
        String codeStr;
        boolean isUnique = false;

        do {
            code = 100000 + secureRandom.nextInt(900000);
            codeStr = String.valueOf(code);
            isUnique = invitationRepository.findByInvitationCode(codeStr).isEmpty();
        } while (!isUnique);

        return codeStr;
    }

    private void sendInvitationEmail(Invitation invitation, String teamName, String phone) {
        emailService.sendInvitationEmail(
                invitation.getEmail(),
                invitation.getFirstName(),
                teamName,
                invitation.getInvitationCode(),
                invitationExpirationHours);

        // Send SMS if phone number is available
        if (phone != null && !phone.trim().isEmpty()) {
            smsService.sendInvitationSms(phone, invitation.getInvitationCode(), teamName);
        }
    }

}