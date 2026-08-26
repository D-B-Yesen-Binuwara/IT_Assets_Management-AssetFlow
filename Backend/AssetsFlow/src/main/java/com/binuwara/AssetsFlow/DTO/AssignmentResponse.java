package com.binuwara.AssetsFlow.DTO;

import com.binuwara.AssetsFlow.Entity.AssignmentStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AssignmentResponse(
        UUID id,
        UUID assetId,
        String assetTag,
        String assetName,
        UUID categoryId,
        String category,
        UUID employeeId,
        String employeeName,
        String employeeNumber,
        UUID locationId,
        String locationName,
        Instant assignedAt,
        LocalDate assignedDate,
        LocalDate expectedReturnDate,
        Instant returnedAt,
        LocalDate closingDate,
        AssignmentStatus status,
        String closingReason,
        String handoverNotes,
        Instant createdAt
) {
}
