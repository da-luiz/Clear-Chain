package com.vms.vendor_management_system.application.dto.vendorcategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Payload for creating a vendor category.
 */
@Getter
@Setter
public class CreateVendorCategoryRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    @Size(max = 1000)
    private String description;

    @NotBlank
    @Size(max = 50)
    private String code;
}






