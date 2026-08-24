package com.binuwara.AssetsFlow.DTO;

import java.time.Instant;
import java.util.UUID;

public record CategoryResponse(UUID id, String name, String description, boolean active, long assetCount, Instant createdAt, Instant updatedAt) {
}
