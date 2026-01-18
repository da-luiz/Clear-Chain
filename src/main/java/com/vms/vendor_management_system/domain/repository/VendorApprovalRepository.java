package com.vms.vendor_management_system.domain.repository;

import com.vms.vendor_management_system.domain.entity.VendorApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for {@link VendorApproval}.
 */
@Repository
public interface VendorApprovalRepository extends JpaRepository<VendorApproval, Long> {

    List<VendorApproval> findByVendorCreationRequestId(Long vendorCreationRequestId);

    List<VendorApproval> findByApproverId(Long approverId);

    @Query("SELECT va FROM VendorApproval va WHERE va.approvalStatus = :status")
    List<VendorApproval> findByStatus(@Param("status") String status);

    @Query("SELECT COUNT(va) FROM VendorApproval va WHERE va.vendorCreationRequest.id = :requestId AND va.approvalStatus = 'APPROVED'")
    long countApprovedByRequest(@Param("requestId") Long requestId);
}


