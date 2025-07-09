package com.biosteel.teams.team.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamAttributeValueId implements Serializable {

    @Column(name = "team_id")
    private UUID teamId;

    @Column(name = "attribute_code")
    private String attributeCode;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        TeamAttributeValueId that = (TeamAttributeValueId) o;
        return Objects.equals(teamId, that.teamId) &&
                Objects.equals(attributeCode, that.attributeCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamId, attributeCode);
    }
}