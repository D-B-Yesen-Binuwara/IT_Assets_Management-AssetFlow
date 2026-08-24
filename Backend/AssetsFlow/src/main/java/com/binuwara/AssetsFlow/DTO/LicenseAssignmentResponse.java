package com.binuwara.AssetsFlow.DTO;

import com.binuwara.AssetsFlow.Entity.LicenseAssignmentStatus;

import java.time.Instant;
import java.util.UUID;

public record LicenseAssignmentResponse(
        UUID id,
        UUID licenseId,
        String software,
        UUID employeeId,
        String employeeName,
        String employeeNumber,
        Instant assignedAt,
        Instant revokedAt,
        LicenseAssignmentStatus status
) {
}
