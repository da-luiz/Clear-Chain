package com.vms.vendor_management_system.presentation.controller;

import com.vms.vendor_management_system.application.dto.dashboard.DashboardSummary;
import com.vms.vendor_management_system.application.service.DashboardApplicationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Dashboard-related endpoints.
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardApplicationService dashboardApplicationService;

    public DashboardController(DashboardApplicationService dashboardApplicationService) {
        this.dashboardApplicationService = dashboardApplicationService;
    }

    @GetMapping
    public DashboardSummary getSummary() {
        return dashboardApplicationService.getSummary();
    }
}

