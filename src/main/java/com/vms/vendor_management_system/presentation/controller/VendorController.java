package com.vms.vendor_management_system.presentation.controller;

import com.vms.vendor_management_system.application.dto.vendor.CreateVendorRequest;
import com.vms.vendor_management_system.application.dto.vendor.VendorResponse;
import com.vms.vendor_management_system.application.service.VendorApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller exposing vendor management endpoints.
 */
@RestController
@RequestMapping("/api/vendors")
@Validated
public class VendorController {

    private final VendorApplicationService vendorApplicationService;

    public VendorController(VendorApplicationService vendorApplicationService) {
        this.vendorApplicationService = vendorApplicationService;
    }

    @GetMapping
    public List<VendorResponse> getRecentVendors(@RequestParam(defaultValue = "10") int limit) {
        int normalizedLimit = Math.min(Math.max(limit, 1), 100);
        return vendorApplicationService.getRecentVendors(normalizedLimit);
    }

    @GetMapping("/{id}")
    public VendorResponse getVendor(@PathVariable Long id) {
        return vendorApplicationService.getVendor(id);
    }

    // Direct vendor creation removed - vendors must be created through VendorCreationRequest workflow
    // @PostMapping
    // public ResponseEntity<VendorResponse> createVendor(@Valid @RequestBody CreateVendorRequest request) {
    //     VendorResponse response = vendorApplicationService.createVendor(request);
    //     return ResponseEntity.status(HttpStatus.CREATED).body(response);
    // }

    @PutMapping("/{id}")
    public VendorResponse updateVendor(@PathVariable Long id, @Valid @RequestBody CreateVendorRequest request) {
        return vendorApplicationService.updateVendor(id, request);
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<Void> activateVendor(@PathVariable Long id) {
        vendorApplicationService.activateVendor(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/suspend")
    public ResponseEntity<Void> suspendVendor(@PathVariable Long id) {
        vendorApplicationService.suspendVendor(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/terminate")
    public ResponseEntity<Void> terminateVendor(@PathVariable Long id) {
        vendorApplicationService.terminateVendor(id);
        return ResponseEntity.noContent().build();
    }
}


