package com.binuwara.AssetsFlow.DTO;

import com.binuwara.AssetsFlow.Entity.AssignmentStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AssignmentRequest(
        UUID assetId,
        UUID employeeId,
        Instant assignedAt,
        LocalDate assignedDate,
        LocalDate expectedReturnDate,
        AssignmentStatus status,
        String handoverNotes
) {
}
