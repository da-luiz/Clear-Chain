package com.vms.vendor_management_system.presentation.controller;

import com.vms.vendor_management_system.application.dto.purchaseorder.CreatePurchaseOrderRequest;
import com.vms.vendor_management_system.application.dto.purchaseorder.PurchaseOrderResponse;
import com.vms.vendor_management_system.application.service.PurchaseOrderApplicationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for managing purchase orders.
 */
@RestController
@RequestMapping("/api/purchase-orders")
@Validated
public class PurchaseOrderController {

    private final PurchaseOrderApplicationService purchaseOrderApplicationService;

    public PurchaseOrderController(PurchaseOrderApplicationService purchaseOrderApplicationService) {
        this.purchaseOrderApplicationService = purchaseOrderApplicationService;
    }

    @GetMapping("/{id}")
    public PurchaseOrderResponse getPurchaseOrder(@PathVariable Long id) {
        return purchaseOrderApplicationService.getPurchaseOrder(id);
    }

    @GetMapping("/vendor/{vendorId}")
    public List<PurchaseOrderResponse> getPurchaseOrdersForVendor(@PathVariable Long vendorId) {
        return purchaseOrderApplicationService.getPurchaseOrdersForVendor(vendorId);
    }

    @GetMapping("/status/{status}")
    public List<PurchaseOrderResponse> getPurchaseOrdersByStatus(@PathVariable String status) {
        return purchaseOrderApplicationService.getPurchaseOrdersByStatus(status);
    }

    @PostMapping
    public ResponseEntity<PurchaseOrderResponse> createPurchaseOrder(@Valid @RequestBody CreatePurchaseOrderRequest request) {
        PurchaseOrderResponse response = purchaseOrderApplicationService.createPurchaseOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/submit")
    public PurchaseOrderResponse submitForApproval(@PathVariable Long id) {
        return purchaseOrderApplicationService.submitForApproval(id);
    }

    @PostMapping("/{id}/approve")
    public PurchaseOrderResponse approvePurchaseOrder(@PathVariable Long id, @RequestParam Long approverId) {
        return purchaseOrderApplicationService.approvePurchaseOrder(id, approverId);
    }

    @PostMapping("/{id}/reject")
    public PurchaseOrderResponse rejectPurchaseOrder(@PathVariable Long id, 
                                                     @RequestParam Long approverId,
                                                     @RequestParam @NotBlank String rejectionReason) {
        return purchaseOrderApplicationService.rejectPurchaseOrder(id, approverId, rejectionReason);
    }

    @PostMapping("/{id}/send")
    public PurchaseOrderResponse sendPurchaseOrder(@PathVariable Long id) {
        return purchaseOrderApplicationService.sendPurchaseOrder(id);
    }

    @PostMapping("/{id}/receive")
    public PurchaseOrderResponse markAsReceived(@PathVariable Long id) {
        return purchaseOrderApplicationService.markAsReceived(id);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelPurchaseOrder(@PathVariable Long id) {
        purchaseOrderApplicationService.cancelPurchaseOrder(id);
        return ResponseEntity.noContent().build();
    }
}






