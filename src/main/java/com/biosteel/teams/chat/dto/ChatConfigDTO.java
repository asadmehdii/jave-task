package com.biosteel.teams.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatConfigDTO {
    private String apiKey;
    private String userId;
    private String userToken;
}