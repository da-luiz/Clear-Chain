package com.vms.vendor_management_system.application.dto.vendorcategory;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * Response representation of a vendor category.
 */
@Value
@Builder
public class VendorCategoryResponse {
    Long id;
    String name;
    String description;
    String code;
    Boolean isActive;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}






