package com.vms.vendor_management_system.presentation.controller;

import com.vms.vendor_management_system.application.dto.department.CreateDepartmentRequest;
import com.vms.vendor_management_system.application.dto.department.DepartmentResponse;
import com.vms.vendor_management_system.application.service.DepartmentApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for managing departments.
 */
@RestController
@RequestMapping("/api/departments")
@Validated
public class DepartmentController {

    private final DepartmentApplicationService departmentApplicationService;

    public DepartmentController(DepartmentApplicationService departmentApplicationService) {
        this.departmentApplicationService = departmentApplicationService;
    }

    @GetMapping
    public List<DepartmentResponse> getAllDepartments() {
        return departmentApplicationService.getAllDepartments();
    }

    @GetMapping("/active")
    public List<DepartmentResponse> getActiveDepartments() {
        return departmentApplicationService.getActiveDepartments();
    }

    @GetMapping("/{id}")
    public DepartmentResponse getDepartment(@PathVariable Long id) {
        return departmentApplicationService.getDepartment(id);
    }

    @PostMapping
    public ResponseEntity<DepartmentResponse> createDepartment(@Valid @RequestBody CreateDepartmentRequest request) {
        DepartmentResponse response = departmentApplicationService.createDepartment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public DepartmentResponse updateDepartment(@PathVariable Long id, @Valid @RequestBody CreateDepartmentRequest request) {
        return departmentApplicationService.updateDepartment(id, request);
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateDepartment(@PathVariable Long id) {
        departmentApplicationService.deactivateDepartment(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<Void> activateDepartment(@PathVariable Long id) {
        departmentApplicationService.activateDepartment(id);
        return ResponseEntity.noContent().build();
    }
}






