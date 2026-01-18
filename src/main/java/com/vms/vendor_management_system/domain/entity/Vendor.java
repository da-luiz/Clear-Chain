package com.vms.vendor_management_system.domain.entity;

import com.vms.vendor_management_system.domain.enums.VendorStatus;
import com.vms.vendor_management_system.domain.valueobjects.Address;
import com.vms.vendor_management_system.domain.valueobjects.Email;
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
 * Vendor entity representing suppliers/vendors in the system
 */
@Entity
@Table(name = "vendors")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Vendor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "vendor_code", unique = true, nullable = false)
    private String vendorCode;
    
    @Column(name = "company_name", nullable = false)
    private String companyName;
    
    @Column(name = "legal_name")
    private String legalName;
    
    @Column(name = "tax_id")
    private String taxId;
    
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "email_value", length = 320))
    private Email email;
    
    @Column(name = "phone")
    private String phone;
    
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "street", column = @Column(name = "address_street")),
            @AttributeOverride(name = "city", column = @Column(name = "address_city")),
            @AttributeOverride(name = "state", column = @Column(name = "address_state")),
            @AttributeOverride(name = "postalCode", column = @Column(name = "address_postal_code")),
            @AttributeOverride(name = "country", column = @Column(name = "address_country"))
    })
    private Address address;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private VendorStatus status = VendorStatus.PENDING_CREATION;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private VendorCategory category;
    
    @OneToMany(mappedBy = "vendor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VendorRating> ratings = new ArrayList<>();
    
    @OneToMany(mappedBy = "vendor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Contract> contracts = new ArrayList<>();
    
    @OneToMany(mappedBy = "vendor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PurchaseOrder> purchaseOrders = new ArrayList<>();
    
    @Column(name = "website")
    private String website;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "notes")
    private String notes;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Vendor(String vendorCode, String companyName, String legalName, Email email, Address address) {
        this.vendorCode = vendorCode;
        this.companyName = companyName;
        this.legalName = legalName;
        this.email = email;
        this.address = address;
    }

    public void approve() {
        this.status = VendorStatus.APPROVED;
    }

    public void reject() {
        this.status = VendorStatus.REJECTED;
    }

    public void activate() {
        this.status = VendorStatus.ACTIVE;
    }

    public void suspend() {
        this.status = VendorStatus.SUSPENDED;
    }

    public void terminate() {
        this.status = VendorStatus.TERMINATED;
    }

    public void setUnderReview() {
        this.status = VendorStatus.UNDER_REVIEW;
    }

    public boolean isActive() {
        return VendorStatus.ACTIVE.equals(this.status);
    }

    public boolean isApproved() {
        return VendorStatus.APPROVED.equals(this.status) || VendorStatus.ACTIVE.equals(this.status);
    }

    public void addRating(VendorRating rating) {
        if (!ratings.contains(rating)) {
            ratings.add(rating);
            rating.setVendor(this);
        }
    }

    public void addContract(Contract contract) {
        if (!contracts.contains(contract)) {
            contracts.add(contract);
            contract.setVendor(this);
        }
    }

    public double getAverageRating() {
        if (ratings.isEmpty()) {
            return 0.0;
        }
        return ratings.stream()
                .mapToDouble(VendorRating::getScore)
                .average()
                .orElse(0.0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vendor vendor = (Vendor) o;
        return Objects.equals(id, vendor.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Vendor{" +
                "id=" + id +
                ", vendorCode='" + vendorCode + '\'' +
                ", companyName='" + companyName + '\'' +
                ", status=" + status +
                '}';
    }
}
