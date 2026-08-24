package com.binuwara.AssetsFlow.Repository;

import com.binuwara.AssetsFlow.Entity.PurchaseOrder;
import com.binuwara.AssetsFlow.Entity.PurchaseOrderStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, UUID> {
    @EntityGraph(attributePaths = {"vendor", "requestedByEmployee", "items", "items.category"})
    List<PurchaseOrder> findAllByOrderByOrderDateDesc();
    @EntityGraph(attributePaths = {"vendor", "requestedByEmployee", "items", "items.category"})
    Optional<PurchaseOrder> findWithDetailsById(UUID id);
    Optional<PurchaseOrder> findByPoNumberIgnoreCase(String poNumber);
    boolean existsByPoNumberIgnoreCase(String poNumber);
    boolean existsByPoNumberIgnoreCaseAndIdNot(String poNumber, UUID id);
    long countByStatus(PurchaseOrderStatus status);
    long countByVendor_Id(UUID vendorId);
}
