package com.vms.vendor_management_system.application.dto.vendorrating;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Payload for creating a vendor rating.
 */
@Getter
@Setter
public class CreateVendorRatingRequest {

    @NotNull
    private Long vendorId;

    @NotNull
    private Long criteriaId;

    @NotNull
    private Integer score;

    @Size(max = 2000)
    private String comments;

    private String evidenceUrl;

    @NotNull
    private Long ratedByUserId;

    private LocalDateTime ratingPeriodStart;

    private LocalDateTime ratingPeriodEnd;
}






