package com.binuwara.AssetsFlow.DTO;

import com.binuwara.AssetsFlow.Entity.AssetCondition;
import com.binuwara.AssetsFlow.Entity.AssetStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AssetResponse(
        UUID id,
        String assetTag,
        String name,
        UUID categoryId,
        String category,
        String serialNumber,
        String brand,
        String modelNo,
        UUID vendorId,
        String vendor,
        UUID locationId,
        String location,
        UUID departmentId,
        String department,
        AssetStatus status,
        AssetCondition condition,
        AssetCondition assetCondition,
        LocalDate purchaseDate,
        BigDecimal purchaseCost,
        Integer warrantyPeriodMonths,
        UUID warrantyId,
        UUID warrantyVendorId,
        String warrantyProvider,
        String warrantyPolicyNumber,
        LocalDate warrantyStartDate,
        LocalDate warrantyEndDate,
        String warrantyCoverage,
        String currency,
        LocalDate retirementDate,
        String disposalNotes,
        String notes,
        UUID assignedToId,
        String assignedTo,
        Instant createdAt,
        Instant updatedAt
) {
}
