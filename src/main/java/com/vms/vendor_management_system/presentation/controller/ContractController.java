package com.vms.vendor_management_system.presentation.controller;

import com.vms.vendor_management_system.application.dto.contract.ContractResponse;
import com.vms.vendor_management_system.application.dto.contract.CreateContractRequest;
import com.vms.vendor_management_system.application.service.ContractApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for managing contracts.
 */
@RestController
@RequestMapping("/api/contracts")
@Validated
public class ContractController {

    private final ContractApplicationService contractApplicationService;

    public ContractController(ContractApplicationService contractApplicationService) {
        this.contractApplicationService = contractApplicationService;
    }

    @GetMapping("/{id}")
    public ContractResponse getContract(@PathVariable Long id) {
        return contractApplicationService.getContract(id);
    }

    @GetMapping("/vendor/{vendorId}")
    public List<ContractResponse> getContractsForVendor(@PathVariable Long vendorId) {
        return contractApplicationService.getContractsForVendor(vendorId);
    }

    @PostMapping
    public ResponseEntity<ContractResponse> createContract(@Valid @RequestBody CreateContractRequest request) {
        ContractResponse response = contractApplicationService.createContract(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/approve")
    public ContractResponse approveContract(@PathVariable Long id, @RequestParam Long approverId) {
        return contractApplicationService.approveContract(id, approverId);
    }

    @PostMapping("/{id}/terminate")
    public ResponseEntity<Void> terminateContract(@PathVariable Long id) {
        contractApplicationService.terminateContract(id);
        return ResponseEntity.noContent().build();
    }
}

