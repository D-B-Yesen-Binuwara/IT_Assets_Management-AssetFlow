package com.binuwara.AssetsFlow.Repository;

import com.binuwara.AssetsFlow.Entity.LicenseStatus;
import com.binuwara.AssetsFlow.Entity.SoftwareLicense;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SoftwareLicenseRepository extends JpaRepository<SoftwareLicense, UUID> {
    @EntityGraph(attributePaths = {"vendor"})
    List<SoftwareLicense> findAllByOrderBySoftwareNameAsc();
    @EntityGraph(attributePaths = {"vendor"})
    Optional<SoftwareLicense> findWithVendorById(UUID id);
    long countByStatus(LicenseStatus status);
    long countByEndDateBetween(LocalDate from, LocalDate to);
}
