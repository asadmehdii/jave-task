package com.biosteel.teams.team.dto;

import java.util.List;

import com.biosteel.teams.event.dto.EventResponseDTO;
import com.biosteel.teams.post.dto.PostDTO;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TeamFeedDTO {
    private List<PostDTO> posts;
    private List<EventResponseDTO> events;
}