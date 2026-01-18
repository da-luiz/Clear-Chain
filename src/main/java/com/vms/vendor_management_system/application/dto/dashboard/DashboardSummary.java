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
 */
@Value
@Builder
public class DashboardSummary {
    long totalActiveUsers;
    long totalVendors;
    long activeVendors;
    long pendingVendors;
    long inactiveVendors;
    long suspendedVendors;
    long pendingVendorRequests;
    long submittedRequests;
    long underReviewRequests;
    long totalPurchaseOrders;
    long totalContracts;
    
    // Real financial metrics from actual data
    Double totalPurchaseOrderSpend;  // Total spend from all approved/sent/received purchase orders
    Double totalContractValue;  // Total value from active/approved contracts
    Double totalSpendYtd;  // Total spend year-to-date from purchase orders
    long pendingApprovalsCount;  // Total pending approvals/reviews
    List<VendorResponse> latestVendors;
    List<VendorCreationRequestResponse> latestVendorRequests;
    List<UserResponse> latestUsers;
    List<PurchaseOrderResponse> pendingApprovalPurchaseOrders;
    List<ContractResponse> expiringContracts;
}

