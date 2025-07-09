package com.biosteel.teams.invitation.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkInvitationRequestDTO {
    @NotNull(message = "Team ID is required")
    private UUID teamId;

    private UUID eventId;

    @NotEmpty(message = "At least one invitation is required")
    @Valid
    private List<InvitationRecipientDTO> recipients;
}