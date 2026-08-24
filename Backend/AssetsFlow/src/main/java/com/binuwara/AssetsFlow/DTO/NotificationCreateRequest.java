package com.binuwara.AssetsFlow.DTO;

import com.binuwara.AssetsFlow.Entity.NotificationPriority;

import java.util.UUID;

public record NotificationCreateRequest(UUID recipientUserId, String notificationType, NotificationPriority priority, String title, String message) {
}
