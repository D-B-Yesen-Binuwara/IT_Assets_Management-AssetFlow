package com.binuwara.AssetsFlow.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AssetValuationRequest(
        LocalDate valuationDate,
        BigDecimal bookValue,
        BigDecimal marketValue,
        BigDecimal depreciationAmount,
        String valuationMethod,
        String currency,
        String notes
) {
}
