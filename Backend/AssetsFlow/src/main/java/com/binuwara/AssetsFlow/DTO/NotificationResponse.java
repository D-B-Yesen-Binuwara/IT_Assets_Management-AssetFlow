package com.binuwara.AssetsFlow.DTO;

import com.binuwara.AssetsFlow.Entity.NotificationPriority;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        String type,
        String notificationType,
        NotificationPriority priority,
        String title,
        String message,
        Instant date,
        Instant createdAt,
        Instant readAt,
        boolean read
) {
}
