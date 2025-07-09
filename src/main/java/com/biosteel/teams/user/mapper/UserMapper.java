package com.biosteel.teams.user.mapper;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.biosteel.teams.role.mapper.RoleMapper;
import com.biosteel.teams.user.dto.UserDto;
import com.biosteel.teams.user.dto.UserListDto;
import com.biosteel.teams.user.model.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserMapper {
    private final RoleMapper roleMapper;

    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        UserDto dto = new UserDto();
        dto.setUserId(user.getUserId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhone(user.getPhone());
        dto.setDateOfBirth(user.getDateOfBirth());
        dto.setGender(user.getGender());
        dto.setProfilePhotoMediaId(user.getProfilePhotoMediaId());
        dto.setRegistrationDate(user.getRegistrationDate());
        dto.setEnabled(user.isEnabled());
        dto.setLastActivityDate(user.getLastActivityDate());

        if (user.getRoles() != null) {
            dto.setRoles(user.getRoles().stream()
                    .map(roleMapper::toDto)
                    .collect(Collectors.toSet()));
        }

        dto.setDateOfBirth(user.getDateOfBirth());

        return dto;
    }

    public UserListDto toUserListDto(User user) {
        if (user == null) {
            return null;
        }

        UserListDto dto = new UserListDto();
        dto.setUserId(user.getUserId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setProfilePhotoMediaId(user.getProfilePhotoMediaId());

        return dto;
    }
}