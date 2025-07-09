package com.biosteel.teams.role.mapper;

import org.springframework.stereotype.Component;

import com.biosteel.teams.role.dto.RoleDto;
import com.biosteel.teams.role.model.Role;

@Component
public class RoleMapper {

    public RoleDto toDto(Role role) {
        if (role == null) {
            return null;
        }

        return new RoleDto(
                role.getRoleId(),
                role.getName(),
                role.getDescription());
    }

    public Role toEntity(RoleDto dto) {
        if (dto == null) {
            return null;
        }

        return new Role(
                dto.getRoleId(),
                dto.getName(),
                dto.getDescription());
    }
}