package com.binuwara.AssetsFlow.Repository;

import com.binuwara.AssetsFlow.Entity.AssetTransfer;
import com.binuwara.AssetsFlow.Entity.AssetTransferStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssetTransferRepository extends JpaRepository<AssetTransfer, UUID> {
    @EntityGraph(attributePaths = {"asset", "previousEmployee", "newEmployee", "previousLocation", "newLocation", "requestedByUser", "approvedByUser", "previousAssignment", "newAssignment"})
    List<AssetTransfer> findAllByOrderByRequestedAtDesc();
    @EntityGraph(attributePaths = {"asset", "previousEmployee", "newEmployee", "previousLocation", "newLocation", "requestedByUser", "approvedByUser", "previousAssignment", "newAssignment"})
    List<AssetTransfer> findAllByAsset_IdOrderByRequestedAtDesc(UUID assetId);
    @EntityGraph(attributePaths = {"asset", "previousEmployee", "newEmployee", "previousLocation", "newLocation", "requestedByUser", "approvedByUser", "previousAssignment", "newAssignment"})
    Optional<AssetTransfer> findWithDetailsById(UUID id);
    Optional<AssetTransfer> findByAsset_IdAndStatusIn(UUID assetId, List<AssetTransferStatus> statuses);
    long countByStatus(AssetTransferStatus status);
}
