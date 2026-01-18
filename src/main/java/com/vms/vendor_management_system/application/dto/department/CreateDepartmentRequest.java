package com.vms.vendor_management_system.application.dto.department;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Payload for creating a department.
 */
@Getter
@Setter
public class CreateDepartmentRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    @Size(max = 1000)
    private String description;

    @NotBlank
    @Size(max = 50)
    private String code;
}






