package com.vms.vendor_management_system.application.mapper;

import com.vms.vendor_management_system.application.dto.vendorperformancecriteria.CreateVendorPerformanceCriteriaRequest;
import com.vms.vendor_management_system.application.dto.vendorperformancecriteria.VendorPerformanceCriteriaResponse;
import com.vms.vendor_management_system.domain.entity.VendorCategory;
import com.vms.vendor_management_system.domain.entity.VendorPerformanceCriteria;

/**
 * Utilities to convert between vendor performance criteria entities and DTOs.
 */
public final class VendorPerformanceCriteriaMapper {

    private VendorPerformanceCriteriaMapper() {
    }

    public static VendorPerformanceCriteria toEntity(CreateVendorPerformanceCriteriaRequest request, VendorCategory category) {
        return new VendorPerformanceCriteria(
                request.getName(),
                request.getDescription(),
                request.getWeight(),
                request.getMaxScore(),
                category
        );
    }

    public static void updateEntity(VendorPerformanceCriteria criteria, CreateVendorPerformanceCriteriaRequest request, VendorCategory category) {
        criteria.setName(request.getName());
        criteria.setDescription(request.getDescription());
        criteria.setWeight(request.getWeight());
        criteria.setMaxScore(request.getMaxScore());
        criteria.setCategory(category);
    }

    public static VendorPerformanceCriteriaResponse toResponse(VendorPerformanceCriteria criteria) {
        if (criteria == null) {
            return null;
        }

        return VendorPerformanceCriteriaResponse.builder()
                .id(criteria.getId())
                .name(criteria.getName())
                .description(criteria.getDescription())
                .weight(criteria.getWeight())
                .maxScore(criteria.getMaxScore())
                .isActive(criteria.getIsActive())
                .categoryId(criteria.getCategory() != null ? criteria.getCategory().getId() : null)
                .categoryName(criteria.getCategory() != null ? criteria.getCategory().getName() : null)
                .createdAt(criteria.getCreatedAt())
                .updatedAt(criteria.getUpdatedAt())
                .build();
    }
}






