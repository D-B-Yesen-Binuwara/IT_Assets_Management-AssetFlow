package com.binuwara.AssetsFlow.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "assets")
public class Asset extends TimestampedEntity {

    @Column(name = "asset_tag", nullable = false, unique = true, length = 50)
    private String assetTag;

    @Column(nullable = false, length = 180)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private AssetCategory category;

    @Column(name = "serial_number", unique = true, length = 120)
    private String serialNumber;

    @Column(length = 120)
    private String manufacturer;

    @Column(length = 120)
    private String model;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id")
    private Vendor vendor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_item_id")
    private PurchaseOrderItem purchaseOrderItem;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "purchase_cost", precision = 14, scale = 2)
    private BigDecimal purchaseCost;

    @Column(nullable = false, length = 3)
    private String currency = "USD";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private AssetStatus status = AssetStatus.AVAILABLE;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_condition", nullable = false, length = 20)
    private AssetCondition condition = AssetCondition.GOOD;

    @Column(name = "retirement_date")
    private LocalDate retirementDate;

    @Column(name = "disposal_notes")
    private String disposalNotes;

    private String notes;

    @OneToMany(mappedBy = "asset")
    private Set<Assignment> assignments = new HashSet<>();

    @OneToMany(mappedBy = "asset")
    private Set<MaintenanceTicket> maintenanceTickets = new HashSet<>();

    @OneToMany(mappedBy = "asset")
    private List<WarrantyPolicy> warrantyPolicies = new ArrayList<>();

    @OneToOne(mappedBy = "asset")
    private AssetDisposal disposal;

    @OneToMany(mappedBy = "asset")
    private List<AssetValuation> valuations = new ArrayList<>();

    @OneToMany(mappedBy = "asset")
    private List<AssetLifecycleEvent> lifecycleEvents = new ArrayList<>();

    @OneToMany(mappedBy = "asset")
    private List<AssetTransfer> transfers = new ArrayList<>();
}
