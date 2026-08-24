package com.binuwara.AssetsFlow.Repository;

import com.binuwara.AssetsFlow.Entity.MaintenanceStatus;
import com.binuwara.AssetsFlow.Entity.MaintenanceTicket;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MaintenanceTicketRepository extends JpaRepository<MaintenanceTicket, UUID> {
    @EntityGraph(attributePaths = {"asset", "asset.category", "requestedByEmployee", "assignedToEmployee", "vendor"})
    List<MaintenanceTicket> findAllByOrderByOpenedAtDesc();
    @EntityGraph(attributePaths = {"asset", "asset.category", "requestedByEmployee", "assignedToEmployee", "vendor"})
    Optional<MaintenanceTicket> findWithReferencesById(UUID id);
    Optional<MaintenanceTicket> findByTicketNumberIgnoreCase(String ticketNumber);
    boolean existsByTicketNumberIgnoreCase(String ticketNumber);
    boolean existsByTicketNumberIgnoreCaseAndIdNot(String ticketNumber, UUID id);
    long countByStatus(MaintenanceStatus status);
    long countByAsset_IdAndStatusIn(UUID assetId, List<MaintenanceStatus> statuses);
    List<MaintenanceTicket> findAllByDueDateLessThanEqualAndStatusIn(LocalDate date, List<MaintenanceStatus> statuses);
}
