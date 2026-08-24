package com.binuwara.AssetsFlow.DTO;

import java.time.Instant;
import java.util.UUID;

public record DepartmentResponse(UUID id, String code, String name, String description, UUID managerId, String managerName, long employeeCount, long assetCount, Instant createdAt, Instant updatedAt) {
}
