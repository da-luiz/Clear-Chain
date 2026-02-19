package com.vms.vendor_management_system.application.dto.auth;

import com.vms.vendor_management_system.domain.enums.UserRole;
import lombok.Builder;
import lombok.Value;

/**
 * Response DTO for successful login.
 */
@Value
@Builder
public class LoginResponse {
    String token;
    String username;
    UserRole role;
    Long userId;
    String firstName;
    String lastName;
}





