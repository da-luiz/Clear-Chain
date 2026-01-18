package com.vms.vendor_management_system.application.dto.contract;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Payload for creating a vendor contract.
 */
@Getter
@Setter
public class CreateContractRequest {

    @NotNull
    private Long vendorId;

    @NotBlank
    @Size(max = 100)
    private String contractNumber;

    @NotBlank
    @Size(max = 255)
    private String title;

    private String description;

    private BigDecimal contractValue;

    @Size(max = 10)
    private String currency;

    @NotNull
    private LocalDate startDate;

    @NotNull
    @FutureOrPresent
    private LocalDate endDate;

    @Size(max = 100)
    private String contractType;

    private String documentUrl;

    private String termsAndConditions;

    @NotNull
    private Long createdByUserId;

    private String renewalTerms;

    private String terminationClause;
}

