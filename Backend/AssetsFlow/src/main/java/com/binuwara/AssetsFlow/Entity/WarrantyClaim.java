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

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "warranty_claims")
public class WarrantyClaim extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warranty_id", nullable = false)
    private WarrantyPolicy warranty;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_user_id")
    private AppUser assignedToUser;

    @Column(name = "claim_number", nullable = false, unique = true, length = 80)
    private String claimNumber;

    @Column(name = "opened_at", nullable = false)
    private Instant openedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WarrantyClaimStatus status = WarrantyClaimStatus.OPEN;

    @Column(nullable = false)
    private String description;

    @Column(name = "claimed_amount", precision = 14, scale = 2)
    private BigDecimal claimedAmount;

    @Column(name = "provider_reference", length = 120)
    private String providerReference;

    @Column(name = "resolution_notes")
    private String resolutionNotes;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @jakarta.persistence.PrePersist
    protected void onCreate() {
        if (openedAt == null) {
            openedAt = Instant.now();
        }
        updatedAt = Instant.now();
    }

    @jakarta.persistence.PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
