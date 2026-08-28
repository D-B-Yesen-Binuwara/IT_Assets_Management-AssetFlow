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
        String brand,
        String modelNo,
        UUID vendorId,
        String vendor,
        UUID purchaseOrderItemId,
        LocalDate purchaseDate,
        BigDecimal purchaseCost,
        Integer warrantyPeriodMonths,
        UUID warrantyVendorId,
        String warrantyProvider,
        String warrantyPolicyNumber,
        LocalDate warrantyStartDate,
        LocalDate warrantyEndDate,
        String warrantyCoverage,
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
