package com.biosteel.teams.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationInfoDto {
    private String name;
    private String version;
    private String status;
    private String environment;
}