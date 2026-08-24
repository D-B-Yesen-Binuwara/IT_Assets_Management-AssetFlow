package com.binuwara.AssetsFlow.DTO;

import java.time.LocalDate;
import java.util.UUID;

public record WarrantyRequest(
        UUID assetId,
        UUID vendorId,
        String provider,
        String policyNumber,
        LocalDate startDate,
        LocalDate endDate,
        String coverage,
        String status,
        Boolean current
) {
}
