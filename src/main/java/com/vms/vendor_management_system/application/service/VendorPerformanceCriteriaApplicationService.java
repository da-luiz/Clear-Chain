package com.vms.vendor_management_system.application.service;

import com.vms.vendor_management_system.application.dto.vendorperformancecriteria.CreateVendorPerformanceCriteriaRequest;
import com.vms.vendor_management_system.application.dto.vendorperformancecriteria.VendorPerformanceCriteriaResponse;
import com.vms.vendor_management_system.application.mapper.VendorPerformanceCriteriaMapper;
import com.vms.vendor_management_system.domain.entity.VendorCategory;
import com.vms.vendor_management_system.domain.entity.VendorPerformanceCriteria;
import com.vms.vendor_management_system.domain.repository.VendorCategoryRepository;
import com.vms.vendor_management_system.domain.repository.VendorPerformanceCriteriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Application service for vendor performance criteria management.
 */
@Service
@Transactional
public class VendorPerformanceCriteriaApplicationService {

    private final VendorPerformanceCriteriaRepository criteriaRepository;
    private final VendorCategoryRepository categoryRepository;

    public VendorPerformanceCriteriaApplicationService(VendorPerformanceCriteriaRepository criteriaRepository,
                                                      VendorCategoryRepository categoryRepository) {
        this.criteriaRepository = criteriaRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<VendorPerformanceCriteriaResponse> getAllCriteria() {
        return criteriaRepository.findAll()
                .stream()
                .map(VendorPerformanceCriteriaMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VendorPerformanceCriteriaResponse> getActiveCriteria() {
        return criteriaRepository.findByIsActiveTrue()
                .stream()
                .map(VendorPerformanceCriteriaMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VendorPerformanceCriteriaResponse> getCriteriaByCategory(Long categoryId) {
        return criteriaRepository.findByCategoryId(categoryId)
                .stream()
                .map(VendorPerformanceCriteriaMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VendorPerformanceCriteriaResponse getCriteria(Long criteriaId) {
        VendorPerformanceCriteria criteria = criteriaRepository.findById(criteriaId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Performance criteria not found"));
        return VendorPerformanceCriteriaMapper.toResponse(criteria);
    }

    public VendorPerformanceCriteriaResponse createCriteria(CreateVendorPerformanceCriteriaRequest request) {
        VendorCategory category = resolveCategory(request.getCategoryId());
        VendorPerformanceCriteria criteria = VendorPerformanceCriteriaMapper.toEntity(request, category);
        VendorPerformanceCriteria saved = criteriaRepository.save(criteria);
        return VendorPerformanceCriteriaMapper.toResponse(saved);
    }

    public VendorPerformanceCriteriaResponse updateCriteria(Long criteriaId, CreateVendorPerformanceCriteriaRequest request) {
        VendorPerformanceCriteria criteria = criteriaRepository.findById(criteriaId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Performance criteria not found"));
        VendorCategory category = resolveCategory(request.getCategoryId());
        VendorPerformanceCriteriaMapper.updateEntity(criteria, request, category);
        VendorPerformanceCriteria saved = criteriaRepository.save(criteria);
        return VendorPerformanceCriteriaMapper.toResponse(saved);
    }

    public void deactivateCriteria(Long criteriaId) {
        VendorPerformanceCriteria criteria = criteriaRepository.findById(criteriaId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Performance criteria not found"));
        criteria.deactivate();
        criteriaRepository.save(criteria);
    }

    public void activateCriteria(Long criteriaId) {
        VendorPerformanceCriteria criteria = criteriaRepository.findById(criteriaId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Performance criteria not found"));
        criteria.activate();
        criteriaRepository.save(criteria);
    }

    private VendorCategory resolveCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Vendor category not found"));
    }
}






