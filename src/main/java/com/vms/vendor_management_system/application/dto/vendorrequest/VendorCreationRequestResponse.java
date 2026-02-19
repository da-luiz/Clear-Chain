package com.vms.vendor_management_system.application.dto.vendorrequest;

import com.vms.vendor_management_system.domain.enums.RequestStatus;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * Response representation of a vendor creation request.
 */
@Value
@Builder
public class VendorCreationRequestResponse {
    Long id;
    String requestNumber;
    RequestStatus status;
    Long vendorId;
    String companyName;
    String legalName;
    String businessJustification;
    Double expectedContractValue;
    Long requestingDepartmentId;
    String requestingDepartmentName;
    Long requestedByUserId;
    String requestedByUsername;
    String rejectionReason;
    String additionalInfoRequired;
    Long reviewedByUserId;
    String reviewedByUsername;
    LocalDateTime reviewedAt;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    
    // Contact details
    String primaryContactName;
    String primaryContactTitle;
    String primaryContactEmail;
    String primaryContactPhone;
    
    // Additional company info
    String businessRegistrationNumber;
    String taxIdentificationNumber;
    String businessType;
    String website;
    Long categoryId;
    String categoryName;
    
    // Address fields
    String addressStreet;
    String addressCity;
    String addressState;
    String addressPostalCode;
    String addressCountry;
    
    // Banking and payment details (filled by Finance Approver)
    String bankName;
    String accountHolderName;
    String accountNumber;
    String swiftBicCode;
    String currency; // Selected by Requester, visible to Finance
    String paymentTerms;
    String preferredPaymentMethod;
    
    // Supporting documents: uploaded files, links, GitHub pages, LinkedIn
    String supportingDocuments; // JSON array: [{"type":"file|link|github|linkedin","value":"...","name":"...","fileName":"..."}]
}

