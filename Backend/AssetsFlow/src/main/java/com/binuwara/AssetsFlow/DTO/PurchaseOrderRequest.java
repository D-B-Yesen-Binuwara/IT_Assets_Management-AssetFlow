package com.binuwara.AssetsFlow.DTO;

import com.binuwara.AssetsFlow.Entity.PurchaseOrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PurchaseOrderRequest(
        String poNumber,
        UUID vendorId,
        UUID requestedByEmployeeId,
        LocalDate orderDate,
        LocalDate expectedDate,
        LocalDate receivedDate,
        PurchaseOrderStatus status,
        String currency,
        BigDecimal subtotal,
        BigDecimal taxAmount,
        BigDecimal totalAmount,
        String notes,
        List<PurchaseOrderItemRequest> items
) {
}
