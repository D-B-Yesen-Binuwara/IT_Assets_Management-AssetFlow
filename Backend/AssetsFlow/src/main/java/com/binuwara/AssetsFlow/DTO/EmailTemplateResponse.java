package com.binuwara.AssetsFlow.DTO;

import java.time.Instant;
import java.util.UUID;

public record EmailTemplateResponse(UUID id, String templateKey, String subjectTemplate, String bodyTemplate, boolean active, Instant createdAt, Instant updatedAt) {
}
