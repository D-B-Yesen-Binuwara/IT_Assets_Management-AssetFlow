package com.binuwara.AssetsFlow.DTO;

public record SettingsRequest(
        String organizationName,
        String industry,
        String primaryContact,
        String currency,
        String timezone,
        String branding,
        String notificationSettings
) {
}
