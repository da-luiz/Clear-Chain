package com.vms.vendor_management_system.application.dto.user;

import com.vms.vendor_management_system.domain.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Request payload for creating a new user.
 */
@Getter
@Setter
public class CreateUserRequest {

    @NotBlank
    @Size(max = 150)
    private String username;

    @NotBlank
    @Size(max = 150)
    private String firstName;

    @NotBlank
    @Size(max = 150)
    private String lastName;

    @NotBlank
    @Email
    @Size(max = 320)
    private String email;

    @NotNull
    private UserRole role;

    private Long departmentId;
    
    // Password is optional - can be set later
    private String password;
}


