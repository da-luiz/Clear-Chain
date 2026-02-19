package com.vms.vendor_management_system.application.service;

import com.vms.vendor_management_system.application.dto.user.CreateUserRequest;
import com.vms.vendor_management_system.application.dto.user.UserResponse;
import com.vms.vendor_management_system.application.mapper.UserMapper;
import com.vms.vendor_management_system.domain.entity.User;
import com.vms.vendor_management_system.domain.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;

/**
 * Service for system bootstrap operations.
 * Handles first-time setup when no users exist.
 */
@Service
@Transactional
public class BootstrapService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public BootstrapService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Check if system has been initialized (has at least one user).
     */
    @Transactional(readOnly = true)
    public boolean isSystemInitialized() {
        // JpaRepository provides count() method
        long userCount = userRepository.count();
        return userCount > 0;
    }

    /**
     * Create the first admin user.
     * Only works if no users exist in the system.
     */
    public UserResponse createFirstAdmin(CreateUserRequest request) {
        // Double-check that system is not initialized
        if (isSystemInitialized()) {
            throw new ResponseStatusException(FORBIDDEN, "System is already initialized. Cannot create bootstrap admin.");
        }

        // Validate password is provided
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "Password is required for admin user.");
        }

        // Check if username already exists (shouldn't happen, but safety check)
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(BAD_REQUEST, "Username already exists.");
        }

        // Check if email already exists
        if (userRepository.existsByEmailValue(request.getEmail())) {
            throw new ResponseStatusException(BAD_REQUEST, "Email already exists.");
        }

        // Create admin user
        User admin = new User(
                request.getUsername(),
                request.getFirstName(),
                request.getLastName(),
                new com.vms.vendor_management_system.domain.valueobjects.Email(request.getEmail()),
                request.getRole(),
                null // Admin doesn't need a department
        );

        // Hash and set password
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        admin.setPassword(hashedPassword);
        admin.setIsActive(true);

        User saved = userRepository.save(admin);
        return UserMapper.toResponse(saved);
    }
}

