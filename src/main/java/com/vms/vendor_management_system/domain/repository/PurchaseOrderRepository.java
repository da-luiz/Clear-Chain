package com.vms.vendor_management_system.domain.repository;

import com.vms.vendor_management_system.domain.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for {@link PurchaseOrder}.
 */
@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    Optional<PurchaseOrder> findByPoNumber(String poNumber);

    List<PurchaseOrder> findByVendorId(Long vendorId);

    List<PurchaseOrder> findByCreatedById(Long userId);

    @Query("SELECT po FROM PurchaseOrder po WHERE po.status = :status")
    List<PurchaseOrder> findByStatus(@Param("status") String status);

    @Query("SELECT po FROM PurchaseOrder po WHERE po.orderDate BETWEEN :start AND :end")
    List<PurchaseOrder> findByOrderDateRange(@Param("start") LocalDate start,
                                             @Param("end") LocalDate end);

    @Query("SELECT po FROM PurchaseOrder po WHERE po.totalAmount >= :threshold")
    List<PurchaseOrder> findRequiringApproval(@Param("threshold") BigDecimal threshold);

    @Query("SELECT COUNT(po) FROM PurchaseOrder po WHERE po.vendor.id = :vendorId AND po.status = 'APPROVED'")
    long countApprovedForVendor(@Param("vendorId") Long vendorId);
}


