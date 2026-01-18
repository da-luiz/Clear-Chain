package com.vms.vendor_management_system.application.mapper;

import com.vms.vendor_management_system.application.dto.user.UserResponse;
import com.vms.vendor_management_system.domain.entity.User;

/**
 * Utility class for mapping {@link User} entities to DTOs.
 */
public final class UserMapper {

    private UserMapper() {
    }

    public static UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail() != null ? user.getEmail().getValue() : null)
                .role(user.getRole())
                .departmentId(user.getDepartment() != null ? user.getDepartment().getId() : null)
                .departmentName(user.getDepartment() != null ? user.getDepartment().getName() : null)
                .active(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}


