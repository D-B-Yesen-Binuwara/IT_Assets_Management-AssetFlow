package com.binuwara.AssetsFlow.DTO;

import java.time.Instant;
import java.util.UUID;

public record NotificationPreferenceResponse(UUID id, String notificationType, boolean inAppEnabled, boolean emailEnabled, Instant createdAt, Instant updatedAt) {
}
