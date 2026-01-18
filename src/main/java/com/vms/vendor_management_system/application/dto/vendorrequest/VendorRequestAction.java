package com.vms.vendor_management_system.application.dto.vendorrequest;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Generic payload for reviewer actions (approve/reject/return/cancel).
 */
@Getter
@Setter
public class VendorRequestAction {

    @NotNull
    private Long reviewerId;

    @Size(max = 4000)
    private String comment;
}

