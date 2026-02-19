package com.vms.vendor_management_system.presentation.controller;

import com.vms.vendor_management_system.application.dto.vendorrequest.AddBankingDetailsRequest;
import com.vms.vendor_management_system.application.dto.vendorrequest.CreateVendorCreationRequest;
import com.vms.vendor_management_system.application.dto.vendorrequest.UpdateVendorCreationRequest;
import com.vms.vendor_management_system.application.dto.vendorrequest.VendorCreationRequestResponse;
import com.vms.vendor_management_system.application.dto.vendorrequest.VendorRequestAction;
import com.vms.vendor_management_system.application.service.VendorCreationRequestApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for vendor creation request workflow.
 */
@RestController
@RequestMapping("/api/vendor-requests")
@Validated
public class VendorCreationRequestController {

    private final VendorCreationRequestApplicationService applicationService;

    public VendorCreationRequestController(VendorCreationRequestApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping("/pending")
    public List<VendorCreationRequestResponse> getPendingRequests() {
        return applicationService.getPendingRequests();
    }

    @GetMapping("/{id}")
    public VendorCreationRequestResponse getRequest(@PathVariable Long id) {
        return applicationService.getRequest(id);
    }

    @PostMapping
    public ResponseEntity<VendorCreationRequestResponse> createRequest(@Valid @RequestBody CreateVendorCreationRequest request) {
        VendorCreationRequestResponse response = applicationService.createRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public VendorCreationRequestResponse updateDraft(@PathVariable Long id, @Valid @RequestBody UpdateVendorCreationRequest request) {
        return applicationService.updateDraft(id, request);
    }

    @PostMapping("/{id}/submit")
    public VendorCreationRequestResponse submit(@PathVariable Long id) {
        return applicationService.submit(id);
    }

    @PostMapping("/{id}/banking-details")
    public VendorCreationRequestResponse addBankingDetails(@PathVariable Long id, @Valid @RequestBody AddBankingDetailsRequest request) {
        return applicationService.addBankingDetails(id, request);
    }

    @PostMapping("/{id}/finance/approve")
    public VendorCreationRequestResponse approveByFinance(@PathVariable Long id, @Valid @RequestBody VendorRequestAction action) {
        return applicationService.approveByFinance(id, action);
    }

    @PostMapping("/{id}/finance/reject")
    public VendorCreationRequestResponse rejectByFinance(@PathVariable Long id, @Valid @RequestBody VendorRequestAction action) {
        return applicationService.rejectByFinance(id, action);
    }

    @PostMapping("/{id}/compliance/approve")
    public VendorCreationRequestResponse approveByCompliance(@PathVariable Long id, @Valid @RequestBody VendorRequestAction action) {
        return applicationService.approveByCompliance(id, action);
    }

    @PostMapping("/{id}/compliance/reject")
    public VendorCreationRequestResponse rejectByCompliance(@PathVariable Long id, @Valid @RequestBody VendorRequestAction action) {
        return applicationService.rejectByCompliance(id, action);
    }

    @PostMapping("/{id}/admin/approve")
    public VendorCreationRequestResponse approveByAdmin(@PathVariable Long id, @Valid @RequestBody VendorRequestAction action) {
        return applicationService.approveByAdmin(id, action);
    }

    @PostMapping("/{id}/admin/reject")
    public VendorCreationRequestResponse rejectByAdmin(@PathVariable Long id, @Valid @RequestBody VendorRequestAction action) {
        return applicationService.rejectByAdmin(id, action);
    }

    // Legacy endpoints for backward compatibility
    @Deprecated
    @PostMapping("/{id}/approve")
    public VendorCreationRequestResponse approve(@PathVariable Long id, @Valid @RequestBody VendorRequestAction action) {
        return applicationService.approve(id, action);
    }

    @Deprecated
    @PostMapping("/{id}/reject")
    public VendorCreationRequestResponse reject(@PathVariable Long id, @Valid @RequestBody VendorRequestAction action) {
        return applicationService.reject(id, action);
    }

    @PostMapping("/{id}/return")
    public VendorCreationRequestResponse returnForInfo(@PathVariable Long id, @Valid @RequestBody VendorRequestAction action) {
        return applicationService.requestAdditionalInfo(id, action);
    }

    @PostMapping("/{id}/cancel")
    public VendorCreationRequestResponse cancel(@PathVariable Long id) {
        return applicationService.cancel(id);
    }
}

