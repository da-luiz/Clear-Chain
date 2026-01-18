package com.vms.vendor_management_system.application.dto.purchaseorder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for creating a new purchase order.
 */
@Getter
@Setter
public class CreatePurchaseOrderRequest {

    @NotNull(message = "Vendor ID is required")
    private Long vendorId;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Total amount is required")
    @Positive(message = "Total amount must be positive")
    private BigDecimal totalAmount;

    @Size(max = 10)
    private String currency;

    @NotNull(message = "Order date is required")
    private LocalDate orderDate;

    private LocalDate expectedDeliveryDate;

    private BigDecimal approvalThreshold;

    @NotNull(message = "Created by user ID is required")
    private Long createdByUserId;

    private String deliveryAddress;

    private String paymentTerms;

    private String notes;
}


