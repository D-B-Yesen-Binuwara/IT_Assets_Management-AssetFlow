package com.binuwara.AssetsFlow.DTO;

import com.binuwara.AssetsFlow.Entity.WarrantyStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record WarrantyResponse(
        UUID id,
        UUID assetId,
        String assetTag,
        String assetName,
        UUID vendorId,
        String vendor,
        String provider,
        String policyNumber,
        LocalDate startDate,
        LocalDate endDate,
        String coverage,
        WarrantyStatus status,
        boolean current,
        long claimCount,
        Instant createdAt,
        Instant updatedAt
) {
}
