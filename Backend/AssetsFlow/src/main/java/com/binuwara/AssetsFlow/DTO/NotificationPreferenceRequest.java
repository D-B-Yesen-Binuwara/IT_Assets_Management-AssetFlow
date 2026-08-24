package com.binuwara.AssetsFlow.DTO;

public record NotificationPreferenceRequest(String notificationType, Boolean inAppEnabled, Boolean emailEnabled) {
}
