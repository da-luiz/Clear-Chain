package com.vms.vendor_management_system.application.mapper;

import com.vms.vendor_management_system.application.dto.vendorrating.CreateVendorRatingRequest;
import com.vms.vendor_management_system.application.dto.vendorrating.VendorRatingResponse;
import com.vms.vendor_management_system.domain.entity.User;
import com.vms.vendor_management_system.domain.entity.Vendor;
import com.vms.vendor_management_system.domain.entity.VendorPerformanceCriteria;
import com.vms.vendor_management_system.domain.entity.VendorRating;

/**
 * Utilities to convert between vendor rating entities and DTOs.
 */
public final class VendorRatingMapper {

    private VendorRatingMapper() {
    }

    public static VendorRating toEntity(CreateVendorRatingRequest request, Vendor vendor, 
                                       VendorPerformanceCriteria criteria, User ratedBy) {
        VendorRating rating = new VendorRating(vendor, criteria, request.getScore(), ratedBy);
        rating.setComments(request.getComments());
        rating.setEvidenceUrl(request.getEvidenceUrl());
        rating.setRatingPeriod(request.getRatingPeriodStart(), request.getRatingPeriodEnd());
        return rating;
    }

    public static VendorRatingResponse toResponse(VendorRating rating) {
        if (rating == null) {
            return null;
        }

        return VendorRatingResponse.builder()
                .id(rating.getId())
                .vendorId(rating.getVendor() != null ? rating.getVendor().getId() : null)
                .vendorName(rating.getVendor() != null ? rating.getVendor().getCompanyName() : null)
                .criteriaId(rating.getCriteria() != null ? rating.getCriteria().getId() : null)
                .criteriaName(rating.getCriteria() != null ? rating.getCriteria().getName() : null)
                .score(rating.getScore())
                .comments(rating.getComments())
                .evidenceUrl(rating.getEvidenceUrl())
                .ratedByUserId(rating.getRatedBy() != null ? rating.getRatedBy().getId() : null)
                .ratedByUsername(rating.getRatedBy() != null ? rating.getRatedBy().getUsername() : null)
                .ratingPeriodStart(rating.getRatingPeriodStart())
                .ratingPeriodEnd(rating.getRatingPeriodEnd())
                .weightedScore(rating.getWeightedScore())
                .createdAt(rating.getCreatedAt())
                .updatedAt(rating.getUpdatedAt())
                .build();
    }
}






