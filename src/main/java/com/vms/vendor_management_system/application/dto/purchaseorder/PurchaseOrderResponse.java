package com.vms.vendor_management_system.application.dto.purchaseorder;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response representation of a purchase order.
 */
@Value
@Builder
public class PurchaseOrderResponse {
    Long id;
    String poNumber;
    Long vendorId;
    String vendorName;
    String description;
    BigDecimal totalAmount;
    String currency;
    LocalDate orderDate;
    LocalDate expectedDeliveryDate;
    String status;
    BigDecimal approvalThreshold;
    Long createdByUserId;
    String createdByUsername;
    Long approvedByUserId;
    String approvedByUsername;
    LocalDateTime approvedAt;
    String rejectionReason;
    String deliveryAddress;
    String paymentTerms;
    String notes;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}






