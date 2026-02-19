package com.vms.vendor_management_system.application.service;

import com.vms.vendor_management_system.application.dto.department.CreateDepartmentRequest;
import com.vms.vendor_management_system.application.dto.department.DepartmentResponse;
import com.vms.vendor_management_system.application.mapper.DepartmentMapper;
import com.vms.vendor_management_system.domain.entity.Department;
import com.vms.vendor_management_system.domain.repository.DepartmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Application service for department management.
 */
@Service
@Transactional
public class DepartmentApplicationService {

    private final DepartmentRepository departmentRepository;

    public DepartmentApplicationService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll()
                .stream()
                .map(DepartmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> getActiveDepartments() {
        return departmentRepository.findByIsActiveTrue()
                .stream()
                .map(DepartmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DepartmentResponse getDepartment(Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Department not found"));
        return DepartmentMapper.toResponse(department);
    }

    public DepartmentResponse createDepartment(CreateDepartmentRequest request) {
        // Check if code already exists
        if (departmentRepository.findByCode(request.getCode()).isPresent()) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "Department code already exists");
        }
        // Check if name already exists
        if (departmentRepository.findByName(request.getName()).isPresent()) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "Department name already exists");
        }
        
        Department department = DepartmentMapper.toEntity(request);
        Department saved = departmentRepository.save(department);
        return DepartmentMapper.toResponse(saved);
    }

    public DepartmentResponse updateDepartment(Long departmentId, CreateDepartmentRequest request) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Department not found"));
        
        // Check if code already exists (excluding current department)
        departmentRepository.findByCode(request.getCode())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(departmentId)) {
                        throw new ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "Department code already exists");
                    }
                });
        // Check if name already exists (excluding current department)
        departmentRepository.findByName(request.getName())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(departmentId)) {
                        throw new ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "Department name already exists");
                    }
                });
        
        DepartmentMapper.updateEntity(department, request);
        Department saved = departmentRepository.save(department);
        return DepartmentMapper.toResponse(saved);
    }

    public void deactivateDepartment(Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Department not found"));
        department.deactivate();
        departmentRepository.save(department);
    }

    public void activateDepartment(Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Department not found"));
        department.activate();
        departmentRepository.save(department);
    }
}






