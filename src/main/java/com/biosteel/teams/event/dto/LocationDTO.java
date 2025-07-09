package com.biosteel.teams.event.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LocationDTO {
    private UUID locationId;

    @Size(max = 255)
    private String addressLine1;

    @Size(max = 255)
    private String addressLine2;

    @Size(max = 255)
    private String majorIntersection;

    @Size(max = 100)
    private String postalZip;

    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String province;

    @Size(max = 100)
    private String country;

    private String placeName;
    private String placeGuid;
    private BigDecimal longitude;
    private BigDecimal latitude;
}