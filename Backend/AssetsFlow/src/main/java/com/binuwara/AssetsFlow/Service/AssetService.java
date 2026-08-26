package com.binuwara.AssetsFlow.Service;

import com.binuwara.AssetsFlow.DTO.AssetDisposalRequest;
import com.binuwara.AssetsFlow.DTO.AssetDisposalResponse;
import com.binuwara.AssetsFlow.DTO.AssetRequest;
import com.binuwara.AssetsFlow.DTO.AssetResponse;
import com.binuwara.AssetsFlow.DTO.AssetTransferRequest;
import com.binuwara.AssetsFlow.DTO.AssetTransferResponse;
import com.binuwara.AssetsFlow.DTO.AssetValuationRequest;
import com.binuwara.AssetsFlow.DTO.AssetValuationResponse;
import com.binuwara.AssetsFlow.DTO.AssignmentResponse;
import com.binuwara.AssetsFlow.DTO.LifecycleEventRequest;
import com.binuwara.AssetsFlow.DTO.LifecycleEventResponse;
import com.binuwara.AssetsFlow.Entity.AppUser;
import com.binuwara.AssetsFlow.Entity.Asset;
import com.binuwara.AssetsFlow.Entity.AssetCategory;
import com.binuwara.AssetsFlow.Entity.AssetCondition;
import com.binuwara.AssetsFlow.Entity.AssetDisposal;
import com.binuwara.AssetsFlow.Entity.AssetDisposalStatus;
import com.binuwara.AssetsFlow.Entity.AssetLifecycleEvent;
import com.binuwara.AssetsFlow.Entity.AssetStatus;
import com.binuwara.AssetsFlow.Entity.AssetTransfer;
import com.binuwara.AssetsFlow.Entity.AssetTransferStatus;
import com.binuwara.AssetsFlow.Entity.AssetValuation;
import com.binuwara.AssetsFlow.Entity.Assignment;
import com.binuwara.AssetsFlow.Entity.AssignmentStatus;
import com.binuwara.AssetsFlow.Entity.Department;
import com.binuwara.AssetsFlow.Entity.Employee;
import com.binuwara.AssetsFlow.Entity.Location;
import com.binuwara.AssetsFlow.Entity.PurchaseOrderItem;
import com.binuwara.AssetsFlow.Entity.Vendor;
import com.binuwara.AssetsFlow.Exception.ApiException;
import com.binuwara.AssetsFlow.Repository.AppUserRepository;
import com.binuwara.AssetsFlow.Repository.AssetCategoryRepository;
import com.binuwara.AssetsFlow.Repository.AssetDisposalRepository;
import com.binuwara.AssetsFlow.Repository.AssetLifecycleEventRepository;
import com.binuwara.AssetsFlow.Repository.AssetRepository;
import com.binuwara.AssetsFlow.Repository.AssetTransferRepository;
import com.binuwara.AssetsFlow.Repository.AssetValuationRepository;
import com.binuwara.AssetsFlow.Repository.AssignmentRepository;
import com.binuwara.AssetsFlow.Repository.AuditLogRepository;
import com.binuwara.AssetsFlow.Repository.DepartmentRepository;
import com.binuwara.AssetsFlow.Repository.EmployeeRepository;
import com.binuwara.AssetsFlow.Repository.LocationRepository;
import com.binuwara.AssetsFlow.Repository.PurchaseOrderItemRepository;
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
public class AssetService {
    private final AssetRepository assetRepository;
    private final AssetCategoryRepository categoryRepository;
    private final VendorRepository vendorRepository;
    private final LocationRepository locationRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssetTransferRepository transferRepository;
    private final AssetDisposalRepository disposalRepository;
    private final AssetValuationRepository valuationRepository;
    private final AssetLifecycleEventRepository lifecycleRepository;
    private final AppUserRepository appUserRepository;
    private final AuditLogRepository auditLogRepository;

    public AssetService(
            AssetRepository assetRepository,
            AssetCategoryRepository categoryRepository,
            VendorRepository vendorRepository,
            LocationRepository locationRepository,
            DepartmentRepository departmentRepository,
            EmployeeRepository employeeRepository,
            PurchaseOrderItemRepository purchaseOrderItemRepository,
            AssignmentRepository assignmentRepository,
            AssetTransferRepository transferRepository,
            AssetDisposalRepository disposalRepository,
            AssetValuationRepository valuationRepository,
            AssetLifecycleEventRepository lifecycleRepository,
            AppUserRepository appUserRepository,
            AuditLogRepository auditLogRepository
    ) {
        this.assetRepository = assetRepository;
        this.categoryRepository = categoryRepository;
        this.vendorRepository = vendorRepository;
        this.locationRepository = locationRepository;
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
        this.purchaseOrderItemRepository = purchaseOrderItemRepository;
        this.assignmentRepository = assignmentRepository;
        this.transferRepository = transferRepository;
        this.disposalRepository = disposalRepository;
        this.valuationRepository = valuationRepository;
        this.lifecycleRepository = lifecycleRepository;
        this.appUserRepository = appUserRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public List<AssetResponse> list(String status, UUID categoryId, UUID departmentId) {
        AssetStatus requestedStatus = DomainSupport.enumValue(status, AssetStatus.class, null);
        return assetRepository.findAllByOrderByAssetTagAsc().stream()
                .filter(asset -> requestedStatus == null || asset.getStatus() == requestedStatus)
                .filter(asset -> categoryId == null || (asset.getCategory() != null && categoryId.equals(asset.getCategory().getId())))
                .filter(asset -> departmentId == null || (asset.getDepartment() != null && departmentId.equals(asset.getDepartment().getId())))
                .map(this::assetResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AssetResponse get(UUID id) {
        return assetResponse(assetRepository.findWithReferencesById(id).orElseThrow(() -> DomainSupport.notFound("Asset")));
    }

    @Transactional
    public AssetResponse create(AssetRequest request, AuthenticatedUser actor) {
        String tag = DomainSupport.text(request.assetTag(), "Asset tag");
        if (assetRepository.existsByAssetTagIgnoreCase(tag)) throw DomainSupport.conflict("Asset tag is already in use.");
        if (StringUtils.hasText(request.serialNumber()) && assetRepository.existsBySerialNumberIgnoreCase(request.serialNumber().trim())) throw DomainSupport.conflict("Serial number is already in use.");
        Asset asset = new Asset();
        asset.setAssetTag(tag);
        asset.setName(DomainSupport.text(request.name(), "Asset name"));
        apply(asset, request, true);
        Asset saved = assetRepository.save(asset);
        DomainSupport.audit(auditLogRepository, appUserRepository, actor, "ASSET", saved.getId(), "CREATED", null);
        return assetResponse(saved);
    }

    @Transactional
    public AssetResponse update(UUID id, AssetRequest request, AuthenticatedUser actor) {
        Asset asset = assetRepository.findById(id).orElseThrow(() -> DomainSupport.notFound("Asset"));
        if (StringUtils.hasText(request.assetTag())) {
            String tag = request.assetTag().trim();
            if (assetRepository.existsByAssetTagIgnoreCaseAndIdNot(tag, id)) throw DomainSupport.conflict("Asset tag is already in use.");
            asset.setAssetTag(tag);
        }
        if (StringUtils.hasText(request.name())) asset.setName(request.name().trim());
        if (StringUtils.hasText(request.serialNumber())) {
            String serial = request.serialNumber().trim();
            if (assetRepository.existsBySerialNumberIgnoreCaseAndIdNot(serial, id)) throw DomainSupport.conflict("Serial number is already in use.");
            asset.setSerialNumber(serial);
        } else if (request.serialNumber() != null) asset.setSerialNumber(null);
        apply(asset, request, false);
        Asset saved = assetRepository.save(asset);
        DomainSupport.audit(auditLogRepository, appUserRepository, actor, "ASSET", saved.getId(), "UPDATED", null);
        return assetResponse(saved);
    }

    @Transactional
    public void delete(UUID id, AuthenticatedUser actor) {
        Asset asset = assetRepository.findById(id).orElseThrow(() -> DomainSupport.notFound("Asset"));
        if (assignmentRepository.existsByAsset_IdAndStatus(id, AssignmentStatus.ACTIVE)) throw DomainSupport.conflict("An actively assigned asset cannot be retired.");
        asset.setStatus(AssetStatus.RETIRED);
        asset.setRetirementDate(LocalDate.now());
        assetRepository.save(asset);
        DomainSupport.audit(auditLogRepository, appUserRepository, actor, "ASSET", id, "RETIRED", null);
    }

    @Transactional
    public AssetTransferResponse transfer(UUID assetId, AssetTransferRequest request, AuthenticatedUser actor) {
        UUID newEmployeeId = DomainSupport.required(request.newEmployeeId(), "New employee");
        String reason = DomainSupport.text(request.reason(), "Transfer reason");
        Asset asset = assetRepository.findById(assetId).orElseThrow(() -> DomainSupport.notFound("Asset"));
        Assignment previous = assignmentRepository.findByAsset_IdAndStatus(assetId, AssignmentStatus.ACTIVE).orElseThrow(() -> new ApiException(HttpStatus.CONFLICT, "Only an actively assigned asset can be transferred."));
        Employee newEmployee = employeeRepository.findById(newEmployeeId).orElseThrow(() -> DomainSupport.notFound("Employee"));
        if (previous.getEmployee().getId().equals(newEmployee.getId())) throw new ApiException(HttpStatus.BAD_REQUEST, "The new employee must be different from the current employee.");
        transferRepository.findByAsset_IdAndStatusIn(assetId, List.of(AssetTransferStatus.PENDING, AssetTransferStatus.APPROVED)).ifPresent(existing -> {
            throw DomainSupport.conflict("This asset already has an open transfer request.");
        });
        Location newLocation = request.newLocationId() == null ? asset.getLocation() : locationRepository.findById(request.newLocationId()).orElseThrow(() -> DomainSupport.notFound("Location"));
        AppUser actorUser = currentUser(actor);
        Instant transferredAt = request.transferredAtInstant() != null ? request.transferredAtInstant() : request.transferredAt() == null ? Instant.now() : request.transferredAt().atStartOfDay(java.time.ZoneOffset.UTC).toInstant();
        if (request.expectedReturnDate() != null && request.expectedReturnDate().isBefore(transferredAt.atZone(java.time.ZoneOffset.UTC).toLocalDate())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Closing date cannot be before the transfer date.");
        }

        previous.setStatus(AssignmentStatus.TRANSFERRED);
        previous.setReturnedAt(transferredAt);
        assignmentRepository.save(previous);

        Assignment next = new Assignment();
        next.setAsset(asset);
        next.setEmployee(newEmployee);
        next.setAssignedByUser(actorUser);
        next.setAssignedAt(transferredAt);
        next.setStatus(AssignmentStatus.ACTIVE);
        next.setExpectedReturnDate(request.expectedReturnDate());
        next.setHandoverNotes(request.notes());
        assignmentRepository.save(next);

        AssetTransfer transfer = new AssetTransfer();
        transfer.setAsset(asset);
        transfer.setPreviousAssignment(previous);
        transfer.setNewAssignment(next);
        transfer.setPreviousEmployee(previous.getEmployee());
        transfer.setNewEmployee(newEmployee);
        transfer.setPreviousLocation(asset.getLocation());
        transfer.setNewLocation(newLocation);
        transfer.setRequestedByUser(actorUser);
        transfer.setApprovedByUser(actorUser);
        transfer.setStatus(AssetTransferStatus.COMPLETED);
        transfer.setReason(reason);
        transfer.setRequestedAt(Instant.now());
        transfer.setApprovedAt(Instant.now());
        transfer.setTransferredAt(transferredAt);
        transfer.setNotes(DomainSupport.optionalText(request.notes()));
        if (newLocation != null) asset.setLocation(newLocation);
        asset.setStatus(AssetStatus.ASSIGNED);
        assetRepository.save(asset);
        AssetTransfer saved = transferRepository.save(transfer);
        DomainSupport.audit(auditLogRepository, appUserRepository, actor, "ASSET_TRANSFER", saved.getId(), "COMPLETED", null);
        return transferResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AssetTransferResponse> transferHistory(UUID assetId) {
        if (!assetRepository.existsById(assetId)) throw DomainSupport.notFound("Asset");
        return transferRepository.findAllByAsset_IdOrderByRequestedAtDesc(assetId).stream().map(this::transferResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AssetTransferResponse> listTransfers() {
        return transferRepository.findAllByOrderByRequestedAtDesc().stream().map(this::transferResponse).toList();
    }

    @Transactional
    public AssetDisposalResponse dispose(UUID assetId, AssetDisposalRequest request, AuthenticatedUser actor) {
        Asset asset = assetRepository.findById(assetId).orElseThrow(() -> DomainSupport.notFound("Asset"));
        if (assignmentRepository.existsByAsset_IdAndStatus(assetId, AssignmentStatus.ACTIVE)) throw DomainSupport.conflict("An actively assigned asset cannot be disposed.");
        AssetDisposal disposal = disposalRepository.findByAsset_Id(assetId).orElseGet(AssetDisposal::new);
        disposal.setAsset(asset);
        if (request.disposalDate() != null) disposal.setDisposalDate(request.disposalDate());
        disposal.setDisposalMethod(DomainSupport.text(request.disposalMethod(), "Disposal method"));
        if (request.reason() != null) disposal.setReason(DomainSupport.optionalText(request.reason()));
        if (request.status() != null) disposal.setStatus(request.status());
        if (request.proceeds() != null) {
            if (request.proceeds().signum() < 0) throw new ApiException(HttpStatus.BAD_REQUEST, "Proceeds cannot be negative.");
            disposal.setProceeds(request.proceeds());
        }
        if (request.currency() != null) disposal.setCurrency(DomainSupport.currency(request.currency()));
        if (request.notes() != null) disposal.setNotes(DomainSupport.optionalText(request.notes()));
        disposal.setApprovedByUser(currentUser(actor));
        AssetDisposal saved = disposalRepository.save(disposal);
        asset.setRetirementDate(asset.getRetirementDate() == null ? LocalDate.now() : asset.getRetirementDate());
        asset.setStatus(saved.getStatus() == AssetDisposalStatus.COMPLETED ? AssetStatus.DISPOSED : AssetStatus.RETIRED);
        assetRepository.save(asset);
        DomainSupport.audit(auditLogRepository, appUserRepository, actor, "ASSET_DISPOSAL", saved.getId(), "UPDATED", null);
        return disposalResponse(saved);
    }

    @Transactional(readOnly = true)
    public AssetDisposalResponse getDisposal(UUID assetId) {
        return disposalRepository.findByAsset_Id(assetId).map(this::disposalResponse).orElseThrow(() -> DomainSupport.notFound("Asset disposal"));
    }

    @Transactional
    public AssetValuationResponse addValuation(UUID assetId, AssetValuationRequest request, AuthenticatedUser actor) {
        Asset asset = assetRepository.findById(assetId).orElseThrow(() -> DomainSupport.notFound("Asset"));
        LocalDate valuationDate = request.valuationDate() == null ? LocalDate.now() : request.valuationDate();
        if (valuationRepository.findByAsset_IdAndValuationDate(assetId, valuationDate).isPresent()) throw DomainSupport.conflict("An asset valuation already exists for this date.");
        AssetValuation valuation = new AssetValuation();
        valuation.setAsset(asset);
        valuation.setValuationDate(valuationDate);
        if (request.bookValue() == null || request.bookValue().signum() < 0) throw new ApiException(HttpStatus.BAD_REQUEST, "Book value must be zero or greater.");
        valuation.setBookValue(request.bookValue());
        if (request.marketValue() != null && request.marketValue().signum() < 0) throw new ApiException(HttpStatus.BAD_REQUEST, "Market value cannot be negative.");
        if (request.depreciationAmount() != null && request.depreciationAmount().signum() < 0) throw new ApiException(HttpStatus.BAD_REQUEST, "Depreciation amount cannot be negative.");
        valuation.setMarketValue(request.marketValue());
        valuation.setDepreciationAmount(request.depreciationAmount() == null ? BigDecimal.ZERO : request.depreciationAmount());
        valuation.setValuationMethod(DomainSupport.optionalText(request.valuationMethod()));
        if (request.currency() != null) valuation.setCurrency(DomainSupport.currency(request.currency()));
        valuation.setCreatedByUser(currentUser(actor));
        valuation.setNotes(DomainSupport.optionalText(request.notes()));
        AssetValuation saved = valuationRepository.save(valuation);
        DomainSupport.audit(auditLogRepository, appUserRepository, actor, "ASSET_VALUATION", saved.getId(), "CREATED", null);
        return valuationResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AssetValuationResponse> valuations(UUID assetId) {
        if (!assetRepository.existsById(assetId)) throw DomainSupport.notFound("Asset");
        return valuationRepository.findAllByAsset_IdOrderByValuationDateDesc(assetId).stream().map(this::valuationResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<LifecycleEventResponse> lifecycle(UUID assetId) {
        if (!assetRepository.existsById(assetId)) throw DomainSupport.notFound("Asset");
        return lifecycleRepository.findAllByAsset_IdOrderByEventAtDesc(assetId).stream().map(this::lifecycleResponse).toList();
    }

    @Transactional
    public LifecycleEventResponse addLifecycleEvent(UUID assetId, LifecycleEventRequest request, AuthenticatedUser actor) {
        Asset asset = assetRepository.findById(assetId).orElseThrow(() -> DomainSupport.notFound("Asset"));
        AssetLifecycleEvent event = new AssetLifecycleEvent();
        event.setAsset(asset);
        event.setEventType(DomainSupport.text(request.eventType(), "Event type"));
        if (request.eventAt() != null) event.setEventAt(request.eventAt());
        event.setActorUser(currentUser(actor));
        event.setAssignment(request.assignmentId() == null ? null : assignmentRepository.findById(request.assignmentId()).orElseThrow(() -> DomainSupport.notFound("Assignment")));
        event.setNotes(DomainSupport.optionalText(request.notes()));
        event.setMetadata(DomainSupport.json(request.metadata(), "Metadata"));
        AssetLifecycleEvent saved = lifecycleRepository.save(event);
        DomainSupport.audit(auditLogRepository, appUserRepository, actor, "ASSET_LIFECYCLE_EVENT", saved.getId(), "CREATED", null);
        return lifecycleResponse(saved);
    }

    private void apply(Asset asset, AssetRequest request, boolean create) {
        if (request.categoryId() != null || StringUtils.hasText(request.category())) asset.setCategory(resolveCategory(request.categoryId(), request.category()));
        else if (create) throw new ApiException(HttpStatus.BAD_REQUEST, "Category is required.");
        if (request.brand() != null) asset.setBrand(DomainSupport.optionalText(request.brand()));
        if (request.modelNo() != null) asset.setModelNo(DomainSupport.optionalText(request.modelNo()));
        if (create && !StringUtils.hasText(asset.getModelNo())) throw new ApiException(HttpStatus.BAD_REQUEST, "Model No is required.");
        if (request.vendorId() != null || StringUtils.hasText(request.vendor())) asset.setVendor(resolveVendor(request.vendorId(), request.vendor()));
        if (request.purchaseOrderItemId() != null) asset.setPurchaseOrderItem(purchaseOrderItemRepository.findById(request.purchaseOrderItemId()).orElseThrow(() -> DomainSupport.notFound("Purchase order item")));
        if (request.purchaseDate() != null) asset.setPurchaseDate(request.purchaseDate());
        if (request.purchaseCost() != null) {
            if (request.purchaseCost().signum() < 0) throw new ApiException(HttpStatus.BAD_REQUEST, "Purchase cost cannot be negative.");
            asset.setPurchaseCost(request.purchaseCost());
        }
        if (request.warrantyPeriodMonths() != null) {
            if (request.warrantyPeriodMonths() < 0) throw new ApiException(HttpStatus.BAD_REQUEST, "Warranty period cannot be negative.");
            asset.setWarrantyPeriodMonths(request.warrantyPeriodMonths());
        }
        if (request.currency() != null) asset.setCurrency(DomainSupport.currency(request.currency()));
        if (request.locationId() != null || StringUtils.hasText(request.location())) asset.setLocation(resolveLocation(request.locationId(), request.location()));
        if (request.departmentId() != null || StringUtils.hasText(request.department())) asset.setDepartment(resolveDepartment(request.departmentId(), request.department()));
        if (StringUtils.hasText(request.status())) asset.setStatus(DomainSupport.enumValue(request.status(), AssetStatus.class, asset.getStatus()));
        String condition = DomainSupport.firstText(request.condition(), request.assetCondition());
        if (StringUtils.hasText(condition)) asset.setCondition(DomainSupport.enumValue(condition, AssetCondition.class, asset.getCondition()));
        if (request.retirementDate() != null) asset.setRetirementDate(request.retirementDate());
        if (request.disposalNotes() != null) asset.setDisposalNotes(DomainSupport.optionalText(request.disposalNotes()));
        if (request.notes() != null) asset.setNotes(DomainSupport.optionalText(request.notes()));
        if (create && asset.getStatus() == AssetStatus.ASSIGNED) throw new ApiException(HttpStatus.BAD_REQUEST, "An asset cannot be created as ASSIGNED without an assignment.");
        if (asset.getId() != null && asset.getStatus() == AssetStatus.ASSIGNED && !assignmentRepository.existsByAsset_IdAndStatus(asset.getId(), AssignmentStatus.ACTIVE)) throw new ApiException(HttpStatus.BAD_REQUEST, "An asset can be ASSIGNED only when it has an active assignment.");
        if (asset.getId() != null && assignmentRepository.existsByAsset_IdAndStatus(asset.getId(), AssignmentStatus.ACTIVE) && asset.getStatus() != AssetStatus.ASSIGNED) throw new ApiException(HttpStatus.CONFLICT, "An asset with an active assignment must remain ASSIGNED.");
    }

    private AssetCategory resolveCategory(UUID id, String value) {
        if (id != null) return categoryRepository.findById(id).orElseThrow(() -> DomainSupport.notFound("Category"));
        return categoryRepository.findByNameIgnoreCase(value.trim()).orElseThrow(() -> DomainSupport.notFound("Category"));
    }

    private Vendor resolveVendor(UUID id, String value) {
        if (id != null) return vendorRepository.findById(id).orElseThrow(() -> DomainSupport.notFound("Vendor"));
        return vendorRepository.findByNameIgnoreCase(value.trim()).orElseThrow(() -> DomainSupport.notFound("Vendor"));
    }

    private Location resolveLocation(UUID id, String value) {
        if (id != null) return locationRepository.findById(id).orElseThrow(() -> DomainSupport.notFound("Location"));
        return locationRepository.findByNameIgnoreCase(value.trim()).orElseGet(() -> locationRepository.findByCodeIgnoreCase(value.trim()).orElseThrow(() -> DomainSupport.notFound("Location")));
    }

    private Department resolveDepartment(UUID id, String value) {
        if (id != null) return departmentRepository.findById(id).orElseThrow(() -> DomainSupport.notFound("Department"));
        return departmentRepository.findByNameIgnoreCase(value.trim()).orElseGet(() -> departmentRepository.findByCodeIgnoreCase(value.trim()).orElseThrow(() -> DomainSupport.notFound("Department")));
    }

    private AppUser currentUser(AuthenticatedUser actor) {
        return actor == null ? null : appUserRepository.getReferenceById(actor.getId());
    }

    private AssetResponse assetResponse(Asset asset) {
        AssetCategory category = asset.getCategory();
        Vendor vendor = asset.getVendor();
        Location location = asset.getLocation();
        Department department = asset.getDepartment();
        Assignment assignment = assignmentRepository.findByAsset_IdAndStatus(asset.getId(), AssignmentStatus.ACTIVE).orElse(null);
        Employee assigned = assignment == null ? null : assignment.getEmployee();
        String assignedName = assigned == null ? null : DomainSupport.fullName(assigned.getFirstName(), assigned.getLastName());
        return new AssetResponse(asset.getId(), asset.getAssetTag(), asset.getName(), category == null ? null : category.getId(), category == null ? null : category.getName(), asset.getSerialNumber(), asset.getBrand(), asset.getModelNo(), vendor == null ? null : vendor.getId(), vendor == null ? null : vendor.getName(), location == null ? null : location.getId(), location == null ? null : location.getName(), department == null ? null : department.getId(), department == null ? null : department.getName(), asset.getStatus(), asset.getCondition(), asset.getCondition(), asset.getPurchaseDate(), asset.getPurchaseCost(), asset.getWarrantyPeriodMonths(), asset.getCurrency(), asset.getRetirementDate(), asset.getDisposalNotes(), asset.getNotes(), assigned == null ? null : assigned.getId(), assignedName, asset.getCreatedAt(), asset.getUpdatedAt());
    }

    private AssetTransferResponse transferResponse(AssetTransfer transfer) {
        Employee previous = transfer.getPreviousEmployee();
        Employee next = transfer.getNewEmployee();
        Location previousLocation = transfer.getPreviousLocation();
        Location newLocation = transfer.getNewLocation();
        return new AssetTransferResponse(transfer.getId(), transfer.getAsset().getId(), transfer.getAsset().getAssetTag(), transfer.getPreviousAssignment() == null ? null : transfer.getPreviousAssignment().getId(), transfer.getNewAssignment() == null ? null : transfer.getNewAssignment().getId(), previous.getId(), DomainSupport.fullName(previous.getFirstName(), previous.getLastName()), next.getId(), DomainSupport.fullName(next.getFirstName(), next.getLastName()), previousLocation == null ? null : previousLocation.getId(), previousLocation == null ? null : previousLocation.getName(), newLocation == null ? null : newLocation.getId(), newLocation == null ? null : newLocation.getName(), transfer.getRequestedByUser() == null ? null : transfer.getRequestedByUser().getId(), transfer.getStatus(), transfer.getReason(), transfer.getRequestedAt(), transfer.getApprovedAt(), transfer.getTransferredAt(), transfer.getNotes());
    }

    private AssetDisposalResponse disposalResponse(AssetDisposal disposal) {
        return new AssetDisposalResponse(disposal.getId(), disposal.getAsset().getId(), disposal.getAsset().getAssetTag(), disposal.getDisposalDate(), disposal.getDisposalMethod(), disposal.getReason(), disposal.getStatus(), disposal.getProceeds(), disposal.getCurrency(), disposal.getNotes(), disposal.getCreatedAt(), disposal.getUpdatedAt());
    }

    private AssetValuationResponse valuationResponse(AssetValuation valuation) {
        return new AssetValuationResponse(valuation.getId(), valuation.getAsset().getId(), valuation.getValuationDate(), valuation.getBookValue(), valuation.getMarketValue(), valuation.getDepreciationAmount(), valuation.getValuationMethod(), valuation.getCurrency(), valuation.getCreatedByUser() == null ? null : valuation.getCreatedByUser().getId(), valuation.getNotes(), valuation.getCreatedAt());
    }

    private LifecycleEventResponse lifecycleResponse(AssetLifecycleEvent event) {
        return new LifecycleEventResponse(event.getId(), event.getAsset().getId(), event.getEventType(), event.getEventAt(), event.getActorUser() == null ? null : event.getActorUser().getId(), event.getFromStatus(), event.getToStatus(), event.getFromLocation() == null ? null : event.getFromLocation().getId(), event.getToLocation() == null ? null : event.getToLocation().getId(), event.getAssignment() == null ? null : event.getAssignment().getId(), event.getNotes(), event.getMetadata(), event.getCreatedAt());
    }
}
