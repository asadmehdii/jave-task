package com.biosteel.teams.sport.model;

import java.io.Serializable;
import java.util.Objects;

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
public class SportAttributeMappingId implements Serializable {

    private static final long serialVersionUID = 1L;

    private String sportTypeCode;
    private String attributeCode;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        SportAttributeMappingId that = (SportAttributeMappingId) o;
        return Objects.equals(sportTypeCode, that.sportTypeCode) &&
                Objects.equals(attributeCode, that.attributeCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sportTypeCode, attributeCode);
    }
}