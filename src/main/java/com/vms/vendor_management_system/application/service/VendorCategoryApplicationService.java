package com.vms.vendor_management_system.application.service;

import com.vms.vendor_management_system.application.dto.vendorcategory.CreateVendorCategoryRequest;
import com.vms.vendor_management_system.application.dto.vendorcategory.VendorCategoryResponse;
import com.vms.vendor_management_system.application.mapper.VendorCategoryMapper;
import com.vms.vendor_management_system.domain.entity.VendorCategory;
import com.vms.vendor_management_system.domain.repository.VendorCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Application service for vendor category management.
 */
@Service
@Transactional
public class VendorCategoryApplicationService {

    private final VendorCategoryRepository vendorCategoryRepository;

    public VendorCategoryApplicationService(VendorCategoryRepository vendorCategoryRepository) {
        this.vendorCategoryRepository = vendorCategoryRepository;
    }

    @Transactional(readOnly = true)
    public List<VendorCategoryResponse> getAllCategories() {
        return vendorCategoryRepository.findAll()
                .stream()
                .map(VendorCategoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VendorCategoryResponse> getActiveCategories() {
        return vendorCategoryRepository.findByIsActiveTrue()
                .stream()
                .map(VendorCategoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VendorCategoryResponse getCategory(Long categoryId) {
        VendorCategory category = vendorCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Vendor category not found"));
        return VendorCategoryMapper.toResponse(category);
    }

    public VendorCategoryResponse createCategory(CreateVendorCategoryRequest request) {
        // Check if code already exists
        if (vendorCategoryRepository.findByCode(request.getCode()).isPresent()) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "Category code already exists");
        }
        // Check if name already exists
        if (vendorCategoryRepository.findByName(request.getName()).isPresent()) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "Category name already exists");
        }
        
        VendorCategory category = VendorCategoryMapper.toEntity(request);
        VendorCategory saved = vendorCategoryRepository.save(category);
        return VendorCategoryMapper.toResponse(saved);
    }

    public VendorCategoryResponse updateCategory(Long categoryId, CreateVendorCategoryRequest request) {
        VendorCategory category = vendorCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Vendor category not found"));
        
        // Check if code already exists (excluding current category)
        vendorCategoryRepository.findByCode(request.getCode())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(categoryId)) {
                        throw new ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "Category code already exists");
                    }
                });
        // Check if name already exists (excluding current category)
        vendorCategoryRepository.findByName(request.getName())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(categoryId)) {
                        throw new ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "Category name already exists");
                    }
                });
        
        VendorCategoryMapper.updateEntity(category, request);
        VendorCategory saved = vendorCategoryRepository.save(category);
        return VendorCategoryMapper.toResponse(saved);
    }

    public void deactivateCategory(Long categoryId) {
        VendorCategory category = vendorCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Vendor category not found"));
        category.deactivate();
        vendorCategoryRepository.save(category);
    }

    public void activateCategory(Long categoryId) {
        VendorCategory category = vendorCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Vendor category not found"));
        category.activate();
        vendorCategoryRepository.save(category);
    }
}






