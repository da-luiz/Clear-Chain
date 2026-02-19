package com.vms.vendor_management_system.presentation.controller;

import com.vms.vendor_management_system.application.dto.vendorperformancecriteria.CreateVendorPerformanceCriteriaRequest;
import com.vms.vendor_management_system.application.dto.vendorperformancecriteria.VendorPerformanceCriteriaResponse;
import com.vms.vendor_management_system.application.service.VendorPerformanceCriteriaApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for managing vendor performance criteria.
 */
@RestController
@RequestMapping("/api/vendor-performance-criteria")
@Validated
public class VendorPerformanceCriteriaController {

    private final VendorPerformanceCriteriaApplicationService criteriaApplicationService;

    public VendorPerformanceCriteriaController(VendorPerformanceCriteriaApplicationService criteriaApplicationService) {
        this.criteriaApplicationService = criteriaApplicationService;
    }

    @GetMapping
    public List<VendorPerformanceCriteriaResponse> getAllCriteria() {
        return criteriaApplicationService.getAllCriteria();
    }

    @GetMapping("/active")
    public List<VendorPerformanceCriteriaResponse> getActiveCriteria() {
        return criteriaApplicationService.getActiveCriteria();
    }

    @GetMapping("/category/{categoryId}")
    public List<VendorPerformanceCriteriaResponse> getCriteriaByCategory(@PathVariable Long categoryId) {
        return criteriaApplicationService.getCriteriaByCategory(categoryId);
    }

    @GetMapping("/{id}")
    public VendorPerformanceCriteriaResponse getCriteria(@PathVariable Long id) {
        return criteriaApplicationService.getCriteria(id);
    }

    @PostMapping
    public ResponseEntity<VendorPerformanceCriteriaResponse> createCriteria(@Valid @RequestBody CreateVendorPerformanceCriteriaRequest request) {
        VendorPerformanceCriteriaResponse response = criteriaApplicationService.createCriteria(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public VendorPerformanceCriteriaResponse updateCriteria(@PathVariable Long id, @Valid @RequestBody CreateVendorPerformanceCriteriaRequest request) {
        return criteriaApplicationService.updateCriteria(id, request);
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateCriteria(@PathVariable Long id) {
        criteriaApplicationService.deactivateCriteria(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<Void> activateCriteria(@PathVariable Long id) {
        criteriaApplicationService.activateCriteria(id);
        return ResponseEntity.noContent().build();
    }
}






