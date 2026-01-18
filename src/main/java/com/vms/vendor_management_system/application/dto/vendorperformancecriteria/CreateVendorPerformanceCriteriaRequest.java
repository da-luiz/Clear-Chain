package com.vms.vendor_management_system.application.dto.vendorperformancecriteria;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Payload for creating vendor performance criteria.
 */
@Getter
@Setter
public class CreateVendorPerformanceCriteriaRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    @Size(max = 2000)
    private String description;

    @NotNull
    @Min(0)
    @Max(1)
    private Double weight;

    @NotNull
    @Min(1)
    private Integer maxScore;

    private Long categoryId;
}






