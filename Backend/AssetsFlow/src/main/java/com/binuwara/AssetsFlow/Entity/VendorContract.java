package com.binuwara.AssetsFlow.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.sql.Types;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "vendor_contracts")
public class VendorContract extends TimestampedEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @Column(name = "contract_number", nullable = false, unique = true, length = 80)
    private String contractNumber;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VendorContractStatus status = VendorContractStatus.DRAFT;

    @Column(name = "contract_value", precision = 14, scale = 2)
    private BigDecimal contractValue;

    @JdbcTypeCode(Types.CHAR)
    @Column(columnDefinition = "char(3)", nullable = false, length = 3)
    private String currency = "USD";

    @Column(name = "document_reference", length = 255)
    private String documentReference;

    private String notes;
}
