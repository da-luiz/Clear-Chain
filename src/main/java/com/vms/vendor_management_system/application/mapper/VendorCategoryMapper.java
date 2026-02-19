package com.vms.vendor_management_system.application.mapper;

import com.vms.vendor_management_system.application.dto.vendorcategory.CreateVendorCategoryRequest;
import com.vms.vendor_management_system.application.dto.vendorcategory.VendorCategoryResponse;
import com.vms.vendor_management_system.domain.entity.VendorCategory;

/**
 * Utilities to convert between vendor category entities and DTOs.
 */
public final class VendorCategoryMapper {

    private VendorCategoryMapper() {
    }

    public static VendorCategory toEntity(CreateVendorCategoryRequest request) {
        return new VendorCategory(
                request.getName(),
                request.getDescription(),
                request.getCode()
        );
    }

    public static void updateEntity(VendorCategory category, CreateVendorCategoryRequest request) {
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setCode(request.getCode());
    }

    public static VendorCategoryResponse toResponse(VendorCategory category) {
        if (category == null) {
            return null;
        }

        return VendorCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .code(category.getCode())
                .isActive(category.getIsActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}






