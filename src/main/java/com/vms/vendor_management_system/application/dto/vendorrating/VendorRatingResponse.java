package com.vms.vendor_management_system.application.dto.vendorrating;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * Response representation of a vendor rating.
 */
@Value
@Builder
public class VendorRatingResponse {
    Long id;
    Long vendorId;
    String vendorName;
    Long criteriaId;
    String criteriaName;
    Integer score;
    String comments;
    String evidenceUrl;
    Long ratedByUserId;
    String ratedByUsername;
    LocalDateTime ratingPeriodStart;
    LocalDateTime ratingPeriodEnd;
    Double weightedScore;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}






