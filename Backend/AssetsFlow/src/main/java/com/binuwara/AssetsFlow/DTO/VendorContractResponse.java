package com.binuwara.AssetsFlow.DTO;

import com.binuwara.AssetsFlow.Entity.VendorContractStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record VendorContractResponse(
        UUID id,
        UUID vendorId,
        String vendor,
        String contractNumber,
        String title,
        LocalDate startDate,
        LocalDate endDate,
        VendorContractStatus status,
        BigDecimal contractValue,
        String currency,
        String documentReference,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {
}
