package com.binuwara.AssetsFlow.DTO;

import com.binuwara.AssetsFlow.Entity.EmployeeStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record EmployeeResponse(
        UUID id,
        String employeeNumber,
        String firstName,
        String lastName,
        String name,
        String email,
        String phone,
        String address,
        String jobTitle,
        UUID branchId,
        String branch,
        UUID departmentId,
        String department,
        EmployeeStatus status,
        LocalDate hireDate,
        LocalDate terminationDate,
        boolean hasAccount,
        Instant createdAt,
        Instant updatedAt
) {
}
