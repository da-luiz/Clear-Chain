package com.vms.vendor_management_system.domain.repository;

import com.vms.vendor_management_system.domain.entity.VendorCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for {@link VendorCategory}.
 */
@Repository
public interface VendorCategoryRepository extends JpaRepository<VendorCategory, Long> {

    Optional<VendorCategory> findByCode(String code);

    Optional<VendorCategory> findByName(String name);

    List<VendorCategory> findByIsActiveTrue();

    @Query("SELECT COUNT(vc) FROM VendorCategory vc WHERE vc.isActive = true")
    long countActiveCategories();

    @Query("SELECT vc FROM VendorCategory vc WHERE vc.name LIKE %:name% ORDER BY vc.name ASC")
    List<VendorCategory> searchActiveCategories(@Param("name") String name);
}


