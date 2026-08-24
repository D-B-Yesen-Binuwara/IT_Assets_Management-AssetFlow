package com.binuwara.AssetsFlow.DTO;

import com.binuwara.AssetsFlow.Entity.AssetTransferStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AssetTransferRequest(
        UUID newEmployeeId,
        UUID newLocationId,
        String reason,
        LocalDate transferredAt,
        Instant transferredAtInstant,
        String notes,
        AssetTransferStatus status
) {
}
