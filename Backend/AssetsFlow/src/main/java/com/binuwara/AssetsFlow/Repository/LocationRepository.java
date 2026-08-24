package com.binuwara.AssetsFlow.Repository;

import com.binuwara.AssetsFlow.Entity.Location;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LocationRepository extends JpaRepository<Location, UUID> {
    @EntityGraph(attributePaths = {"parentLocation"})
    List<Location> findAllByOrderByNameAsc();

    @EntityGraph(attributePaths = {"parentLocation"})
    Optional<Location> findWithParentById(UUID id);

    Optional<Location> findByCodeIgnoreCase(String code);
    Optional<Location> findByNameIgnoreCase(String name);
    boolean existsByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCaseAndIdNot(String code, UUID id);
}
