package com.vms.vendor_management_system.application.service;

import com.vms.vendor_management_system.application.dto.vendorrating.CreateVendorRatingRequest;
import com.vms.vendor_management_system.application.dto.vendorrating.VendorRatingResponse;
import com.vms.vendor_management_system.application.mapper.VendorRatingMapper;
import com.vms.vendor_management_system.domain.entity.User;
import com.vms.vendor_management_system.domain.entity.Vendor;
import com.vms.vendor_management_system.domain.entity.VendorPerformanceCriteria;
import com.vms.vendor_management_system.domain.entity.VendorRating;
import com.vms.vendor_management_system.domain.repository.UserRepository;
import com.vms.vendor_management_system.domain.repository.VendorPerformanceCriteriaRepository;
import com.vms.vendor_management_system.domain.repository.VendorRatingRepository;
import com.vms.vendor_management_system.domain.repository.VendorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Application service for vendor rating management.
 */
@Service
@Transactional
public class VendorRatingApplicationService {

    private final VendorRatingRepository ratingRepository;
    private final VendorRepository vendorRepository;
    private final VendorPerformanceCriteriaRepository criteriaRepository;
    private final UserRepository userRepository;

    public VendorRatingApplicationService(VendorRatingRepository ratingRepository,
                                         VendorRepository vendorRepository,
                                         VendorPerformanceCriteriaRepository criteriaRepository,
                                         UserRepository userRepository) {
        this.ratingRepository = ratingRepository;
        this.vendorRepository = vendorRepository;
        this.criteriaRepository = criteriaRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<VendorRatingResponse> getRatingsForVendor(Long vendorId) {
        return ratingRepository.findByVendorId(vendorId)
                .stream()
                .map(VendorRatingMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VendorRatingResponse> getRatingsByCriteria(Long criteriaId) {
        return ratingRepository.findByCriteriaId(criteriaId)
                .stream()
                .map(VendorRatingMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VendorRatingResponse getRating(Long ratingId) {
        VendorRating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Vendor rating not found"));
        return VendorRatingMapper.toResponse(rating);
    }

    public VendorRatingResponse createRating(CreateVendorRatingRequest request) {
        Vendor vendor = vendorRepository.findById(request.getVendorId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Vendor not found"));
        VendorPerformanceCriteria criteria = criteriaRepository.findById(request.getCriteriaId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Performance criteria not found"));
        User ratedBy = userRepository.findById(request.getRatedByUserId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
        
        VendorRating rating = VendorRatingMapper.toEntity(request, vendor, criteria, ratedBy);
        VendorRating saved = ratingRepository.save(rating);
        return VendorRatingMapper.toResponse(saved);
    }

    public VendorRatingResponse updateRating(Long ratingId, Integer newScore, String comments, String evidenceUrl) {
        VendorRating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Vendor rating not found"));
        
        if (newScore != null) {
            rating.updateScore(newScore);
        }
        if (comments != null) {
            rating.setComments(comments);
        }
        if (evidenceUrl != null) {
            rating.addEvidence(evidenceUrl);
        }
        
        VendorRating saved = ratingRepository.save(rating);
        return VendorRatingMapper.toResponse(saved);
    }

    public void deleteRating(Long ratingId) {
        VendorRating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Vendor rating not found"));
        ratingRepository.delete(rating);
    }
}






