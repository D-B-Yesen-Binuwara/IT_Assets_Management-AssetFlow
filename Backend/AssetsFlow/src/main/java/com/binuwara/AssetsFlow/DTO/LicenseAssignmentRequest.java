package com.binuwara.AssetsFlow.DTO;

import com.binuwara.AssetsFlow.Entity.LicenseAssignmentStatus;

import java.time.Instant;
import java.util.UUID;

public record LicenseAssignmentRequest(
        UUID licenseId,
        UUID employeeId,
        Instant assignedAt,
        LicenseAssignmentStatus status
) {
}
