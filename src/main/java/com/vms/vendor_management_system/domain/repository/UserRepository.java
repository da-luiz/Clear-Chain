package com.vms.vendor_management_system.domain.repository;

import com.vms.vendor_management_system.domain.entity.User;
import com.vms.vendor_management_system.domain.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmailValue(String email);
    
    Optional<User> findByOauthProviderAndOauthId(String oauthProvider, String oauthId);
    
    List<User> findByRole(UserRole role);
    
    List<User> findByDepartmentId(Long departmentId);
    
    List<User> findByIsActiveTrue();
    
    List<User> findByIsActiveFalse();
    
    @Query("SELECT u FROM User u WHERE u.role IN :roles AND u.isActive = true")
    List<User> findActiveUsersByRoles(@Param("roles") List<UserRole> roles);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true")
    long countActiveUsers();
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.isActive = true")
    long countActiveUsersByRole(@Param("role") UserRole role);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmailValue(String email);
}
