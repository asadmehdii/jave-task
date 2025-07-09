package com.biosteel.teams.team.mapper;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.biosteel.teams.common.exception.ResourceNotFoundException;
import com.biosteel.teams.contact.model.PlayerContact;
import com.biosteel.teams.contact.repository.ContactRepository;
import com.biosteel.teams.contact.repository.PlayerContactRepository;
import com.biosteel.teams.invitation.dto.InvitationResponseDTO;
import com.biosteel.teams.invitation.mapper.InvitationMapper;
import com.biosteel.teams.invitation.repository.InvitationRepository;
import com.biosteel.teams.player.repository.PlayerRepository;
import com.biosteel.teams.sport.mapper.AttributeValueMapper;
import com.biosteel.teams.sport.service.SportAttributeService;
import com.biosteel.teams.team.dto.ContactResponseDTO;
import com.biosteel.teams.team.dto.TeamDTO;
import com.biosteel.teams.team.dto.TeamMemberMinimalDTO;
import com.biosteel.teams.team.dto.TeamMemberRequestDTO;
import com.biosteel.teams.team.dto.TeamMemberResponseDTO;
import com.biosteel.teams.team.model.Team;
import com.biosteel.teams.team.model.TeamMember;
import com.biosteel.teams.team.model.TeamMemberAttributeValue;
import com.biosteel.teams.team.repository.TeamMemberRepository;
import com.biosteel.teams.team.repository.TeamRepository;
import com.biosteel.teams.team.service.TeamService;
import com.biosteel.teams.user.mapper.UserMapper;
import com.biosteel.teams.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TeamMemberMapper {

    private final PlayerRepository playerRepository;
    private final ContactRepository contactRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final SportAttributeService sportAttributeService;
    private final AttributeValueMapper attributeValueMapper;
    private final TeamMemberRepository teamMemberRepository;
    private final InvitationRepository invitationRepository;
    private final InvitationMapper invitationMapper;
    private final TeamRepository teamRepository;
    private final PlayerContactRepository playerContactRepository;
    private final TeamMapper teamMapper;

    public TeamMemberMinimalDTO toDTO(TeamMember member) {
        if (member == null) {
            return null;
        }

        TeamMemberMinimalDTO dto = new TeamMemberMinimalDTO();
        dto.setRole(member.getRole());
        dto.setJoinedAt(member.getJoinedAt());
        dto.setTeamMemberUuid(member.getId());
        dto.setTeamId(member.getTeamId());

        if (member.getPlayerId() != null) {
            // For player, set isPlayer to true and populate details from player
            dto.setIsPlayer(true);
            playerRepository.findById(member.getPlayerId())
                    .ifPresent(player -> {
                        dto.setFirstName(player.getFirstName());
                        dto.setLastName(player.getLastName());
                        dto.setLogoMediaId(player.getLogoMediaId());
                        dto.setPlayerId(player.getPlayerId());
                    });
        } else if (member.getUserId() != null) {
            // For user, set isPlayer to false and populate details from user
            dto.setIsPlayer(false);
            userRepository.findById(member.getUserId())
                    .ifPresent(user -> {
                        dto.setFirstName(user.getFirstName());
                        dto.setLastName(user.getLastName());
                        dto.setEmail(user.getEmail());
                        dto.setLogoMediaId(user.getProfilePhotoMediaId());
                        dto.setUserId(user.getUserId());
                    });
        }

        return dto;
    }

    public TeamMember toEntity(TeamMemberMinimalDTO dto) {
        if (dto == null) {
            return null;
        }

        TeamMember member = new TeamMember();
        member.setId(dto.getTeamMemberUuid());
        member.setTeamId(dto.getTeamId());
        member.setUserId(dto.getUserId());
        member.setPlayerId(dto.getPlayerId());
        member.setRole(dto.getRole());
        member.setJoinedAt(dto.getJoinedAt());
        return member;
    }

    public TeamMember toEntityForCreate(TeamMemberRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        TeamMember member = new TeamMember();
        member.setUserId(dto.getUserId());
        member.setPlayerId(dto.getPlayerId());
        member.setRole(dto.getRole());
        member.setJoinedAt(LocalDateTime.now());
        return member;
    }

    public List<TeamMemberMinimalDTO> toDTOList(List<TeamMember> members) {
        if (members == null) {
            return Collections.emptyList();
        }

        return members.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<TeamMember> toEntityList(List<TeamMemberMinimalDTO> dtos) {
        if (dtos == null) {
            return Collections.emptyList();
        }

        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    public TeamMemberResponseDTO toResponseDTO(TeamMember member, List<PlayerContact> playerContacts) {
        if (member == null) {
            return null;
        }

        Boolean isAdmin = TeamService.isAdminRole(member.getRole());
        TeamMemberResponseDTO.TeamMemberResponseDTOBuilder builder = TeamMemberResponseDTO.builder()
                .teamMemberUuid(member.getId())
                .teamId(member.getTeamId())
                .isAdmin(isAdmin)
                .role(member.getRole())
                .joinedAt(member.getJoinedAt())
                .leftAt(member.getLeftAt())
                .isPlayer(member.getIsPlayer())
                .isContact(member.getIsContact())
                .invitationId(member.getInvitationId());

        TeamDTO teamDTO = getTeamDTO(member.getTeamId());

        // Handle player information
        if (member.getIsPlayer() != null && member.getIsPlayer() && member.getPlayerId() != null) {
            populatePlayerInformation(builder, member.getPlayerId(), member.getId());

            // Add contact information for players
            if (playerContacts != null && !playerContacts.isEmpty()) {
                List<ContactResponseDTO> contactDTOs = playerContacts.stream()
                        .map(pc -> this.mapPlayerContactToDTO(pc, teamDTO))
                        .collect(Collectors.toList());
                builder.contacts(contactDTOs);
            }
        }

        // Handle user information (non-player users)
        if (member.getIsPlayer() != null && !member.getIsPlayer() && member.getUserId() != null) {
            populateUserInformation(builder, member.getUserId());
        }

        // Handle contact information (non-player contacts)
        if (member.getIsContact() != null && member.getIsContact() && member.getContactId() != null) {
            populateContactInformation(builder, member.getContactId());
        }

        // Handle invitation information
        if (member.getInvitationId() != null) {
            invitationRepository.findById(member.getInvitationId()).ifPresent(invitation -> {
                // Get team information for the invitation

                InvitationResponseDTO invitationDTO = invitationMapper.toDto(invitation, teamDTO);
                builder.invitation(invitationDTO);
            });
            builder.invitationId(member.getInvitationId());
        }

        return builder.build();
    }

    private void populatePlayerInformation(TeamMemberResponseDTO.TeamMemberResponseDTOBuilder builder,
            UUID playerId, UUID teamMemberId) {
        playerRepository.findById(playerId).ifPresent(player -> {
            builder
                    .playerId(player.getPlayerId())
                    .firstName(player.getFirstName())
                    .lastName(player.getLastName())
                    .email(player.getEmail())
                    .phone(player.getPhone())
                    .dateOfBirth(player.getDateOfBirth())
                    .gender(player.getGender())
                    .logoMediaId(player.getLogoMediaId())
                    .jerseyNumber(player.getJerseyNumber())
                    .primaryPosition(player.getPrimaryPosition())
                    .secondaryPosition(player.getSecondaryPosition());

            // Add parent user information if available
            if (player.getParentUser() != null) {
                builder.parentUser(userMapper.toDto(player.getParentUser()));
            }

            // Add player attributes
            List<TeamMemberAttributeValue> attributes = sportAttributeService
                    .getAllTeamMemberAttributeValues(teamMemberId);
            builder.playerAttributes(attributeValueMapper.toTeamMemberAttributeDTOList(attributes));
        });
    }

    private void populateUserInformation(TeamMemberResponseDTO.TeamMemberResponseDTOBuilder builder, UUID userId) {
        userRepository.findById(userId).ifPresent(user -> {
            builder
                    .userId(user.getUserId())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .dateOfBirth(user.getDateOfBirth())
                    .gender(user.getGender())
                    .logoMediaId(user.getProfilePhotoMediaId());
        });
    }

    private void populateContactInformation(TeamMemberResponseDTO.TeamMemberResponseDTOBuilder builder,
            UUID contactId) {
        contactRepository.findById(contactId).ifPresent(contact -> {
            builder
                    .contactId(contact.getContactId())
                    .firstName(contact.getFirstName())
                    .lastName(contact.getLastName())
                    .email(contact.getEmail())
                    .phone(contact.getPhone())
                    .dateOfBirth(contact.getDateOfBirth())
                    .gender(contact.getGender())
                    .logoMediaId(contact.getLogoMediaId())
                    .relationshipType(getRelationshipTypeForContact(contactId));
        });
    }

    private String getRelationshipTypeForContact(UUID contactId) {
        Optional<TeamMember> contactMember = teamMemberRepository.findByContactIdAndLeftAtIsNull(contactId)
                .stream()
                .findFirst();

        if (contactMember.isPresent()) {
            return playerContactRepository.findByContactMemberIdAndDeletedAtIsNull(contactMember.get().getId())
                    .stream()
                    .findFirst()
                    .map(PlayerContact::getRelationshipType)
                    .orElse(null);
        }

        return null;
    }

    private ContactResponseDTO mapPlayerContactToDTO(PlayerContact playerContact, TeamDTO teamDTO) {
        ContactResponseDTO.ContactResponseDTOBuilder builder = ContactResponseDTO
                .builder()
                .playerContactId(playerContact.getPlayerContactId())
                .contactMemberId(playerContact.getContactMemberId())
                .relationshipType(playerContact.getRelationshipType())
                .isPrimaryContact(playerContact.getIsPrimaryContact())
                .canPickup(playerContact.getCanPickup())
                .isEmergencyContact(playerContact.getIsEmergencyContact())
                .canViewMedicalInfo(playerContact.getCanViewMedicalInfo())
                .receivesNotifications(playerContact.getReceivesNotifications())
                .notes(playerContact.getNotes());

        // Get contact details from the contact member
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
                    invitationRepository.findById(contactMember.getInvitationId()).ifPresent(invitation -> {
                        InvitationResponseDTO invitationDTO = invitationMapper.toDto(invitation, teamDTO);
                        builder.invitationId(invitation.getInvitationId())
                                .invitation(invitationDTO);
                    });
                }
            });
        }

        return builder.build();
    }

    private TeamDTO getTeamDTO(UUID teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));
        return teamMapper.toDTO(team);
    }
}