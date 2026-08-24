package com.binuwara.AssetsFlow.DTO;

import com.binuwara.AssetsFlow.Entity.AssetDisposalStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AssetDisposalResponse(
        UUID id,
        UUID assetId,
        String assetTag,
        LocalDate disposalDate,
        String disposalMethod,
        String reason,
        AssetDisposalStatus status,
        BigDecimal proceeds,
        String currency,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {
}
