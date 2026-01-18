package com.vms.vendor_management_system.application.service;

import com.vms.vendor_management_system.application.dto.purchaseorder.CreatePurchaseOrderRequest;
import com.vms.vendor_management_system.application.dto.purchaseorder.PurchaseOrderResponse;
import com.vms.vendor_management_system.application.mapper.PurchaseOrderMapper;
import com.vms.vendor_management_system.domain.entity.PurchaseOrder;
import com.vms.vendor_management_system.domain.entity.User;
import com.vms.vendor_management_system.domain.entity.Vendor;
import com.vms.vendor_management_system.domain.repository.PurchaseOrderRepository;
import com.vms.vendor_management_system.domain.repository.UserRepository;
import com.vms.vendor_management_system.domain.repository.VendorRepository;
import com.vms.vendor_management_system.domain.repository.VendorCreationRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Application service for purchase order lifecycle.
 */
@Service
@Transactional
public class PurchaseOrderApplicationService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final VendorRepository vendorRepository;
    private final UserRepository userRepository;
    private final VendorCreationRequestRepository vendorCreationRequestRepository;

    public PurchaseOrderApplicationService(PurchaseOrderRepository purchaseOrderRepository,
                                          VendorRepository vendorRepository,
                                          UserRepository userRepository,
                                          VendorCreationRequestRepository vendorCreationRequestRepository) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.vendorRepository = vendorRepository;
        this.userRepository = userRepository;
        this.vendorCreationRequestRepository = vendorCreationRequestRepository;
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> getPurchaseOrdersForVendor(Long vendorId) {
        return purchaseOrderRepository.findByVendorId(vendorId)
                .stream()
                .map(PurchaseOrderMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PurchaseOrderResponse getPurchaseOrder(Long purchaseOrderId) {
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Purchase order not found"));
        return PurchaseOrderMapper.toResponse(purchaseOrder);
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> getPurchaseOrdersByStatus(String status) {
        return purchaseOrderRepository.findByStatus(status)
                .stream()
                .map(PurchaseOrderMapper::toResponse)
                .collect(Collectors.toList());
    }

    public PurchaseOrderResponse createPurchaseOrder(CreatePurchaseOrderRequest request) {
        Vendor vendor = vendorRepository.findById(request.getVendorId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Vendor not found"));
        User creator = userRepository.findById(request.getCreatedByUserId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Creator not found"));
        
        // If currency is not provided, get it from the vendor's creation request
        String currency = request.getCurrency();
        if (currency == null || currency.trim().isEmpty()) {
            List<com.vms.vendor_management_system.domain.entity.VendorCreationRequest> vendorRequests = 
                vendorCreationRequestRepository.findByVendorId(vendor.getId());
            if (!vendorRequests.isEmpty()) {
                // Get currency from the most recent ACTIVE vendor creation request
                // This is the request that created this vendor, containing the requester's chosen currency
                com.vms.vendor_management_system.domain.entity.VendorCreationRequest vendorRequest = vendorRequests.get(0);
                currency = vendorRequest.getCurrency();
            }
        }
        
        // Always set the currency if we found one from the vendor request, even if request provided empty currency
        if (currency != null && !currency.trim().isEmpty()) {
            request.setCurrency(currency);
        }
        
        PurchaseOrder purchaseOrder = PurchaseOrderMapper.toEntity(request, vendor, creator);
        PurchaseOrder saved = purchaseOrderRepository.save(purchaseOrder);
        return PurchaseOrderMapper.toResponse(saved);
    }

    public PurchaseOrderResponse submitForApproval(Long purchaseOrderId) {
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Purchase order not found"));
        purchaseOrder.submitForApproval();
        PurchaseOrder saved = purchaseOrderRepository.save(purchaseOrder);
        return PurchaseOrderMapper.toResponse(saved);
    }

    public PurchaseOrderResponse approvePurchaseOrder(Long purchaseOrderId, Long approverId) {
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Purchase order not found"));
        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Approver not found"));
        purchaseOrder.approve(approver);
        PurchaseOrder saved = purchaseOrderRepository.save(purchaseOrder);
        return PurchaseOrderMapper.toResponse(saved);
    }

    public PurchaseOrderResponse rejectPurchaseOrder(Long purchaseOrderId, Long approverId, String rejectionReason) {
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Purchase order not found"));
        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Approver not found"));
        purchaseOrder.reject(approver, rejectionReason);
        PurchaseOrder saved = purchaseOrderRepository.save(purchaseOrder);
        return PurchaseOrderMapper.toResponse(saved);
    }

    public PurchaseOrderResponse sendPurchaseOrder(Long purchaseOrderId) {
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Purchase order not found"));
        purchaseOrder.send();
        PurchaseOrder saved = purchaseOrderRepository.save(purchaseOrder);
        return PurchaseOrderMapper.toResponse(saved);
    }

    public PurchaseOrderResponse markAsReceived(Long purchaseOrderId) {
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Purchase order not found"));
        purchaseOrder.markAsReceived();
        PurchaseOrder saved = purchaseOrderRepository.save(purchaseOrder);
        return PurchaseOrderMapper.toResponse(saved);
    }

    public void cancelPurchaseOrder(Long purchaseOrderId) {
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Purchase order not found"));
        purchaseOrder.cancel();
        purchaseOrderRepository.save(purchaseOrder);
    }
}






