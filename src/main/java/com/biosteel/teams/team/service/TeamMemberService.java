package com.biosteel.teams.team.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.biosteel.teams.chat.service.StreamChatService;
import com.biosteel.teams.common.exception.ResourceNotFoundException;
import com.biosteel.teams.common.exception.ValidationException;
import com.biosteel.teams.contact.model.Contact;
import com.biosteel.teams.contact.model.PlayerContact;
import com.biosteel.teams.contact.repository.ContactRepository;
import com.biosteel.teams.contact.repository.PlayerContactRepository;
import com.biosteel.teams.invitation.dto.InvitationRequestDTO;
import com.biosteel.teams.invitation.dto.InvitationResponseDTO;
import com.biosteel.teams.invitation.mapper.InvitationMapper;
import com.biosteel.teams.invitation.model.Invitation;
import com.biosteel.teams.invitation.repository.InvitationRepository;
import com.biosteel.teams.invitation.service.InvitationService;
import com.biosteel.teams.notification.dto.NotificationDTO;
import com.biosteel.teams.notification.dto.NotificationTarget;
import com.biosteel.teams.notification.service.NotificationService;
import com.biosteel.teams.player.model.Player;
import com.biosteel.teams.player.repository.PlayerRepository;
import com.biosteel.teams.sport.service.SportAttributeService;
import com.biosteel.teams.team.dto.ContactRequestDTO;
import com.biosteel.teams.team.dto.ContactResponseDTO;
import com.biosteel.teams.team.dto.TeamDTO;
import com.biosteel.teams.team.dto.TeamMemberRequestDTO;
import com.biosteel.teams.team.dto.TeamMemberResponseDTO;
import com.biosteel.teams.team.mapper.TeamMapper;
import com.biosteel.teams.team.mapper.TeamMemberMapper;
import com.biosteel.teams.team.model.Team;
import com.biosteel.teams.team.model.TeamMember;
import com.biosteel.teams.team.repository.TeamMemberRepository;
import com.biosteel.teams.team.repository.TeamRepository;
import com.biosteel.teams.user.model.User;
import com.biosteel.teams.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TeamMemberService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final PlayerRepository playerRepository;
    private final ContactRepository contactRepository;
    private final PlayerContactRepository playerContactRepository;
    private final UserRepository userRepository;
    private final SportAttributeService sportAttributeService;
    private final InvitationService invitationService;
    private final TeamMemberMapper teamMemberMapper;
    private final StreamChatService streamChatService;
    private final NotificationService notificationService;
    private final InvitationRepository invitationRepository;
    private final InvitationMapper invitationMapper;
    private final TeamMapper teamMapper;

    @Transactional
    public TeamMemberResponseDTO addTeamMember(UUID userId, UUID teamId, TeamMemberRequestDTO request) {
        // Validate team access
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));

        // Validate request
        validateTeamMemberRequest(request);

        TeamMember savedMember;
        List<PlayerContact> playerContacts = new ArrayList<>();

        if (request.getIsPlayer() != null && request.getIsPlayer()) {
            savedMember = handlePlayerMemberCreation(teamId, request, team);
            if (request.getContacts() != null && !request.getContacts().isEmpty()) {
                playerContacts = handlePlayerContacts(savedMember.getId(), request.getContacts(), teamId, team);
            }
        } else {
            savedMember = handleNonPlayerMemberCreation(teamId, request, team);
        }

        // Handle invitation if requested
        Invitation invitation = null;
        UUID invitationId = null;
        if (request.getInviteToJoinApp() && request.getEmail() != null) {
            invitation = createInvitation(userId, teamId, request, savedMember.getRole());
            invitationId = invitation.getInvitationId();
            savedMember.setInvitationId(invitation.getInvitationId());
            savedMember = teamMemberRepository.save(savedMember);
        }

        // Send notification if the user is found with the email address
        final UUID finalInvitationId = invitationId;
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            userRepository.findByEmailIgnoreCase(request.getEmail())
                    .ifPresent(user -> {
                        sendAddTeamMemberNotification(team, user.getUserId(),
                                team.getUserId(), finalInvitationId);
                    });

        }

        // Build and return response
        return teamMemberMapper.toResponseDTO(savedMember, playerContacts);
    }

    private TeamMember handlePlayerMemberCreation(UUID teamId, TeamMemberRequestDTO request, Team team) {
        Player player;

        if (request.getPlayerId() != null) {
            // Adding existing player
            player = playerRepository.findById(request.getPlayerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Player not found"));
        } else {
            // Creating new player
            player = createNewPlayer(request, team.getSportType().getCode());
        }

        // Create team member record
        TeamMember member = TeamMember.builder()
                .teamId(teamId)
                .playerId(player.getPlayerId())
                .role("PLAYER")
                .isPlayer(true)
                .isContact(false)
                .joinedAt(LocalDateTime.now())
                .userId(getUserIdByEmail(request.getEmail()))
                .build();

        member = teamMemberRepository.save(member);

        // Save player attributes if provided
        if (request.getPlayerAttributes() != null && !request.getPlayerAttributes().isEmpty()) {
            sportAttributeService.updateTeamMemberAttributes(member.getId(), request.getPlayerAttributes());
        }

        return member;
    }

    private TeamMember handleNonPlayerMemberCreation(UUID teamId, TeamMemberRequestDTO request, Team team) {
        TeamMember member;
        if (request.getUserId() != null) {
            // Adding existing user
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            member = TeamMember.builder()
                    .teamId(teamId)
                    .userId(user.getUserId())
                    .role(resolveNonPlayerRole(request))
                    .isPlayer(false)
                    .isContact(false)
                    .joinedAt(LocalDateTime.now())
                    .build();
        } else {
            // Creating contact for non-player (coach, manager, etc.)
            Contact contact = createContactFromRequest(request);

            member = TeamMember.builder()
                    .teamId(teamId)
                    .contactId(contact.getContactId())
                    .role(resolveNonPlayerRole(request))
                    .isPlayer(false)
                    .isContact(true)
                    .joinedAt(LocalDateTime.now())
                    .userId(getUserIdByEmail(request.getEmail()))
                    .build();
        }

        member = teamMemberRepository.save(member);
        return member;
    }

    private UUID getUserIdByEmail(String email) {
        UUID userId = null;
        if (email != null && !email.isEmpty()) {
            Optional<User> userOpt = userRepository.findByEmailIgnoreCase(email);
            if (userOpt.isPresent()) {
                userId = userOpt.get().getUserId();
            }
        }
        return userId;
    }

    private Player createNewPlayer(TeamMemberRequestDTO request, String sportTypeCode) {
        Player player = Player.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .logoMediaId(request.getLogoMediaId())
                .build();

        return playerRepository.save(player);
    }

    private Contact createContactFromRequest(TeamMemberRequestDTO request) {
        Contact contact = Contact.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .logoMediaId(request.getLogoMediaId())
                .build();

        return contactRepository.save(contact);
    }

    private List<PlayerContact> handlePlayerContacts(UUID playerMemberId,
            List<ContactRequestDTO> contactRequests,
            UUID teamId, Team team) {

        List<PlayerContact> playerContacts = new ArrayList<>();

        for (ContactRequestDTO contactRequest : contactRequests) {
            Contact contact;

            if (contactRequest.getContactId() != null) {
                // Use existing contact
                contact = contactRepository.findById(contactRequest.getContactId())
                        .orElseThrow(() -> new ResourceNotFoundException("Contact not found"));
            } else {
                // Create new contact
                contact = Contact.builder()
                        .firstName(contactRequest.getFirstName())
                        .lastName(contactRequest.getLastName())
                        .email(contactRequest.getEmail())
                        .phone(contactRequest.getPhone())
                        .build();
                contact = contactRepository.save(contact);
            }

            // Create contact as team member if they should receive invitations
            TeamMember contactMember = TeamMember.builder()
                    .teamId(teamId)
                    .contactId(contact.getContactId())
                    .role("PLAYER_CONTACT")
                    .isPlayer(false)
                    .isContact(true)
                    .joinedAt(null)
                    .userId(getUserIdByEmail(contact.getEmail()))
                    .build();
            contactMember = teamMemberRepository.save(contactMember);

            // Create player-contact relationship
            PlayerContact playerContact = PlayerContact.builder()
                    .playerMemberId(playerMemberId)
                    .contactMemberId(contactMember.getId())
                    .relationshipType(contactRequest.getRelationshipType())
                    .isPrimaryContact(contactRequest.getIsPrimaryContact())
                    .canPickup(contactRequest.getCanPickup())
                    .isEmergencyContact(contactRequest.getIsEmergencyContact())
                    .canViewMedicalInfo(contactRequest.getCanViewMedicalInfo())
                    .receivesNotifications(contactRequest.getReceivesNotifications())
                    .notes(contactRequest.getNotes())
                    .build();

            playerContact = playerContactRepository.save(playerContact);
            playerContacts.add(playerContact);

            // Send invitation to contact if they have email
            if (contact.getEmail() != null && !contact.getEmail().isEmpty()) {
                try {
                    Invitation contactInvitation = createContactInvitation(teamId, contact, team);
                    contactMember.setInvitationId(contactInvitation.getInvitationId());
                    teamMemberRepository.save(contactMember);

                    // Send notification if the user is found with the email address
                    userRepository.findByEmailIgnoreCase(contact.getEmail())
                            .ifPresent(user -> {
                                sendAddTeamMemberNotification(team, user.getUserId(),
                                        team.getUserId(), contactInvitation.getInvitationId());
                            });

                } catch (Exception e) {
                    log.warn("Failed to create invitation for contact {}: {}", contact.getEmail(), e.getMessage());
                }
            }
        }

        return playerContacts;
    }

    private Invitation createInvitation(UUID requestingUserId, UUID teamId,
            TeamMemberRequestDTO request, String role) {
        InvitationRequestDTO invitationRequest = InvitationRequestDTO.builder()
                .teamId(teamId)
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .teamRole(role)
                .build();

        return invitationService.createInvitation(requestingUserId, invitationRequest)
                .getInvitation();
    }

    private Invitation createContactInvitation(UUID teamId, Contact contact, Team team) {
        InvitationRequestDTO invitationRequest = InvitationRequestDTO.builder()
                .teamId(teamId)
                .email(contact.getEmail())
                .firstName(contact.getFirstName())
                .lastName(contact.getLastName())
                .phone(contact.getPhone())
                .teamRole("PLAYER_CONTACT")
                .build();

        return invitationService.createInvitation(team.getUserId(), invitationRequest)
                .getInvitation();
    }

    private void validateTeamMemberRequest(TeamMemberRequestDTO request) {
        if (request.getInviteToJoinApp() && (request.getEmail() == null || request.getEmail().trim().isEmpty())) {
            throw new ValidationException("Email is required when inviting to join app");
        }

    }

    private String resolveNonPlayerRole(TeamMemberRequestDTO request) {
        if (request.getRole() == null || request.getRole().trim().isEmpty()) {
            return "COACH";
        }
        return request.getRole();
    }

    @Transactional(readOnly = true)
    public TeamMemberResponseDTO getTeamMember(UUID teamMemberId) {
        TeamMember member = teamMemberRepository.findById(teamMemberId)
                .orElseThrow(() -> new ResourceNotFoundException("Team member not found"));

        List<PlayerContact> playerContacts = new ArrayList<>();
        if (member.getIsPlayer() && member.getPlayerId() != null) {
            playerContacts = playerContactRepository.findByPlayerMemberIdAndDeletedAtIsNull(member.getId());
        }

        return teamMemberMapper.toResponseDTO(member, playerContacts);
    }

    @Transactional(readOnly = true)
    public List<TeamMemberResponseDTO> getTeamMembers(UUID userId, UUID teamId) {
        // Validate team access
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));

        // Get all team members
        List<TeamMember> members = teamMemberRepository.findByTeamIdAndLeftAtIsNull(teamId);

        if (members.isEmpty()) {
            log.warn("No active members found for team: {}", teamId);
            return new ArrayList<>();
        }

        return mapTeamMembersToResponseDTOs(members);
    }

    @Transactional(readOnly = true)
    public Optional<TeamMember> getTeamMemberByInvitation(UUID invitationId) {
        List<TeamMember> members = teamMemberRepository.findByInvitationIdAndLeftAtIsNull(invitationId);
        if (members.size() > 1) {
            log.warn("Multiple team members found for invitation: {}", invitationId);
        }
        return members.isEmpty() ? Optional.empty() : Optional.of(members.get(0));
    }

    private List<TeamMemberResponseDTO> mapTeamMembersToResponseDTOs(List<TeamMember> members) {
        return members.stream()
                .map(member -> {
                    List<PlayerContact> playerContacts = new ArrayList<>();
                    if (member.getIsPlayer() != null && member.getIsPlayer() && member.getPlayerId() != null) {
                        playerContacts = playerContactRepository.findByPlayerMemberIdAndDeletedAtIsNull(member.getId());
                    }
                    return teamMemberMapper.toResponseDTO(member, playerContacts);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public TeamMemberResponseDTO acceptInvitation(UUID userId, Invitation invitation) {
        Optional<TeamMember> memberOpt = getTeamMemberByInvitation(invitation.getInvitationId());

        if (memberOpt.isEmpty()) {
            throw new ValidationException("Invalid invitation - no team member found");
        }

        TeamMember member = memberOpt.get();
        member.setUserId(userId);
        member.setJoinedAt(LocalDateTime.now());
        teamMemberRepository.save(member);

        if (member.getUserId() != null) {
            User memberUser = userRepository.findById(member.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            UUID teamId = member.getTeamId();

            Team team = teamRepository.findByIdAndDeletedAtIsNull(teamId)
                    .orElseThrow(() -> new ResourceNotFoundException("Team not found"));
            streamChatService.addMemberToChannel(
                    teamId,
                    member.getUserId(),
                    memberUser.getUserName());
            sendAddTeamMemberNotification(team, member.getUserId(),
                    team.getUserId(), invitation.getInvitationId());
        }

        List<PlayerContact> playerContacts = new ArrayList<>();
        if (member.getIsPlayer() != null && member.getIsPlayer() && member.getPlayerId() != null) {
            playerContacts = playerContactRepository.findByPlayerMemberIdAndDeletedAtIsNull(member.getId());
        }
        return teamMemberMapper.toResponseDTO(member, playerContacts);
    }

    @Transactional
    public TeamMemberResponseDTO updateTeamMember(UUID userId, UUID teamId, UUID teamMemberId,
            TeamMemberRequestDTO request) {
        // Validate team access
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));

        TeamMember member = teamMemberRepository.findByIdAndTeamId(teamMemberId, teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team member not found"));

        // Update basic member information
        member.setRole(resolveNonPlayerRole(request));

        // Update player-specific information
        if (member.getIsPlayer() && member.getPlayerId() != null) {
            updatePlayerInformation(member.getPlayerId(), request);

            // Update player attributes
            if (request.getPlayerAttributes() != null) {
                sportAttributeService.updateTeamMemberAttributes(member.getId(), request.getPlayerAttributes());
            }

            // Update contacts
            if (request.getContacts() != null) {
                updatePlayerContacts(member.getId(), request.getContacts());
            }
        }

        // Update contact information for non-players
        if (member.getIsContact() && member.getContactId() != null) {
            updateContactInformation(member.getContactId(), request);
        }

        member = teamMemberRepository.save(member);
        member = teamMemberRepository.findById(member.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Team member not found"));

        List<PlayerContact> playerContacts = new ArrayList<>();
        if (member.getIsPlayer()) {
            playerContacts = playerContactRepository.findByPlayerMemberIdAndDeletedAtIsNull(member.getId());
        }

        return teamMemberMapper.toResponseDTO(member, playerContacts);
    }

    private void updatePlayerInformation(UUID playerId, TeamMemberRequestDTO request) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found"));

        player.setFirstName(request.getFirstName());
        player.setLastName(request.getLastName());
        player.setEmail(request.getEmail());
        player.setPhone(request.getPhone());
        player.setDateOfBirth(request.getDateOfBirth());
        player.setGender(request.getGender());
        player.setLogoMediaId(request.getLogoMediaId());

        playerRepository.save(player);
    }

    private void updateContactInformation(UUID contactId, TeamMemberRequestDTO request) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found"));

        contact.setFirstName(request.getFirstName());
        contact.setLastName(request.getLastName());
        contact.setEmail(request.getEmail());
        contact.setPhone(request.getPhone());
        contact.setDateOfBirth(request.getDateOfBirth());
        contact.setGender(request.getGender());
        contact.setLogoMediaId(request.getLogoMediaId());

        contactRepository.save(contact);
    }

    private void updatePlayerContacts(UUID playerMemberId,
            List<ContactRequestDTO> contactRequests) {
        // This is a simplified update - in production, you'd want more sophisticated
        // handling
        // of adding/removing/updating contacts
        List<PlayerContact> existingContacts = playerContactRepository
                .findByPlayerMemberIdAndDeletedAtIsNull(playerMemberId);

        // For now, we'll just update the relationship properties of existing contacts
        // A full implementation would handle adding new contacts and removing old ones
        for (PlayerContact existingContact : existingContacts) {
            contactRequests.stream()
                    .filter(req -> req.getContactId() != null)
                    .filter(req -> {
                        // Find matching contact by ID or email
                        return req.getContactId().equals(existingContact.getContactMemberId());
                    })
                    .findFirst()
                    .ifPresent(req -> {
                        existingContact.setRelationshipType(req.getRelationshipType());
                        existingContact.setIsPrimaryContact(req.getIsPrimaryContact());
                        existingContact.setCanPickup(req.getCanPickup());
                        existingContact.setIsEmergencyContact(req.getIsEmergencyContact());
                        existingContact.setCanViewMedicalInfo(req.getCanViewMedicalInfo());
                        existingContact.setReceivesNotifications(req.getReceivesNotifications());
                        existingContact.setNotes(req.getNotes());
                        playerContactRepository.save(existingContact);
                    });
        }
    }

    @Transactional
    public void removeTeamMember(UUID userId, UUID teamId, UUID teamMemberId) {
        TeamMember member = teamMemberRepository.findByIdAndTeamId(teamMemberId, teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team member not found"));

        // Remove player-contact relationships if this is a player
        if (member.getIsPlayer()) {
            List<PlayerContact> playerContacts = playerContactRepository
                    .findByPlayerMemberIdAndDeletedAtIsNull(member.getId());
            playerContactRepository.deleteAll(playerContacts);
        }

        // Remove team member
        teamMemberRepository.delete(member);

        if (member.getUserId() != null) {
            streamChatService.removeMemberFromChannel(teamId, member.getUserId());
        }
    }

    public void sendAddTeamMemberNotification(Team team, UUID addedUserId, UUID addedByUserId, UUID invitationId) {
        User user = userRepository.findById(addedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found for: " + addedByUserId));
        String addedByName = user.getUserName();

        Map<String, String> data = new HashMap<>();
        data.put("action", "user_added_to_team");
        data.put("teamId", team.getId().toString());
        data.put("invitationId", invitationId != null ? invitationId.toString() : "");

        NotificationTarget target = new NotificationTarget();
        target.setTargetType(NotificationTarget.TargetType.USER);
        target.setUserId(addedUserId);

        NotificationDTO notification = new NotificationDTO();
        notification.setTitle(team.getName());
        notification.setBody(addedByName + " added you to the team. Click to accept the invite.");
        notification.setData(data);
        notification.setTarget(target);

        notificationService.sendNotification(notification);
    }

    @Transactional(readOnly = true)
    public List<ContactResponseDTO> getMemberContacts(UUID userId, UUID teamId, UUID memberId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));

        TeamMember member = teamMemberRepository.findByIdAndTeamId(memberId, teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team member not found"));

        if (!Boolean.TRUE.equals(member.getIsPlayer())) {
            throw new ValidationException("Only player members can have contacts");
        }

        List<PlayerContact> playerContacts = playerContactRepository.findByPlayerMemberIdAndDeletedAtIsNull(memberId);

        return playerContacts.stream()
                .map(this::mapPlayerContactToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ContactResponseDTO addMemberContact(UUID userId, UUID teamId, UUID memberId,
            ContactRequestDTO contactRequest) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));

        TeamMember member = teamMemberRepository.findByIdAndTeamId(memberId, teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team member not found"));

        if (!Boolean.TRUE.equals(member.getIsPlayer())) {
            throw new ValidationException("Only player members can have contacts");
        }

        Contact contact;
        if (contactRequest.getContactId() != null) {
            contact = contactRepository.findById(contactRequest.getContactId())
                    .orElseThrow(() -> new ResourceNotFoundException("Contact not found"));

            updateContactFromRequest(contact, contactRequest);
            contact = contactRepository.save(contact);
        } else {
            contact = createContactFromRequest(contactRequest);
        }

        TeamMember contactMember = TeamMember.builder()
                .teamId(teamId)
                .contactId(contact.getContactId())
                .role("PLAYER_CONTACT")
                .isPlayer(false)
                .isContact(true)
                .joinedAt(null)
                .userId(getUserIdByEmail(contact.getEmail()))
                .build();
        contactMember = teamMemberRepository.save(contactMember);

        PlayerContact playerContact = PlayerContact.builder()
                .playerMemberId(memberId)
                .contactMemberId(contactMember.getId())
                .relationshipType(contactRequest.getRelationshipType())
                .isPrimaryContact(contactRequest.getIsPrimaryContact())
                .canPickup(contactRequest.getCanPickup())
                .isEmergencyContact(contactRequest.getIsEmergencyContact())
                .canViewMedicalInfo(contactRequest.getCanViewMedicalInfo())
                .receivesNotifications(contactRequest.getReceivesNotifications())
                .notes(contactRequest.getNotes())
                .build();

        playerContact = playerContactRepository.save(playerContact);

        if (contact.getEmail() != null && !contact.getEmail().isEmpty()) {
            try {
                Invitation contactInvitation = createContactInvitation(teamId, contact, team);
                contactMember.setInvitationId(contactInvitation.getInvitationId());
                teamMemberRepository.save(contactMember);

                userRepository.findByEmailIgnoreCase(contact.getEmail())
                        .ifPresent(user -> {
                            sendAddTeamMemberNotification(team, user.getUserId(), team.getUserId(),
                                    contactInvitation.getInvitationId());
                        });

            } catch (Exception e) {
                log.warn("Failed to create invitation for contact {}: {}", contact.getEmail(), e.getMessage());
            }
        }

        return mapPlayerContactToResponseDTO(playerContact);
    }

    @Transactional
    public ContactResponseDTO updateMemberContact(UUID userId, UUID teamId, UUID memberId, UUID contactId,
            ContactRequestDTO contactRequest) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));

        TeamMember member = teamMemberRepository.findByIdAndTeamId(memberId, teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team member not found"));

        if (!Boolean.TRUE.equals(member.getIsPlayer())) {
            throw new ValidationException("Only player members can have contacts");
        }

        PlayerContact playerContact = playerContactRepository.findByPlayerMemberIdAndDeletedAtIsNull(memberId)
                .stream()
                .filter(pc -> {
                    // Find the contact member and check if it matches the contactId
                    return teamMemberRepository.findById(pc.getContactMemberId())
                            .map(TeamMember::getContactId)
                            .filter(cId -> cId.equals(contactId))
                            .isPresent();
                })
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found for this team member"));

        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found"));

        updateContactFromRequest(contact, contactRequest);
        contact = contactRepository.save(contact);

        playerContact.setRelationshipType(contactRequest.getRelationshipType());
        playerContact.setIsPrimaryContact(contactRequest.getIsPrimaryContact());
        playerContact.setCanPickup(contactRequest.getCanPickup());
        playerContact.setIsEmergencyContact(contactRequest.getIsEmergencyContact());
        playerContact.setCanViewMedicalInfo(contactRequest.getCanViewMedicalInfo());
        playerContact.setReceivesNotifications(contactRequest.getReceivesNotifications());
        playerContact.setNotes(contactRequest.getNotes());

        playerContact = playerContactRepository.save(playerContact);

        return mapPlayerContactToResponseDTO(playerContact);
    }

    @Transactional
    public void removeMemberContact(UUID userId, UUID teamId, UUID memberId, UUID contactId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));

        TeamMember member = teamMemberRepository.findByIdAndTeamId(memberId, teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team member not found"));

        if (!Boolean.TRUE.equals(member.getIsPlayer())) {
            throw new ValidationException("Only player members can have contacts");
        }

        PlayerContact playerContact = playerContactRepository.findByPlayerMemberIdAndDeletedAtIsNull(memberId)
                .stream()
                .filter(pc -> {
                    return teamMemberRepository.findById(pc.getContactMemberId())
                            .map(TeamMember::getContactId)
                            .filter(cId -> cId.equals(contactId))
                            .isPresent();
                })
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found for this team member"));

        playerContactRepository.delete(playerContact);

        TeamMember contactMember = teamMemberRepository.findById(playerContact.getContactMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Contact member not found"));

        teamMemberRepository.delete(contactMember);
    }

    private ContactResponseDTO mapPlayerContactToResponseDTO(PlayerContact playerContact) {
        ContactResponseDTO.ContactResponseDTOBuilder builder = ContactResponseDTO.builder()
                .playerContactId(playerContact.getPlayerContactId())
                .contactMemberId(playerContact.getContactMemberId())
                .relationshipType(playerContact.getRelationshipType())
                .isPrimaryContact(playerContact.getIsPrimaryContact())
                .canPickup(playerContact.getCanPickup())
                .isEmergencyContact(playerContact.getIsEmergencyContact())
                .canViewMedicalInfo(playerContact.getCanViewMedicalInfo())
                .receivesNotifications(playerContact.getReceivesNotifications())
                .notes(playerContact.getNotes());

        if (playerContact.getContactMemberId() != null) {
            teamMemberRepository.findById(playerContact.getContactMemberId()).ifPresent(contactMember -> {
                if (contactMember.getContactId() != null) {
                    contactRepository.findById(contactMember.getContactId()).ifPresent(contact -> {
                        builder
                                .contactId(contact.getContactId())
                                .firstName(contact.getFirstName())
                                .lastName(contact.getLastName())
                                .email(contact.getEmail())
                                .phone(contact.getPhone())
                                .logoMediaId(contact.getLogoMediaId());
                    });
                }

                if (contactMember.getInvitationId() != null) {
                    Team team = teamRepository.findById(contactMember.getTeamId())
                            .orElseThrow(() -> new ResourceNotFoundException("Team not found"));

                    invitationRepository.findById(contactMember.getInvitationId()).ifPresent(invitation -> {
                        TeamDTO teamDTO = teamMapper.toDTO(team);
                        InvitationResponseDTO invitationDTO = invitationMapper.toDto(invitation, teamDTO);
                        builder.invitationId(invitation.getInvitationId())
                                .invitation(invitationDTO);
                    });
                }
            });
        }

        return builder.build();
    }

    private void updateContactFromRequest(Contact contact, ContactRequestDTO request) {
        if (request.getFirstName() != null) {
            contact.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            contact.setLastName(request.getLastName());
        }
        if (request.getEmail() != null) {
            contact.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            contact.setPhone(request.getPhone());
        }
        if (request.getLogoMediaId() != null) {
            contact.setLogoMediaId(request.getLogoMediaId());
        }
    }

    private Contact createContactFromRequest(ContactRequestDTO request) {
        Contact contact = Contact.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .logoMediaId(request.getLogoMediaId())
                .build();

        return contactRepository.save(contact);
    }

    @Transactional(readOnly = true)
    public List<TeamMemberResponseDTO> filterTeamMembers(UUID userId, UUID teamId, String memberRole, String currentUserRole) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));

        List<TeamMember> members;
        if ("PARENT".equalsIgnoreCase(currentUserRole) || "PARENT_CONTACT".equalsIgnoreCase(currentUserRole)) {
            members = teamMemberRepository.findByTeamIdAndRoleAndLeftAtIsNull(teamId, "PLAYER");
        } else if (memberRole != null && !memberRole.isEmpty()) {
            members = teamMemberRepository.findByTeamIdAndRoleAndLeftAtIsNull(teamId, memberRole);
        } else {
            members = teamMemberRepository.findByTeamIdAndLeftAtIsNull(teamId);
        }
        return mapTeamMembersToResponseDTOs(members);
    }
}