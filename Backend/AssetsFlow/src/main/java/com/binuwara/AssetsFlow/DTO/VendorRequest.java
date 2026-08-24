package com.binuwara.AssetsFlow.DTO;

import com.binuwara.AssetsFlow.Entity.VendorStatus;

public record VendorRequest(
        String vendorCode,
        String name,
        String contactName,
        String contact,
        String email,
        String phone,
        String category,
        String address,
        VendorStatus status
) {
}
