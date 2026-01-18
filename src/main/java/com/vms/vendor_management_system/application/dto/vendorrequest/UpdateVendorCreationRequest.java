package com.vms.vendor_management_system.application.dto.vendorrequest;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Payload for updating an existing vendor creation request before submission.
 * Filled by the Requester.
 */
@Getter
@Setter
public class UpdateVendorCreationRequest {

    @NotBlank
    @Size(max = 255)
    private String companyName;

    @Size(max = 255)
    private String legalName;

    @Size(max = 4000)
    private String businessJustification;

    private Double expectedContractValue;

    @NotNull
    private Long requestingDepartmentId;

    // Contact details
    @Size(max = 255)
    private String primaryContactName;

    @Size(max = 255)
    private String primaryContactTitle;

    @Email
    @Size(max = 320)
    private String primaryContactEmail;

    @Size(max = 50)
    private String primaryContactPhone;

    // Additional company info
    @Size(max = 100)
    private String businessRegistrationNumber;

    @Size(max = 100)
    private String taxIdentificationNumber;

    @Size(max = 100)
    private String businessType;

    @Size(max = 255)
    private String website;

    // Address fields
    @Size(max = 255)
    private String addressStreet;

    @Size(max = 255)
    private String addressCity;

    @Size(max = 255)
    private String addressState;

    @Size(max = 50)
    private String addressPostalCode;

    @Size(max = 255)
    private String addressCountry;

    private Long categoryId;
    
    // Currency selected by Requester
    @Size(max = 10)
    private String currency;
    
    // Supporting documents: uploaded files, links, GitHub pages, LinkedIn (JSON array)
    @Size(max = 10000)
    private String supportingDocuments;
}

