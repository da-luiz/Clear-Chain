package com.vms.vendor_management_system.application.dto.dashboard;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

/**
 * Single data point for procurement activity chart (e.g. count per day).
 */
@Value
@Builder
public class ProcurementActivityPoint {
    LocalDate date;
    long count;
}
