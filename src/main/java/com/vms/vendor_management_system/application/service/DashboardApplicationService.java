package com.vms.vendor_management_system.application.service;

import com.vms.vendor_management_system.application.dto.contract.ContractResponse;
import com.vms.vendor_management_system.application.dto.dashboard.DashboardSummary;
import com.vms.vendor_management_system.application.dto.purchaseorder.PurchaseOrderResponse;
import com.vms.vendor_management_system.application.dto.user.UserResponse;
import com.vms.vendor_management_system.application.dto.vendor.VendorResponse;
import com.vms.vendor_management_system.application.dto.vendorrequest.VendorCreationRequestResponse;
import com.vms.vendor_management_system.application.mapper.ContractMapper;
import com.vms.vendor_management_system.application.mapper.PurchaseOrderMapper;
import com.vms.vendor_management_system.application.mapper.UserMapper;
import com.vms.vendor_management_system.application.mapper.VendorCreationRequestMapper;
import com.vms.vendor_management_system.application.mapper.VendorMapper;
import com.vms.vendor_management_system.domain.enums.RequestStatus;
import com.vms.vendor_management_system.domain.enums.VendorStatus;
import com.vms.vendor_management_system.domain.repository.ContractRepository;
import com.vms.vendor_management_system.domain.repository.PurchaseOrderRepository;
import com.vms.vendor_management_system.domain.repository.UserRepository;
import com.vms.vendor_management_system.domain.repository.VendorCreationRequestRepository;
import com.vms.vendor_management_system.domain.repository.VendorRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Aggregates dashboard metrics for presentation layer.
 */
@Service
@Transactional(readOnly = true)
public class DashboardApplicationService {

    private static final int DEFAULT_LIMIT = 5;

    private final UserRepository userRepository;
    private final VendorRepository vendorRepository;
    private final VendorCreationRequestRepository vendorCreationRequestRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final ContractRepository contractRepository;

    public DashboardApplicationService(UserRepository userRepository,
                                       VendorRepository vendorRepository,
                                       VendorCreationRequestRepository vendorCreationRequestRepository,
                                       PurchaseOrderRepository purchaseOrderRepository,
                                       ContractRepository contractRepository) {
        this.userRepository = userRepository;
        this.vendorRepository = vendorRepository;
        this.vendorCreationRequestRepository = vendorCreationRequestRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.contractRepository = contractRepository;
    }

    public DashboardSummary getSummary() {
        long activeUsers = userRepository.countActiveUsers();
        
        // Count actual vendors
        long actualVendors = vendorRepository.count();
        long activeVendors = vendorRepository.countByStatus(VendorStatus.ACTIVE);
        // Only count PENDING_CREATION as pending - APPROVED vendors should be activated
        long pendingVendors = vendorRepository.countByStatus(VendorStatus.PENDING_CREATION);
        long inactiveVendors = vendorRepository.countByStatus(VendorStatus.INACTIVE);
        long suspendedVendors = vendorRepository.countByStatus(VendorStatus.SUSPENDED);
        
        // Count pending vendor requests (pending finance review + pending compliance review + pending admin review)
        long pendingFinanceRequests = vendorCreationRequestRepository.countByStatus(RequestStatus.PENDING_FINANCE_REVIEW);
        long pendingComplianceRequests = vendorCreationRequestRepository.countByStatus(RequestStatus.PENDING_COMPLIANCE_REVIEW);
        long pendingAdminRequests = vendorCreationRequestRepository.countByStatus(RequestStatus.PENDING_ADMIN_REVIEW);
        long pendingRequests = pendingFinanceRequests + pendingComplianceRequests + pendingAdminRequests;
        
        // Total vendors = actual vendors + pending vendor requests (requests that will become vendors)
        long totalVendors = actualVendors + pendingRequests;
        
        long totalPurchaseOrders = purchaseOrderRepository.count();
        long totalContracts = contractRepository.count();

        List<VendorResponse> latestVendors = vendorRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, DEFAULT_LIMIT))
                .stream()
                .map(VendorMapper::toResponse)
                .toList();

        List<VendorCreationRequestResponse> latestRequests = vendorCreationRequestRepository.findByStatusIn(
                        List.of(RequestStatus.PENDING_FINANCE_REVIEW, RequestStatus.PENDING_COMPLIANCE_REVIEW, RequestStatus.PENDING_ADMIN_REVIEW))
                .stream()
                .limit(DEFAULT_LIMIT)
                .map(VendorCreationRequestMapper::toResponse)
                .toList();

        List<UserResponse> latestUsers = userRepository.findByIsActiveTrue()
                .stream()
                .limit(DEFAULT_LIMIT)
                .map(UserMapper::toResponse)
                .toList();

        // Get pending approval purchase orders (PENDING_APPROVAL status)
        List<PurchaseOrderResponse> pendingApprovalPOs = purchaseOrderRepository.findByStatus("PENDING_APPROVAL")
                .stream()
                .limit(DEFAULT_LIMIT)
                .map(PurchaseOrderMapper::toResponse)
                .toList();

        // Get contracts expiring in next 90 days
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(90);
        List<ContractResponse> expiringContracts = contractRepository.findContractsExpiringBetween(today, futureDate)
                .stream()
                .limit(DEFAULT_LIMIT)
                .map(ContractMapper::toResponse)
                .toList();

        // Calculate real financial metrics from actual data
        // Total spend from Purchase Orders this year (approved, sent, or received)
        BigDecimal totalSpendFromPOs = purchaseOrderRepository.findAll().stream()
                .filter(po -> {
                    if (po.getOrderDate() == null || po.getStatus() == null) return false;
                    return po.getOrderDate().getYear() == today.getYear() 
                            && (po.getStatus().equals("APPROVED") || po.getStatus().equals("SENT") || po.getStatus().equals("RECEIVED"));
                })
                .map(po -> po.getTotalAmount() != null ? po.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Total value from active contracts
        BigDecimal totalContractValue = contractRepository.findAll().stream()
                .filter(c -> {
                    if (c.getStatus() == null) return false;
                    return c.getStatus().equals("ACTIVE") || c.getStatus().equals("APPROVED");
                })
                .map(c -> c.getContractValue() != null ? c.getContractValue() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Total spend from all Purchase Orders (all time)
        BigDecimal totalPurchaseOrderSpend = purchaseOrderRepository.findAll().stream()
                .filter(po -> po.getStatus() != null && 
                        (po.getStatus().equals("APPROVED") || po.getStatus().equals("SENT") || po.getStatus().equals("RECEIVED")))
                .map(po -> po.getTotalAmount() != null ? po.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Count pending approvals (pending vendor requests + pending approval POs)
        long pendingApprovalsCount = pendingRequests + purchaseOrderRepository.findByStatus("PENDING_APPROVAL").size();

        return DashboardSummary.builder()
                .totalActiveUsers(activeUsers)
                .totalVendors(totalVendors)
                .activeVendors(activeVendors)
                .pendingVendors(pendingVendors)
                .inactiveVendors(inactiveVendors)
                .suspendedVendors(suspendedVendors)
                .pendingVendorRequests(pendingRequests)
                .submittedRequests(pendingFinanceRequests)  // Using pendingFinanceRequests for submittedRequests
                .underReviewRequests(pendingComplianceRequests)  // Using pendingComplianceRequests for underReviewRequests
                .totalPurchaseOrders(totalPurchaseOrders)
                .totalContracts(totalContracts)
                .totalPurchaseOrderSpend(totalPurchaseOrderSpend.doubleValue())
                .totalContractValue(totalContractValue.doubleValue())
                .totalSpendYtd(totalSpendFromPOs.doubleValue())
                .pendingApprovalsCount(pendingApprovalsCount)
                .latestVendors(latestVendors)
                .latestVendorRequests(latestRequests)
                .latestUsers(latestUsers)
                .pendingApprovalPurchaseOrders(pendingApprovalPOs)
                .expiringContracts(expiringContracts)
                .build();
    }
}

