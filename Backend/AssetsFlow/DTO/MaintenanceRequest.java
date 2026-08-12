package com.binuwara.AssetsFlow.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record MaintenanceRequest(
        UUID assetId,
        String issue,
        String description,
        String priority,
        LocalDate dueDate,
        UUID vendorId,
        UUID requestedByEmployeeId,
        UUID assignedToEmployeeId,
        BigDecimal cost,
        String status,
        String resolution
) {
}
