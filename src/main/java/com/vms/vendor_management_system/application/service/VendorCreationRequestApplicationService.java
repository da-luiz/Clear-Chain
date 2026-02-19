package com.vms.vendor_management_system.application.service;

import com.vms.vendor_management_system.application.dto.vendorrequest.AddBankingDetailsRequest;
import com.vms.vendor_management_system.application.dto.vendorrequest.CreateVendorCreationRequest;
import com.vms.vendor_management_system.application.dto.vendorrequest.UpdateVendorCreationRequest;
import com.vms.vendor_management_system.application.dto.vendorrequest.VendorCreationRequestResponse;
import com.vms.vendor_management_system.application.dto.vendorrequest.VendorRequestAction;
import com.vms.vendor_management_system.application.mapper.VendorCreationRequestMapper;
import com.vms.vendor_management_system.domain.entity.Department;
import com.vms.vendor_management_system.domain.entity.User;
import com.vms.vendor_management_system.domain.entity.VendorCategory;
import com.vms.vendor_management_system.domain.entity.VendorCreationRequest;
import com.vms.vendor_management_system.domain.enums.RequestStatus;
import com.vms.vendor_management_system.domain.repository.DepartmentRepository;
import com.vms.vendor_management_system.domain.repository.UserRepository;
import com.vms.vendor_management_system.domain.repository.VendorCategoryRepository;
import com.vms.vendor_management_system.domain.repository.VendorCreationRequestRepository;
import com.vms.vendor_management_system.domain.service.VendorManagementService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Application service handling vendor creation request lifecycle.
 */
@Service
@Transactional
public class VendorCreationRequestApplicationService {

    private final VendorCreationRequestRepository vendorCreationRequestRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final VendorCategoryRepository vendorCategoryRepository;
    private final VendorManagementService vendorManagementService;

    public VendorCreationRequestApplicationService(VendorCreationRequestRepository vendorCreationRequestRepository,
                                                   DepartmentRepository departmentRepository,
                                                   UserRepository userRepository,
                                                   VendorCategoryRepository vendorCategoryRepository,
                                                   VendorManagementService vendorManagementService) {
        this.vendorCreationRequestRepository = vendorCreationRequestRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.vendorCategoryRepository = vendorCategoryRepository;
        this.vendorManagementService = vendorManagementService;
    }

    @Transactional(readOnly = true)
    public List<VendorCreationRequestResponse> getPendingRequests() {
        return vendorManagementService.getPendingRequests()
                .stream()
                .map(VendorCreationRequestMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VendorCreationRequestResponse getRequest(Long requestId) {
        VendorCreationRequest request = vendorCreationRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Vendor request not found"));
        return VendorCreationRequestMapper.toResponse(request);
    }

    public VendorCreationRequestResponse createRequest(CreateVendorCreationRequest payload) {
        Department department = departmentRepository.findById(payload.getRequestingDepartmentId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Department not found"));
        User requester = userRepository.findById(payload.getRequestedByUserId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Requesting user not found"));

        VendorCreationRequest request = new VendorCreationRequest(
                generateRequestNumber(),
                department,
                requester,
                payload.getCompanyName()
        );
        request.setLegalName(payload.getLegalName());
        request.setBusinessJustification(payload.getBusinessJustification());
        request.setExpectedContractValue(payload.getExpectedContractValue());
        // Currency and supporting documents
        request.setCurrency(payload.getCurrency());
        request.setSupportingDocuments(payload.getSupportingDocuments());
        // Contact details
        request.setPrimaryContactName(payload.getPrimaryContactName());
        request.setPrimaryContactTitle(payload.getPrimaryContactTitle());
        request.setPrimaryContactEmail(payload.getPrimaryContactEmail());
        request.setPrimaryContactPhone(payload.getPrimaryContactPhone());
        // Additional company info
        request.setBusinessRegistrationNumber(payload.getBusinessRegistrationNumber());
        request.setTaxIdentificationNumber(payload.getTaxIdentificationNumber());
        request.setBusinessType(payload.getBusinessType());
        request.setWebsite(payload.getWebsite());
        // Address fields
        request.setAddressStreet(payload.getAddressStreet());
        request.setAddressCity(payload.getAddressCity());
        request.setAddressState(payload.getAddressState());
        request.setAddressPostalCode(payload.getAddressPostalCode());
        request.setAddressCountry(payload.getAddressCountry());
        // Category
        if (payload.getCategoryId() != null) {
            VendorCategory category = vendorCategoryRepository.findById(payload.getCategoryId())
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Category not found"));
            request.setCategory(category);
        }
        vendorCreationRequestRepository.save(request);
        return VendorCreationRequestMapper.toResponse(request);
    }

    public VendorCreationRequestResponse updateDraft(Long requestId, UpdateVendorCreationRequest payload) {
        VendorCreationRequest request = vendorCreationRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Vendor request not found"));
        if (request.getStatus() != RequestStatus.DRAFT && request.getStatus() != RequestStatus.RETURNED_FOR_INFO) {
            throw new ResponseStatusException(BAD_REQUEST, "Only draft or returned requests can be updated");
        }
        Department department = departmentRepository.findById(payload.getRequestingDepartmentId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Department not found"));
        VendorCategory category = payload.getCategoryId() != null ? 
                vendorCategoryRepository.findById(payload.getCategoryId())
                        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Category not found")) : null;
        request.setCompanyName(payload.getCompanyName());
        request.setLegalName(payload.getLegalName());
        request.setBusinessJustification(payload.getBusinessJustification());
        request.setExpectedContractValue(payload.getExpectedContractValue());
        request.setRequestingDepartment(department);
        request.setCategory(category);
        // Contact details
        request.setPrimaryContactName(payload.getPrimaryContactName());
        request.setPrimaryContactTitle(payload.getPrimaryContactTitle());
        request.setPrimaryContactEmail(payload.getPrimaryContactEmail());
        request.setPrimaryContactPhone(payload.getPrimaryContactPhone());
        // Additional company info
        request.setBusinessRegistrationNumber(payload.getBusinessRegistrationNumber());
        request.setTaxIdentificationNumber(payload.getTaxIdentificationNumber());
        request.setBusinessType(payload.getBusinessType());
        request.setWebsite(payload.getWebsite());
        // Address fields
        request.setAddressStreet(payload.getAddressStreet());
        request.setAddressCity(payload.getAddressCity());
        request.setAddressState(payload.getAddressState());
        request.setAddressPostalCode(payload.getAddressPostalCode());
        request.setAddressCountry(payload.getAddressCountry());
        // Currency and supporting documents
        request.setCurrency(payload.getCurrency());
        request.setSupportingDocuments(payload.getSupportingDocuments());
        vendorCreationRequestRepository.save(request);
        return VendorCreationRequestMapper.toResponse(request);
    }

    public VendorCreationRequestResponse submit(Long requestId) {
        VendorCreationRequest request = vendorCreationRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Vendor request not found"));
        // Submit moves status from DRAFT to PENDING_COMPLIANCE_REVIEW (new workflow: Compliance first)
        request.submit();
        vendorCreationRequestRepository.save(request);
        return VendorCreationRequestMapper.toResponse(request);
    }

    public VendorCreationRequestResponse addBankingDetails(Long requestId, AddBankingDetailsRequest payload) {
        VendorCreationRequest request = vendorCreationRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Vendor request not found"));
        // Banking details can only be added during finance review (Finance Approver adds these)
        if (request.getStatus() != RequestStatus.PENDING_FINANCE_REVIEW) {
            throw new ResponseStatusException(BAD_REQUEST, "Banking details can only be added to requests pending finance review");
        }
        request.setBankName(payload.getBankName());
        request.setAccountHolderName(payload.getAccountHolderName());
        request.setAccountNumber(payload.getAccountNumber());
        request.setSwiftBicCode(payload.getSwiftBicCode());
        // Note: Currency is set by Requester, Finance cannot change it
        // request.setCurrency(payload.getCurrency()); // Removed - currency is set by requester
        request.setPaymentTerms(payload.getPaymentTerms());
        request.setPreferredPaymentMethod(payload.getPreferredPaymentMethod());
        vendorCreationRequestRepository.save(request);
        return VendorCreationRequestMapper.toResponse(request);
    }

    /**
     * Finance approves a request - moves to Admin review (final step before Active)
     * Finance cannot approve without banking details
     */
    public VendorCreationRequestResponse approveByFinance(Long requestId, VendorRequestAction action) {
        VendorCreationRequest request = vendorCreationRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Vendor request not found"));
        if (!request.hasBankingDetails()) {
            throw new ResponseStatusException(BAD_REQUEST, "Finance cannot approve without banking details. Please add bank name, account number, and account holder name.");
        }
        User financeReviewer = userRepository.findById(action.getReviewerId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Reviewer not found"));
        vendorManagementService.approveByFinance(request, financeReviewer);
        vendorCreationRequestRepository.save(request);
        return VendorCreationRequestMapper.toResponse(request);
    }

    /**
     * Finance rejects a request
     */
    public VendorCreationRequestResponse rejectByFinance(Long requestId, VendorRequestAction action) {
        VendorCreationRequest request = vendorCreationRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Vendor request not found"));
        User financeReviewer = userRepository.findById(action.getReviewerId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Reviewer not found"));
        vendorManagementService.rejectByFinance(request, financeReviewer, action.getComment());
        vendorCreationRequestRepository.save(request);
        return VendorCreationRequestMapper.toResponse(request);
    }

    /**
     * Compliance approves a request - moves to Finance review
     */
    public VendorCreationRequestResponse approveByCompliance(Long requestId, VendorRequestAction action) {
        VendorCreationRequest request = vendorCreationRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Vendor request not found"));
        User complianceReviewer = userRepository.findById(action.getReviewerId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Reviewer not found"));
        vendorManagementService.approveByCompliance(request, complianceReviewer);
        vendorCreationRequestRepository.save(request);
        return VendorCreationRequestMapper.toResponse(request);
    }

    /**
     * Compliance rejects a request
     */
    public VendorCreationRequestResponse rejectByCompliance(Long requestId, VendorRequestAction action) {
        VendorCreationRequest request = vendorCreationRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Vendor request not found"));
        User complianceReviewer = userRepository.findById(action.getReviewerId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Reviewer not found"));
        vendorManagementService.rejectByCompliance(request, complianceReviewer, action.getComment());
        vendorCreationRequestRepository.save(request);
        return VendorCreationRequestMapper.toResponse(request);
    }

    /**
     * Admin approves a request - creates active vendor (final approval)
     */
    public VendorCreationRequestResponse approveByAdmin(Long requestId, VendorRequestAction action) {
        VendorCreationRequest request = vendorCreationRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Vendor request not found"));
        User adminReviewer = userRepository.findById(action.getReviewerId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Reviewer not found"));
        vendorManagementService.approveByAdmin(request, adminReviewer);
        vendorCreationRequestRepository.save(request);
        return VendorCreationRequestMapper.toResponse(request);
    }

    /**
     * Admin rejects a request
     */
    public VendorCreationRequestResponse rejectByAdmin(Long requestId, VendorRequestAction action) {
        VendorCreationRequest request = vendorCreationRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Vendor request not found"));
        User adminReviewer = userRepository.findById(action.getReviewerId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Reviewer not found"));
        vendorManagementService.rejectByAdmin(request, adminReviewer, action.getComment());
        vendorCreationRequestRepository.save(request);
        return VendorCreationRequestMapper.toResponse(request);
    }

    // Legacy methods for backward compatibility
    @Deprecated
    public VendorCreationRequestResponse approve(Long requestId, VendorRequestAction action) {
        VendorCreationRequest request = vendorCreationRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Vendor request not found"));
        // Determine which approval method to use based on current status
        if (request.getStatus() == RequestStatus.PENDING_COMPLIANCE_REVIEW) {
            return approveByCompliance(requestId, action);
        } else if (request.getStatus() == RequestStatus.PENDING_FINANCE_REVIEW) {
            return approveByFinance(requestId, action);
        } else if (request.getStatus() == RequestStatus.PENDING_ADMIN_REVIEW) {
            return approveByAdmin(requestId, action);
        } else {
            throw new ResponseStatusException(BAD_REQUEST, "Request is not in a state that can be approved");
        }
    }

    @Deprecated
    public VendorCreationRequestResponse reject(Long requestId, VendorRequestAction action) {
        VendorCreationRequest request = vendorCreationRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Vendor request not found"));
        // Determine which rejection method to use based on current status
        if (request.getStatus() == RequestStatus.PENDING_COMPLIANCE_REVIEW) {
            return rejectByCompliance(requestId, action);
        } else if (request.getStatus() == RequestStatus.PENDING_FINANCE_REVIEW) {
            return rejectByFinance(requestId, action);
        } else if (request.getStatus() == RequestStatus.PENDING_ADMIN_REVIEW) {
            return rejectByAdmin(requestId, action);
        } else {
            throw new ResponseStatusException(BAD_REQUEST, "Request is not in a state that can be rejected");
        }
    }

    /**
     * Request additional information - allows Finance/Compliance to add comments
     * without changing status (comments stored in additionalInfoRequired field)
     */
    public VendorCreationRequestResponse requestAdditionalInfo(Long requestId, VendorRequestAction action) {
        VendorCreationRequest request = vendorCreationRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Vendor request not found"));
        
        // Can only request additional info during review stages
        if (request.getStatus() != RequestStatus.PENDING_COMPLIANCE_REVIEW && 
            request.getStatus() != RequestStatus.PENDING_FINANCE_REVIEW &&
            request.getStatus() != RequestStatus.PENDING_ADMIN_REVIEW) {
            throw new ResponseStatusException(BAD_REQUEST, "Additional info can only be requested during compliance, finance, or admin review");
        }
        
        // Store the additional info request (status remains in current review stage)
        request.setAdditionalInfoRequired(action.getComment());
        vendorCreationRequestRepository.save(request);
        return VendorCreationRequestMapper.toResponse(request);
    }

    public VendorCreationRequestResponse cancel(Long requestId) {
        VendorCreationRequest request = vendorCreationRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Vendor request not found"));
        request.cancel();
        vendorCreationRequestRepository.save(request);
        return VendorCreationRequestMapper.toResponse(request);
    }

    private String generateRequestNumber() {
        return "VCR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}

