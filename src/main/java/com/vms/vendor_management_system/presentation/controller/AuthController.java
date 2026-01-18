package com.vms.vendor_management_system.presentation.controller;

import com.vms.vendor_management_system.application.dto.auth.LoginRequest;
import com.vms.vendor_management_system.application.dto.auth.LoginResponse;
import com.vms.vendor_management_system.domain.entity.User;
import com.vms.vendor_management_system.domain.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication endpoints.
 * NO AUTHENTICATION - Any username can log in without password
 * User must exist in database and will keep their role
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        // Find user by username (no password check - all users can log in)
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElse(null);

        if (user == null || !user.getIsActive()) {
            return ResponseEntity.status(401).body("Invalid username or user is inactive.");
        }

        // Return success response with user info (preserves role)
        LoginResponse response = LoginResponse.builder()
                .token("token-for-" + user.getUsername()) // Simple token, not validated
                .username(user.getUsername())
                .role(user.getRole())
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();

        return ResponseEntity.ok(response);
    }
}



