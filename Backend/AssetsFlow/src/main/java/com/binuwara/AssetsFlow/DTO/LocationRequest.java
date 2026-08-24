package com.binuwara.AssetsFlow.DTO;

import java.util.UUID;

public record LocationRequest(
        String code,
        String name,
        String locationType,
        String type,
        UUID parentLocationId,
        String parentLocation,
        String addressLine,
        String address,
        String city,
        String country,
        String branch
) {
}
