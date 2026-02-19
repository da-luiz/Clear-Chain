package com.vms.vendor_management_system.domain.enums;

/**
 * Enum representing different user roles in the vendor management system
 * Based on vendor request workflow roles
 */
public enum UserRole {
    ADMIN("VMS Admin (General Overseer)"),
    DEPARTMENT_REQUESTER("Department Requester"),
    FINANCE_APPROVER("Finance Approver"),
    COMPLIANCE_APPROVER("Compliance Approver");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
