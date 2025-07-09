package com.biosteel.teams.media.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaDto {
    private UUID mediaId;
    private UUID teamId;
    private UUID userId;
    private String mediaType;
    private String fileSystemId;
    private String fileName;
    private String fileExtension;
    private Long fileSize;
    private String title;
    private String body;
    private String mimeType;
    private String url;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}