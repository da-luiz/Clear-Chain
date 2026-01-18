package com.vms.vendor_management_system.presentation.controller;

import com.vms.vendor_management_system.application.dto.vendorcategory.CreateVendorCategoryRequest;
import com.vms.vendor_management_system.application.dto.vendorcategory.VendorCategoryResponse;
import com.vms.vendor_management_system.application.service.VendorCategoryApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for managing vendor categories.
 */
@RestController
@RequestMapping("/api/vendor-categories")
@Validated
public class VendorCategoryController {

    private final VendorCategoryApplicationService vendorCategoryApplicationService;

    public VendorCategoryController(VendorCategoryApplicationService vendorCategoryApplicationService) {
        this.vendorCategoryApplicationService = vendorCategoryApplicationService;
    }

    @GetMapping
    public List<VendorCategoryResponse> getAllCategories() {
        return vendorCategoryApplicationService.getAllCategories();
    }

    @GetMapping("/active")
    public List<VendorCategoryResponse> getActiveCategories() {
        return vendorCategoryApplicationService.getActiveCategories();
    }

    @GetMapping("/{id}")
    public VendorCategoryResponse getCategory(@PathVariable Long id) {
        return vendorCategoryApplicationService.getCategory(id);
    }

    @PostMapping
    public ResponseEntity<VendorCategoryResponse> createCategory(@Valid @RequestBody CreateVendorCategoryRequest request) {
        VendorCategoryResponse response = vendorCategoryApplicationService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public VendorCategoryResponse updateCategory(@PathVariable Long id, @Valid @RequestBody CreateVendorCategoryRequest request) {
        return vendorCategoryApplicationService.updateCategory(id, request);
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateCategory(@PathVariable Long id) {
        vendorCategoryApplicationService.deactivateCategory(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<Void> activateCategory(@PathVariable Long id) {
        vendorCategoryApplicationService.activateCategory(id);
        return ResponseEntity.noContent().build();
    }
}






