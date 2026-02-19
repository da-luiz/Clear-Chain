package com.vms.vendor_management_system.application.mapper;

import com.vms.vendor_management_system.application.dto.purchaseorder.CreatePurchaseOrderRequest;
import com.vms.vendor_management_system.application.dto.purchaseorder.PurchaseOrderResponse;
import com.vms.vendor_management_system.domain.entity.PurchaseOrder;
import com.vms.vendor_management_system.domain.entity.User;
import com.vms.vendor_management_system.domain.entity.Vendor;

import java.util.Locale;
import java.util.UUID;

/**
 * Utilities to convert between purchase order entities and DTOs.
 */
public final class PurchaseOrderMapper {

    private PurchaseOrderMapper() {
    }

    public static PurchaseOrder toEntity(CreatePurchaseOrderRequest request, Vendor vendor, User creator) {
        String poNumber = generatePoNumber(vendor.getCompanyName());
        PurchaseOrder purchaseOrder = new PurchaseOrder(
                poNumber,
                vendor,
                request.getDescription(),
                request.getTotalAmount(),
                request.getOrderDate(),
                creator
        );
        purchaseOrder.setExpectedDeliveryDate(request.getExpectedDeliveryDate());
        purchaseOrder.setApprovalThreshold(request.getApprovalThreshold());
        purchaseOrder.setCurrency(request.getCurrency());
        purchaseOrder.setDeliveryAddress(request.getDeliveryAddress());
        purchaseOrder.setPaymentTerms(request.getPaymentTerms());
        purchaseOrder.setNotes(request.getNotes());
        return purchaseOrder;
    }

    public static PurchaseOrderResponse toResponse(PurchaseOrder purchaseOrder) {
        if (purchaseOrder == null) {
            return null;
        }

        return PurchaseOrderResponse.builder()
                .id(purchaseOrder.getId())
                .poNumber(purchaseOrder.getPoNumber())
                .vendorId(purchaseOrder.getVendor() != null ? purchaseOrder.getVendor().getId() : null)
                .vendorName(purchaseOrder.getVendor() != null ? purchaseOrder.getVendor().getCompanyName() : null)
                .description(purchaseOrder.getDescription())
                .totalAmount(purchaseOrder.getTotalAmount())
                .currency(purchaseOrder.getCurrency())
                .orderDate(purchaseOrder.getOrderDate())
                .expectedDeliveryDate(purchaseOrder.getExpectedDeliveryDate())
                .status(purchaseOrder.getStatus())
                .approvalThreshold(purchaseOrder.getApprovalThreshold())
                .createdByUserId(purchaseOrder.getCreatedBy() != null ? purchaseOrder.getCreatedBy().getId() : null)
                .createdByUsername(purchaseOrder.getCreatedBy() != null ? purchaseOrder.getCreatedBy().getUsername() : null)
                .approvedByUserId(purchaseOrder.getApprovedBy() != null ? purchaseOrder.getApprovedBy().getId() : null)
                .approvedByUsername(purchaseOrder.getApprovedBy() != null ? purchaseOrder.getApprovedBy().getUsername() : null)
                .approvedAt(purchaseOrder.getApprovedAt())
                .rejectionReason(purchaseOrder.getRejectionReason())
                .deliveryAddress(purchaseOrder.getDeliveryAddress())
                .paymentTerms(purchaseOrder.getPaymentTerms())
                .notes(purchaseOrder.getNotes())
                .createdAt(purchaseOrder.getCreatedAt())
                .updatedAt(purchaseOrder.getUpdatedAt())
                .build();
    }

    private static String generatePoNumber(String companyName) {
        String base = companyName == null ? "PO" :
                companyName.replaceAll("[^A-Za-z0-9]", "")
                        .toUpperCase(Locale.ROOT)
                        .substring(0, Math.min(4, companyName.length()));
        String suffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        return "PO-" + base + "-" + suffix;
    }
}






