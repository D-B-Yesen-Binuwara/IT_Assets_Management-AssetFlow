package com.binuwara.AssetsFlow.Service;

import com.binuwara.AssetsFlow.DTO.AssignmentRequest;
import com.binuwara.AssetsFlow.DTO.AssignmentResponse;
import com.binuwara.AssetsFlow.Entity.AppUser;
import com.binuwara.AssetsFlow.Entity.Asset;
import com.binuwara.AssetsFlow.Entity.AssetStatus;
import com.binuwara.AssetsFlow.Entity.Assignment;
import com.binuwara.AssetsFlow.Entity.AssignmentStatus;
import com.binuwara.AssetsFlow.Entity.Employee;
import com.binuwara.AssetsFlow.Entity.EmployeeStatus;
import com.binuwara.AssetsFlow.Repository.AppUserRepository;
import com.binuwara.AssetsFlow.Repository.AssetRepository;
import com.binuwara.AssetsFlow.Repository.AssignmentRepository;
import com.binuwara.AssetsFlow.Repository.AuditLogRepository;
import com.binuwara.AssetsFlow.Repository.EmployeeRepository;
import com.binuwara.AssetsFlow.Security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class AssignmentService {
    private final AssignmentRepository assignmentRepository;
    private final AssetRepository assetRepository;
    private final EmployeeRepository employeeRepository;
    private final AppUserRepository appUserRepository;
    private final AuditLogRepository auditLogRepository;

    public AssignmentService(AssignmentRepository assignmentRepository, AssetRepository assetRepository, EmployeeRepository employeeRepository, AppUserRepository appUserRepository, AuditLogRepository auditLogRepository) {
        this.assignmentRepository = assignmentRepository;
        this.assetRepository = assetRepository;
        this.employeeRepository = employeeRepository;
        this.appUserRepository = appUserRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public List<AssignmentResponse> list(String status, UUID employeeId, UUID assetId) {
        AssignmentStatus requested = DomainSupport.enumValue(status, AssignmentStatus.class, null);
        return assignmentRepository.findAllByOrderByAssignedAtDesc().stream()
                .filter(item -> requested == null || item.getStatus() == requested)
                .filter(item -> employeeId == null || employeeId.equals(item.getEmployee().getId()))
                .filter(item -> assetId == null || assetId.equals(item.getAsset().getId()))
                .map(this::response)
                .toList();
    }

    @Transactional(readOnly = true)
    public AssignmentResponse get(UUID id) {
        return response(assignmentRepository.findWithReferencesById(id).orElseThrow(() -> DomainSupport.notFound("Assignment")));
    }

    @Transactional
    public AssignmentResponse create(AssignmentRequest request, AuthenticatedUser actor) {
        Asset asset = assetRepository.findById(DomainSupport.required(request.assetId(), "Asset")).orElseThrow(() -> DomainSupport.notFound("Asset"));
        Employee employee = employeeRepository.findById(DomainSupport.required(request.employeeId(), "Employee")).orElseThrow(() -> DomainSupport.notFound("Employee"));
        if (employee.getStatus() != EmployeeStatus.ACTIVE) throw DomainSupport.conflict("Only active employees can receive assignments.");
        AssignmentStatus status = request.status() == null ? AssignmentStatus.ACTIVE : request.status();
        Instant assignedAt = resolveAssignedAt(request);
        if (request.expectedReturnDate() != null && request.expectedReturnDate().isBefore(assignedAt.atZone(ZoneOffset.UTC).toLocalDate())) {
            throw new com.binuwara.AssetsFlow.Exception.ApiException(HttpStatus.BAD_REQUEST, "Expected return date cannot be before the assignment date.");
        }
        if (status == AssignmentStatus.ACTIVE) {
            if (assignmentRepository.existsByAsset_IdAndStatus(asset.getId(), AssignmentStatus.ACTIVE)) throw DomainSupport.conflict("The asset already has an active assignment.");
            if (asset.getStatus() != AssetStatus.AVAILABLE && asset.getStatus() != AssetStatus.ASSIGNED) throw DomainSupport.conflict("The asset is not available for assignment.");
        }
        Assignment assignment = new Assignment();
        assignment.setAsset(asset);
        assignment.setEmployee(employee);
        assignment.setAssignedByUser(currentUser(actor));
        assignment.setAssignedAt(assignedAt);
        assignment.setExpectedReturnDate(request.expectedReturnDate());
        assignment.setStatus(status);
        assignment.setHandoverNotes(DomainSupport.optionalText(request.handoverNotes()));
        if (status != AssignmentStatus.ACTIVE) {
            Instant returnedAt = Instant.now();
            if (returnedAt.isBefore(assignedAt)) throw new com.binuwara.AssetsFlow.Exception.ApiException(HttpStatus.BAD_REQUEST, "A closed assignment cannot have a future assignment date.");
            assignment.setReturnedAt(returnedAt);
        }
        Assignment saved = assignmentRepository.save(assignment);
        syncAssetStatus(asset);
        DomainSupport.audit(auditLogRepository, appUserRepository, actor, "ASSIGNMENT", saved.getId(), "CREATED", null);
        return response(saved);
    }

    @Transactional
    public AssignmentResponse update(UUID id, AssignmentRequest request, AuthenticatedUser actor) {
        Assignment assignment = assignmentRepository.findById(id).orElseThrow(() -> DomainSupport.notFound("Assignment"));
        if (request.expectedReturnDate() != null) assignment.setExpectedReturnDate(request.expectedReturnDate());
        if (request.handoverNotes() != null) assignment.setHandoverNotes(DomainSupport.optionalText(request.handoverNotes()));
        if (request.status() != null && request.status() != assignment.getStatus()) closeOrReopen(assignment, request.status());
        Assignment saved = assignmentRepository.save(assignment);
        syncAssetStatus(saved.getAsset());
        DomainSupport.audit(auditLogRepository, appUserRepository, actor, "ASSIGNMENT", saved.getId(), "UPDATED", null);
        return response(saved);
    }

    @Transactional
    public void delete(UUID id, AuthenticatedUser actor) {
        Assignment assignment = assignmentRepository.findById(id).orElseThrow(() -> DomainSupport.notFound("Assignment"));
        if (assignment.getStatus() == AssignmentStatus.ACTIVE) {
            assignment.setStatus(AssignmentStatus.CANCELLED);
            assignment.setReturnedAt(Instant.now());
            assignmentRepository.save(assignment);
            syncAssetStatus(assignment.getAsset());
        }
        DomainSupport.audit(auditLogRepository, appUserRepository, actor, "ASSIGNMENT", id, "CANCELLED", null);
    }

    private void closeOrReopen(Assignment assignment, AssignmentStatus status) {
        if (status == AssignmentStatus.ACTIVE) {
            if (assignmentRepository.existsByAsset_IdAndStatus(assignment.getAsset().getId(), AssignmentStatus.ACTIVE)) throw DomainSupport.conflict("The asset already has an active assignment.");
            if (assignment.getAsset().getStatus() != AssetStatus.AVAILABLE && assignment.getAsset().getStatus() != AssetStatus.ASSIGNED) throw DomainSupport.conflict("The asset is not available for assignment.");
            assignment.setReturnedAt(null);
        } else if (assignment.getStatus() == AssignmentStatus.ACTIVE) {
            assignment.setReturnedAt(Instant.now());
        }
        assignment.setStatus(status);
    }

    private void syncAssetStatus(Asset asset) {
        boolean active = assignmentRepository.existsByAsset_IdAndStatus(asset.getId(), AssignmentStatus.ACTIVE);
        if (active) asset.setStatus(AssetStatus.ASSIGNED);
        else if (asset.getStatus() == AssetStatus.ASSIGNED) asset.setStatus(AssetStatus.AVAILABLE);
        assetRepository.save(asset);
    }

    private Instant resolveAssignedAt(AssignmentRequest request) {
        if (request.assignedAt() != null) return request.assignedAt();
        if (request.assignedDate() != null) return request.assignedDate().atStartOfDay(ZoneOffset.UTC).toInstant();
        return Instant.now();
    }

    private AppUser currentUser(AuthenticatedUser actor) {
        return actor == null ? null : appUserRepository.getReferenceById(actor.getId());
    }

    private AssignmentResponse response(Assignment assignment) {
        Asset asset = assignment.getAsset();
        Employee employee = assignment.getEmployee();
        return new AssignmentResponse(assignment.getId(), asset.getId(), asset.getAssetTag(), asset.getName(), employee.getId(), DomainSupport.fullName(employee.getFirstName(), employee.getLastName()), employee.getEmployeeNumber(), asset.getLocation() == null ? null : asset.getLocation().getId(), asset.getLocation() == null ? null : asset.getLocation().getName(), assignment.getAssignedAt(), assignment.getAssignedAt() == null ? null : assignment.getAssignedAt().atZone(ZoneOffset.UTC).toLocalDate(), assignment.getExpectedReturnDate(), assignment.getReturnedAt(), assignment.getStatus(), assignment.getHandoverNotes(), assignment.getCreatedAt());
    }
}
