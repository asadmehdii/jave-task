package com.biosteel.teams.post.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PostReactionDTO {
    private UUID id;
    private UUID userId;
    private String reactionType;
    private LocalDateTime createdAt;
}