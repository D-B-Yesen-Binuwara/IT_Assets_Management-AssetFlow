package com.binuwara.AssetsFlow.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "vendors")
public class Vendor extends TimestampedEntity {

    @Column(name = "vendor_code", nullable = false, unique = true, length = 40)
    private String vendorCode;

    @Column(nullable = false, unique = true, length = 180)
    private String name;

    @Column(name = "contact_name", length = 120)
    private String contactName;

    @Column(length = 255)
    private String email;

    @Column(length = 40)
    private String phone;

    @Column(length = 100)
    private String category;

    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VendorStatus status = VendorStatus.ACTIVE;

    @OneToMany(mappedBy = "vendor")
    private Set<Asset> assets = new HashSet<>();

    @OneToMany(mappedBy = "vendor")
    private Set<PurchaseOrder> purchaseOrders = new HashSet<>();
}
