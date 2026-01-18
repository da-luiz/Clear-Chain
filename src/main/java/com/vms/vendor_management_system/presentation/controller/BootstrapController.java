package com.vms.vendor_management_system.presentation.controller;

import com.vms.vendor_management_system.application.dto.user.CreateUserRequest;
import com.vms.vendor_management_system.application.dto.user.UserResponse;
import com.vms.vendor_management_system.application.service.BootstrapService;
import com.vms.vendor_management_system.domain.enums.UserRole;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for system bootstrap (first-time setup).
 * Allows creating the first admin user when no users exist.
 */
@RestController
@RequestMapping("/api/bootstrap")
@CrossOrigin(origins = "*")
@Validated
public class BootstrapController {

    private final BootstrapService bootstrapService;

    public BootstrapController(BootstrapService bootstrapService) {
        this.bootstrapService = bootstrapService;
    }

    /**
     * Check if system is initialized (has at least one user).
     */
    @GetMapping("/status")
    public ResponseEntity<BootstrapStatusResponse> getBootstrapStatus() {
        boolean isInitialized = bootstrapService.isSystemInitialized();
        return ResponseEntity.ok(new BootstrapStatusResponse(isInitialized));
    }

    /**
     * Create the first admin user (only works if no users exist).
     */
    @PostMapping("/create-admin")
    public ResponseEntity<UserResponse> createFirstAdmin(@Valid @RequestBody CreateFirstAdminRequest request) {
        if (bootstrapService.isSystemInitialized()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .build();
        }

        // Create admin user request
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername(request.getUsername());
        createUserRequest.setFirstName(request.getFirstName());
        createUserRequest.setLastName(request.getLastName());
        createUserRequest.setEmail(request.getEmail());
        createUserRequest.setPassword(request.getPassword());
        createUserRequest.setRole(UserRole.ADMIN);
        createUserRequest.setDepartmentId(null); // Admin doesn't need a department

        UserResponse response = bootstrapService.createFirstAdmin(createUserRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Request DTO for creating first admin.
     */
    public static class CreateFirstAdminRequest {
        private String username;
        private String firstName;
        private String lastName;
        private String email;
        private String password;

        // Getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    /**
     * Response DTO for bootstrap status.
     */
    public static class BootstrapStatusResponse {
        private boolean initialized;

        public BootstrapStatusResponse(boolean initialized) {
            this.initialized = initialized;
        }

        public boolean isInitialized() { return initialized; }
        public void setInitialized(boolean initialized) { this.initialized = initialized; }
    }
}




