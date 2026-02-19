package com.vms.vendor_management_system.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * VendorApproval entity representing approval workflow for vendor creation requests
 */
@Entity
@Table(name = "vendor_approvals")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class VendorApproval {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_creation_request_id", nullable = false)
    private VendorCreationRequest vendorCreationRequest;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id", nullable = false)
    private User approver;
    
    @Column(name = "approval_status", nullable = false)
    private String approvalStatus; // APPROVED, REJECTED, PENDING
    
    @Column(name = "comments")
    private String comments;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public VendorApproval(VendorCreationRequest vendorCreationRequest, User approver) {
        this.vendorCreationRequest = vendorCreationRequest;
        this.approver = approver;
        this.approvalStatus = "PENDING";
    }

    public void approve(String comments) {
        this.approvalStatus = "APPROVED";
        this.comments = comments;
        this.approvedAt = LocalDateTime.now();
    }

    public void reject(String comments) {
        this.approvalStatus = "REJECTED";
        this.comments = comments;
        this.approvedAt = LocalDateTime.now();
    }

    public boolean isApproved() {
        return "APPROVED".equals(this.approvalStatus);
    }

    public boolean isRejected() {
        return "REJECTED".equals(this.approvalStatus);
    }

    public boolean isPending() {
        return "PENDING".equals(this.approvalStatus);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VendorApproval that = (VendorApproval) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "VendorApproval{" +
                "id=" + id +
                ", approver=" + approver.getUsername() +
                ", approvalStatus='" + approvalStatus + '\'' +
                '}';
    }
}
