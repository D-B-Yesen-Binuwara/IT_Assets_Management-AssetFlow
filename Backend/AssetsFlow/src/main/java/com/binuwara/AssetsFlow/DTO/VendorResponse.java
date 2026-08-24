package com.binuwara.AssetsFlow.DTO;

import com.binuwara.AssetsFlow.Entity.VendorStatus;

import java.time.Instant;
import java.util.UUID;

public record VendorResponse(
        UUID id,
        String vendorCode,
        String name,
        String contactName,
        String contact,
        String email,
        String phone,
        String category,
        String address,
        VendorStatus status,
        long assetCount,
        long purchaseOrderCount,
        Instant createdAt,
        Instant updatedAt
) {
}
