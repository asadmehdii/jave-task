package com.biosteel.teams.sport.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.biosteel.teams.common.exception.ResourceNotFoundException;
import com.biosteel.teams.sport.dto.ScoreTypeDTO;
import com.biosteel.teams.sport.mapper.ScoreTypeMapper;
import com.biosteel.teams.sport.repository.ScoreTypeRepository;
import com.biosteel.teams.sport.repository.SportTypeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScoreTypeService {
    private final ScoreTypeRepository scoreTypeRepository;
    private final SportTypeRepository sportTypeRepository;
    private final ScoreTypeMapper scoreTypeMapper;

    @Transactional(readOnly = true)
    public List<ScoreTypeDTO> getAllScoreTypes() {
        return scoreTypeMapper.toDtoList(scoreTypeRepository.findAll());
    }

    @Transactional(readOnly = true)
    public List<ScoreTypeDTO> getScoreTypesForSport(String sportTypeCode) {
        // Verify sport type exists
        if (!sportTypeRepository.existsByCode(sportTypeCode)) {
            throw new ResourceNotFoundException("Sport type not found: " + sportTypeCode);
        }

        return scoreTypeMapper.toDtoList(
                scoreTypeRepository.findBySportType(sportTypeCode));
    }
}