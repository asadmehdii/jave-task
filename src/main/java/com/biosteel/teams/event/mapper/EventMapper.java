package com.biosteel.teams.event.mapper;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.biosteel.teams.event.dto.EventAttendanceRequestDTO;
import com.biosteel.teams.event.dto.EventAttendanceResponseDTO;
import com.biosteel.teams.event.dto.EventRequestDTO;
import com.biosteel.teams.event.dto.EventResponseDTO;
import com.biosteel.teams.event.dto.LocationDTO;
import com.biosteel.teams.event.model.Event;
import com.biosteel.teams.event.model.EventAttendance;
import com.biosteel.teams.event.model.Location;
import com.biosteel.teams.game.mapper.GameMapper;
import com.biosteel.teams.practice.mapper.PracticeMapper;
import com.biosteel.teams.team.dto.TeamMemberResponseDTO;
import com.biosteel.teams.team.service.TeamMemberService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EventMapper {
    private final TeamMemberService teamMemberService;
    private final GameMapper gameMapper;
    private final PracticeMapper practiceMapper;

    public LocationDTO toLocationDto(Location entity) {
        if (entity == null) {
            return null;
        }

        LocationDTO dto = new LocationDTO();
        dto.setLocationId(entity.getLocationId());
        dto.setAddressLine1(entity.getAddressLine1());
        dto.setAddressLine2(entity.getAddressLine2());
        dto.setMajorIntersection(entity.getMajorIntersection());
        dto.setPostalZip(entity.getPostalZip());
        dto.setCity(entity.getCity());
        dto.setProvince(entity.getProvince());
        dto.setCountry(entity.getCountry());
        dto.setPlaceName(entity.getPlaceName());
        dto.setPlaceGuid(entity.getPlaceGuid());
        dto.setLongitude(entity.getLongitude());
        dto.setLatitude(entity.getLatitude());
        return dto;
    }

    public Location toLocationEntity(LocationDTO dto) {
        if (dto == null) {
            return null;
        }

        Location entity = new Location();
        updateLocationFromDto(dto, entity);
        return entity;
    }

    public void updateLocationFromDto(LocationDTO dto, Location entity) {
        if (dto == null || entity == null) {
            return;
        }

        if (entity.getLocationId() == null) {
            entity.setLocationId(UUID.randomUUID());
        }
        entity.setAddressLine1(dto.getAddressLine1());
        entity.setAddressLine2(dto.getAddressLine2());
        entity.setMajorIntersection(dto.getMajorIntersection());
        entity.setPostalZip(dto.getPostalZip());
        entity.setCity(dto.getCity());
        entity.setProvince(dto.getProvince());
        entity.setCountry(dto.getCountry());
        entity.setPlaceName(dto.getPlaceName());
        entity.setPlaceGuid(dto.getPlaceGuid());
        entity.setLongitude(dto.getLongitude());
        entity.setLatitude(dto.getLatitude());
        entity.setUpdatedAt(ZonedDateTime.now());
    }

    public Event toEntity(EventRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Event event = new Event();
        event.setEventId(UUID.randomUUID());
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setStartTime(dto.getStartTime());
        event.setEndTime(dto.getEndTime());
        event.setArrivalTime(dto.getArrivalTime());
        event.setEventType(dto.getEventType());
        event.setEventStatus(dto.getEventStatus());
        event.setIsRecurring(dto.getIsRecurring());
        event.setRecurrenceRule(dto.getRecurrenceRule());
        event.setBannerMediaId(dto.getBannerMediaId());
        event.setLogoMediaId(dto.getLogoMediaId());
        event.setStreamId(dto.getStreamId());

        // Handle location
        if (dto.getLocation() != null) {
            Location location = toLocationEntity(dto.getLocation());
            event.setLocation(location);
            event.setLocationId(location.getLocationId());
        }

        // Set audit fields
        ZonedDateTime now = ZonedDateTime.now();
        event.setCreatedAt(now);
        event.setUpdatedAt(now);

        return event;
    }

    public EventResponseDTO toDto(Event event) {
        if (event == null) {
            return null;
        }

        EventResponseDTO dto = new EventResponseDTO();
        dto.setEventId(event.getEventId());
        dto.setTeamId(event.getTeamId());
        dto.setUserId(event.getUserId());
        dto.setTitle(event.getTitle());
        dto.setDescription(event.getDescription());
        dto.setStartTime(event.getStartTime());
        dto.setArrivalTime(event.getArrivalTime());
        dto.setEndTime(event.getEndTime());
        dto.setEventType(event.getEventType());
        dto.setEventStatus(event.getEventStatus());
        dto.setIsRecurring(event.getIsRecurring());
        dto.setRecurrenceRule(event.getRecurrenceRule());
        dto.setBannerMediaId(event.getBannerMediaId());
        dto.setLogoMediaId(event.getLogoMediaId());
        dto.setCreatedAt(event.getCreatedAt());
        dto.setUpdatedAt(event.getUpdatedAt());
        dto.setStreamId(event.getStreamId());

        // Handle location
        if (event.getLocation() != null) {
            dto.setLocation(toLocationDto(event.getLocation()));
        }

        if (event.getGame() != null) {
            dto.setGame(gameMapper.toDto(event.getGame(), event));
        }

        // Map practice if present
        if (event.getPractice() != null) {
            dto.setPractice(practiceMapper.toDto(event.getPractice(), event));
        }

        return dto;
    }

    public List<EventResponseDTO> toDtoList(List<Event> entities) {
        if (entities == null) {
            return null;
        }

        List<EventResponseDTO> dtos = new ArrayList<>(entities.size());
        for (Event entity : entities) {
            dtos.add(toDto(entity));
        }

        return dtos;
    }

    public void updateEntityFromDto(EventRequestDTO dto, Event entity) {
        if (dto == null || entity == null) {
            return;
        }

        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setStartTime(dto.getStartTime());
        entity.setEndTime(dto.getEndTime());
        entity.setArrivalTime(dto.getArrivalTime());
        entity.setEventType(dto.getEventType());
        entity.setEventStatus(dto.getEventStatus());
        entity.setIsRecurring(dto.getIsRecurring());
        entity.setRecurrenceRule(dto.getRecurrenceRule());
        entity.setBannerMediaId(dto.getBannerMediaId());
        entity.setLogoMediaId(dto.getLogoMediaId());
        entity.setStreamId(dto.getStreamId());

        // Handle location update
        if (dto.getLocation() != null) {
            Location location = entity.getLocation();
            if (location == null) {
                location = new Location();
            }
            updateLocationFromDto(dto.getLocation(), location);
            entity.setLocation(location);
            entity.setLocationId(location.getLocationId());
        }

        entity.setUpdatedAt(ZonedDateTime.now());
    }

    // ... existing EventAttendance mapping methods remain unchanged ...
    public EventAttendance toEntity(EventAttendanceRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        EventAttendance attendance = new EventAttendance();
        attendance.setEventAttendanceId(UUID.randomUUID());
        attendance.setTeamMemberId(dto.getTeamMemberId());
        attendance.setEventAttendanceStatus(dto.getEventAttendanceStatus());
        attendance.setNotes(dto.getNotes());
        attendance.setRegisteredAt(ZonedDateTime.now());

        // Set audit fields
        ZonedDateTime now = ZonedDateTime.now();
        attendance.setCreatedAt(now);
        attendance.setUpdatedAt(now);

        return attendance;
    }

    public EventAttendanceResponseDTO toDto(EventAttendance entity) {
        if (entity == null) {
            return null;
        }

        EventAttendanceResponseDTO dto = new EventAttendanceResponseDTO();
        dto.setEventAttendanceId(entity.getEventAttendanceId());
        dto.setEventId(entity.getEventId());
        dto.setTeamMemberId(entity.getTeamMemberId());
        dto.setEventAttendanceStatus(entity.getEventAttendanceStatus());
        dto.setNotes(entity.getNotes());
        dto.setRegisteredAt(entity.getRegisteredAt());
        dto.setCheckedInAt(entity.getCheckedInAt());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        // Add team member details
        if (entity.getTeamMemberId() != null) {
            TeamMemberResponseDTO teamMember = teamMemberService.getTeamMember(entity.getTeamMemberId());
            dto.setMember(teamMember);
        }

        return dto;
    }

    public List<EventAttendanceResponseDTO> toAttendanceDtoList(List<EventAttendance> entities) {
        if (entities == null) {
            return null;
        }

        List<EventAttendanceResponseDTO> dtos = new ArrayList<>(entities.size());
        for (EventAttendance entity : entities) {
            dtos.add(toDto(entity));
        }

        return dtos;
    }
}