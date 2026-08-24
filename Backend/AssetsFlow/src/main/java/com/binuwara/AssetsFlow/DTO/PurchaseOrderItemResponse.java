package com.binuwara.AssetsFlow.DTO;

import java.math.BigDecimal;
import java.util.UUID;

public record PurchaseOrderItemResponse(UUID id, UUID categoryId, String category, String description, Integer quantity, Integer receivedQuantity, BigDecimal unitCost) {
}
