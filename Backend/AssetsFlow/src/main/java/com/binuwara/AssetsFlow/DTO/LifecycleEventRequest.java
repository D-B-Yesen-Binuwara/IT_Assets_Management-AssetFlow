package com.binuwara.AssetsFlow.DTO;

import java.time.Instant;
import java.util.UUID;

public record LifecycleEventRequest(String eventType, Instant eventAt, UUID assignmentId, String notes, String metadata) {
}
