package com.vms.vendor_management_system.application.dto.contract;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response representation of a contract.
 */
@Value
@Builder
public class ContractResponse {
    Long id;
    Long vendorId;
    String vendorName;
    String contractNumber;
    String title;
    String description;
    BigDecimal contractValue;
    LocalDate startDate;
    LocalDate endDate;
    String contractType;
    String status;
    String currency;
    String documentUrl;
    String termsAndConditions;
    Long createdByUserId;
    String createdByUsername;
    Long approvedByUserId;
    String approvedByUsername;
    LocalDateTime approvedAt;
    String renewalTerms;
    String terminationClause;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}

