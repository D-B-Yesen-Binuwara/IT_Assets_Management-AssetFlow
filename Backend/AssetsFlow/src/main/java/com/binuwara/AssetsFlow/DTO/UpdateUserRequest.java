package com.binuwara.AssetsFlow.DTO;

import com.binuwara.AssetsFlow.Entity.UserStatus;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Size(max = 80) String username,
        @Size(min = 8, max = 128) String password,
        String confirmPassword,
        UserStatus status,
        @Size(max = 50) String role
) {
}
