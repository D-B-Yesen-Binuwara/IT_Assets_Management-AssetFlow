package com.binuwara.AssetsFlow.DTO;

import com.binuwara.AssetsFlow.Entity.AppUser;
import com.binuwara.AssetsFlow.Entity.Role;
import com.binuwara.AssetsFlow.Entity.UserStatus;

import java.util.Comparator;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record UserResponse(
        UUID id,
        UUID employeeId,
        String employeeNumber,
        String email,
        String firstName,
        String lastName,
        String phone,
        String address,
        String departmentCode,
        String departmentName,
        String username,
        UserStatus status,
        Set<String> roles
) {
    public static UserResponse from(AppUser user) {
        var employee = user.getEmployee();
        var department = employee == null ? null : employee.getDepartment();

        Set<String> roleNames = user.getRoles() == null
                ? Set.of()
                : user.getRoles().stream()
                .map(Role::getName)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toUnmodifiableSet());

        return new UserResponse(
                user.getId(),
                employee == null ? null : employee.getId(),
                employee == null ? null : employee.getEmployeeNumber(),
                employee == null ? null : employee.getEmail(),
                employee == null ? null : employee.getFirstName(),
                employee == null ? null : employee.getLastName(),
                employee == null ? null : employee.getPhone(),
                employee == null ? null : employee.getAddress(),
                department == null ? null : department.getCode(),
                department == null ? null : department.getName(),
                user.getUsername(),
                user.getStatus(),
                roleNames
        );
    }
}
