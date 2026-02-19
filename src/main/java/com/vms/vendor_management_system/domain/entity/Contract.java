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
 * Contract entity representing vendor contracts
 */
@Entity
@Table(name = "contracts")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Contract {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "contract_number", unique = true, nullable = false)
    private String contractNumber;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;
    
    @Column(name = "title", nullable = false)
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "contract_value", precision = 19, scale = 2)
    private BigDecimal contractValue;
    
    @Column(name = "currency", length = 10)
    private String currency;
    
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
    
    @Column(name = "contract_type")
    private String contractType; // SERVICE, SUPPLY, MAINTENANCE, etc.
    
    @Column(name = "status", nullable = false)
    private String status = "DRAFT"; // DRAFT, ACTIVE, EXPIRED, TERMINATED
    
    @Column(name = "document_url")
    private String documentUrl; // URL to uploaded contract document
    
    @Column(name = "terms_and_conditions", columnDefinition = "TEXT")
    private String termsAndConditions;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_user_id")
    private User approvedBy;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    @Column(name = "renewal_terms")
    private String renewalTerms;
    
    @Column(name = "termination_clause", columnDefinition = "TEXT")
    private String terminationClause;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Contract(String contractNumber, Vendor vendor, String title, LocalDate startDate, LocalDate endDate, User createdBy) {
        this.contractNumber = contractNumber;
        this.vendor = vendor;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdBy = createdBy;
    }

    public void approve(User approver) {
        this.status = "ACTIVE";
        this.approvedBy = approver;
        this.approvedAt = LocalDateTime.now();
    }

    public void terminate() {
        this.status = "TERMINATED";
    }

    public void expire() {
        this.status = "EXPIRED";
    }

    public boolean isActive() {
        return "ACTIVE".equals(this.status) && 
               LocalDate.now().isAfter(startDate) && 
               LocalDate.now().isBefore(endDate);
    }

    public boolean isExpired() {
        return LocalDate.now().isAfter(endDate);
    }

    public long getDaysUntilExpiration() {
        return LocalDate.now().until(endDate).getDays();
    }

    public boolean isNearExpiration(int daysThreshold) {
        return getDaysUntilExpiration() <= daysThreshold && getDaysUntilExpiration() > 0;
    }

    public void uploadDocument(String documentUrl) {
        this.documentUrl = documentUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contract contract = (Contract) o;
        return Objects.equals(id, contract.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Contract{" +
                "id=" + id +
                ", contractNumber='" + contractNumber + '\'' +
                ", vendor=" + vendor.getCompanyName() +
                ", title='" + title + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
