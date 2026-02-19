package com.vms.vendor_management_system.application.dto.vendor;

import com.vms.vendor_management_system.domain.enums.VendorStatus;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * Response payload representing vendor details.
 */
@Value
@Builder
public class VendorResponse {
    Long id;
    String vendorCode;
    String companyName;
    String legalName;
    String taxId;
    String email;
    String phone;
    String street;
    String city;
    String state;
    String postalCode;
    String country;
    VendorStatus status;
    Long categoryId;
    String categoryName;
    String website;
    String description;
    String notes;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}


