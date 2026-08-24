package com.binuwara.AssetsFlow.Service;

import com.binuwara.AssetsFlow.DTO.LicenseAssignmentRequest;
import com.binuwara.AssetsFlow.DTO.LicenseAssignmentResponse;
import com.binuwara.AssetsFlow.DTO.LicenseRequest;
import com.binuwara.AssetsFlow.DTO.LicenseResponse;
import com.binuwara.AssetsFlow.Entity.AppUser;
import com.binuwara.AssetsFlow.Entity.Employee;
import com.binuwara.AssetsFlow.Entity.EmployeeStatus;
import com.binuwara.AssetsFlow.Entity.LicenseAssignment;
import com.binuwara.AssetsFlow.Entity.LicenseAssignmentStatus;
import com.binuwara.AssetsFlow.Entity.LicenseStatus;
import com.binuwara.AssetsFlow.Entity.LicenseType;
import com.binuwara.AssetsFlow.Entity.SoftwareLicense;
import com.binuwara.AssetsFlow.Entity.Vendor;
import com.binuwara.AssetsFlow.Exception.ApiException;
import com.binuwara.AssetsFlow.Repository.AppUserRepository;
import com.binuwara.AssetsFlow.Repository.AuditLogRepository;
import com.binuwara.AssetsFlow.Repository.EmployeeRepository;
import com.binuwara.AssetsFlow.Repository.LicenseAssignmentRepository;
import com.binuwara.AssetsFlow.Repository.SoftwareLicenseRepository;
import com.binuwara.AssetsFlow.Repository.VendorRepository;
import com.binuwara.AssetsFlow.Security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class LicenseService {
    private final SoftwareLicenseRepository licenseRepository;
    private final LicenseAssignmentRepository assignmentRepository;
    private final VendorRepository vendorRepository;
    private final EmployeeRepository employeeRepository;
    private final AppUserRepository appUserRepository;
    private final AuditLogRepository auditLogRepository;

    public LicenseService(SoftwareLicenseRepository licenseRepository, LicenseAssignmentRepository assignmentRepository, VendorRepository vendorRepository, EmployeeRepository employeeRepository, AppUserRepository appUserRepository, AuditLogRepository auditLogRepository) {
        this.licenseRepository = licenseRepository;
        this.assignmentRepository = assignmentRepository;
        this.vendorRepository = vendorRepository;
        this.employeeRepository = employeeRepository;
        this.appUserRepository = appUserRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public List<LicenseResponse> list(String status) {
        LicenseStatus requested = DomainSupport.enumValue(status, LicenseStatus.class, null);
        return licenseRepository.findAllByOrderBySoftwareNameAsc().stream()
                .filter(license -> requested == null || reportingStatus(license) == requested)
                .map(this::response)
                .toList();
    }

    @Transactional(readOnly = true)
    public LicenseResponse get(UUID id) {
        return response(licenseRepository.findWithVendorById(id).orElseThrow(() -> DomainSupport.notFound("Software license")));
    }

    @Transactional
    public LicenseResponse create(LicenseRequest request, AuthenticatedUser actor) {
        SoftwareLicense license = new SoftwareLicense();
        apply(license, request, true);
        SoftwareLicense saved = licenseRepository.save(license);
        DomainSupport.audit(auditLogRepository, appUserRepository, actor, "SOFTWARE_LICENSE", saved.getId(), "CREATED", null);
        return response(saved);
    }

    @Transactional
    public LicenseResponse update(UUID id, LicenseRequest request, AuthenticatedUser actor) {
        SoftwareLicense license = licenseRepository.findById(id).orElseThrow(() -> DomainSupport.notFound("Software license"));
        apply(license, request, false);
        long activeAssignments = assignmentRepository.countByLicense_IdAndStatus(id, LicenseAssignmentStatus.ACTIVE);
        if (license.getSeatCount() != null && activeAssignments > license.getSeatCount()) throw new ApiException(HttpStatus.CONFLICT, "Seat count cannot be lower than active assignments.");
        SoftwareLicense saved = licenseRepository.save(license);
        DomainSupport.audit(auditLogRepository, appUserRepository, actor, "SOFTWARE_LICENSE", saved.getId(), "UPDATED", null);
        return response(saved);
    }

    @Transactional
    public void delete(UUID id, AuthenticatedUser actor) {
        SoftwareLicense license = licenseRepository.findById(id).orElseThrow(() -> DomainSupport.notFound("Software license"));
        license.setStatus(LicenseStatus.CANCELLED);
        licenseRepository.save(license);
        DomainSupport.audit(auditLogRepository, appUserRepository, actor, "SOFTWARE_LICENSE", id, "CANCELLED", null);
    }

    @Transactional(readOnly = true)
    public List<LicenseAssignmentResponse> assignments(UUID licenseId) {
        if (!licenseRepository.existsById(licenseId)) throw DomainSupport.notFound("Software license");
        return assignmentRepository.findAllByLicense_IdOrderByAssignedAtDesc(licenseId).stream().map(this::assignmentResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<LicenseAssignmentResponse> allAssignments() {
        return assignmentRepository.findAllByOrderByAssignedAtDesc().stream().map(this::assignmentResponse).toList();
    }

    @Transactional
    public LicenseAssignmentResponse assign(LicenseAssignmentRequest request, AuthenticatedUser actor) {
        SoftwareLicense license = licenseRepository.findById(DomainSupport.required(request.licenseId(), "License")).orElseThrow(() -> DomainSupport.notFound("Software license"));
        Employee employee = employeeRepository.findById(DomainSupport.required(request.employeeId(), "Employee")).orElseThrow(() -> DomainSupport.notFound("Employee"));
        if (employee.getStatus() != EmployeeStatus.ACTIVE) throw DomainSupport.conflict("Only active employees can receive a license.");
        LicenseAssignmentStatus status = request.status() == null ? LicenseAssignmentStatus.ACTIVE : request.status();
        if (status == LicenseAssignmentStatus.ACTIVE) {
            if (reportingStatus(license) != LicenseStatus.ACTIVE) throw DomainSupport.conflict("Only active licenses can be assigned.");
            if (assignmentRepository.existsByLicense_IdAndEmployee_IdAndStatus(license.getId(), employee.getId(), LicenseAssignmentStatus.ACTIVE)) throw DomainSupport.conflict("This employee already has this license.");
            if (assignmentRepository.countByLicense_IdAndStatus(license.getId(), LicenseAssignmentStatus.ACTIVE) >= license.getSeatCount()) throw DomainSupport.conflict("The license has no available seats.");
        }
        LicenseAssignment assignment = new LicenseAssignment();
        assignment.setLicense(license);
        assignment.setEmployee(employee);
        assignment.setAssignedByUser(currentUser(actor));
        assignment.setAssignedAt(request.assignedAt() == null ? Instant.now() : request.assignedAt());
        assignment.setStatus(status);
        if (status == LicenseAssignmentStatus.REVOKED) assignment.setRevokedAt(Instant.now());
        LicenseAssignment saved = assignmentRepository.save(assignment);
        DomainSupport.audit(auditLogRepository, appUserRepository, actor, "LICENSE_ASSIGNMENT", saved.getId(), "CREATED", null);
        return assignmentResponse(saved);
    }

    @Transactional
    public LicenseAssignmentResponse revoke(UUID id, AuthenticatedUser actor) {
        LicenseAssignment assignment = assignmentRepository.findById(id).orElseThrow(() -> DomainSupport.notFound("License assignment"));
        assignment.setStatus(LicenseAssignmentStatus.REVOKED);
        assignment.setRevokedAt(Instant.now());
        LicenseAssignment saved = assignmentRepository.save(assignment);
        DomainSupport.audit(auditLogRepository, appUserRepository, actor, "LICENSE_ASSIGNMENT", id, "REVOKED", null);
        return assignmentResponse(saved);
    }

    private void apply(SoftwareLicense license, LicenseRequest request, boolean create) {
        String software = DomainSupport.firstText(request.softwareName(), request.software());
        if (create || StringUtils.hasText(software)) license.setSoftwareName(DomainSupport.text(software, "Software name"));
        if (request.vendorId() != null) license.setVendor(vendorRepository.findById(request.vendorId()).orElseThrow(() -> DomainSupport.notFound("Vendor")));
        else if (StringUtils.hasText(request.vendor())) license.setVendor(vendorRepository.findByNameIgnoreCase(request.vendor().trim()).orElseThrow(() -> DomainSupport.notFound("Vendor")));
        String type = DomainSupport.firstText(request.licenseType(), request.type());
        if (create) {
            if (!StringUtils.hasText(type)) throw new ApiException(HttpStatus.BAD_REQUEST, "License type is required.");
            license.setLicenseType(DomainSupport.enumValue(type, LicenseType.class, null));
        } else if (StringUtils.hasText(type)) {
            license.setLicenseType(DomainSupport.enumValue(type, LicenseType.class, license.getLicenseType()));
        }
        if (request.licenseKey() != null) license.setLicenseKey(request.licenseKey());
        Integer seats = request.seatCount() != null ? request.seatCount() : request.seats();
        if (seats != null) {
            if (seats < 1) throw new ApiException(HttpStatus.BAD_REQUEST, "Seat count must be greater than zero.");
            license.setSeatCount(seats);
        }
        if (request.startDate() != null) license.setStartDate(request.startDate());
        if (request.endDate() != null) license.setEndDate(request.endDate());
        if (license.getStartDate() != null && license.getEndDate() != null && license.getEndDate().isBefore(license.getStartDate())) throw new ApiException(HttpStatus.BAD_REQUEST, "License end date cannot be before start date.");
        if (request.purchaseCost() != null) {
            if (request.purchaseCost().signum() < 0) throw new ApiException(HttpStatus.BAD_REQUEST, "Purchase cost cannot be negative.");
            license.setPurchaseCost(request.purchaseCost());
        }
        if (request.status() != null) license.setStatus(DomainSupport.enumValue(request.status(), LicenseStatus.class, license.getStatus()));
        if (request.notes() != null) license.setNotes(DomainSupport.optionalText(request.notes()));
    }

    private LicenseStatus reportingStatus(SoftwareLicense license) {
        if (license.getStatus() == LicenseStatus.CANCELLED) return LicenseStatus.CANCELLED;
        if (license.getEndDate() != null && license.getEndDate().isBefore(LocalDate.now())) return LicenseStatus.EXPIRED;
        if (license.getEndDate() != null && !license.getEndDate().isAfter(LocalDate.now().plusDays(30))) return LicenseStatus.EXPIRING;
        return LicenseStatus.ACTIVE;
    }

    private AppUser currentUser(AuthenticatedUser actor) {
        return actor == null ? null : appUserRepository.getReferenceById(actor.getId());
    }

    private LicenseResponse response(SoftwareLicense license) {
        Vendor vendor = license.getVendor();
        int used = (int) assignmentRepository.countByLicense_IdAndStatus(license.getId(), LicenseAssignmentStatus.ACTIVE);
        LicenseType type = license.getLicenseType();
        return new LicenseResponse(license.getId(), license.getSoftwareName(), license.getSoftwareName(), vendor == null ? null : vendor.getId(), vendor == null ? null : vendor.getName(), type, type, license.getSeatCount(), license.getSeatCount(), used, null, license.getStartDate(), license.getEndDate(), license.getPurchaseCost(), reportingStatus(license), license.getNotes(), license.getCreatedAt(), license.getUpdatedAt());
    }

    private LicenseAssignmentResponse assignmentResponse(LicenseAssignment assignment) {
        Employee employee = assignment.getEmployee();
        return new LicenseAssignmentResponse(assignment.getId(), assignment.getLicense().getId(), assignment.getLicense().getSoftwareName(), employee.getId(), DomainSupport.fullName(employee.getFirstName(), employee.getLastName()), employee.getEmployeeNumber(), assignment.getAssignedAt(), assignment.getRevokedAt(), assignment.getStatus());
    }
}
