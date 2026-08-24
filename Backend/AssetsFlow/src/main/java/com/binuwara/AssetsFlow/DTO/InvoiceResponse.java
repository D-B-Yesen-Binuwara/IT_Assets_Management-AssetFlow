package com.binuwara.AssetsFlow.DTO;

import com.binuwara.AssetsFlow.Entity.InvoiceStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record InvoiceResponse(
        UUID id,
        String invoiceNumber,
        UUID purchaseOrderId,
        String poNumber,
        UUID vendorId,
        String vendor,
        LocalDate invoiceDate,
        LocalDate dueDate,
        LocalDate paidDate,
        InvoiceStatus status,
        String currency,
        BigDecimal subtotal,
        BigDecimal taxAmount,
        BigDecimal totalAmount,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {
}
