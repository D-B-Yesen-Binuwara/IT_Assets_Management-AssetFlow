package com.binuwara.AssetsFlow.DTO;

import com.binuwara.AssetsFlow.Entity.MaintenancePriority;
import com.binuwara.AssetsFlow.Entity.MaintenanceStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record MaintenanceResponse(
        UUID id,
        String ticketNumber,
        UUID assetId,
        String assetTag,
        String assetName,
        String issue,
        String description,
        MaintenancePriority priority,
        MaintenanceStatus status,
        UUID requestedByEmployeeId,
        String requestedByEmployee,
        UUID assignedToEmployeeId,
        String assignedToEmployee,
        UUID vendorId,
        String vendor,
        Instant openedAt,
        LocalDate dueDate,
        Instant startedAt,
        Instant completedAt,
        BigDecimal cost,
        String resolution,
        Instant createdAt,
        Instant updatedAt
) {
}
