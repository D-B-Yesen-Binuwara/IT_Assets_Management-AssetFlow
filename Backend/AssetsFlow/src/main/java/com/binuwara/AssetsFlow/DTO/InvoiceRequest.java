package com.binuwara.AssetsFlow.DTO;

import com.binuwara.AssetsFlow.Entity.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record InvoiceRequest(
        String invoiceNumber,
        UUID purchaseOrderId,
        UUID vendorId,
        LocalDate invoiceDate,
        LocalDate dueDate,
        LocalDate paidDate,
        InvoiceStatus status,
        String currency,
        BigDecimal subtotal,
        BigDecimal taxAmount,
        BigDecimal totalAmount,
        String notes
) {
}
