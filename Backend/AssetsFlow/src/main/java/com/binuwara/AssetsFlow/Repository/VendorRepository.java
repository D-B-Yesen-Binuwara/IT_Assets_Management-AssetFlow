package com.binuwara.AssetsFlow.Repository;

import com.binuwara.AssetsFlow.Entity.Vendor;
import com.binuwara.AssetsFlow.Entity.VendorStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VendorRepository extends JpaRepository<Vendor, UUID> {
    List<Vendor> findAllByOrderByNameAsc();
    Optional<Vendor> findByVendorCodeIgnoreCase(String vendorCode);
    Optional<Vendor> findByNameIgnoreCase(String name);
    boolean existsByVendorCodeIgnoreCase(String vendorCode);
    boolean existsByVendorCodeIgnoreCaseAndIdNot(String vendorCode, UUID id);
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);
    long countByStatus(VendorStatus status);
}
