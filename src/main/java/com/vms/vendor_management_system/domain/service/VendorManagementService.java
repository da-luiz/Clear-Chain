package com.vms.vendor_management_system.domain.service;

import com.vms.vendor_management_system.domain.entity.*;
import com.vms.vendor_management_system.domain.enums.RequestStatus;
import com.vms.vendor_management_system.domain.enums.VendorStatus;
import com.vms.vendor_management_system.domain.repository.VendorCreationRequestRepository;
import com.vms.vendor_management_system.domain.repository.VendorRepository;
import com.vms.vendor_management_system.domain.valueobjects.Address;
import com.vms.vendor_management_system.domain.valueobjects.Email;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Domain service for vendor management business logic
 */
@Service
@Transactional
public class VendorManagementService {
    
    @Autowired
    private VendorRepository vendorRepository;
    
    @Autowired
    private VendorCreationRequestRepository vendorCreationRequestRepository;
    
    /**
     * Creates a new vendor from an approved vendor creation request (ACTIVE status)
     * Only called after Admin approves (final approval)
     */
    public Vendor createVendorFromRequest(VendorCreationRequest request) {
        if (!RequestStatus.ACTIVE.equals(request.getStatus())) {
            throw new IllegalStateException("Cannot create vendor from non-active request. Request must be approved by Admin first.");
        }
        
        // Generate vendor code
        String vendorCode = generateVendorCode(request.getCompanyName());
        
        // Create email value object if available
        Email email = null;
        if (request.getPrimaryContactEmail() != null && !request.getPrimaryContactEmail().trim().isEmpty()) {
            email = new Email(request.getPrimaryContactEmail());
        }
        
        // Create address value object if available
        Address address = null;
        if (request.getAddressStreet() != null && request.getAddressCity() != null && request.getAddressCountry() != null) {
            address = new Address(
                request.getAddressStreet(),
                request.getAddressCity(),
                request.getAddressState(),
                request.getAddressPostalCode(),
                request.getAddressCountry()
            );
        }
        
        // Create vendor entity
        Vendor vendor = new Vendor(
            vendorCode,
            request.getCompanyName(),
            request.getLegalName(),
            email,
            address
        );
        
        // Set additional vendor details
        vendor.setTaxId(request.getTaxIdentificationNumber());
        vendor.setPhone(request.getPrimaryContactPhone());
        vendor.setCategory(request.getCategory());
        vendor.setWebsite(request.getWebsite());
        vendor.setDescription(request.getBusinessJustification());
        // Automatically activate vendor since it's already been approved through the request workflow
        vendor.setStatus(VendorStatus.ACTIVE);
        
        // Save vendor
        Vendor savedVendor = vendorRepository.save(vendor);
        
        // Link request to vendor
        request.setVendor(savedVendor);
        vendorCreationRequestRepository.save(request);
        
        return savedVendor;
    }
    
    /**
     * Finance approves a vendor creation request - moves to Admin review
     */
    public void approveByFinance(VendorCreationRequest request, User financeReviewer) {
        if (!request.canBeApprovedByFinance()) {
            throw new IllegalStateException("Request cannot be approved by Finance in current status: " + request.getStatus());
        }
        request.approveByFinance(financeReviewer);
        vendorCreationRequestRepository.save(request);
    }

    /**
     * Finance rejects a vendor creation request
     */
    public void rejectByFinance(VendorCreationRequest request, User financeReviewer, String rejectionReason) {
        if (!request.canBeRejectedByFinance()) {
            throw new IllegalStateException("Request cannot be rejected by Finance in current status: " + request.getStatus());
        }
        request.rejectByFinance(financeReviewer, rejectionReason);
        vendorCreationRequestRepository.save(request);
    }

    /**
     * Compliance approves a vendor creation request - moves to Finance review
     */
    public void approveByCompliance(VendorCreationRequest request, User complianceReviewer) {
        if (!request.canBeApprovedByCompliance()) {
            throw new IllegalStateException("Request cannot be approved by Compliance in current status: " + request.getStatus());
        }
        request.approveByCompliance(complianceReviewer);
        vendorCreationRequestRepository.save(request);
    }

    /**
     * Compliance rejects a vendor creation request
     */
    public void rejectByCompliance(VendorCreationRequest request, User complianceReviewer, String rejectionReason) {
        if (!request.canBeRejectedByCompliance()) {
            throw new IllegalStateException("Request cannot be rejected by Compliance in current status: " + request.getStatus());
        }
        request.rejectByCompliance(complianceReviewer, rejectionReason);
        vendorCreationRequestRepository.save(request);
    }

    /**
     * Admin approves a vendor creation request - creates active vendor (final approval)
     */
    public void approveByAdmin(VendorCreationRequest request, User adminReviewer) {
        if (!request.canBeApprovedByAdmin()) {
            throw new IllegalStateException("Request cannot be approved by Admin in current status: " + request.getStatus());
        }
        request.approveByAdmin(adminReviewer);
        // Create vendor from approved request (only Admin can create vendor)
        createVendorFromRequest(request);
        vendorCreationRequestRepository.save(request);
    }

    /**
     * Admin rejects a vendor creation request
     */
    public void rejectByAdmin(VendorCreationRequest request, User adminReviewer, String rejectionReason) {
        if (!request.canBeRejectedByAdmin()) {
            throw new IllegalStateException("Request cannot be rejected by Admin in current status: " + request.getStatus());
        }
        request.rejectByAdmin(adminReviewer, rejectionReason);
        vendorCreationRequestRepository.save(request);
    }

    // Legacy methods for backward compatibility
    @Deprecated
    public void approveVendorRequest(VendorCreationRequest request, User approver) {
        // Try to determine which approver based on current status
        if (request.getStatus() == RequestStatus.PENDING_COMPLIANCE_REVIEW) {
            approveByCompliance(request, approver);
        } else if (request.getStatus() == RequestStatus.PENDING_FINANCE_REVIEW) {
            approveByFinance(request, approver);
        } else if (request.getStatus() == RequestStatus.PENDING_ADMIN_REVIEW) {
            approveByAdmin(request, approver);
        } else {
            throw new IllegalStateException("Request cannot be approved in current status: " + request.getStatus());
        }
    }
    
    @Deprecated
    public void rejectVendorRequest(VendorCreationRequest request, User reviewer, String rejectionReason) {
        // Try to determine which approver based on current status
        if (request.getStatus() == RequestStatus.PENDING_COMPLIANCE_REVIEW) {
            rejectByCompliance(request, reviewer, rejectionReason);
        } else if (request.getStatus() == RequestStatus.PENDING_FINANCE_REVIEW) {
            rejectByFinance(request, reviewer, rejectionReason);
        } else if (request.getStatus() == RequestStatus.PENDING_ADMIN_REVIEW) {
            rejectByAdmin(request, reviewer, rejectionReason);
        } else {
            throw new IllegalStateException("Request cannot be rejected in current status: " + request.getStatus());
        }
    }
    
    /**
     * Activates a vendor
     */
    public void activateVendor(Vendor vendor) {
        // Allow activation from PENDING_CREATION or APPROVED status
        if (!VendorStatus.PENDING_CREATION.equals(vendor.getStatus()) && 
            !VendorStatus.APPROVED.equals(vendor.getStatus())) {
            throw new IllegalStateException("Only pending or approved vendors can be activated");
        }
        
        vendor.activate();
        vendorRepository.save(vendor);
    }
    
    /**
     * Suspends a vendor
     */
    public void suspendVendor(Vendor vendor) {
        if (!vendor.isActive()) {
            throw new IllegalStateException("Cannot suspend inactive vendor");
        }
        
        vendor.suspend();
        vendorRepository.save(vendor);
    }
    
    /**
     * Terminates a vendor
     */
    public void terminateVendor(Vendor vendor) {
        vendor.terminate();
        vendorRepository.save(vendor);
    }
    
    /**
     * Gets pending vendor creation requests
     */
    public List<VendorCreationRequest> getPendingRequests() {
        return vendorCreationRequestRepository.findPendingRequests();
    }
    
    /**
     * Gets recent vendor creation requests
     */
    public List<VendorCreationRequest> getRecentRequests(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        return vendorCreationRequestRepository.findRecentRequests(startDate);
    }
    
    /**
     * Generates a unique vendor code
     */
    private String generateVendorCode(String companyName) {
        String baseCode = companyName.replaceAll("[^A-Za-z0-9]", "").toUpperCase().substring(0, Math.min(6, companyName.length()));
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(8);
        return baseCode + timestamp;
    }
}
