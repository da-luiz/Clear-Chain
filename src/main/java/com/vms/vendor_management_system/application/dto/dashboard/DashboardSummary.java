package com.vms.vendor_management_system.application.dto.dashboard;

import com.vms.vendor_management_system.application.dto.contract.ContractResponse;
import com.vms.vendor_management_system.application.dto.purchaseorder.PurchaseOrderResponse;
import com.vms.vendor_management_system.application.dto.user.UserResponse;
import com.vms.vendor_management_system.application.dto.vendor.VendorResponse;
import com.vms.vendor_management_system.application.dto.vendorrequest.VendorCreationRequestResponse;
import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * High-level dashboard metrics.
 * Aligned with ClearChain workflow: Vendor Onboarding → Contract Setup → Procurement.
 */
@Value
@Builder
public class DashboardSummary {
    long totalActiveUsers;
    long totalVendors;
    long activeVendors;
    long approvedVendors;       // Approved + Active (for dashboard "Approved Vendors" card)
    long pendingVendors;
    long inactiveVendors;
    long suspendedVendors;
    long pendingVendorRequests;
    long submittedRequests;
    long underReviewRequests;
    long totalPurchaseOrders;
    long totalContracts;
    long activeContractsCount;   // Count of contracts with status ACTIVE

    // Real financial metrics from actual data
    Double totalPurchaseOrderSpend;
    Double totalContractValue;
    Double totalSpendYtd;
    long pendingApprovalsCount;

    List<VendorResponse> latestVendors;
    List<VendorCreationRequestResponse> latestVendorRequests;
    List<UserResponse> latestUsers;
    List<PurchaseOrderResponse> pendingApprovalPurchaseOrders;
    List<ContractResponse> expiringContracts;           // Expiring in 30 days (for dashboard card)
    List<ProcurementActivityPoint> procurementActivity; // Last 30 days for chart
    List<RecentActivityItem> recentActivity;
}

