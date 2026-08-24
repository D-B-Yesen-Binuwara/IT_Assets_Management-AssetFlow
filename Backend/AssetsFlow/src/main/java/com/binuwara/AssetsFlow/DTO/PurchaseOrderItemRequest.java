package com.binuwara.AssetsFlow.DTO;

import java.math.BigDecimal;
import java.util.UUID;

public record PurchaseOrderItemRequest(
        UUID id,
        UUID categoryId,
        String description,
        Integer quantity,
        Integer receivedQuantity,
        BigDecimal unitCost
) {
}
