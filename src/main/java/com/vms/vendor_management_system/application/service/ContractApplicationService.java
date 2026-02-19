package com.vms.vendor_management_system.application.service;

import com.vms.vendor_management_system.application.dto.contract.ContractResponse;
import com.vms.vendor_management_system.application.dto.contract.CreateContractRequest;
import com.vms.vendor_management_system.application.mapper.ContractMapper;
import com.vms.vendor_management_system.domain.entity.Contract;
import com.vms.vendor_management_system.domain.entity.User;
import com.vms.vendor_management_system.domain.entity.Vendor;
import com.vms.vendor_management_system.domain.repository.ContractRepository;
import com.vms.vendor_management_system.domain.repository.UserRepository;
import com.vms.vendor_management_system.domain.repository.VendorRepository;
import com.vms.vendor_management_system.domain.repository.VendorCreationRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Application service for contract lifecycle.
 */
@Service
@Transactional
public class ContractApplicationService {

    private final ContractRepository contractRepository;
    private final VendorRepository vendorRepository;
    private final UserRepository userRepository;
    private final VendorCreationRequestRepository vendorCreationRequestRepository;

    public ContractApplicationService(ContractRepository contractRepository,
                                      VendorRepository vendorRepository,
                                      UserRepository userRepository,
                                      VendorCreationRequestRepository vendorCreationRequestRepository) {
        this.contractRepository = contractRepository;
        this.vendorRepository = vendorRepository;
        this.userRepository = userRepository;
        this.vendorCreationRequestRepository = vendorCreationRequestRepository;
    }

    @Transactional(readOnly = true)
    public List<ContractResponse> getContractsForVendor(Long vendorId) {
        return contractRepository.findByVendorId(vendorId)
                .stream()
                .map(contract -> enrichContractWithCurrency(contract))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ContractResponse getContract(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Contract not found"));
        return enrichContractWithCurrency(contract);
    }
    
    /**
     * Enriches contract response with currency from vendor creation request if not already set
     */
    private ContractResponse enrichContractWithCurrency(Contract contract) {
        ContractResponse response = ContractMapper.toResponse(contract);
        
        // If contract doesn't have currency, get it from the vendor's creation request
        if (response.getCurrency() == null || response.getCurrency().trim().isEmpty()) {
            if (contract.getVendor() != null) {
                List<com.vms.vendor_management_system.domain.entity.VendorCreationRequest> vendorRequests = 
                    vendorCreationRequestRepository.findByVendorId(contract.getVendor().getId());
                if (!vendorRequests.isEmpty()) {
                    // Get currency from the most recent ACTIVE vendor creation request
                    String currency = vendorRequests.get(0).getCurrency();
                    if (currency != null && !currency.trim().isEmpty()) {
                        // Create a new response with the currency
                        return ContractResponse.builder()
                                .id(response.getId())
                                .vendorId(response.getVendorId())
                                .vendorName(response.getVendorName())
                                .contractNumber(response.getContractNumber())
                                .title(response.getTitle())
                                .description(response.getDescription())
                                .contractValue(response.getContractValue())
                                .currency(currency)
                                .startDate(response.getStartDate())
                                .endDate(response.getEndDate())
                                .contractType(response.getContractType())
                                .status(response.getStatus())
                                .documentUrl(response.getDocumentUrl())
                                .termsAndConditions(response.getTermsAndConditions())
                                .createdByUserId(response.getCreatedByUserId())
                                .createdByUsername(response.getCreatedByUsername())
                                .approvedByUserId(response.getApprovedByUserId())
                                .approvedByUsername(response.getApprovedByUsername())
                                .approvedAt(response.getApprovedAt())
                                .renewalTerms(response.getRenewalTerms())
                                .terminationClause(response.getTerminationClause())
                                .createdAt(response.getCreatedAt())
                                .updatedAt(response.getUpdatedAt())
                                .build();
                    }
                }
            }
        }
        
        return response;
    }

    public ContractResponse createContract(CreateContractRequest request) {
        Vendor vendor = vendorRepository.findById(request.getVendorId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Vendor not found"));
        User creator = userRepository.findById(request.getCreatedByUserId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Creator not found"));
        
        // If currency is not provided, get it from the vendor's creation request
        String currency = request.getCurrency();
        if (currency == null || currency.trim().isEmpty()) {
            List<com.vms.vendor_management_system.domain.entity.VendorCreationRequest> vendorRequests = 
                vendorCreationRequestRepository.findByVendorId(vendor.getId());
            if (!vendorRequests.isEmpty()) {
                // Get currency from the most recent ACTIVE vendor creation request (first in DESC order)
                // This is the request that created this vendor, containing the requester's chosen currency
                com.vms.vendor_management_system.domain.entity.VendorCreationRequest vendorRequest = vendorRequests.get(0);
                currency = vendorRequest.getCurrency();
            }
        }
        
        // Always set the currency if we found one from the vendor request, even if request provided empty currency
        if (currency != null && !currency.trim().isEmpty()) {
            request.setCurrency(currency);
        }
        
        Contract contract = ContractMapper.toEntity(request, vendor, creator);
        Contract saved = contractRepository.save(contract);
        return ContractMapper.toResponse(saved);
    }

    public ContractResponse approveContract(Long contractId, Long approverId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Contract not found"));
        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Approver not found"));
        contract.approve(approver);
        Contract saved = contractRepository.save(contract);
        return ContractMapper.toResponse(saved);
    }

    public void terminateContract(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Contract not found"));
        contract.terminate();
        contractRepository.save(contract);
    }
}

