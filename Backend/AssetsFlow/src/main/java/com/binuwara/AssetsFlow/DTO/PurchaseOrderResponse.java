package com.binuwara.AssetsFlow.DTO;

import com.binuwara.AssetsFlow.Entity.PurchaseOrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PurchaseOrderResponse(
        UUID id,
        String poNumber,
        UUID vendorId,
        String vendor,
        UUID requestedByEmployeeId,
        String requestedBy,
        LocalDate orderDate,
        LocalDate expectedDate,
        LocalDate receivedDate,
        PurchaseOrderStatus status,
        String currency,
        BigDecimal subtotal,
        BigDecimal taxAmount,
        BigDecimal totalAmount,
        String notes,
        List<PurchaseOrderItemResponse> items,
        Instant createdAt,
        Instant updatedAt
) {
}
