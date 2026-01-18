package com.vms.vendor_management_system.domain.entity;

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
 * VendorCategory entity representing different categories of vendors
 */
@Entity
@Table(name = "vendor_categories")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class VendorCategory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false, unique = true)
    private String name;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "code", nullable = false, unique = true)
    private String code;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Vendor> vendors = new ArrayList<>();
    
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VendorPerformanceCriteria> performanceCriteria = new ArrayList<>();
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public VendorCategory(String name, String description, String code) {
        this.name = name;
        this.description = description;
        this.code = code;
    }

    public void addVendor(Vendor vendor) {
        if (!vendors.contains(vendor)) {
            vendors.add(vendor);
            vendor.setCategory(this);
        }
    }

    public void removeVendor(Vendor vendor) {
        vendors.remove(vendor);
        vendor.setCategory(null);
    }

    public void addPerformanceCriteria(VendorPerformanceCriteria criteria) {
        if (!performanceCriteria.contains(criteria)) {
            performanceCriteria.add(criteria);
            criteria.setCategory(this);
        }
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VendorCategory that = (VendorCategory) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "VendorCategory{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
