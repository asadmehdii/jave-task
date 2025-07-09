package com.biosteel.teams.media.mapper;

import org.springframework.stereotype.Component;

import com.biosteel.teams.media.dto.MediaDto;
import com.biosteel.teams.media.model.Media;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MediaMapper {

    public MediaDto toDto(Media media) {
        if (media == null) {
            return null;
        }

        return MediaDto.builder()
                .mediaId(media.getMediaId())
                .teamId(media.getTeamId())
                .userId(media.getUserId())
                .mediaType(media.getMediaType())
                .fileSystemId(media.getFileSystemId())
                .fileName(media.getFileName())
                .fileExtension(media.getFileExtension())
                .fileSize(media.getFileSize())
                .title(media.getTitle())
                .body(media.getBody())
                .mimeType(media.getMimeType())
                .url(media.getUrl())
                .createdAt(media.getCreatedAt())
                .updatedAt(media.getUpdatedAt())
                .deletedAt(media.getDeletedAt())
                .build();
    }

    public Media toEntity(MediaDto dto) {
        if (dto == null) {
            return null;
        }

        Media media = new Media();
        media.setMediaId(dto.getMediaId());
        media.setTeamId(dto.getTeamId());
        media.setUserId(dto.getUserId());
        media.setMediaType(dto.getMediaType());
        media.setFileSystemId(dto.getFileSystemId());
        media.setFileName(dto.getFileName());
        media.setFileExtension(dto.getFileExtension());
        media.setFileSize(dto.getFileSize());
        media.setTitle(dto.getTitle());
        media.setBody(dto.getBody());
        media.setMimeType(dto.getMimeType());
        media.setUrl(dto.getUrl());
        media.setCreatedAt(dto.getCreatedAt());
        media.setUpdatedAt(dto.getUpdatedAt());
        media.setDeletedAt(dto.getDeletedAt());

        return media;
    }
}