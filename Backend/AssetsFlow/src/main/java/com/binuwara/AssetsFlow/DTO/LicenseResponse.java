package com.binuwara.AssetsFlow.DTO;

import com.binuwara.AssetsFlow.Entity.LicenseStatus;
import com.binuwara.AssetsFlow.Entity.LicenseType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record LicenseResponse(
        UUID id,
        String softwareName,
        String software,
        UUID vendorId,
        String vendor,
        LicenseType licenseType,
        LicenseType type,
        Integer seatCount,
        Integer seats,
        Integer usedSeats,
        String licenseKey,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal purchaseCost,
        LicenseStatus status,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {
}
