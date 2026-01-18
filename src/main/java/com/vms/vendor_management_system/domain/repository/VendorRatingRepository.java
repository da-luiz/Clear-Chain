package com.vms.vendor_management_system.domain.repository;

import com.vms.vendor_management_system.domain.entity.VendorRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for {@link VendorRating}.
 */
@Repository
public interface VendorRatingRepository extends JpaRepository<VendorRating, Long> {

    List<VendorRating> findByVendorId(Long vendorId);

    List<VendorRating> findByCriteriaId(Long criteriaId);

    @Query("SELECT vr FROM VendorRating vr WHERE vr.vendor.id = :vendorId AND vr.ratingPeriodStart >= :start AND vr.ratingPeriodEnd <= :end")
    List<VendorRating> findByVendorAndPeriod(@Param("vendorId") Long vendorId,
                                             @Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end);

    @Query("SELECT AVG(vr.score) FROM VendorRating vr WHERE vr.vendor.id = :vendorId")
    Double calculateAverageScore(@Param("vendorId") Long vendorId);
}


