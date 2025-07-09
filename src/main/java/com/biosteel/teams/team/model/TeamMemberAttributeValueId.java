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
public class TeamMemberAttributeValueId implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID teamMemberId;

    @Column(name = "attribute_code")
    private String attributeCode;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        TeamMemberAttributeValueId that = (TeamMemberAttributeValueId) o;
        return Objects.equals(teamMemberId, that.teamMemberId) &&
                Objects.equals(attributeCode, that.attributeCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamMemberId, attributeCode);
    }
}