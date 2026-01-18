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
 * VendorRating entity representing performance ratings for vendors
 */
@Entity
@Table(name = "vendor_ratings")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class VendorRating {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criteria_id", nullable = false)
    private VendorPerformanceCriteria criteria;
    
    @Column(name = "score", nullable = false)
    private Integer score;
    
    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;
    
    @Column(name = "evidence_url")
    private String evidenceUrl; // URL to uploaded evidence
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rated_by_user_id", nullable = false)
    private User ratedBy;
    
    @Column(name = "rating_period_start")
    private LocalDateTime ratingPeriodStart;
    
    @Column(name = "rating_period_end")
    private LocalDateTime ratingPeriodEnd;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public VendorRating(Vendor vendor, VendorPerformanceCriteria criteria, Integer score, User ratedBy) {
        this.vendor = vendor;
        this.criteria = criteria;
        this.score = score;
        this.ratedBy = ratedBy;
        
        // Validate score against criteria
        if (!criteria.isValidScore(score)) {
            throw new IllegalArgumentException("Score must be between 0 and " + criteria.getMaxScore());
        }
    }

    public void updateScore(Integer newScore) {
        if (!criteria.isValidScore(newScore)) {
            throw new IllegalArgumentException("Score must be between 0 and " + criteria.getMaxScore());
        }
        this.score = newScore;
    }

    public void addEvidence(String evidenceUrl) {
        this.evidenceUrl = evidenceUrl;
    }

    public void setRatingPeriod(LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null && start.isAfter(end)) {
            throw new IllegalArgumentException("Rating period start cannot be after end");
        }
        this.ratingPeriodStart = start;
        this.ratingPeriodEnd = end;
    }

    public double getWeightedScore() {
        return score * criteria.getWeight();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VendorRating that = (VendorRating) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "VendorRating{" +
                "id=" + id +
                ", vendor=" + vendor.getCompanyName() +
                ", criteria=" + criteria.getName() +
                ", score=" + score +
                ", ratedBy=" + ratedBy.getUsername() +
                '}';
    }
}
