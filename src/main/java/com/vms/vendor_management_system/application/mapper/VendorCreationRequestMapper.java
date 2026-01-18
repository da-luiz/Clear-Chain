package com.vms.vendor_management_system.application.mapper;

import com.vms.vendor_management_system.application.dto.vendorrequest.VendorApprovalResponse;
import com.vms.vendor_management_system.application.dto.vendorrequest.VendorCreationRequestResponse;
import com.vms.vendor_management_system.domain.entity.VendorApproval;
import com.vms.vendor_management_system.domain.entity.VendorCreationRequest;

/**
 * Mapper utilities for vendor creation requests and approvals.
 */
public final class VendorCreationRequestMapper {

    private VendorCreationRequestMapper() {
    }

    public static VendorCreationRequestResponse toResponse(VendorCreationRequest request) {
        if (request == null) {
            return null;
        }

        return VendorCreationRequestResponse.builder()
                .id(request.getId())
                .requestNumber(request.getRequestNumber())
                .status(request.getStatus())
                .vendorId(request.getVendor() != null ? request.getVendor().getId() : null)
                .companyName(request.getCompanyName())
                .legalName(request.getLegalName())
                .businessJustification(request.getBusinessJustification())
                .expectedContractValue(request.getExpectedContractValue())
                .requestingDepartmentId(request.getRequestingDepartment() != null ? request.getRequestingDepartment().getId() : null)
                .requestingDepartmentName(request.getRequestingDepartment() != null ? request.getRequestingDepartment().getName() : null)
                .requestedByUserId(request.getRequestedBy() != null ? request.getRequestedBy().getId() : null)
                .requestedByUsername(request.getRequestedBy() != null ? request.getRequestedBy().getUsername() : null)
                .rejectionReason(request.getRejectionReason())
                .additionalInfoRequired(request.getAdditionalInfoRequired())
                .reviewedByUserId(request.getReviewedBy() != null ? request.getReviewedBy().getId() : null)
                .reviewedByUsername(request.getReviewedBy() != null ? request.getReviewedBy().getUsername() : null)
                .reviewedAt(request.getReviewedAt())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                // Contact details
                .primaryContactName(request.getPrimaryContactName())
                .primaryContactTitle(request.getPrimaryContactTitle())
                .primaryContactEmail(request.getPrimaryContactEmail())
                .primaryContactPhone(request.getPrimaryContactPhone())
                // Additional company info
                .businessRegistrationNumber(request.getBusinessRegistrationNumber())
                .taxIdentificationNumber(request.getTaxIdentificationNumber())
                .businessType(request.getBusinessType())
                .website(request.getWebsite())
                .categoryId(request.getCategory() != null ? request.getCategory().getId() : null)
                .categoryName(request.getCategory() != null ? request.getCategory().getName() : null)
                // Address fields
                .addressStreet(request.getAddressStreet())
                .addressCity(request.getAddressCity())
                .addressState(request.getAddressState())
                .addressPostalCode(request.getAddressPostalCode())
                .addressCountry(request.getAddressCountry())
                // Banking and payment details
                .bankName(request.getBankName())
                .accountHolderName(request.getAccountHolderName())
                .accountNumber(request.getAccountNumber())
                .swiftBicCode(request.getSwiftBicCode())
                .currency(request.getCurrency())
                .paymentTerms(request.getPaymentTerms())
                .preferredPaymentMethod(request.getPreferredPaymentMethod())
                // Supporting documents
                .supportingDocuments(request.getSupportingDocuments())
                .build();
    }

    public static VendorApprovalResponse toResponse(VendorApproval approval) {
        if (approval == null) {
            return null;
        }

        return VendorApprovalResponse.builder()
                .id(approval.getId())
                .vendorCreationRequestId(approval.getVendorCreationRequest() != null ? approval.getVendorCreationRequest().getId() : null)
                .approverId(approval.getApprover() != null ? approval.getApprover().getId() : null)
                .approverUsername(approval.getApprover() != null ? approval.getApprover().getUsername() : null)
                .approvalStatus(approval.getApprovalStatus())
                .comments(approval.getComments())
                .approvedAt(approval.getApprovedAt())
                .build();
    }
}

