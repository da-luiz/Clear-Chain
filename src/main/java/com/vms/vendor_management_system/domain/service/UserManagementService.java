package com.vms.vendor_management_system.domain.service;

import com.vms.vendor_management_system.domain.entity.Department;
import com.vms.vendor_management_system.domain.entity.User;
import com.vms.vendor_management_system.domain.enums.UserRole;
import com.vms.vendor_management_system.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Domain service for user management business logic
 */
@Service
@Transactional
public class UserManagementService {
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Creates a new user
     */
    public User createUser(String username, String firstName, String lastName, String email, UserRole role, Department department) {
        // Check if username already exists
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }
        
        // Check if email already exists
        if (userRepository.existsByEmailValue(email)) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }
        
        User user = new User(username, firstName, lastName, new com.vms.vendor_management_system.domain.valueobjects.Email(email), role, department);
        return userRepository.save(user);
    }
    
    /**
     * Updates user information
     */
    public User updateUser(Long userId, String firstName, String lastName, String email, UserRole role, Department department) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        
        // Check if email is being changed and if new email already exists
        if (!user.getEmail().getValue().equals(email) && userRepository.existsByEmailValue(email)) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }
        
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(new com.vms.vendor_management_system.domain.valueobjects.Email(email));
        user.setRole(role);
        user.setDepartment(department);
        
        return userRepository.save(user);
    }
    
    /**
     * Deactivates a user
     */
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        
        user.deactivate();
        userRepository.save(user);
    }
    
    /**
     * Activates a user
     */
    public void activateUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        
        user.activate();
        userRepository.save(user);
    }
    
    /**
     * Gets all active users
     */
    public List<User> getActiveUsers() {
        return userRepository.findByIsActiveTrue();
    }
    
    /**
     * Gets users by role
     */
    public List<User> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role);
    }
    
    /**
     * Gets users by department
     */
    public List<User> getUsersByDepartment(Long departmentId) {
        return userRepository.findByDepartmentId(departmentId);
    }
    
    /**
     * Gets users who can approve vendors
     */
    public List<User> getVendorApprovers() {
        return userRepository.findActiveUsersByRoles(List.of(
            UserRole.ADMIN, 
            UserRole.FINANCE_APPROVER, 
            UserRole.COMPLIANCE_APPROVER
        ));
    }
    
    /**
     * Gets user by username
     */
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    /**
     * Gets user by email
     */
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmailValue(email);
    }
    
    /**
     * Gets total count of active users
     */
    public long getActiveUserCount() {
        return userRepository.countActiveUsers();
    }
    
    /**
     * Gets count of active users by role
     */
    public long getActiveUserCountByRole(UserRole role) {
        return userRepository.countActiveUsersByRole(role);
    }
    
    /**
     * Updates user's last login time
     */
    public void updateLastLogin(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.updateLastLogin();
            userRepository.save(user);
        }
    }
}
