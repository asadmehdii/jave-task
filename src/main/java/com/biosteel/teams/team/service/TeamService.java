package com.biosteel.teams.team.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.biosteel.teams.chat.service.StreamChatService;
import com.biosteel.teams.common.exception.ForbiddenException;
import com.biosteel.teams.common.exception.ResourceNotFoundException;
import com.biosteel.teams.contact.model.Contact;
import com.biosteel.teams.contact.repository.ContactRepository;
import com.biosteel.teams.media.service.MediaService;
import com.biosteel.teams.player.model.Player;
import com.biosteel.teams.player.repository.PlayerRepository;
import com.biosteel.teams.sport.mapper.AttributeValueMapper;
import com.biosteel.teams.sport.model.SportType;
import com.biosteel.teams.sport.repository.SportTypeRepository;
import com.biosteel.teams.sport.service.SportAttributeService;
import com.biosteel.teams.team.dto.TeamDTO;
import com.biosteel.teams.team.dto.TeamFeedDTO;
import com.biosteel.teams.team.dto.TeamLiveDTO;
import com.biosteel.teams.team.dto.TeamMemberMinimalDTO;
import com.biosteel.teams.team.dto.TeamMemberRequestDTO;
import com.biosteel.teams.team.dto.TeamMinimalDTO;
import com.biosteel.teams.team.mapper.TeamMapper;
import com.biosteel.teams.team.model.Team;
import com.biosteel.teams.team.model.TeamAttributeValue;
import com.biosteel.teams.team.model.TeamMember;
import com.biosteel.teams.team.repository.TeamMemberRepository;
import com.biosteel.teams.team.repository.TeamRepository;
import com.biosteel.teams.user.model.User;
import com.biosteel.teams.user.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TeamService {
        private static final String ROLE_ADMIN = "ADMIN";
        private static final String ROLE_COACH = "COACH";
        private static final String ROLE_MANAGER = "MANAGER";
        private final TeamRepository teamRepository;
        private final TeamMemberRepository teamMemberRepository;
        private final TeamMapper teamMapper;
        private final PlayerRepository playerRepository;
        private final SportTypeRepository sportTypeRepository;
        private final SportAttributeService sportAttributeService;
        private final AttributeValueMapper attributeValueMapper;
        private final StreamChatService streamChatService;
        private final UserRepository userRepository;
        private final ContactRepository contactRepository;
        private final MediaService mediaService;

        public TeamDTO createTeam(UUID userId, TeamDTO teamDTO) {
                // userService.validateUserAccess(userId); // This should go to the controller

                SportType sportType = sportTypeRepository.findByCode(teamDTO.getSportType())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Sport type not found: " + teamDTO.getSportType()));

                Team team = teamMapper.toEntityForCreate(teamDTO, sportType);
                team.setUserId(userId);
                team = teamRepository.save(team);
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                // Add creator as team member with ADMIN role
                TeamMember adminMember = createAdminTeamMember(team.getId(), user);

                // Save attributes if present
                if (teamDTO.getAttributes() != null && !teamDTO.getAttributes().isEmpty()) {
                        sportAttributeService.updateTeamAttributes(team.getId(), teamDTO.getAttributes());
                }

                // Create Stream chat channel for the team
                streamChatService.createTeamChannel(team.getId(), userId, team.getName(), user.getUserName());

                return getTeamWithAttributes(team, List.of(adminMember), userId);
        }

        public TeamMember createAdminTeamMember(UUID teamId, User user) {
                TeamMemberRequestDTO request = new TeamMemberRequestDTO();
                request.setFirstName(user.getFirstName());
                request.setLastName(user.getLastName());
                request.setEmail(user.getEmail());
                request.setPhone(user.getPhone());
                request.setDateOfBirth(user.getDateOfBirth());
                request.setGender(user.getGender());

                Contact contact = Contact.builder()
                                .firstName(request.getFirstName())
                                .lastName(request.getLastName())
                                .email(request.getEmail())
                                .phone(request.getPhone())
                                .dateOfBirth(request.getDateOfBirth())
                                .gender(request.getGender())
                                .build();

                Contact savedContact = contactRepository.save(contact);
                TeamMember member = TeamMember.builder()
                                .teamId(teamId)
                                .contactId(savedContact.getContactId())
                                .userId(user.getUserId())
                                .role("ADMIN")
                                .isPlayer(false)
                                .isContact(true)
                                .joinedAt(LocalDateTime.now())
                                .build();

                TeamMember savedMember = teamMemberRepository.save(member);

                streamChatService.addMemberToChannel(
                                teamId,
                                user.getUserId(),
                                user.getUserName());

                return savedMember;
        }

        public TeamDTO getTeam(UUID teamId) {
                Team team = teamRepository.findById(teamId)
                                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));
                return teamMapper.toDTO(team);
        }

        public TeamDTO getTeam(UUID userId, UUID teamId) {
                validateTeamMembership(userId, teamId);

                Team team = teamRepository.findById(teamId)
                                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));

                boolean isAdmin = false;
                if (team.getUserId().equals(userId)) {
                        isAdmin = true;
                } else {
                        List<TeamMember> userMemberships = teamMemberRepository
                                        .findAllByTeamIdAndUserIdAndLeftAtIsNull(teamId, userId);

                        isAdmin = userMemberships.stream()
                                        .anyMatch(member -> isAdminRole(member.getRole()));
                }

                TeamDTO dto = teamMapper.toDTO(team);

                List<TeamAttributeValue> attributes = sportAttributeService.getAllTeamAttributeValues(team.getId());
                dto.setAttributes(attributeValueMapper.toTeamAttributeDTOList(attributes));

                dto.setIsAdmin(isAdmin);

                return dto;
        }

        public List<TeamDTO> getUserTeams(UUID userId) {
                // userService.validateUserAccess(userId); // This should go to the controller

                List<Player> userPlayers = playerRepository
                                .findAllByParentUserUserIdAndDeletedAtIsNull(userId, Pageable.unpaged())
                                .getContent();

                List<UUID> playerIds = userPlayers.stream()
                                .map(Player::getPlayerId)
                                .collect(Collectors.toList());

                List<Team> ownedTeams = teamRepository.findByUserIdAndDeletedAtIsNull(userId);
                List<TeamMember> activeMembers = teamMemberRepository
                                .findByUserIdOrPlayerIdInAndLeftAtIsNull(userId, playerIds);

                if (activeMembers.isEmpty() && ownedTeams.isEmpty()) {
                        return Collections.emptyList();
                }

                List<UUID> memberTeamIds = activeMembers.stream()
                                .map(TeamMember::getTeamId)
                                .distinct()
                                .collect(Collectors.toList());

                List<Team> memberTeams = !memberTeamIds.isEmpty()
                                ? teamRepository.findByIdInAndDeletedAtIsNull(memberTeamIds)
                                : Collections.emptyList();

                // Create a map of team IDs to admin status for this user
                Map<UUID, Boolean> teamAdminStatus = new HashMap<>();

                // First, check for team ownership (owners are always admins)
                ownedTeams.forEach(team -> teamAdminStatus.put(team.getId(), true));

                // Then check team membership with ADMIN role
                activeMembers.forEach(member -> {
                        if (member.getUserId() != null && member.getUserId().equals(userId)) {
                                boolean isAdmin = isAdminRole(member.getRole());
                                // Only update if not already set to true (owned teams already set to true)
                                if (!teamAdminStatus.containsKey(member.getTeamId())
                                                || !teamAdminStatus.get(member.getTeamId())) {
                                        teamAdminStatus.put(member.getTeamId(), isAdmin);
                                }
                        }
                });

                // Load all teams with their attributes and add admin status
                return Stream.concat(ownedTeams.stream(), memberTeams.stream())
                                .distinct()
                                .map(team -> {
                                        TeamDTO dto = getTeamWithAttributes(team, null, null);
                                        // Add admin status flag to the DTO
                                        dto.setIsAdmin(teamAdminStatus.getOrDefault(team.getId(), false));
                                        return dto;
                                })
                                .collect(Collectors.toList());
        }

        public ResponseEntity<Resource> getPublicAvatar(UUID teamId) {
                Team team = teamRepository.findByIdAndDeletedAtIsNull(teamId)
                                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));

                if (team.getLogoMediaId() != null) {
                        return mediaService.streamMediaInline(team.getLogoMediaId());
                }

                return ResponseEntity.noContent().build();
        }

        public Page<TeamMinimalDTO> searchTeams(String sportTypeCode, PageRequest pageRequest) {
                return teamRepository.findAllBySportTypeCodeAndDeletedAtIsNull(
                                sportTypeCode,
                                pageRequest).map(team -> mapToMinimalDTO(team));
        }

        private TeamDTO getTeamWithAttributes(Team team, List<TeamMember> activeMembers, UUID userId) {
                TeamDTO dto = teamMapper.toDTO(team);
                List<TeamAttributeValue> attributes = sportAttributeService.getAllTeamAttributeValues(team.getId());
                dto.setAttributes(attributeValueMapper.toTeamAttributeDTOList(attributes));

                if (activeMembers != null && !activeMembers.isEmpty() && userId != null) {
                        activeMembers.forEach(member -> {
                                if (member.getUserId() != null && member.getUserId().equals(userId)) {
                                        dto.setIsAdmin(isAdminRole(member.getRole()));
                                }
                        });
                }
                return dto;
        }

        public TeamDTO updateTeam(UUID userId, UUID teamId, TeamDTO teamDTO) {
                // userService.validateUserAccess(userId); // This should go to the controller
                validateTeamAdmin(userId, teamId);

                Team team = teamRepository.findByIdAndUserIdAndDeletedAtIsNull(teamId, userId)
                                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));

                SportType sportType = sportTypeRepository.findByCode(teamDTO.getSportType())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Sport type not found: " + teamDTO.getSportType()));

                team.setName(teamDTO.getName());
                team.setDescription(teamDTO.getDescription());
                team.setSportType(sportType);
                team.setAgeGroup(teamDTO.getAgeGroup());
                team.setDivision(teamDTO.getDivision());
                team.setSeasonYear(teamDTO.getSeasonYear());
                team.setLogoMediaId(teamDTO.getLogoMediaId());
                team.setUpdatedAt(LocalDateTime.now());

                team = teamRepository.save(team);

                // Update attributes
                if (teamDTO.getAttributes() != null) {
                        sportAttributeService.updateTeamAttributes(team.getId(), teamDTO.getAttributes());
                }

                List<TeamMember> members = teamMemberRepository.findByTeamIdAndLeftAtIsNull(teamId);

                return getTeamWithAttributes(team, members, userId);
        }

        public void deleteTeam(UUID userId, UUID teamId) {
                // userService.validateUserAccess(userId); // This should go to the controller
                validateTeamAdmin(userId, teamId);
                Team team = teamRepository.findByIdAndUserIdAndDeletedAtIsNull(teamId, userId)
                                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));

                team.setDeletedAt(LocalDateTime.now());
                teamRepository.save(team);
                streamChatService.deleteChannel(teamId);
        }

        public TeamLiveDTO getTeamLive(UUID userId, UUID teamId) {
                // TODO
                Team team = teamRepository.findByIdAndUserIdAndDeletedAtIsNull(teamId, userId)
                                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));

                return new TeamLiveDTO();
        }

        public Page<TeamMemberMinimalDTO> searchTeamMemberCandidates(Pageable pageable, String query) {
                // Get all users and players with pagination
                Page<User> users;
                Page<Player> players;

                if (query != null && !query.trim().isEmpty()) {
                        users = userRepository
                                        .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                                                        query.trim(), query.trim(), query.trim(), pageable);
                        players = playerRepository
                                        .findAllByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseAndDeletedAtIsNull(
                                                        query.trim(), query.trim(), pageable);
                } else {
                        users = userRepository.findAll(pageable);
                        players = playerRepository.findAllByDeletedAtIsNull(pageable);
                }

                // Combine and map results
                List<TeamMemberMinimalDTO> combinedResults = new ArrayList<>();

                // Map users
                users.getContent().forEach(user -> {
                        TeamMemberMinimalDTO dto = TeamMemberMinimalDTO.builder()
                                        .firstName(user.getFirstName())
                                        .lastName(user.getLastName())
                                        .email(user.getEmail())
                                        .userId(user.getUserId())
                                        .logoMediaId(user.getProfilePhotoMediaId())
                                        .isPlayer(false)
                                        .build();
                        combinedResults.add(dto);
                });

                // Map players
                players.getContent().forEach(player -> {
                        TeamMemberMinimalDTO dto = TeamMemberMinimalDTO.builder()
                                        .firstName(player.getFirstName())
                                        .lastName(player.getLastName())
                                        .email(null)
                                        .playerId(player.getPlayerId())
                                        .logoMediaId(player.getLogoMediaId())
                                        .isPlayer(true)
                                        .build();
                        combinedResults.add(dto);
                });

                // Sort combined results
                String sortProperty = pageable.getSort().iterator().next().getProperty();
                boolean isAsc = pageable.getSort().iterator().next().isAscending();

                Comparator<TeamMemberMinimalDTO> comparator = (dto1, dto2) -> {
                        String value1 = getValue(dto1, sortProperty);
                        String value2 = getValue(dto2, sortProperty);
                        if (value1 == null && value2 == null) {
                                return 0;
                        }
                        if (value1 == null) {
                                return isAsc ? -1 : 1;
                        }
                        if (value2 == null) {
                                return isAsc ? 1 : -1;
                        }
                        return isAsc ? value1.compareToIgnoreCase(value2) : value2.compareToIgnoreCase(value1);
                };
                combinedResults.sort(comparator);
                combinedResults.sort(comparator);
                long totalElements = users.getTotalElements() + players.getTotalElements();
                return new PageImpl<>(
                                combinedResults,
                                pageable,
                                totalElements);
        }

        private String getValue(TeamMemberMinimalDTO dto, String property) {
                if (dto == null)
                        return null;

                return switch (property.toLowerCase()) {
                        case "firstname" -> dto.getFirstName();
                        case "lastname" -> dto.getLastName();
                        case "email" -> dto.getEmail();
                        default -> null;
                };
        }

        public TeamFeedDTO getTeamFeed(UUID userId, UUID teamId) {
                // TODO
                Team team = teamRepository.findByIdAndUserIdAndDeletedAtIsNull(teamId, userId)
                                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));

                return new TeamFeedDTO();
        }

        private void validateTeamAdmin(UUID userId, UUID teamId) {
                validateTeamAccess(userId, teamId);
        }

        private boolean validateTeamRole(UUID userId, UUID teamId, String role) {
                List<TeamMember> memberships = teamMemberRepository.findByTeamIdAndLeftAtIsNull(teamId)
                                .stream()
                                .filter(m -> m.getUserId() != null && m.getUserId().equals(userId))
                                .collect(Collectors.toList());

                if (memberships.isEmpty()) {
                        throw new ForbiddenException("User is not a team member");
                }

                boolean hasRole = memberships.stream()
                                .anyMatch(member -> role.equalsIgnoreCase(member.getRole()));

                return hasRole;
        }

        @Transactional
        public Team validateTeamAccess(UUID userId, UUID teamId) {
                Team team = teamRepository.findById(teamId)
                                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));

                boolean isCreatedByUser = userId.equals(team.getUserId());
                boolean isTeamOwner = team.getUserId().equals(userId);
                boolean isAdmin = validateTeamRole(userId, teamId, ROLE_ADMIN);
                boolean isTeamManager = validateTeamRole(userId, teamId, ROLE_MANAGER);
                boolean isCoach = validateTeamRole(userId, teamId, ROLE_COACH);
                if (!isCreatedByUser && !isAdmin && !isTeamOwner && !isTeamManager && !isCoach) {
                        throw new ForbiddenException("You don't have permission to manage this team");
                }
                return team;
        }

        private void validateTeamMembership(UUID userId, UUID teamId) {
                List<TeamDTO> userTeams = getUserTeams(userId);
                boolean isMember = userTeams.stream()
                                .anyMatch(team -> team.getId().equals(teamId));

                if (!isMember) {
                        log.warn("Access denied: User {} is not a member of team {}", userId, teamId);
                        throw new ForbiddenException("User is not a team member");
                }
        }

        private TeamMinimalDTO mapToMinimalDTO(Team team) {
                TeamMinimalDTO dto = new TeamMinimalDTO();
                dto.setTeamId(team.getId());
                dto.setName(team.getName());
                dto.setDescription(team.getDescription());
                dto.setSportTypeCode(team.getSportType() == null ? null : team.getSportType().getCode());
                dto.setAgeGroup(team.getAgeGroup());
                dto.setDivision(team.getDivision());
                dto.setSeasonYear(team.getSeasonYear());
                dto.setLogoMediaId(team.getLogoMediaId() == null ? null : team.getLogoMediaId().toString());
                return dto;
        }

        public static boolean isAdminRole(String role) {
                if (role == null)
                        return false;
                return ROLE_ADMIN.equalsIgnoreCase(role) ||
                                ROLE_COACH.equalsIgnoreCase(role) ||
                                ROLE_MANAGER.equalsIgnoreCase(role);
        }
}