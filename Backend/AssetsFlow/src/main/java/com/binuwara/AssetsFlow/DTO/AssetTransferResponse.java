package com.binuwara.AssetsFlow.DTO;

import com.binuwara.AssetsFlow.Entity.AssetTransferStatus;

import java.time.Instant;
import java.util.UUID;

public record AssetTransferResponse(
        UUID id,
        UUID assetId,
        String assetTag,
        UUID previousAssignmentId,
        UUID newAssignmentId,
        UUID previousEmployeeId,
        String previousEmployeeName,
        UUID newEmployeeId,
        String newEmployeeName,
        UUID previousLocationId,
        String previousLocation,
        UUID newLocationId,
        String newLocation,
        UUID requestedByUserId,
        AssetTransferStatus status,
        String reason,
        Instant requestedAt,
        Instant approvedAt,
        Instant transferredAt,
        String notes
) {
}
