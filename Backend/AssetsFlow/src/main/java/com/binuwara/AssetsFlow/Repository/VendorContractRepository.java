package com.binuwara.AssetsFlow.Repository;

import com.binuwara.AssetsFlow.Entity.VendorContract;
import com.binuwara.AssetsFlow.Entity.VendorContractStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VendorContractRepository extends JpaRepository<VendorContract, UUID> {
    @EntityGraph(attributePaths = {"vendor"})
    List<VendorContract> findAllByOrderByStartDateDesc();
    @EntityGraph(attributePaths = {"vendor"})
    Optional<VendorContract> findWithVendorById(UUID id);
    Optional<VendorContract> findByContractNumberIgnoreCase(String contractNumber);
    boolean existsByContractNumberIgnoreCase(String contractNumber);
    boolean existsByContractNumberIgnoreCaseAndIdNot(String contractNumber, UUID id);
    long countByStatus(VendorContractStatus status);
}
