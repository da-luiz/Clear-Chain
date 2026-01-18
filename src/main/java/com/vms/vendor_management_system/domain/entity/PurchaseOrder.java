package com.vms.vendor_management_system.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * PurchaseOrder entity representing purchase orders
 */
@Entity
@Table(name = "purchase_orders")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class PurchaseOrder {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "po_number", unique = true, nullable = false)
    private String poNumber;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "total_amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal totalAmount;
    
    @Column(name = "currency", length = 10)
    private String currency;
    
    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;
    
    @Column(name = "expected_delivery_date")
    private LocalDate expectedDeliveryDate;
    
    @Column(name = "status", nullable = false)
    private String status = "DRAFT"; // DRAFT, PENDING_APPROVAL, APPROVED, REJECTED, SENT, RECEIVED, CANCELLED
    
    @Column(name = "approval_threshold", precision = 19, scale = 2)
    private BigDecimal approvalThreshold;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_user_id")
    private User approvedBy;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    @Column(name = "rejection_reason")
    private String rejectionReason;
    
    @Column(name = "delivery_address", columnDefinition = "TEXT")
    private String deliveryAddress;
    
    @Column(name = "payment_terms")
    private String paymentTerms;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public PurchaseOrder(String poNumber, Vendor vendor, String description, BigDecimal totalAmount, LocalDate orderDate, User createdBy) {
        this.poNumber = poNumber;
        this.vendor = vendor;
        this.description = description;
        this.totalAmount = totalAmount;
        this.orderDate = orderDate;
        this.createdBy = createdBy;
    }

    public void submitForApproval() {
        if (!"DRAFT".equals(this.status)) {
            throw new IllegalStateException("Only draft purchase orders can be submitted for approval");
        }
        this.status = "PENDING_APPROVAL";
    }

    public void approve(User approver) {
        if (!"PENDING_APPROVAL".equals(this.status)) {
            throw new IllegalStateException("Only pending approval purchase orders can be approved");
        }
        this.status = "APPROVED";
        this.approvedBy = approver;
        this.approvedAt = LocalDateTime.now();
    }

    public void reject(User approver, String rejectionReason) {
        if (!"PENDING_APPROVAL".equals(this.status)) {
            throw new IllegalStateException("Only pending approval purchase orders can be rejected");
        }
        this.status = "REJECTED";
        this.approvedBy = approver;
        this.approvedAt = LocalDateTime.now();
        this.rejectionReason = rejectionReason;
    }

    public void send() {
        if (!"APPROVED".equals(this.status)) {
            throw new IllegalStateException("Only approved purchase orders can be sent");
        }
        this.status = "SENT";
    }

    public void markAsReceived() {
        if (!"SENT".equals(this.status)) {
            throw new IllegalStateException("Only sent purchase orders can be marked as received");
        }
        this.status = "RECEIVED";
    }

    public void cancel() {
        if ("RECEIVED".equals(this.status)) {
            throw new IllegalStateException("Cannot cancel received purchase orders");
        }
        this.status = "CANCELLED";
    }

    public boolean requiresApproval() {
        return approvalThreshold != null && totalAmount.compareTo(approvalThreshold) > 0;
    }

    public boolean isApproved() {
        return "APPROVED".equals(this.status) || "SENT".equals(this.status) || "RECEIVED".equals(this.status);
    }

    public boolean canBeApproved() {
        return "PENDING_APPROVAL".equals(this.status);
    }

    public boolean canBeRejected() {
        return "PENDING_APPROVAL".equals(this.status);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PurchaseOrder that = (PurchaseOrder) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PurchaseOrder{" +
                "id=" + id +
                ", poNumber='" + poNumber + '\'' +
                ", vendor=" + vendor.getCompanyName() +
                ", totalAmount=" + totalAmount +
                ", status='" + status + '\'' +
                '}';
    }
}
