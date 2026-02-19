package com.vms.vendor_management_system.application.dto.vendor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Request payload for creating or updating vendor information.
 */
@Getter
@Setter
public class CreateVendorRequest {

    @NotBlank
    @Size(max = 255)
    private String companyName;

    @Size(max = 255)
    private String legalName;

    @Size(max = 100)
    private String taxId;

    @Email
    @Size(max = 320)
    private String email;

    @Size(max = 50)
    private String phone;

    @Size(max = 255)
    private String street;

    @Size(max = 255)
    private String city;

    @Size(max = 255)
    private String state;

    @Size(max = 50)
    private String postalCode;

    @Size(max = 255)
    private String country;

    private Long categoryId;

    @Size(max = 255)
    private String website;

    private String description;

    private String notes;
}


