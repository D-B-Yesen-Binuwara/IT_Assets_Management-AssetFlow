package com.binuwara.AssetsFlow.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record AssetRequest(
        String assetTag,
        String name,
        UUID categoryId,
        String category,
        String serialNumber,
        String manufacturer,
        String model,
        UUID vendorId,
        String vendor,
        UUID purchaseOrderItemId,
        LocalDate purchaseDate,
        BigDecimal purchaseCost,
        String currency,
        UUID locationId,
        String location,
        UUID departmentId,
        String department,
        String status,
        String condition,
        String assetCondition,
        LocalDate retirementDate,
        String disposalNotes,
        String notes
) {
}
