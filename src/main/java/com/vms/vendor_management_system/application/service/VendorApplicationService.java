package com.vms.vendor_management_system.application.service;

import com.vms.vendor_management_system.application.dto.vendor.CreateVendorRequest;
import com.vms.vendor_management_system.application.dto.vendor.VendorResponse;
import com.vms.vendor_management_system.application.mapper.VendorMapper;
import com.vms.vendor_management_system.domain.entity.Vendor;
import com.vms.vendor_management_system.domain.entity.VendorCategory;
import com.vms.vendor_management_system.domain.enums.VendorStatus;
import com.vms.vendor_management_system.domain.repository.VendorCategoryRepository;
import com.vms.vendor_management_system.domain.repository.VendorRepository;
import com.vms.vendor_management_system.domain.service.VendorManagementService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Application service encapsulating vendor-related use cases.
 */
@Service
@Transactional
public class VendorApplicationService {

    private final VendorRepository vendorRepository;
    private final VendorCategoryRepository vendorCategoryRepository;
    private final VendorManagementService vendorManagementService;

    public VendorApplicationService(VendorRepository vendorRepository,
                                    VendorCategoryRepository vendorCategoryRepository,
                                    VendorManagementService vendorManagementService) {
        this.vendorRepository = vendorRepository;
        this.vendorCategoryRepository = vendorCategoryRepository;
        this.vendorManagementService = vendorManagementService;
    }

    @Transactional(readOnly = true)
    public List<VendorResponse> getRecentVendors(int limit) {
        return vendorRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit))
                .stream()
                .map(VendorMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VendorResponse getVendor(Long vendorId) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Vendor not found"));
        return VendorMapper.toResponse(vendor);
    }

    public VendorResponse createVendor(CreateVendorRequest request) {
        VendorCategory category = resolveCategory(request.getCategoryId());
        Vendor vendor = VendorMapper.toEntity(request, category);
        vendor.setVendorCode(generateVendorCode(request.getCompanyName()));
        Vendor saved = vendorRepository.save(vendor);
        return VendorMapper.toResponse(saved);
    }

    public VendorResponse updateVendor(Long vendorId, CreateVendorRequest request) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Vendor not found"));
        VendorCategory category = resolveCategory(request.getCategoryId());
        VendorMapper.updateEntity(vendor, request, category);
        Vendor saved = vendorRepository.save(vendor);
        return VendorMapper.toResponse(saved);
    }

    public void activateVendor(Long vendorId) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Vendor not found"));
        // Allow activation from PENDING_CREATION (direct creation) or APPROVED (from approval workflow)
        if (VendorStatus.PENDING_CREATION.equals(vendor.getStatus())) {
            vendor.activate(); // Direct activation for directly created vendors
        } else {
            vendorManagementService.activateVendor(vendor); // Uses business logic for approved vendors
        }
        vendorRepository.save(vendor);
    }

    public void suspendVendor(Long vendorId) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Vendor not found"));
        vendorManagementService.suspendVendor(vendor);
        vendorRepository.save(vendor);
    }

    public void terminateVendor(Long vendorId) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Vendor not found"));
        vendorManagementService.terminateVendor(vendor);
        vendorRepository.save(vendor);
    }

    public void approveVendorRequest(Long requestId, Long approverId) {
        // This method is deprecated - use VendorCreationRequestApplicationService.approve() instead
        // Keeping for backward compatibility but delegating to the proper service
        // Note: This would require injecting VendorCreationRequestApplicationService, 
        // but to avoid circular dependencies, this method is kept as a no-op
        // The actual approval workflow is handled by VendorCreationRequestApplicationService
    }

    private VendorCategory resolveCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return vendorCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Vendor category not found"));
    }

    private String generateVendorCode(String companyName) {
        String base = companyName == null ? "VENDOR" :
                companyName.replaceAll("[^A-Za-z0-9]", "")
                        .toUpperCase(Locale.ROOT)
                        .substring(0, Math.min(6, companyName.length()));
        String suffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase(Locale.ROOT);
        return base + "-" + suffix;
    }
}

