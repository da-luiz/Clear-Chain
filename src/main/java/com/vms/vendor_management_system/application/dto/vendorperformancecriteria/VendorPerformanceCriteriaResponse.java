package com.vms.vendor_management_system.application.dto.vendorperformancecriteria;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * Response representation of vendor performance criteria.
 */
@Value
@Builder
public class VendorPerformanceCriteriaResponse {
    Long id;
    String name;
    String description;
    Double weight;
    Integer maxScore;
    Boolean isActive;
    Long categoryId;
    String categoryName;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}






