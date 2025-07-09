package com.biosteel.teams.practice.mapper;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.biosteel.teams.event.dto.LocationDTO;
import com.biosteel.teams.event.model.Event;
import com.biosteel.teams.event.model.Location;
import com.biosteel.teams.practice.dto.PracticeRequestDTO;
import com.biosteel.teams.practice.dto.PracticeResponseDTO;
import com.biosteel.teams.practice.model.Practice;

@Component
public class PracticeMapper {

    public Practice toPracticeEntity(PracticeRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Practice practice = new Practice();
        practice.setInstructions(dto.getInstructions());
        return practice;
    }

    public Location toLocationEntity(LocationDTO dto) {
        if (dto == null) {
            return null;
        }

        Location entity = new Location();
        entity.setLocationId(dto.getLocationId());
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
        return entity;
    }

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

    public Event toEventEntity(PracticeRequestDTO dto, UUID teamId, UUID userId, Practice practice) {
        if (dto == null) {
            return null;
        }

        Event event = new Event();
        event.setEventId(UUID.randomUUID());
        event.setTeamId(teamId);
        event.setUserId(userId);
        event.setPractice(toPracticeEntity(dto));
        event.setEventType("PRACTICE");
        event.setEventStatus("SCHEDULED");
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setStartTime(dto.getStartTime());
        event.setEndTime(dto.getEndTime());
        event.setArrivalTime(dto.getArrivalTime());
        event.setIsRecurring(false);
        event.setPractice(practice);

        // Handle location
        if (dto.getLocation() != null) {
            Location location = toLocationEntity(dto.getLocation());
            if (location.getLocationId() == null) {
                location.setLocationId(UUID.randomUUID());
            }
            event.setLocation(location);
            event.setLocationId(location.getLocationId());
        }

        return event;
    }

    public void updateEventFromDto(PracticeRequestDTO dto, Event event) {
        if (dto == null || event == null) {
            return;
        }

        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setStartTime(dto.getStartTime());
        event.setEndTime(dto.getEndTime());
        event.setArrivalTime(dto.getArrivalTime());

        // Handle location update
        if (dto.getLocation() != null) {
            Location location = event.getLocation();
            if (location == null) {
                location = new Location();
                location.setLocationId(UUID.randomUUID());
            }
            Location updatedLocation = toLocationEntity(dto.getLocation());
            updatedLocation.setLocationId(location.getLocationId());
            event.setLocation(updatedLocation);
            event.setLocationId(updatedLocation.getLocationId());
        }
    }

    public void updatePracticeFromDto(PracticeRequestDTO dto, Practice practice) {
        if (dto == null || practice == null) {
            return;
        }

        practice.setInstructions(dto.getInstructions());
    }

    public PracticeResponseDTO toDto(Practice practice, Event event) {
        if (practice == null || event == null) {
            return null;
        }

        PracticeResponseDTO dto = new PracticeResponseDTO();
        dto.setPracticeId(practice.getPracticeId());
        dto.setTitle(event.getTitle());
        dto.setDescription(event.getDescription());
        dto.setStartTime(event.getStartTime());
        dto.setEndTime(event.getEndTime());
        dto.setArrivalTime(event.getArrivalTime());
        dto.setInstructions(practice.getInstructions());
        dto.setEventStatus(event.getEventStatus());
        dto.setCreatedAt(practice.getCreatedAt());
        dto.setUpdatedAt(practice.getUpdatedAt());

        // Handle location
        if (event.getLocation() != null) {
            dto.setLocation(toLocationDto(event.getLocation()));
        }

        return dto;
    }

    public List<PracticeResponseDTO> toDtoList(List<Practice> practices, List<Event> events) {
        if (practices == null || events == null) {
            return null;
        }

        return practices.stream()
                .map(practice -> {
                    Event matchingEvent = events.stream()
                            .filter(event -> event.getPracticeId().equals(practice.getPracticeId()))
                            .findFirst()
                            .orElse(null);
                    return matchingEvent != null ? toDto(practice, matchingEvent) : null;
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

}