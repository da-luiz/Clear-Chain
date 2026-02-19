package com.vms.vendor_management_system.application.dto.dashboard;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * Single item for the recent activity feed (e.g. "Vendor ABC approved", "Contract created for Delta").
 */
@Value
@Builder
public class RecentActivityItem {
    String type;       // VENDOR_APPROVED, CONTRACT_CREATED, PURCHASE_ORDER_CREATED
    String message;
    LocalDateTime timestamp;
    String entityType;  // vendor, contract, purchase-order
    Long entityId;
}
