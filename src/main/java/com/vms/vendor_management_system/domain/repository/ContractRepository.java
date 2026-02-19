package com.vms.vendor_management_system.domain.repository;

import com.vms.vendor_management_system.domain.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for {@link Contract}.
 */
@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {

    Optional<Contract> findByContractNumber(String contractNumber);

    List<Contract> findByVendorId(Long vendorId);

    @Query("SELECT c FROM Contract c WHERE c.status = :status")
    List<Contract> findByStatus(@Param("status") String status);

    @Query("SELECT c FROM Contract c WHERE c.endDate BETWEEN :start AND :end")
    List<Contract> findContractsExpiringBetween(@Param("start") LocalDate start,
                                                @Param("end") LocalDate end);

    @Query("SELECT COUNT(c) FROM Contract c WHERE c.vendor.id = :vendorId AND c.status = 'ACTIVE'")
    long countActiveContractsForVendor(@Param("vendorId") Long vendorId);

    @Query("SELECT COUNT(c) FROM Contract c WHERE c.status = :status")
    long countByStatus(@Param("status") String status);
}


