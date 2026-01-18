package com.vms.vendor_management_system.application.mapper;

import com.vms.vendor_management_system.application.dto.contract.ContractResponse;
import com.vms.vendor_management_system.application.dto.contract.CreateContractRequest;
import com.vms.vendor_management_system.domain.entity.Contract;
import com.vms.vendor_management_system.domain.entity.User;
import com.vms.vendor_management_system.domain.entity.Vendor;

/**
 * Utilities to convert between contract entities and DTOs.
 */
public final class ContractMapper {

    private ContractMapper() {
    }

    public static Contract toEntity(CreateContractRequest request, Vendor vendor, User creator) {
        Contract contract = new Contract(
                request.getContractNumber(),
                vendor,
                request.getTitle(),
                request.getStartDate(),
                request.getEndDate(),
                creator
        );
        contract.setDescription(request.getDescription());
        contract.setContractValue(request.getContractValue());
        contract.setCurrency(request.getCurrency());
        contract.setContractType(request.getContractType());
        contract.setDocumentUrl(request.getDocumentUrl());
        contract.setTermsAndConditions(request.getTermsAndConditions());
        contract.setRenewalTerms(request.getRenewalTerms());
        contract.setTerminationClause(request.getTerminationClause());
        return contract;
    }

    public static ContractResponse toResponse(Contract contract) {
        if (contract == null) {
            return null;
        }

        return ContractResponse.builder()
                .id(contract.getId())
                .vendorId(contract.getVendor() != null ? contract.getVendor().getId() : null)
                .vendorName(contract.getVendor() != null ? contract.getVendor().getCompanyName() : null)
                .contractNumber(contract.getContractNumber())
                .title(contract.getTitle())
                .description(contract.getDescription())
                .contractValue(contract.getContractValue())
                .startDate(contract.getStartDate())
                .endDate(contract.getEndDate())
                .contractType(contract.getContractType())
                .status(contract.getStatus())
                .currency(contract.getCurrency())
                .documentUrl(contract.getDocumentUrl())
                .termsAndConditions(contract.getTermsAndConditions())
                .createdByUserId(contract.getCreatedBy() != null ? contract.getCreatedBy().getId() : null)
                .createdByUsername(contract.getCreatedBy() != null ? contract.getCreatedBy().getUsername() : null)
                .approvedByUserId(contract.getApprovedBy() != null ? contract.getApprovedBy().getId() : null)
                .approvedByUsername(contract.getApprovedBy() != null ? contract.getApprovedBy().getUsername() : null)
                .approvedAt(contract.getApprovedAt())
                .renewalTerms(contract.getRenewalTerms())
                .terminationClause(contract.getTerminationClause())
                .createdAt(contract.getCreatedAt())
                .updatedAt(contract.getUpdatedAt())
                .build();
    }
}

