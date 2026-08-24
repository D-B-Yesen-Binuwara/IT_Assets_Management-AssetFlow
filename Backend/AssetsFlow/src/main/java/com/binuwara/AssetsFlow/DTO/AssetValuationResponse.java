package com.binuwara.AssetsFlow.DTO;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AssetValuationResponse(
        UUID id,
        UUID assetId,
        LocalDate valuationDate,
        BigDecimal bookValue,
        BigDecimal marketValue,
        BigDecimal depreciationAmount,
        String valuationMethod,
        String currency,
        UUID createdByUserId,
        String notes,
        Instant createdAt
) {
}
