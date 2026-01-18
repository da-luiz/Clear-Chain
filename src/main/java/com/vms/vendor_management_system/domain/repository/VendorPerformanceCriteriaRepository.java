package com.vms.vendor_management_system.domain.repository;

import com.vms.vendor_management_system.domain.entity.VendorPerformanceCriteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for {@link VendorPerformanceCriteria}.
 */
@Repository
public interface VendorPerformanceCriteriaRepository extends JpaRepository<VendorPerformanceCriteria, Long> {

    List<VendorPerformanceCriteria> findByCategoryId(Long categoryId);

    List<VendorPerformanceCriteria> findByIsActiveTrue();

    @Query("SELECT vpc FROM VendorPerformanceCriteria vpc WHERE vpc.category.id = :categoryId AND vpc.isActive = true")
    List<VendorPerformanceCriteria> findActiveByCategory(@Param("categoryId") Long categoryId);

    @Query("SELECT COUNT(vpc) FROM VendorPerformanceCriteria vpc WHERE vpc.category.id = :categoryId")
    long countByCategory(@Param("categoryId") Long categoryId);
}

