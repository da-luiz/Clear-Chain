package com.vms.vendor_management_system.application.mapper;

import com.vms.vendor_management_system.application.dto.department.CreateDepartmentRequest;
import com.vms.vendor_management_system.application.dto.department.DepartmentResponse;
import com.vms.vendor_management_system.domain.entity.Department;

/**
 * Utilities to convert between department entities and DTOs.
 */
public final class DepartmentMapper {

    private DepartmentMapper() {
    }

    public static Department toEntity(CreateDepartmentRequest request) {
        return new Department(
                request.getName(),
                request.getDescription(),
                request.getCode()
        );
    }

    public static void updateEntity(Department department, CreateDepartmentRequest request) {
        department.setName(request.getName());
        department.setDescription(request.getDescription());
        department.setCode(request.getCode());
    }

    public static DepartmentResponse toResponse(Department department) {
        if (department == null) {
            return null;
        }

        return DepartmentResponse.builder()
                .id(department.getId())
                .name(department.getName())
                .description(department.getDescription())
                .code(department.getCode())
                .isActive(department.getIsActive())
                .createdAt(department.getCreatedAt())
                .updatedAt(department.getUpdatedAt())
                .build();
    }
}






