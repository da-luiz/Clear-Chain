package com.vms.vendor_management_system.domain.entity;

import com.vms.vendor_management_system.domain.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * VendorCreationRequest entity representing vendor creation requests from departments
 */
@Entity
@Table(name = "vendor_creation_requests")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class VendorCreationRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "request_number", unique = true, nullable = false)
    private String requestNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RequestStatus status = RequestStatus.DRAFT;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requesting_department_id", nullable = false)
    private Department requestingDepartment;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by_user_id", nullable = false)
    private User requestedBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id")
    private Vendor vendor;
    
    @Column(name = "company_name", nullable = false)
    private String companyName;
    
    @Column(name = "legal_name")
    private String legalName;
    
    @Column(name = "business_justification", columnDefinition = "TEXT")
    private String businessJustification;
    
    @Column(name = "expected_contract_value")
    private Double expectedContractValue;
    
    @Column(name = "rejection_reason")
    private String rejectionReason;
    
    @Column(name = "additional_info_required")
    private String additionalInfoRequired;
    
    // Contact details (filled by Requester)
    @Column(name = "primary_contact_name")
    private String primaryContactName;
    
    @Column(name = "primary_contact_title")
    private String primaryContactTitle;
    
    @Column(name = "primary_contact_email", length = 320)
    private String primaryContactEmail;
    
    @Column(name = "primary_contact_phone", length = 50)
    private String primaryContactPhone;
    
    // Additional company info (filled by Requester)
    @Column(name = "business_registration_number", length = 100)
    private String businessRegistrationNumber;
    
    @Column(name = "tax_identification_number", length = 100)
    private String taxIdentificationNumber;
    
    @Column(name = "business_type", length = 100)
    private String businessType;
    
    @Column(name = "website", length = 255)
    private String website;
    
    @Column(name = "address_street")
    private String addressStreet;
    
    @Column(name = "address_city")
    private String addressCity;
    
    @Column(name = "address_state")
    private String addressState;
    
    @Column(name = "address_postal_code", length = 50)
    private String addressPostalCode;
    
    @Column(name = "address_country")
    private String addressCountry;
    
    // Banking and payment details (filled by Purchasing Team)
    @Column(name = "bank_name")
    private String bankName;
    
    @Column(name = "account_holder_name")
    private String accountHolderName;
    
    @Column(name = "account_number", length = 100)
    private String accountNumber;
    
    @Column(name = "swift_bic_code", length = 50)
    private String swiftBicCode;
    
    // Currency selected by Requester (moved from Finance section)
    @Column(name = "currency", length = 10)
    private String currency;
    
    @Column(name = "payment_terms", length = 100)
    private String paymentTerms;
    
    @Column(name = "preferred_payment_method", length = 100)
    private String preferredPaymentMethod;
    
    // Files, links, GitHub pages, LinkedIn (stored as JSON array)
    // Format: [{"type":"file|link|github|linkedin","value":"...","name":"...","fileName":"..."}]
    // For uploaded files: fileName is the original filename, value is the file path/URL
    @Column(name = "supporting_documents", columnDefinition = "TEXT")
    private String supportingDocuments;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private VendorCategory category;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_user_id")
    private User reviewedBy;
    
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
    
    @OneToMany(mappedBy = "vendorCreationRequest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VendorApproval> approvals = new ArrayList<>();
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public VendorCreationRequest(String requestNumber, Department requestingDepartment, User requestedBy, String companyName) {
        this.requestNumber = requestNumber;
        this.requestingDepartment = requestingDepartment;
        this.requestedBy = requestedBy;
        this.companyName = companyName;
    }

    /**
     * Submit draft request to Compliance for review (new workflow: Compliance first)
     */
    public void submit() {
        if (this.status != RequestStatus.DRAFT && this.status != RequestStatus.RETURNED_FOR_INFO) {
            throw new IllegalStateException("Only draft or returned requests can be submitted");
        }
        this.status = RequestStatus.PENDING_COMPLIANCE_REVIEW;
    }

    /**
     * Compliance approves the request - moves to Finance review
     */
    public void approveByCompliance(User complianceReviewer) {
        if (this.status != RequestStatus.PENDING_COMPLIANCE_REVIEW) {
            throw new IllegalStateException("Only requests pending compliance review can be approved by Compliance");
        }
        this.status = RequestStatus.PENDING_FINANCE_REVIEW;
        this.reviewedBy = complianceReviewer;
        this.reviewedAt = LocalDateTime.now();
    }

    /**
     * Compliance rejects the request
     */
    public void rejectByCompliance(User complianceReviewer, String rejectionReason) {
        if (this.status != RequestStatus.PENDING_COMPLIANCE_REVIEW) {
            throw new IllegalStateException("Only requests pending compliance review can be rejected by Compliance");
        }
        this.status = RequestStatus.REJECTED_BY_COMPLIANCE;
        this.reviewedBy = complianceReviewer;
        this.reviewedAt = LocalDateTime.now();
        this.rejectionReason = rejectionReason;
    }

    /**
     * Finance approves the request - moves to Admin review
     * Finance cannot approve without banking details
     */
    public void approveByFinance(User financeReviewer) {
        if (this.status != RequestStatus.PENDING_FINANCE_REVIEW) {
            throw new IllegalStateException("Only requests pending finance review can be approved by Finance");
        }
        // Validate banking details are present
        if (bankName == null || bankName.trim().isEmpty() ||
            accountNumber == null || accountNumber.trim().isEmpty() ||
            accountHolderName == null || accountHolderName.trim().isEmpty()) {
            throw new IllegalStateException("Finance cannot approve without banking details (bank name, account number, account holder name)");
        }
        this.status = RequestStatus.PENDING_ADMIN_REVIEW;
        this.reviewedBy = financeReviewer;
        this.reviewedAt = LocalDateTime.now();
    }

    /**
     * Finance rejects the request
     */
    public void rejectByFinance(User financeReviewer, String rejectionReason) {
        if (this.status != RequestStatus.PENDING_FINANCE_REVIEW) {
            throw new IllegalStateException("Only requests pending finance review can be rejected by Finance");
        }
        this.status = RequestStatus.REJECTED_BY_FINANCE;
        this.reviewedBy = financeReviewer;
        this.reviewedAt = LocalDateTime.now();
        this.rejectionReason = rejectionReason;
    }

    /**
     * Admin approves the request - creates active vendor (final approval)
     */
    public void approveByAdmin(User adminReviewer) {
        if (this.status != RequestStatus.PENDING_ADMIN_REVIEW) {
            throw new IllegalStateException("Only requests pending admin review can be approved by Admin");
        }
        this.status = RequestStatus.ACTIVE;
        this.reviewedBy = adminReviewer;
        this.reviewedAt = LocalDateTime.now();
    }

    /**
     * Admin rejects the request
     */
    public void rejectByAdmin(User adminReviewer, String rejectionReason) {
        if (this.status != RequestStatus.PENDING_ADMIN_REVIEW) {
            throw new IllegalStateException("Only requests pending admin review can be rejected by Admin");
        }
        this.status = RequestStatus.REJECTED_BY_ADMIN;
        this.reviewedBy = adminReviewer;
        this.reviewedAt = LocalDateTime.now();
        this.rejectionReason = rejectionReason;
    }

    public void cancel() {
        if (this.status == RequestStatus.ACTIVE || 
            this.status == RequestStatus.REJECTED_BY_FINANCE || 
            this.status == RequestStatus.REJECTED_BY_COMPLIANCE) {
            throw new IllegalStateException("Cannot cancel active or rejected requests");
        }
        this.status = RequestStatus.CANCELLED;
    }

    /**
     * Legacy methods for backward compatibility - delegate to new workflow methods
     */
    @Deprecated
    public void approve(User reviewer) {
        // Legacy method - delegate to appropriate approver based on status
        if (this.status == RequestStatus.PENDING_COMPLIANCE_REVIEW) {
            approveByCompliance(reviewer);
        } else if (this.status == RequestStatus.PENDING_FINANCE_REVIEW) {
            approveByFinance(reviewer);
        } else if (this.status == RequestStatus.PENDING_ADMIN_REVIEW) {
            approveByAdmin(reviewer);
        } else {
            throw new IllegalStateException("Request is not in a state that can be approved");
        }
    }

    @Deprecated
    public void reject(User reviewer, String rejectionReason) {
        // Legacy method - delegate to appropriate approver based on status
        if (this.status == RequestStatus.PENDING_COMPLIANCE_REVIEW) {
            rejectByCompliance(reviewer, rejectionReason);
        } else if (this.status == RequestStatus.PENDING_FINANCE_REVIEW) {
            rejectByFinance(reviewer, rejectionReason);
        } else if (this.status == RequestStatus.PENDING_ADMIN_REVIEW) {
            rejectByAdmin(reviewer, rejectionReason);
        } else {
            throw new IllegalStateException("Request is not in a state that can be rejected");
        }
    }

    public boolean canBeApprovedByCompliance() {
        return this.status == RequestStatus.PENDING_COMPLIANCE_REVIEW;
    }

    public boolean canBeRejectedByCompliance() {
        return this.status == RequestStatus.PENDING_COMPLIANCE_REVIEW;
    }

    public boolean canBeApprovedByFinance() {
        return this.status == RequestStatus.PENDING_FINANCE_REVIEW;
    }

    public boolean canBeRejectedByFinance() {
        return this.status == RequestStatus.PENDING_FINANCE_REVIEW;
    }

    public boolean canBeApprovedByAdmin() {
        return this.status == RequestStatus.PENDING_ADMIN_REVIEW;
    }

    public boolean canBeRejectedByAdmin() {
        return this.status == RequestStatus.PENDING_ADMIN_REVIEW;
    }

    public boolean hasBankingDetails() {
        return bankName != null && !bankName.trim().isEmpty() &&
               accountNumber != null && !accountNumber.trim().isEmpty() &&
               accountHolderName != null && !accountHolderName.trim().isEmpty();
    }

    // Legacy compatibility methods
    @Deprecated
    public boolean canBeApproved() {
        return canBeApprovedByCompliance() || canBeApprovedByFinance() || canBeApprovedByAdmin();
    }

    @Deprecated
    public boolean canBeRejected() {
        return canBeRejectedByCompliance() || canBeRejectedByFinance() || canBeRejectedByAdmin();
    }

    public void addApproval(VendorApproval approval) {
        if (!approvals.contains(approval)) {
            approvals.add(approval);
            approval.setVendorCreationRequest(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VendorCreationRequest that = (VendorCreationRequest) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "VendorCreationRequest{" +
                "id=" + id +
                ", requestNumber='" + requestNumber + '\'' +
                ", status=" + status +
                ", companyName='" + companyName + '\'' +
                '}';
    }
}
