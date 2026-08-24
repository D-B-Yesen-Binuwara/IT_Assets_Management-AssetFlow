package com.binuwara.AssetsFlow.DTO;

import com.binuwara.AssetsFlow.Entity.LocationType;

import java.time.Instant;
import java.util.UUID;

public record LocationResponse(
        UUID id,
        String code,
        String name,
        LocationType locationType,
        LocationType type,
        UUID parentLocationId,
        String parentLocation,
        String addressLine,
        String address,
        String city,
        String country,
        String branch,
        long assetCount,
        Instant createdAt,
        Instant updatedAt
) {
}
