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

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "employees")
public class Employee extends TimestampedEntity {

    @Column(name = "employee_number", nullable = false, unique = true, length = 40)
    private String employeeNumber;

    @Column(name = "first_name", nullable = false, length = 80)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 80)
    private String lastName;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(length = 40)
    private String phone;

    @Column(length = 255)
    private String address;

    @Column(name = "job_title", length = 120)
    private String jobTitle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Location branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmployeeStatus status = EmployeeStatus.ACTIVE;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "termination_date")
    private LocalDate terminationDate;

    @OneToOne(mappedBy = "employee", fetch = FetchType.LAZY)
    private AppUser appUser;

    @OneToMany(mappedBy = "employee")
    private Set<Assignment> assignments = new HashSet<>();

    @OneToMany(mappedBy = "employee")
    private Set<LicenseAssignment> licenseAssignments = new HashSet<>();

    @OneToMany(mappedBy = "previousEmployee")
    private Set<AssetTransfer> outgoingAssetTransfers = new HashSet<>();

    @OneToMany(mappedBy = "newEmployee")
    private Set<AssetTransfer> incomingAssetTransfers = new HashSet<>();

    @OneToMany(mappedBy = "employee")
    private Set<EmployeeDepartmentHistory> departmentHistory = new HashSet<>();
}
