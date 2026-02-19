package com.vms.vendor_management_system.application.service;

import com.vms.vendor_management_system.application.dto.contract.ContractResponse;
import com.vms.vendor_management_system.application.dto.dashboard.DashboardSummary;
import com.vms.vendor_management_system.application.dto.dashboard.ProcurementActivityPoint;
import com.vms.vendor_management_system.application.dto.dashboard.RecentActivityItem;
import com.vms.vendor_management_system.application.dto.purchaseorder.PurchaseOrderResponse;
import com.vms.vendor_management_system.application.dto.user.UserResponse;
import com.vms.vendor_management_system.application.dto.vendor.VendorResponse;
import com.vms.vendor_management_system.application.dto.vendorrequest.VendorCreationRequestResponse;
import com.vms.vendor_management_system.application.mapper.ContractMapper;
import com.vms.vendor_management_system.application.mapper.PurchaseOrderMapper;
import com.vms.vendor_management_system.application.mapper.UserMapper;
import com.vms.vendor_management_system.application.mapper.VendorCreationRequestMapper;
import com.vms.vendor_management_system.application.mapper.VendorMapper;
import com.vms.vendor_management_system.domain.entity.Contract;
import com.vms.vendor_management_system.domain.entity.PurchaseOrder;
import com.vms.vendor_management_system.domain.entity.Vendor;
import com.vms.vendor_management_system.domain.entity.VendorCreationRequest;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        LocalDate today = LocalDate.now();

        // Count actual vendors
        long actualVendors = vendorRepository.count();
        long activeVendors = vendorRepository.countByStatus(VendorStatus.ACTIVE);
        long approvedVendorsCount = vendorRepository.countByStatusIn(List.of(VendorStatus.APPROVED, VendorStatus.ACTIVE));
        long pendingVendors = vendorRepository.countByStatus(VendorStatus.PENDING_CREATION);
        long inactiveVendors = vendorRepository.countByStatus(VendorStatus.INACTIVE);
        long suspendedVendors = vendorRepository.countByStatus(VendorStatus.SUSPENDED);

        // Pending vendor requests (Compliance + Admin review per ClearChain workflow)
        long pendingFinanceRequests = vendorCreationRequestRepository.countByStatus(RequestStatus.PENDING_FINANCE_REVIEW);
        long pendingComplianceRequests = vendorCreationRequestRepository.countByStatus(RequestStatus.PENDING_COMPLIANCE_REVIEW);
        long pendingAdminRequests = vendorCreationRequestRepository.countByStatus(RequestStatus.PENDING_ADMIN_REVIEW);
        long pendingRequests = pendingComplianceRequests + pendingAdminRequests + pendingFinanceRequests;

        long totalVendors = actualVendors + pendingRequests;
        long totalPurchaseOrders = purchaseOrderRepository.count();
        long totalContracts = contractRepository.count();
        long activeContractsCount = contractRepository.countByStatus("ACTIVE");

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

        List<PurchaseOrderResponse> pendingApprovalPOs = purchaseOrderRepository.findByStatus("PENDING_APPROVAL")
                .stream()
                .limit(DEFAULT_LIMIT)
                .map(PurchaseOrderMapper::toResponse)
                .toList();

        // Contracts expiring in next 30 days (ClearChain dashboard card)
        LocalDate in30Days = today.plusDays(30);
        List<ContractResponse> expiringContracts = contractRepository.findContractsExpiringBetween(today, in30Days)
                .stream()
                .limit(DEFAULT_LIMIT)
                .map(ContractMapper::toResponse)
                .toList();

        // Procurement activity: count of POs per day for last 30 days
        List<ProcurementActivityPoint> procurementActivity = buildProcurementActivity(today);

        // Recent activity feed: vendor approvals, contract created, PO created
        List<RecentActivityItem> recentActivity = buildRecentActivity();

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
        
        long pendingApprovalsCount = pendingRequests + purchaseOrderRepository.findByStatus("PENDING_APPROVAL").size();

        return DashboardSummary.builder()
                .totalActiveUsers(activeUsers)
                .totalVendors(totalVendors)
                .activeVendors(activeVendors)
                .approvedVendors(approvedVendorsCount)
                .pendingVendors(pendingVendors)
                .inactiveVendors(inactiveVendors)
                .suspendedVendors(suspendedVendors)
                .pendingVendorRequests(pendingRequests)
                .submittedRequests(pendingFinanceRequests)
                .underReviewRequests(pendingComplianceRequests)
                .totalPurchaseOrders(totalPurchaseOrders)
                .totalContracts(totalContracts)
                .activeContractsCount(activeContractsCount)
                .totalPurchaseOrderSpend(totalPurchaseOrderSpend.doubleValue())
                .totalContractValue(totalContractValue.doubleValue())
                .totalSpendYtd(totalSpendFromPOs.doubleValue())
                .pendingApprovalsCount(pendingApprovalsCount)
                .latestVendors(latestVendors)
                .latestVendorRequests(latestRequests)
                .latestUsers(latestUsers)
                .pendingApprovalPurchaseOrders(pendingApprovalPOs)
                .expiringContracts(expiringContracts)
                .procurementActivity(procurementActivity)
                .recentActivity(recentActivity)
                .build();
    }

    private List<ProcurementActivityPoint> buildProcurementActivity(LocalDate today) {
        LocalDate start = today.minusDays(30);
        List<PurchaseOrder> pos = purchaseOrderRepository.findByOrderDateRange(start, today);
        Map<LocalDate, Long> countByDate = pos.stream()
                .filter(po -> po.getOrderDate() != null)
                .collect(Collectors.groupingBy(PurchaseOrder::getOrderDate, Collectors.counting()));
        List<ProcurementActivityPoint> result = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(today); d = d.plusDays(1)) {
            result.add(ProcurementActivityPoint.builder()
                    .date(d)
                    .count(countByDate.getOrDefault(d, 0L))
                    .build());
        }
        return result;
    }

    private List<RecentActivityItem> buildRecentActivity() {
        List<RecentActivityItem> items = new ArrayList<>();
        // Vendors (created = approved in ClearChain flow)
        List<Vendor> recentVendors = vendorRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 5));
        for (Vendor v : recentVendors) {
            LocalDateTime ts = v.getCreatedAt() != null ? v.getCreatedAt() : LocalDateTime.now();
            items.add(RecentActivityItem.builder()
                    .type("VENDOR_APPROVED")
                    .message("Vendor " + (v.getCompanyName() != null ? v.getCompanyName() : "Unknown") + " approved")
                    .timestamp(ts)
                    .entityType("vendor")
                    .entityId(v.getId())
                    .build());
        }
        // Contracts created
        List<Contract> recentContracts = contractRepository.findAll().stream()
                .sorted(Comparator.comparing(Contract::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .toList();
        for (Contract c : recentContracts) {
            LocalDateTime ts = c.getCreatedAt() != null ? c.getCreatedAt() : LocalDateTime.now();
            String vendorName = c.getVendor() != null ? c.getVendor().getCompanyName() : "Unknown";
            items.add(RecentActivityItem.builder()
                    .type("CONTRACT_CREATED")
                    .message("Contract created for " + vendorName)
                    .timestamp(ts)
                    .entityType("contract")
                    .entityId(c.getId())
                    .build());
        }
        // Purchase orders created
        List<PurchaseOrder> recentPOs = purchaseOrderRepository.findAll().stream()
                .sorted(Comparator.comparing(PurchaseOrder::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .toList();
        for (PurchaseOrder po : recentPOs) {
            LocalDateTime ts = po.getCreatedAt() != null ? po.getCreatedAt() : LocalDateTime.now();
            items.add(RecentActivityItem.builder()
                    .type("PURCHASE_ORDER_CREATED")
                    .message("Purchase order #" + (po.getPoNumber() != null ? po.getPoNumber() : po.getId()) + " created")
                    .timestamp(ts)
                    .entityType("purchase-order")
                    .entityId(po.getId())
                    .build());
        }
        return items.stream()
                .sorted(Comparator.comparing(RecentActivityItem::getTimestamp, Comparator.reverseOrder()))
                .limit(10)
                .toList();
    }
}

