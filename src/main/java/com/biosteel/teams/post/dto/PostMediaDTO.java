package com.biosteel.teams.post.dto;

import java.util.UUID;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PostMediaDTO {
    private UUID id;
    private UUID mediaId;
    private Integer displayOrder;
    private String title;
    private String body;
}
