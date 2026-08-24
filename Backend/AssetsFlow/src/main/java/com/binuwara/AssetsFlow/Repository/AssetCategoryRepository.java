package com.binuwara.AssetsFlow.Repository;

import com.binuwara.AssetsFlow.Entity.AssetCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssetCategoryRepository extends JpaRepository<AssetCategory, UUID> {
    List<AssetCategory> findAllByOrderByNameAsc();
    Optional<AssetCategory> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);
}
