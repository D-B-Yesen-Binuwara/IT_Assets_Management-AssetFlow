package com.binuwara.AssetsFlow.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record LicenseRequest(
        String softwareName,
        String software,
        UUID vendorId,
        String vendor,
        String licenseType,
        String type,
        String licenseKey,
        Integer seatCount,
        Integer seats,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal purchaseCost,
        String status,
        String notes
) {
}
