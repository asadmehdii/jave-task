package com.biosteel.teams.practice.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.biosteel.teams.common.exception.ResourceNotFoundException;
import com.biosteel.teams.event.model.Event;
import com.biosteel.teams.event.model.Location;
import com.biosteel.teams.event.repository.EventRepository;
import com.biosteel.teams.event.repository.LocationRepository;
import com.biosteel.teams.event.service.EventNotificationService;
import com.biosteel.teams.practice.dto.PracticeRequestDTO;
import com.biosteel.teams.practice.dto.PracticeResponseDTO;
import com.biosteel.teams.practice.mapper.PracticeMapper;
import com.biosteel.teams.practice.model.Practice;
import com.biosteel.teams.practice.repository.PracticeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PracticeService {
    private final PracticeRepository practiceRepository;
    private final EventRepository eventRepository;
    private final LocationRepository locationRepository;
    private final PracticeMapper practiceMapper;
    private final EventNotificationService eventNotificationService;

    @Transactional
    public PracticeResponseDTO createPractice(UUID userId, UUID teamId, PracticeRequestDTO practiceDTO) {
        // Create practice record
        Practice practice = practiceMapper.toPracticeEntity(practiceDTO);
        practice = practiceRepository.save(practice);

        // Handle location if provided
        Location location = null;
        if (practiceDTO.getLocation() != null) {
            location = practiceMapper.toLocationEntity(practiceDTO.getLocation());
            if (location.getLocationId() == null) {
                location.setLocationId(UUID.randomUUID());
            }
            location = locationRepository.save(location);
        }

        // Create associated event using mapper
        Event event = practiceMapper.toEventEntity(practiceDTO, teamId, userId, practice);
        if (location != null) {
            event.setLocation(location);
            event.setLocationId(location.getLocationId());
        }
        event = eventRepository.save(event);

        eventNotificationService.sendNewEventNotification(userId, teamId, event);

        return practiceMapper.toDto(practice, event);
    }

    @Transactional(readOnly = true)
    public List<PracticeResponseDTO> getTeamPractices(UUID userId, UUID teamId) {
        // Find all practices associated with events for this team
        List<Event> events = eventRepository.findByTeamIdAndEventType(teamId, "PRACTICE");
        List<UUID> practiceIds = events.stream()
                .map(Event::getPracticeId)
                .collect(Collectors.toList());

        List<Practice> practices = practiceRepository.findAllById(practiceIds);
        return practiceMapper.toDtoList(practices, events);
    }

    @Transactional(readOnly = true)
    public PracticeResponseDTO getPractice(UUID userId, UUID teamId, UUID practiceId) {
        Practice practice = practiceRepository.findById(practiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Practice not found"));

        Event event = eventRepository.findByPractice_PracticeId(practiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Associated event not found"));

        return practiceMapper.toDto(practice, event);
    }

    @Transactional
    public PracticeResponseDTO updatePractice(UUID userId, UUID teamId, UUID practiceId,
            PracticeRequestDTO practiceDTO) {
        Practice practice = practiceRepository.findById(practiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Practice not found"));

        // Update practice details
        practiceMapper.updatePracticeFromDto(practiceDTO, practice);
        practice = practiceRepository.save(practice);

        // Update associated event using mapper
        Event event = eventRepository.findByPractice_PracticeId(practiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Associated event not found"));

        // Handle location update if provided
        if (practiceDTO.getLocation() != null) {
            Location location = event.getLocation();
            if (location == null) {
                location = new Location();
                location.setLocationId(UUID.randomUUID());
            }
            Location updatedLocation = practiceMapper.toLocationEntity(practiceDTO.getLocation());
            updatedLocation.setLocationId(location.getLocationId());
            location = locationRepository.save(updatedLocation);

            event.setLocation(location);
            event.setLocationId(location.getLocationId());
        }

        practiceMapper.updateEventFromDto(practiceDTO, event);
        event = eventRepository.save(event);

        return practiceMapper.toDto(practice, event);
    }

    @Transactional
    public void deletePractice(UUID userId, UUID teamId, UUID practiceId) {
        // Verify practice exists
        if (!practiceRepository.existsById(practiceId)) {
            throw new ResourceNotFoundException("Practice not found");
        }

        // Delete practice and associated event will be deleted via cascade
        practiceRepository.deleteById(practiceId);
        log.info("Deleted practice with ID: {}", practiceId);
    }

}