package com.binuwara.AssetsFlow.DTO;

import com.binuwara.AssetsFlow.Entity.AssetDisposalStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AssetDisposalRequest(
        LocalDate disposalDate,
        String disposalMethod,
        String reason,
        AssetDisposalStatus status,
        BigDecimal proceeds,
        String currency,
        String notes
) {
}
