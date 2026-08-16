package com.binuwara.AssetsFlow.DTO;

import java.time.Instant;

public record AuthResponse(
        String message,
        Instant expiresAt,
        UserResponse user
) {
}
