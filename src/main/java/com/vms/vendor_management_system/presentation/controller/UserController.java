package com.vms.vendor_management_system.presentation.controller;

import com.vms.vendor_management_system.application.dto.user.CreateUserRequest;
import com.vms.vendor_management_system.application.dto.user.UpdateUserRequest;
import com.vms.vendor_management_system.application.dto.user.UserResponse;
import com.vms.vendor_management_system.application.service.UserApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller exposing user management endpoints.
 */
@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {

    private final UserApplicationService userApplicationService;

    public UserController(UserApplicationService userApplicationService) {
        this.userApplicationService = userApplicationService;
    }

    @GetMapping
    public List<UserResponse> getActiveUsers() {
        return userApplicationService.getActiveUsers();
    }

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Long id) {
        return userApplicationService.getUser(id);
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        // Note: Authorization check should be added here in production
        // For now, we allow user creation but in production you should check:
        // 1. User is authenticated
        // 2. User has ADMIN role
        // This can be done via Spring Security @PreAuthorize or manual check
        UserResponse response = userApplicationService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public UserResponse updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return userApplicationService.updateUser(id, request);
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
        userApplicationService.deactivateUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<Void> activateUser(@PathVariable Long id) {
        userApplicationService.activateUser(id);
        return ResponseEntity.noContent().build();
    }
}


