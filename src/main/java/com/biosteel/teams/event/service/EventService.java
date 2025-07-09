package com.biosteel.teams.event.service;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.biosteel.teams.common.exception.ResourceNotFoundException;
import com.biosteel.teams.event.dto.EventAttendanceRequestDTO;
import com.biosteel.teams.event.dto.EventAttendanceResponseDTO;
import com.biosteel.teams.event.dto.EventAttendanceUpdateRequestDTO;
import com.biosteel.teams.event.dto.EventRequestDTO;
import com.biosteel.teams.event.dto.EventResponseDTO;
import com.biosteel.teams.event.mapper.EventMapper;
import com.biosteel.teams.event.model.Event;
import com.biosteel.teams.event.model.EventAttendance;
import com.biosteel.teams.event.model.Location;
import com.biosteel.teams.event.repository.EventAttendanceRepository;
import com.biosteel.teams.event.repository.EventRepository;
import com.biosteel.teams.event.repository.LocationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class EventService {
    private final EventRepository eventRepository;
    private final EventAttendanceRepository eventAttendanceRepository;
    private final EventMapper eventMapper;
    private final LocationRepository locationRepository;
    private final EventNotificationService eventNotificationService;

    @Transactional
    public EventResponseDTO createEvent(UUID userId, UUID teamId, EventRequestDTO eventDTO) {
        Event event = eventMapper.toEntity(eventDTO);

        // Save location if provided
        if (eventDTO.getLocation() != null) {
            Location location = eventMapper.toLocationEntity(eventDTO.getLocation());
            if (location.getLocationId() == null) {
                location.setLocationId(UUID.randomUUID());
            }
            location = locationRepository.save(location);
            event.setLocation(location);
        }

        event.setTeamId(teamId);
        event.setUserId(userId);
        event = eventRepository.save(event);

        eventNotificationService.sendNewEventNotification(userId, teamId, event);
        return eventMapper.toDto(event);
    }

    @Transactional(readOnly = true)
    public EventResponseDTO getEvent(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        return eventMapper.toDto(event);
    }

    @Transactional(readOnly = true)
    public List<EventResponseDTO> getTeamEvents(UUID userId, UUID teamId) {
        List<Event> events = eventRepository.findByTeamId(teamId);
        return eventMapper.toDtoList(events);
    }

    @Transactional
    public EventResponseDTO updateEvent(UUID userId, UUID teamId, UUID eventId, EventRequestDTO eventDTO) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        // Update location if provided
        if (eventDTO.getLocation() != null) {
            Location location = event.getLocation();
            if (location == null) {
                location = new Location();
                location.setLocationId(UUID.randomUUID());
            }
            eventMapper.updateLocationFromDto(eventDTO.getLocation(), location);
            location = locationRepository.save(location);
            event.setLocation(location);
        }

        eventMapper.updateEntityFromDto(eventDTO, event);
        event = eventRepository.save(event);
        return eventMapper.toDto(event);
    }

    @Transactional
    public void deleteEvent(UUID userId, UUID teamId, UUID eventId) {
        eventRepository.deleteById(eventId);
    }

    @Transactional
    public EventAttendanceResponseDTO recordAttendance(
            UUID userId,
            UUID teamId,
            UUID eventId,
            EventAttendanceRequestDTO attendanceDTO) {
        EventAttendance attendance = eventMapper.toEntity(attendanceDTO);
        attendance.setEventId(eventId);
        attendance = eventAttendanceRepository.save(attendance);
        return eventMapper.toDto(attendance);
    }

    @Transactional
    public EventAttendanceResponseDTO updateAttendance(
            UUID userId,
            UUID teamId,
            UUID eventId,
            UUID eventAttendanceId,
            EventAttendanceUpdateRequestDTO attendanceDTO) {
        EventAttendance attendance = eventAttendanceRepository.findById(eventAttendanceId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Event attendance not found with ID: " + eventAttendanceId));

        if (!attendance.getEventId().equals(eventId)) {
            throw new IllegalArgumentException("Event attendance does not belong to the specified event");
        }
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with ID: " + eventId));
        if (!event.getTeamId().equals(teamId)) {
            throw new IllegalArgumentException("Event does not belong to the specified team");
        }
        attendance.setEventAttendanceStatus(attendanceDTO.getEventAttendanceStatus());
        attendance.setNotes(attendanceDTO.getNotes());

        if (attendanceDTO.getRegisteredAt() != null) {
            attendance.setRegisteredAt(attendanceDTO.getRegisteredAt());
        }
        if (attendanceDTO.getCheckedInAt() != null) {
            attendance.setCheckedInAt(attendanceDTO.getCheckedInAt());
        }
        attendance.setUpdatedAt(ZonedDateTime.now());
        attendance = eventAttendanceRepository.save(attendance);
        return eventMapper.toDto(attendance);
    }

    @Transactional(readOnly = true)
    public List<EventAttendanceResponseDTO> getEventAttendance(UUID userId, UUID teamId, UUID eventId) {
        List<EventAttendance> attendances = eventAttendanceRepository.findByEventId(eventId);
        return eventMapper.toAttendanceDtoList(attendances);
    }

    @Transactional(readOnly = true)
    public Page<EventResponseDTO> getUserEvents(
            UUID userId,
            String timeFilter,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String eventType,
            int page,
            int size) {

        LocalDateTime now = LocalDateTime.now();

        // If no custom date range is provided, set based on timeFilter
        if (startDate == null && endDate == null) {
            switch (timeFilter.toUpperCase()) {
                case "PAST":
                    endDate = now;
                    break;
                case "UPCOMING":
                    startDate = now;
                    break;
                case "ALL":
                    break;
                default:
                    // Default to upcoming events
                    startDate = now;
                    break;
            }
        }

        int offset = page * size;
        List<Event> events;
        long total;

        if (eventType != null) {
            events = eventRepository.findEventsForUserTeamsByType(
                    userId, eventType, startDate, endDate, offset, size);
            total = eventRepository.countEventsForUserTeamsByType(
                    userId, eventType, startDate, endDate);
        } else {
            events = eventRepository.findEventsForUserTeams(
                    userId, startDate, endDate, offset, size);
            total = eventRepository.countEventsForUserTeams(
                    userId, startDate, endDate);
        }

        List<EventResponseDTO> dtos = eventMapper.toDtoList(events);
        return new PageImpl<>(dtos, PageRequest.of(page, size), total);
    }

}