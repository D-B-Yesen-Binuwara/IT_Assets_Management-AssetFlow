package com.binuwara.AssetsFlow.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record AssetStatusChangeRequest(
        String status,
        String reason,
        String description,
        String priority,
        LocalDate effectiveDate,
        LocalDate startDate,
        LocalDate dueDate,
        UUID vendorId,
        UUID assignedToEmployeeId,
        LocalDate disposalDate,
        String disposalMethod,
        BigDecimal proceeds,
        String notes
) {
}
