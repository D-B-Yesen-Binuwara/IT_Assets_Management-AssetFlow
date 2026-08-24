package com.binuwara.AssetsFlow.DTO;

import com.binuwara.AssetsFlow.Entity.WarrantyClaimStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record WarrantyClaimRequest(
        UUID warrantyId,
        String claimNumber,
        String description,
        BigDecimal claimedAmount,
        String providerReference,
        WarrantyClaimStatus status,
        UUID assignedToUserId,
        String resolutionNotes
) {
}
