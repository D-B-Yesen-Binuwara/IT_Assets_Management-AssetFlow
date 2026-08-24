package com.binuwara.AssetsFlow.Repository;

import com.binuwara.AssetsFlow.Entity.PurchaseOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PurchaseOrderItemRepository extends JpaRepository<PurchaseOrderItem, UUID> {
    List<PurchaseOrderItem> findAllByPurchaseOrder_Id(UUID purchaseOrderId);
}
