package com.vms.vendor_management_system.presentation.controller;

import com.vms.vendor_management_system.application.dto.vendorrating.CreateVendorRatingRequest;
import com.vms.vendor_management_system.application.dto.vendorrating.VendorRatingResponse;
import com.vms.vendor_management_system.application.service.VendorRatingApplicationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for managing vendor ratings.
 */
@RestController
@RequestMapping("/api/vendor-ratings")
@Validated
public class VendorRatingController {

    private final VendorRatingApplicationService ratingApplicationService;

    public VendorRatingController(VendorRatingApplicationService ratingApplicationService) {
        this.ratingApplicationService = ratingApplicationService;
    }

    @GetMapping("/{id}")
    public VendorRatingResponse getRating(@PathVariable Long id) {
        return ratingApplicationService.getRating(id);
    }

    @GetMapping("/vendor/{vendorId}")
    public List<VendorRatingResponse> getRatingsForVendor(@PathVariable Long vendorId) {
        return ratingApplicationService.getRatingsForVendor(vendorId);
    }

    @GetMapping("/criteria/{criteriaId}")
    public List<VendorRatingResponse> getRatingsByCriteria(@PathVariable Long criteriaId) {
        return ratingApplicationService.getRatingsByCriteria(criteriaId);
    }

    @PostMapping
    public ResponseEntity<VendorRatingResponse> createRating(@Valid @RequestBody CreateVendorRatingRequest request) {
        VendorRatingResponse response = ratingApplicationService.createRating(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public VendorRatingResponse updateRating(@PathVariable Long id,
                                             @RequestParam(required = false) @Min(0) Integer score,
                                             @RequestParam(required = false) String comments,
                                             @RequestParam(required = false) String evidenceUrl) {
        return ratingApplicationService.updateRating(id, score, comments, evidenceUrl);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRating(@PathVariable Long id) {
        ratingApplicationService.deleteRating(id);
        return ResponseEntity.noContent().build();
    }
}






