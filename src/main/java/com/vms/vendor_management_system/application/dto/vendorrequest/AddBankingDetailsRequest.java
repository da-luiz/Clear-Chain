package com.vms.vendor_management_system.application.dto.vendorrequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for adding banking and payment details to a submitted vendor creation request.
 * This is filled by the Purchasing Team.
 */
@Getter
@Setter
public class AddBankingDetailsRequest {

    @NotBlank
    @Size(max = 255)
    private String bankName;

    @NotBlank
    @Size(max = 255)
    private String accountHolderName;

    @NotBlank
    @Size(max = 100)
    private String accountNumber;

    @Size(max = 50)
    private String swiftBicCode;

    @Size(max = 10)
    private String currency;

    @Size(max = 100)
    private String paymentTerms;

    @Size(max = 100)
    private String preferredPaymentMethod;
}





