package com.binuwara.AssetsFlow.DTO;

import com.binuwara.AssetsFlow.Entity.WarrantyClaimStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WarrantyClaimResponse(
        UUID id,
        UUID warrantyId,
        UUID assetId,
        String assetTag,
        String claimNumber,
        Instant openedAt,
        Instant resolvedAt,
        WarrantyClaimStatus status,
        String description,
        BigDecimal claimedAmount,
        String providerReference,
        UUID assignedToUserId,
        String resolutionNotes,
        Instant updatedAt
) {
}
