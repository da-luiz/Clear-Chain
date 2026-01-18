package com.vms.vendor_management_system.domain.repository;

import com.vms.vendor_management_system.domain.entity.Vendor;
import com.vms.vendor_management_system.domain.enums.VendorStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Vendor entity
 */
@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long> {
    
    Optional<Vendor> findByVendorCode(String vendorCode);
    
    List<Vendor> findByStatus(VendorStatus status);
    
    List<Vendor> findByCategoryId(Long categoryId);
    
    @Query("SELECT v FROM Vendor v WHERE v.status IN :statuses")
    List<Vendor> findByStatusIn(@Param("statuses") List<VendorStatus> statuses);
    
    @Query("SELECT v FROM Vendor v WHERE v.companyName LIKE %:name% OR v.legalName LIKE %:name%")
    List<Vendor> findByNameContaining(@Param("name") String name);
    
    @Query("SELECT COUNT(v) FROM Vendor v WHERE v.status = :status")
    long countByStatus(@Param("status") VendorStatus status);
    
    @Query("SELECT COUNT(v) FROM Vendor v WHERE v.status IN :statuses")
    long countByStatusIn(@Param("statuses") List<VendorStatus> statuses);
    
    @Query("SELECT v FROM Vendor v WHERE v.createdAt >= :startDate ORDER BY v.createdAt DESC")
    List<Vendor> findRecentVendors(@Param("startDate") LocalDateTime startDate);
    
    List<Vendor> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    @Query("SELECT v FROM Vendor v WHERE v.status = 'ACTIVE' ORDER BY v.companyName")
    List<Vendor> findActiveVendorsOrderByName();
    
    boolean existsByVendorCode(String vendorCode);
    
    boolean existsByEmailValue(String email);
}
