package com.vms.vendor_management_system.application.service;

import com.vms.vendor_management_system.application.dto.user.CreateUserRequest;
import com.vms.vendor_management_system.application.dto.user.UpdateUserRequest;
import com.vms.vendor_management_system.application.dto.user.UserResponse;
import com.vms.vendor_management_system.application.mapper.UserMapper;
import com.vms.vendor_management_system.domain.entity.Department;
import com.vms.vendor_management_system.domain.entity.User;
import com.vms.vendor_management_system.domain.repository.DepartmentRepository;
import com.vms.vendor_management_system.domain.repository.UserRepository;
import com.vms.vendor_management_system.domain.service.UserManagementService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Application layer orchestrator exposing user use-cases to the presentation layer.
 */
@Service
@Transactional
public class UserApplicationService {

    private final UserManagementService userManagementService;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    public UserApplicationService(UserManagementService userManagementService,
                                  UserRepository userRepository,
                                  DepartmentRepository departmentRepository,
                                  PasswordEncoder passwordEncoder) {
        this.userManagementService = userManagementService;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getActiveUsers() {
        return userManagementService.getActiveUsers()
                .stream()
                .map(UserMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
        return UserMapper.toResponse(user);
    }

    public UserResponse createUser(CreateUserRequest request) {
        Department department = resolveDepartment(request.getDepartmentId());
        User user = userManagementService.createUser(
                request.getUsername(),
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getRole(),
                department
        );
        // Hash and set password only if provided
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            String hashedPassword = passwordEncoder.encode(request.getPassword());
            user.setPassword(hashedPassword);
        }
        // Otherwise, password remains null (to be set later)
        userRepository.save(user);
        return UserMapper.toResponse(user);
    }

    public UserResponse updateUser(Long userId, UpdateUserRequest request) {
        Department department = resolveDepartment(request.getDepartmentId());
        User user = userManagementService.updateUser(
                userId,
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getRole(),
                department
        );
        return UserMapper.toResponse(user);
    }

    public void deactivateUser(Long userId) {
        userManagementService.deactivateUser(userId);
    }

    public void activateUser(Long userId) {
        userManagementService.activateUser(userId);
    }

    private Department resolveDepartment(Long departmentId) {
        if (departmentId == null) {
            return null;
        }
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Department not found"));
    }
}


