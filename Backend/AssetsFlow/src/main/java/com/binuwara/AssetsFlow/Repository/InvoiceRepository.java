package com.binuwara.AssetsFlow.Repository;

import com.binuwara.AssetsFlow.Entity.Invoice;
import com.binuwara.AssetsFlow.Entity.InvoiceStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    @EntityGraph(attributePaths = {"vendor", "purchaseOrder"})
    List<Invoice> findAllByOrderByInvoiceDateDesc();
    @EntityGraph(attributePaths = {"vendor", "purchaseOrder"})
    Optional<Invoice> findWithReferencesById(UUID id);
    Optional<Invoice> findByInvoiceNumberIgnoreCase(String invoiceNumber);
    boolean existsByInvoiceNumberIgnoreCase(String invoiceNumber);
    boolean existsByInvoiceNumberIgnoreCaseAndIdNot(String invoiceNumber, UUID id);
    long countByStatus(InvoiceStatus status);
}
