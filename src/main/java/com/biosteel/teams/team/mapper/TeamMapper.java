package com.biosteel.teams.team.mapper;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.biosteel.teams.sport.model.SportType;
import com.biosteel.teams.team.dto.TeamDTO;
import com.biosteel.teams.team.model.Team;

@Component
public class TeamMapper {

    public TeamDTO toDTO(Team team) {
        if (team == null) {
            return null;
        }

        TeamDTO dto = new TeamDTO();
        dto.setId(team.getId());
        dto.setName(team.getName());
        dto.setDescription(team.getDescription());
        dto.setSportType(team.getSportType() != null ? team.getSportType().getCode() : null);
        dto.setAgeGroup(team.getAgeGroup());
        dto.setDivision(team.getDivision());
        dto.setSeasonYear(team.getSeasonYear());
        dto.setLogoMediaId(team.getLogoMediaId());
        return dto;
    }

    public Team toEntity(TeamDTO dto, SportType sportType) {
        if (dto == null) {
            return null;
        }

        Team team = new Team();
        team.setId(dto.getId());
        team.setName(dto.getName());
        team.setDescription(dto.getDescription());
        team.setSportType(sportType); // Set the SportType object
        team.setAgeGroup(dto.getAgeGroup());
        team.setDivision(dto.getDivision());
        team.setSeasonYear(dto.getSeasonYear());
        team.setLogoMediaId(dto.getLogoMediaId());
        return team;
    }

    public Team toEntityForCreate(TeamDTO dto, SportType sportType) {
        if (dto == null) {
            return null;
        }

        Team team = new Team();
        team.setName(dto.getName());
        team.setDescription(dto.getDescription());
        team.setSportType(sportType); // Set the SportType object
        team.setAgeGroup(dto.getAgeGroup());
        team.setDivision(dto.getDivision());
        team.setSeasonYear(dto.getSeasonYear());
        team.setLogoMediaId(dto.getLogoMediaId());
        team.setCreatedAt(LocalDateTime.now());
        return team;
    }

    public List<TeamDTO> toDTOList(List<Team> teams) {
        if (teams == null) {
            return Collections.emptyList();
        }

        return teams.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<Team> toEntityList(List<TeamDTO> dtos, List<SportType> sportTypes) {
        if (dtos == null || sportTypes == null) {
            return Collections.emptyList();
        }

        return dtos.stream()
                .map(dto -> {
                    SportType sportType = sportTypes.stream()
                            .filter(st -> st.getCode().equals(dto.getSportType()))
                            .findFirst()
                            .orElse(null);
                    return toEntity(dto, sportType);
                })
                .collect(Collectors.toList());
    }
}
