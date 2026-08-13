package com.binuwara.AssetsFlow.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "asset_transfers")
public class AssetTransfer extends TimestampedEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "previous_assignment_id", nullable = false)
    private Assignment previousAssignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_assignment_id")
    private Assignment newAssignment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "previous_employee_id", nullable = false)
    private Employee previousEmployee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "new_employee_id", nullable = false)
    private Employee newEmployee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "previous_location_id")
    private Location previousLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_location_id")
    private Location newLocation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requested_by_user_id", nullable = false)
    private AppUser requestedByUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_user_id")
    private AppUser approvedByUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssetTransferStatus status = AssetTransferStatus.PENDING;

    @Column(nullable = false, length = 255)
    private String reason;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt = Instant.now();

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "transferred_at")
    private Instant transferredAt;

    private String notes;
}
