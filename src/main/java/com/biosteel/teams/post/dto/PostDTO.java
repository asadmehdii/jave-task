package com.biosteel.teams.post.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PostDTO {
    private UUID id;
    private UUID teamId;
    private UUID eventId;
    private UUID userId;
    private String postType;
    private String layoutType;
    private String content;
    private String visibility;
    private LocalDateTime createdAt;
    private List<PostMediaDTO> media;
    private List<PostCommentDTO> comments;
    private List<PostReactionDTO> reactions;
}
