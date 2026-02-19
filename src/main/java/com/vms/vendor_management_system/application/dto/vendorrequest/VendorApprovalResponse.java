package com.vms.vendor_management_system.application.dto.vendorrequest;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * Response representation of an approval decision.
 */
@Value
@Builder
public class VendorApprovalResponse {
    Long id;
    Long vendorCreationRequestId;
    Long approverId;
    String approverUsername;
    String approvalStatus;
    String comments;
    LocalDateTime approvedAt;
}

