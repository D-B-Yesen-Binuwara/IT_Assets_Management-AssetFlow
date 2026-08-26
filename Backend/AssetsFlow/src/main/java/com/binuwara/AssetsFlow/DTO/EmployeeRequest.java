package com.binuwara.AssetsFlow.DTO;

import com.binuwara.AssetsFlow.Entity.EmployeeStatus;

import java.time.LocalDate;
import java.util.UUID;

public record EmployeeRequest(
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
        LocalDate terminationDate
) {
}
