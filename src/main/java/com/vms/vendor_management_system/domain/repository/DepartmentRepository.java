package com.vms.vendor_management_system.domain.repository;

import com.vms.vendor_management_system.domain.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for {@link Department} aggregates.
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Optional<Department> findByCode(String code);

    Optional<Department> findByName(String name);

    List<Department> findByIsActiveTrue();

    @Query("SELECT COUNT(d) FROM Department d WHERE d.isActive = true")
    long countActiveDepartments();

    @Query("SELECT COUNT(d) FROM Department d WHERE d.code = :code AND d.isActive = true")
    long countActiveDepartmentsByCode(@Param("code") String code);
}


