package com.binuwara.AssetsFlow.Repository;

import com.binuwara.AssetsFlow.Entity.Asset;
import com.binuwara.AssetsFlow.Entity.AssetStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssetRepository extends JpaRepository<Asset, UUID> {
    @EntityGraph(attributePaths = {"category", "vendor", "location", "department"})
    List<Asset> findAllByOrderByAssetTagAsc();

    @EntityGraph(attributePaths = {"category", "vendor", "location", "department"})
    Optional<Asset> findWithReferencesById(UUID id);

    Optional<Asset> findByAssetTagIgnoreCase(String assetTag);

    boolean existsByAssetTagIgnoreCase(String assetTag);

    boolean existsByAssetTagIgnoreCaseAndIdNot(String assetTag, UUID id);

    boolean existsBySerialNumberIgnoreCase(String serialNumber);

    boolean existsBySerialNumberIgnoreCaseAndIdNot(String serialNumber, UUID id);

    long countByStatus(AssetStatus status);

    long countByDepartment_Id(UUID departmentId);

    long countByCategory_Id(UUID categoryId);

    long countByLocation_Id(UUID locationId);

    long countByVendor_Id(UUID vendorId);
}
