package com.binuwara.AssetsFlow.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.sql.Types;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "organization_settings")
public class OrganizationSettings {

    @Id
    @Column(nullable = false)
    private Short id = 1;

    @Column(name = "organization_name", nullable = false, length = 180)
    private String organizationName = "AssetFlow";

    @Column(length = 120)
    private String industry;

    @Column(name = "primary_contact", length = 180)
    private String primaryContact;

    @JdbcTypeCode(Types.CHAR)
    @Column(columnDefinition = "char(3)", nullable = false, length = 3)
    private String currency = "USD";

    @Column(nullable = false, length = 80)
    private String timezone = "UTC";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private String branding = "{}";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "notification_settings", columnDefinition = "jsonb", nullable = false)
    private String notificationSettings = "{}";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private AppUser updatedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
