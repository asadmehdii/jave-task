package com.biosteel.teams.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatTokenDTO {
    private String token;
    private String apiKey;
    private String userId;
}
