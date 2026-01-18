package com.vms.vendor_management_system.domain.repository;

import com.vms.vendor_management_system.domain.entity.VendorCreationRequest;
import com.vms.vendor_management_system.domain.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for VendorCreationRequest entity
 */
@Repository
public interface VendorCreationRequestRepository extends JpaRepository<VendorCreationRequest, Long> {
    
    List<VendorCreationRequest> findByStatus(RequestStatus status);
    
    List<VendorCreationRequest> findByRequestingDepartmentId(Long departmentId);
    
    List<VendorCreationRequest> findByRequestedById(Long userId);
    
    List<VendorCreationRequest> findByReviewedById(Long userId);
    
    @Query("SELECT vcr FROM VendorCreationRequest vcr WHERE vcr.status IN :statuses")
    List<VendorCreationRequest> findByStatusIn(@Param("statuses") List<RequestStatus> statuses);
    
    @Query("SELECT vcr FROM VendorCreationRequest vcr WHERE vcr.createdAt >= :startDate ORDER BY vcr.createdAt DESC")
    List<VendorCreationRequest> findRecentRequests(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT COUNT(vcr) FROM VendorCreationRequest vcr WHERE vcr.status = :status")
    long countByStatus(@Param("status") RequestStatus status);
    
    @Query("SELECT COUNT(vcr) FROM VendorCreationRequest vcr WHERE vcr.requestingDepartment.id = :departmentId AND vcr.status = :status")
    long countByDepartmentAndStatus(@Param("departmentId") Long departmentId, @Param("status") RequestStatus status);
    
    @Query("SELECT vcr FROM VendorCreationRequest vcr WHERE vcr.status = 'PENDING_COMPLIANCE_REVIEW' OR vcr.status = 'PENDING_FINANCE_REVIEW' OR vcr.status = 'PENDING_ADMIN_REVIEW' ORDER BY vcr.createdAt ASC")
    List<VendorCreationRequest> findPendingRequests();
    
    @Query("SELECT vcr FROM VendorCreationRequest vcr WHERE vcr.reviewedBy.id = :reviewerId AND vcr.status IN :statuses")
    List<VendorCreationRequest> findByReviewerAndStatusIn(@Param("reviewerId") Long reviewerId, @Param("statuses") List<RequestStatus> statuses);
    
    @Query("SELECT vcr FROM VendorCreationRequest vcr WHERE vcr.vendor.id = :vendorId AND vcr.status = 'ACTIVE' ORDER BY vcr.createdAt DESC")
    List<VendorCreationRequest> findByVendorId(@Param("vendorId") Long vendorId);
}
