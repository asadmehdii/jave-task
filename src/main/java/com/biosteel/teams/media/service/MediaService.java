package com.biosteel.teams.media.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.biosteel.teams.common.exception.ResourceNotFoundException;
import com.biosteel.teams.media.dto.MediaDto;
import com.biosteel.teams.media.mapper.MediaMapper;
import com.biosteel.teams.media.model.Media;
import com.biosteel.teams.media.repository.MediaRepository;
import com.biosteel.teams.user.model.User;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaService {

    private final MediaRepository mediaRepository;
    private final S3Service s3Service;
    private final MediaMapper mediaMapper;

    @Value("${openapi.server.url}")
    private String serverUrl;

    @Transactional
    public MediaDto uploadMedia(MultipartFile file, UUID teamId, UUID userId, String title, String body) {
        try {
            // Generate new UUID for media
            UUID mediaId = UUID.randomUUID();

            // Construct S3 key using folder structure:
            // team_id/user_id(optional)/media_id.extension
            String fileExtension = getFileExtension(file.getOriginalFilename());
            String s3Key = constructS3Key(teamId, userId, mediaId, fileExtension);

            // Upload to S3
            String url = s3Service.uploadFile(file, s3Key);

            // Create media entity
            Media media = new Media();
            media.setMediaId(mediaId);
            media.setTeamId(teamId);
            media.setUserId(userId);
            media.setMediaType(determineMediaType(file.getContentType()));
            media.setFileSystemId(s3Key);
            media.setFileName(file.getOriginalFilename());
            media.setFileExtension(fileExtension);
            media.setFileSize(file.getSize());
            media.setTitle(title);
            media.setBody(body);
            media.setMimeType(file.getContentType());
            media.setUrl(url);
            media.setCreatedAt(LocalDateTime.now());
            media.setUpdatedAt(LocalDateTime.now());

            Media savedMedia = mediaRepository.save(media);
            return mediaMapper.toDto(savedMedia);

        } catch (IOException e) {
            log.error("Error uploading file to S3", e);
            throw new RuntimeException("Failed to upload media", e);
        }
    }

    @Transactional
    public MediaDto updateMediaWithFile(UUID mediaId, MultipartFile file, String title, String body) {
        try {
            // Fetch existing media
            Media media = mediaRepository.findById(mediaId)
                    .orElseThrow(() -> new ResourceNotFoundException("Media not found"));

            // Delete existing file from S3
            s3Service.deleteFile(media.getFileSystemId());

            // Construct new S3 key maintaining the same structure
            String fileExtension = getFileExtension(file.getOriginalFilename());
            String s3Key = constructS3Key(media.getTeamId(), media.getUserId(), mediaId, fileExtension);

            // Upload new file to S3
            String url = s3Service.uploadFile(file, s3Key);

            // Update media entity
            media.setMediaType(determineMediaType(file.getContentType()));
            media.setFileSystemId(s3Key);
            media.setFileName(file.getOriginalFilename());
            media.setFileExtension(fileExtension);
            media.setFileSize(file.getSize());
            media.setMimeType(file.getContentType());
            media.setUrl(url);

            if (title != null) {
                media.setTitle(title);
            }
            if (body != null) {
                media.setBody(body);
            }

            media.setUpdatedAt(LocalDateTime.now());

            Media updatedMedia = mediaRepository.save(media);
            return mediaMapper.toDto(updatedMedia);

        } catch (IOException e) {
            log.error("Error updating file in S3", e);
            throw new RuntimeException("Failed to update media", e);
        }
    }

    public MediaDto getMedia(UUID mediaId) {
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new ResourceNotFoundException("Media not found"));
        return mediaMapper.toDto(media);
    }

    public List<MediaDto> getTeamMedia(UUID teamId) {
        List<Media> mediaList = mediaRepository.findByTeamId(teamId);
        return mediaList.stream()
                .map(mediaMapper::toDto)
                .collect(Collectors.toList());
    }

    public String getMediaUrl(UUID mediaId) {
        return serverUrl + "/api/media/" + mediaId;
    }

    @Transactional
    public MediaDto updateMedia(User user, UUID mediaId, String title, String body, MultipartFile file) {
        if (user == null) {
            new ResourceNotFoundException("User not found");
        }

        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new ResourceNotFoundException("Media not found"));

        if (title != null) {
            media.setTitle(title);
        }
        if (body != null) {
            media.setBody(body);
        }

        updateMediaWithFile(mediaId, file, title, body);
        media.setUpdatedAt(LocalDateTime.now());
        Media updatedMedia = mediaRepository.save(media);
        return mediaMapper.toDto(updatedMedia);
    }

    @Transactional
    public void deleteMedia(UUID mediaId) {
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new ResourceNotFoundException("Media not found"));

        // Soft delete in database
        media.setDeletedAt(LocalDateTime.now());
        mediaRepository.save(media);

        // Delete from S3
        s3Service.deleteFile(media.getFileSystemId());
    }

    private String getFileExtension(String filename) {
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    private String constructS3Key(UUID teamId, UUID userId, UUID mediaId, String extension) {
        StringBuilder key = new StringBuilder();

        if (teamId != null) {
            key.append(teamId.toString()).append("/");
        }
        if (userId != null) {
            key.append(userId.toString()).append("/");
        }
        key.append(mediaId.toString()).append(".").append(extension);
        return key.toString();
    }

    private String determineMediaType(String mimeType) {
        if (mimeType.startsWith("image/"))
            return "IMAGE";
        if (mimeType.startsWith("video/"))
            return "VIDEO";
        if (mimeType.startsWith("audio/"))
            return "AUDIO";
        return "DOCUMENT";
    }

    public ResponseEntity<Resource> streamMedia(UUID mediaId) {
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new ResourceNotFoundException("Media not found"));

        if (media.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Media has been deleted");
        }

        Resource resource = s3Service.getFileAsResource(media.getFileSystemId());
        ObjectMetadata metadata = s3Service.getFileMetadata(media.getFileSystemId());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(media.getMimeType()))
                .contentLength(metadata.getContentLength())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + media.getFileName() + "\"")
                .body(resource);
    }

    public ResponseEntity<Resource> streamMediaInline(UUID mediaId) {
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new ResourceNotFoundException("Media not found"));

        if (media.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Media has been deleted");
        }

        Resource resource = s3Service.getFileAsResource(media.getFileSystemId());
        ObjectMetadata metadata = s3Service.getFileMetadata(media.getFileSystemId());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(media.getMimeType()))
                .contentLength(metadata.getContentLength())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + media.getFileName() + "\"")
                .body(resource);
    }
}