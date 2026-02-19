package com.vms.vendor_management_system.domain.enums;

/**
 * Enum representing different statuses of vendors in the system
 */
public enum VendorStatus {
    PENDING_CREATION("Pending Creation"),
    UNDER_REVIEW("Under Review"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    SUSPENDED("Suspended"),
    TERMINATED("Terminated");

    private final String displayName;

    VendorStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
