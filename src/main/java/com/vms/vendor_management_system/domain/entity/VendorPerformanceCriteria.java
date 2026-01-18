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
 * VendorPerformanceCriteria entity representing performance criteria for vendor evaluation
 */
@Entity
@Table(name = "vendor_performance_criteria")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class VendorPerformanceCriteria {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "weight", nullable = false)
    private Double weight; // Weight of this criteria in overall rating (0.0 to 1.0)
    
    @Column(name = "max_score", nullable = false)
    private Integer maxScore; // Maximum possible score for this criteria
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private VendorCategory category;
    
    @OneToMany(mappedBy = "criteria", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VendorRating> ratings = new ArrayList<>();
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public VendorPerformanceCriteria(String name, String description, Double weight, Integer maxScore, VendorCategory category) {
        this.name = name;
        this.description = description;
        this.weight = weight;
        this.maxScore = maxScore;
        this.category = category;
    }

    public void addRating(VendorRating rating) {
        if (!ratings.contains(rating)) {
            ratings.add(rating);
            rating.setCriteria(this);
        }
    }

    public void removeRating(VendorRating rating) {
        ratings.remove(rating);
        rating.setCriteria(null);
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }

    public boolean isValidScore(Integer score) {
        return score != null && score >= 0 && score <= maxScore;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VendorPerformanceCriteria that = (VendorPerformanceCriteria) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "VendorPerformanceCriteria{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", weight=" + weight +
                ", maxScore=" + maxScore +
                ", isActive=" + isActive +
                '}';
    }
}
