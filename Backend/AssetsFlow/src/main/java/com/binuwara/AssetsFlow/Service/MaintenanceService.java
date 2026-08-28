package com.binuwara.AssetsFlow.Service;

import com.binuwara.AssetsFlow.DTO.MaintenanceRequest;
import com.binuwara.AssetsFlow.DTO.MaintenanceResponse;
import com.binuwara.AssetsFlow.Entity.AppUser;
import com.binuwara.AssetsFlow.Entity.Asset;
import com.binuwara.AssetsFlow.Entity.AssetStatus;
import com.binuwara.AssetsFlow.Entity.Employee;
import com.binuwara.AssetsFlow.Entity.MaintenancePriority;
import com.binuwara.AssetsFlow.Entity.MaintenanceStatus;
import com.binuwara.AssetsFlow.Entity.MaintenanceTicket;
import com.binuwara.AssetsFlow.Entity.Vendor;
import com.binuwara.AssetsFlow.Exception.ApiException;
import com.binuwara.AssetsFlow.Repository.AppUserRepository;
import com.binuwara.AssetsFlow.Repository.AssetRepository;
import com.binuwara.AssetsFlow.Repository.AuditLogRepository;
import com.binuwara.AssetsFlow.Repository.EmployeeRepository;
import com.binuwara.AssetsFlow.Repository.MaintenanceTicketRepository;
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
public class MaintenanceService {
    private final MaintenanceTicketRepository ticketRepository;
    private final AssetRepository assetRepository;
    private final EmployeeRepository employeeRepository;
    private final VendorRepository vendorRepository;
    private final AppUserRepository appUserRepository;
    private final AuditLogRepository auditLogRepository;

    public MaintenanceService(MaintenanceTicketRepository ticketRepository, AssetRepository assetRepository, EmployeeRepository employeeRepository, VendorRepository vendorRepository, AppUserRepository appUserRepository, AuditLogRepository auditLogRepository) {
        this.ticketRepository = ticketRepository;
        this.assetRepository = assetRepository;
        this.employeeRepository = employeeRepository;
        this.vendorRepository = vendorRepository;
        this.appUserRepository = appUserRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public List<MaintenanceResponse> list(String status, String priority, UUID assetId) {
        MaintenanceStatus requestedStatus = DomainSupport.enumValue(status, MaintenanceStatus.class, null);
        MaintenancePriority requestedPriority = DomainSupport.enumValue(priority, MaintenancePriority.class, null);
        return ticketRepository.findAllByOrderByOpenedAtDesc().stream()
                .filter(ticket -> requestedStatus == null || ticket.getStatus() == requestedStatus)
                .filter(ticket -> requestedPriority == null || ticket.getPriority() == requestedPriority)
                .filter(ticket -> assetId == null || assetId.equals(ticket.getAsset().getId()))
                .map(this::response)
                .toList();
    }

    @Transactional(readOnly = true)
    public MaintenanceResponse get(UUID id) {
        return response(ticketRepository.findWithReferencesById(id).orElseThrow(() -> DomainSupport.notFound("Maintenance ticket")));
    }

    @Transactional
    public MaintenanceResponse create(MaintenanceRequest request, AuthenticatedUser actor) {
        Asset asset = assetRepository.findById(DomainSupport.required(request.assetId(), "Asset")).orElseThrow(() -> DomainSupport.notFound("Asset"));
        MaintenanceTicket ticket = new MaintenanceTicket();
        String number = StringUtils.hasText(request.ticketNumber()) ? request.ticketNumber().trim() : generatedNumber();
        if (ticketRepository.existsByTicketNumberIgnoreCase(number)) throw DomainSupport.conflict("Ticket number is already in use.");
        ticket.setTicketNumber(number);
        ticket.setAsset(asset);
        ticket.setIssue(DomainSupport.text(request.issue(), "Issue"));
        ticket.setDescription(DomainSupport.optionalText(request.description()));
        ticket.setPriority(DomainSupport.enumValue(request.priority(), MaintenancePriority.class, MaintenancePriority.MEDIUM));
        ticket.setStatus(DomainSupport.enumValue(request.status(), MaintenanceStatus.class, MaintenanceStatus.OPEN));
        ticket.setStartDate(request.startDate() == null ? LocalDate.now() : request.startDate());
        ticket.setDueDate(request.dueDate());
        ticket.setVendor(resolveVendor(request.vendorId()));
        ticket.setRequestedByEmployee(resolveEmployee(request.requestedByEmployeeId()));
        ticket.setAssignedToEmployee(resolveEmployee(request.assignedToEmployeeId()));
        ticket.setCost(request.cost() == null ? BigDecimal.ZERO : nonNegative(request.cost(), "Cost"));
        ticket.setResolution(DomainSupport.optionalText(request.resolution()));
        validateDates(ticket);
        applyTimestamps(ticket);
        validateAssetStateForMaintenance(asset, ticket.getStatus());
        MaintenanceTicket saved = ticketRepository.save(ticket);
        syncAssetStatus(saved.getAsset());
        DomainSupport.audit(auditLogRepository, appUserRepository, actor, "MAINTENANCE_TICKET", saved.getId(), "CREATED", null);
        return response(saved);
    }

    @Transactional
    public MaintenanceResponse update(UUID id, MaintenanceRequest request, AuthenticatedUser actor) {
        MaintenanceTicket ticket = ticketRepository.findById(id).orElseThrow(() -> DomainSupport.notFound("Maintenance ticket"));
        if (StringUtils.hasText(request.ticketNumber())) {
            String number = request.ticketNumber().trim();
            if (ticketRepository.existsByTicketNumberIgnoreCaseAndIdNot(number, id)) throw DomainSupport.conflict("Ticket number is already in use.");
            ticket.setTicketNumber(number);
        }
        if (request.issue() != null) ticket.setIssue(DomainSupport.text(request.issue(), "Issue"));
        if (request.description() != null) ticket.setDescription(DomainSupport.optionalText(request.description()));
        if (request.priority() != null) ticket.setPriority(DomainSupport.enumValue(request.priority(), MaintenancePriority.class, ticket.getPriority()));
        if (request.status() != null) ticket.setStatus(DomainSupport.enumValue(request.status(), MaintenanceStatus.class, ticket.getStatus()));
        if (request.startDate() != null) ticket.setStartDate(request.startDate());
        if (request.dueDate() != null) ticket.setDueDate(request.dueDate());
        if (request.vendorId() != null) ticket.setVendor(resolveVendor(request.vendorId()));
        if (request.requestedByEmployeeId() != null) ticket.setRequestedByEmployee(resolveEmployee(request.requestedByEmployeeId()));
        if (request.assignedToEmployeeId() != null) ticket.setAssignedToEmployee(resolveEmployee(request.assignedToEmployeeId()));
        if (request.cost() != null) ticket.setCost(nonNegative(request.cost(), "Cost"));
        if (request.resolution() != null) ticket.setResolution(DomainSupport.optionalText(request.resolution()));
        validateDates(ticket);
        applyTimestamps(ticket);
        validateAssetStateForMaintenance(ticket.getAsset(), ticket.getStatus());
        MaintenanceTicket saved = ticketRepository.save(ticket);
        syncAssetStatus(saved.getAsset());
        DomainSupport.audit(auditLogRepository, appUserRepository, actor, "MAINTENANCE_TICKET", saved.getId(), "UPDATED", null);
        return response(saved);
    }

    @Transactional
    public void delete(UUID id, AuthenticatedUser actor) {
        MaintenanceTicket ticket = ticketRepository.findById(id).orElseThrow(() -> DomainSupport.notFound("Maintenance ticket"));
        Asset asset = ticket.getAsset();
        ticketRepository.delete(ticket);
        ticketRepository.flush();
        syncAssetStatus(asset);
        DomainSupport.audit(auditLogRepository, appUserRepository, actor, "MAINTENANCE_TICKET", id, "DELETED", null);
    }

    private void validateAssetStateForMaintenance(Asset asset, MaintenanceStatus status) {
        if (status == MaintenanceStatus.OPEN || status == MaintenanceStatus.IN_PROGRESS || status == MaintenanceStatus.ON_HOLD) {
            if (asset.getStatus() == AssetStatus.ASSIGNED) throw new ApiException(HttpStatus.CONFLICT, "An assigned asset cannot be placed under maintenance until it is returned.");
            if (asset.getStatus() == AssetStatus.RETIRED || asset.getStatus() == AssetStatus.DISPOSED || asset.getStatus() == AssetStatus.LOST) throw new ApiException(HttpStatus.CONFLICT, "This asset is not eligible for maintenance.");
        }
    }

    private void syncAssetStatus(Asset asset) {
        long activeTickets = ticketRepository.countByAsset_IdAndStatusIn(asset.getId(), List.of(MaintenanceStatus.OPEN, MaintenanceStatus.IN_PROGRESS, MaintenanceStatus.ON_HOLD));
        if (activeTickets > 0) {
            asset.setStatus(AssetStatus.UNDER_MAINTENANCE);
        } else if (asset.getStatus() == AssetStatus.UNDER_MAINTENANCE) {
            asset.setStatus(AssetStatus.AVAILABLE);
        }
        assetRepository.save(asset);
    }

    private void applyTimestamps(MaintenanceTicket ticket) {
        if (ticket.getStatus() == MaintenanceStatus.IN_PROGRESS && ticket.getStartedAt() == null) ticket.setStartedAt(Instant.now());
        if (ticket.getStatus() == MaintenanceStatus.COMPLETED && ticket.getCompletedAt() == null) {
            ticket.setCompletedAt(Instant.now());
            if (ticket.getStartedAt() == null) ticket.setStartedAt(ticket.getCompletedAt());
        }
    }

    private void validateDates(MaintenanceTicket ticket) {
        if (ticket.getStartDate() == null) throw new ApiException(HttpStatus.BAD_REQUEST, "Maintenance start date is required.");
        if (ticket.getDueDate() != null && ticket.getDueDate().isBefore(ticket.getStartDate())) throw new ApiException(HttpStatus.BAD_REQUEST, "Maintenance due date cannot be before the start date.");
    }

    private BigDecimal nonNegative(BigDecimal value, String field) {
        if (value.signum() < 0) throw new ApiException(HttpStatus.BAD_REQUEST, field + " cannot be negative.");
        return value;
    }

    private Vendor resolveVendor(UUID id) {
        return id == null ? null : vendorRepository.findById(id).orElseThrow(() -> DomainSupport.notFound("Vendor"));
    }

    private Employee resolveEmployee(UUID id) {
        return id == null ? null : employeeRepository.findById(id).orElseThrow(() -> DomainSupport.notFound("Employee"));
    }

    private AppUser currentUser(AuthenticatedUser actor) {
        return actor == null ? null : appUserRepository.getReferenceById(actor.getId());
    }

    private String generatedNumber() {
        return "MNT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private MaintenanceResponse response(MaintenanceTicket ticket) {
        Asset asset = ticket.getAsset();
        Employee requested = ticket.getRequestedByEmployee();
        Employee assigned = ticket.getAssignedToEmployee();
        Vendor vendor = ticket.getVendor();
        return new MaintenanceResponse(ticket.getId(), ticket.getTicketNumber(), asset.getId(), asset.getAssetTag(), asset.getName(), ticket.getIssue(), ticket.getDescription(), ticket.getPriority(), ticket.getStatus(), requested == null ? null : requested.getId(), requested == null ? null : DomainSupport.fullName(requested.getFirstName(), requested.getLastName()), assigned == null ? null : assigned.getId(), assigned == null ? null : DomainSupport.fullName(assigned.getFirstName(), assigned.getLastName()), vendor == null ? null : vendor.getId(), vendor == null ? null : vendor.getName(), ticket.getOpenedAt(), ticket.getStartDate(), ticket.getDueDate(), ticket.getStartedAt(), ticket.getCompletedAt(), ticket.getCost(), ticket.getResolution(), ticket.getCreatedAt(), ticket.getUpdatedAt());
    }
}
