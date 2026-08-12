package com.binuwara.AssetsFlow.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "asset_categories")
public class AssetCategory extends TimestampedEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    private String description;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "category")
    private Set<Asset> assets = new HashSet<>();

    @OneToMany(mappedBy = "category")
    private Set<PurchaseOrderItem> purchaseOrderItems = new HashSet<>();
}
