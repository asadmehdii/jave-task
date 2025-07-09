package com.biosteel.teams.event.model;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "location", schema = "biosteel")
@Data
public class Location {
    @Id
    @Column(name = "location_id")
    private UUID locationId;

    @Column(name = "address_line_1")
    private String addressLine1;

    @Column(name = "address_line_2")
    private String addressLine2;

    @Column(name = "major_intersection")
    private String majorIntersection;

    @Column(name = "postal_zip")
    private String postalZip;

    @Column(name = "city")
    private String city;

    @Column(name = "province")
    private String province;

    @Column(name = "country")
    private String country;

    @Column(name = "place_name")
    private String placeName;

    @Column(name = "place_guid")
    private String placeGuid;

    @Column(name = "longitude", columnDefinition = "numeric")
    private BigDecimal longitude;

    @Column(name = "latitude", columnDefinition = "numeric")
    private BigDecimal latitude;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @Column(name = "deleted_at")
    private ZonedDateTime deletedAt;
}