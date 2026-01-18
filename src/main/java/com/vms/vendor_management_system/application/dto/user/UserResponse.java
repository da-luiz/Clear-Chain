package com.vms.vendor_management_system.application.dto.user;

import com.vms.vendor_management_system.domain.enums.UserRole;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * Response payload for user operations.
 */
@Value
@Builder
public class UserResponse {
    Long id;
    String username;
    String firstName;
    String lastName;
    String email;
    UserRole role;
    Long departmentId;
    String departmentName;
    Boolean active;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}


