package com.vms.vendor_management_system.domain.enums;

/**
 * Enum representing different statuses of vendor creation requests
 * New Workflow: DRAFT → PENDING_COMPLIANCE_REVIEW → PENDING_FINANCE_REVIEW → PENDING_ADMIN_REVIEW → ACTIVE
 */
public enum RequestStatus {
    DRAFT("Draft"),
    PENDING_COMPLIANCE_REVIEW("Pending Compliance Review"),
    REJECTED_BY_COMPLIANCE("Rejected by Compliance"),
    PENDING_FINANCE_REVIEW("Pending Finance Review"),
    REJECTED_BY_FINANCE("Rejected by Finance"),
    PENDING_ADMIN_REVIEW("Pending Admin Review"),
    REJECTED_BY_ADMIN("Rejected by Admin"),
    ACTIVE("Active Vendor"),
    CANCELLED("Cancelled"),
    RETURNED_FOR_INFO("Returned for Additional Information");

    private final String displayName;

    RequestStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
