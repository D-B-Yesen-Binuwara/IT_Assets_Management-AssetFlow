package com.binuwara.AssetsFlow.DTO;

import java.time.Instant;
import java.util.UUID;

public record LifecycleEventResponse(
        UUID id,
        UUID assetId,
        String eventType,
        Instant eventAt,
        UUID actorUserId,
        String fromStatus,
        String toStatus,
        UUID fromLocationId,
        UUID toLocationId,
        UUID assignmentId,
        String notes,
        String metadata,
        Instant createdAt
) {
}
