package com.vms.vendor_management_system.application.dto.department;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * Response representation of a department.
 */
@Value
@Builder
public class DepartmentResponse {
    Long id;
    String name;
    String description;
    String code;
    Boolean isActive;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}






