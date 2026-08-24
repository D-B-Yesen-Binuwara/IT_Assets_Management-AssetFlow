package com.binuwara.AssetsFlow.DTO;

import java.time.Instant;

public record SettingsResponse(
        String organizationName,
        String industry,
        String primaryContact,
        String currency,
        String timezone,
        String branding,
        String notificationSettings,
        Instant createdAt,
        Instant updatedAt
) {
}
