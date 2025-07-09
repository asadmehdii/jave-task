// PeriodType.java
package com.biosteel.teams.game.model;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.biosteel.teams.sport.model.SportType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sport_period_definition", schema = "biosteel")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SportPeriodDefinition {

    @Id
    @Column(name = "sport_period_definition_id")
    private UUID sportPeriodDefinitionId;

    @Column(name = "sport_type_code")
    private String sportTypeCode;

    @ManyToOne
    @JoinColumn(name = "sport_type_code", referencedColumnName = "code", insertable = false, updatable = false)
    private SportType sportType;

    @Column(name = "period_type_code")
    private String periodTypeCode;

    @ManyToOne
    @JoinColumn(name = "period_type_code", referencedColumnName = "code", insertable = false, updatable = false)
    private PeriodType periodType;

    @Column(name = "period_number")
    private Integer periodNumber;

    @Column(name = "period_name")
    private String periodName;

    @Column(name = "default_duration_minutes")
    private Integer defaultDurationMinutes;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "is_scoring_period")
    private Boolean isScoringPeriod;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;
}