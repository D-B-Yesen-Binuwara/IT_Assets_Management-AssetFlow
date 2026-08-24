package com.binuwara.AssetsFlow.DTO;

import java.util.UUID;

public record DepartmentRequest(String code, String name, String description, UUID managerEmployeeId, String managerEmployeeNumber) {
}
