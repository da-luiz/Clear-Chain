package com.vms.vendor_management_system.presentation.controller;

import com.vms.vendor_management_system.application.dto.auth.LoginRequest;
import com.vms.vendor_management_system.application.dto.auth.LoginResponse;
import com.vms.vendor_management_system.application.security.JwtUtil;
import com.vms.vendor_management_system.domain.entity.User;
import com.vms.vendor_management_system.domain.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication endpoints.
 * Login is secured by username + password (BCrypt) and returns a JWT.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        // Find user by username
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElse(null);

        if (user == null || !user.getIsActive()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid username or user is inactive.");
        }

        // Validate password (stored as BCrypt hash during bootstrap/admin creation)
        String storedPasswordHash = user.getPassword();
        if (storedPasswordHash == null || !passwordEncoder.matches(loginRequest.getPassword(), storedPasswordHash)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid username or password.");
        }

        // Return success response with user info and a real JWT token
        String token = jwtUtil.generateToken(user.getUsername());
        LoginResponse response = LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole())
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();

        return ResponseEntity.ok(response);
    }
}



