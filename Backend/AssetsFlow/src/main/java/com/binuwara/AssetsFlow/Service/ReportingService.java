package com.binuwara.AssetsFlow.Service;

import com.binuwara.AssetsFlow.DTO.DashboardSummaryResponse;
import com.binuwara.AssetsFlow.DTO.LifecycleEventResponse;
import com.binuwara.AssetsFlow.DTO.ReportSummaryResponse;
import com.binuwara.AssetsFlow.Entity.Asset;
import com.binuwara.AssetsFlow.Entity.AssetLifecycleEvent;
import com.binuwara.AssetsFlow.Entity.AssetStatus;
import com.binuwara.AssetsFlow.Entity.Assignment;
import com.binuwara.AssetsFlow.Entity.AssignmentStatus;
import com.binuwara.AssetsFlow.Entity.Employee;
import com.binuwara.AssetsFlow.Entity.MaintenanceStatus;
import com.binuwara.AssetsFlow.Entity.PurchaseOrderStatus;
import com.binuwara.AssetsFlow.Entity.WarrantyStatus;
import com.binuwara.AssetsFlow.Repository.AssetLifecycleEventRepository;
import com.binuwara.AssetsFlow.Repository.AssetRepository;
import com.binuwara.AssetsFlow.Repository.AssetValuationRepository;
import com.binuwara.AssetsFlow.Repository.AssignmentRepository;
import com.binuwara.AssetsFlow.Repository.DepartmentRepository;
import com.binuwara.AssetsFlow.Repository.MaintenanceTicketRepository;
import com.binuwara.AssetsFlow.Repository.NotificationRepository;
import com.binuwara.AssetsFlow.Repository.PurchaseOrderRepository;
import com.binuwara.AssetsFlow.Repository.WarrantyClaimRepository;
import com.binuwara.AssetsFlow.Repository.WarrantyPolicyRepository;
import com.binuwara.AssetsFlow.Security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ReportingService {
    private final AssetRepository assetRepository;
    private final AssignmentRepository assignmentRepository;
    private final DepartmentRepository departmentRepository;
    private final MaintenanceTicketRepository maintenanceRepository;
    private final WarrantyPolicyRepository warrantyRepository;
    private final WarrantyClaimRepository claimRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final AssetValuationRepository valuationRepository;
    private final AssetLifecycleEventRepository lifecycleRepository;
    private final NotificationRepository notificationRepository;

    public ReportingService(AssetRepository assetRepository, AssignmentRepository assignmentRepository, DepartmentRepository departmentRepository, MaintenanceTicketRepository maintenanceRepository, WarrantyPolicyRepository warrantyRepository, WarrantyClaimRepository claimRepository, PurchaseOrderRepository purchaseOrderRepository, AssetValuationRepository valuationRepository, AssetLifecycleEventRepository lifecycleRepository, NotificationRepository notificationRepository) {
        this.assetRepository = assetRepository;
        this.assignmentRepository = assignmentRepository;
        this.departmentRepository = departmentRepository;
        this.maintenanceRepository = maintenanceRepository;
        this.warrantyRepository = warrantyRepository;
        this.claimRepository = claimRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.valuationRepository = valuationRepository;
        this.lifecycleRepository = lifecycleRepository;
        this.notificationRepository = notificationRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse dashboard(AuthenticatedUser actor) {
        List<Asset> assets = assetRepository.findAllByOrderByAssetTagAsc();
        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (AssetStatus status : AssetStatus.values()) byStatus.put(status.name(), assets.stream().filter(asset -> asset.getStatus() == status).count());
        Map<String, Long> byCategory = new LinkedHashMap<>();
        assets.forEach(asset -> byCategory.merge(asset.getCategory() == null ? "Uncategorized" : asset.getCategory().getName(), 1L, Long::sum));
        Map<String, Long> byDepartment = new LinkedHashMap<>();
        assets.forEach(asset -> byDepartment.merge(asset.getDepartment() == null ? "Unassigned" : asset.getDepartment().getName(), 1L, Long::sum));
        BigDecimal value = assets.stream().map(asset -> valuationRepository.findFirstByAsset_IdOrderByValuationDateDesc(asset.getId()).map(item -> item.getBookValue()).orElse(asset.getPurchaseCost() == null ? BigDecimal.ZERO : asset.getPurchaseCost())).reduce(BigDecimal.ZERO, BigDecimal::add);
        long unread = actor == null ? 0 : notificationRepository.countByRecipientUser_IdAndReadAtIsNull(actor.getId());
        return new DashboardSummaryResponse(assets.size(), byStatus.getOrDefault(AssetStatus.AVAILABLE.name(), 0L), byStatus.getOrDefault(AssetStatus.ASSIGNED.name(), 0L), byStatus.getOrDefault(AssetStatus.UNDER_MAINTENANCE.name(), 0L), byStatus.getOrDefault(AssetStatus.RETIRED.name(), 0L) + byStatus.getOrDefault(AssetStatus.DISPOSED.name(), 0L), warrantyRepository.countByEndDateBetween(LocalDate.now(), LocalDate.now().plusDays(60)), value, departmentRepository.count(), maintenanceRepository.countByStatus(MaintenanceStatus.OPEN), unread, byStatus, byCategory, byDepartment);
    }

    @Transactional(readOnly = true)
    public List<LifecycleEventResponse> activity() {
        return lifecycleRepository.findTop100ByOrderByEventAtDesc().stream().map(this::lifecycleResponse).toList();
    }

    @Transactional(readOnly = true)
    public ReportSummaryResponse report(String report, LocalDate from, LocalDate to) {
        String selected = report == null || report.isBlank() ? "overview" : report.trim().toLowerCase();
        LocalDate start = from == null ? LocalDate.now().minusYears(1) : from;
        LocalDate end = to == null ? LocalDate.now() : to;
        if (end.isBefore(start)) throw new com.binuwara.AssetsFlow.Exception.ApiException(HttpStatus.BAD_REQUEST, "Report end date cannot be before start date.");
        Map<String, Object> metrics = new LinkedHashMap<>();
        List<Map<String, Object>> series = new ArrayList<>();
        List<Map<String, Object>> breakdowns = new ArrayList<>();
        switch (selected) {
            case "overview" -> {
                metrics.put("totalAssets", assetRepository.count());
                metrics.put("activeAssignments", assignmentRepository.countByStatus(AssignmentStatus.ACTIVE));
                metrics.put("openMaintenance", maintenanceRepository.countByStatus(MaintenanceStatus.OPEN));
                metrics.put("warrantyClaims", claimRepository.count());
                metrics.put("departments", departmentRepository.count());
                assetRepository.findAllByOrderByAssetTagAsc().forEach(asset -> addBreakdown(breakdowns, "status", asset.getStatus().name()));
            }
            case "department" -> departmentRepository.findAllByOrderByNameAsc().forEach(department -> {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("departmentId", department.getId()); row.put("department", department.getName()); row.put("assetCount", assetRepository.countByDepartment_Id(department.getId())); row.put("employeeCount", department.getEmployees().size()); breakdowns.add(row);
            });
            case "maintenance" -> {
                var tickets = maintenanceRepository.findAllByOrderByOpenedAtDesc();
                metrics.put("totalTickets", tickets.size()); metrics.put("totalCost", tickets.stream().map(item -> item.getCost() == null ? BigDecimal.ZERO : item.getCost()).reduce(BigDecimal.ZERO, BigDecimal::add));
                tickets.stream().filter(item -> item.getOpenedAt() != null && !item.getOpenedAt().atZone(ZoneOffset.UTC).toLocalDate().isBefore(start) && !item.getOpenedAt().atZone(ZoneOffset.UTC).toLocalDate().isAfter(end)).forEach(item -> addSeries(series, item.getOpenedAt().atZone(ZoneOffset.UTC).toLocalDate().toString(), item.getCost()));
            }
            case "procurement" -> {
                var orders = purchaseOrderRepository.findAllByOrderByOrderDateDesc();
                metrics.put("totalOrders", orders.size()); metrics.put("totalSpend", orders.stream().map(item -> item.getTotalAmount() == null ? BigDecimal.ZERO : item.getTotalAmount()).reduce(BigDecimal.ZERO, BigDecimal::add));
                for (PurchaseOrderStatus status : PurchaseOrderStatus.values()) { Map<String, Object> row = new LinkedHashMap<>(); row.put("status", status.name()); row.put("count", orders.stream().filter(item -> item.getStatus() == status).count()); breakdowns.add(row); }
            }
            case "warranty" -> {
                for (WarrantyStatus status : WarrantyStatus.values()) { Map<String, Object> row = new LinkedHashMap<>(); row.put("status", status.name()); row.put("count", warrantyRepository.countByStatus(status)); breakdowns.add(row); }
                metrics.put("claims", claimRepository.count());
            }
            case "lifecycle" -> {
                var events = lifecycleRepository.findTop100ByOrderByEventAtDesc();
                metrics.put("events", events.size());
                events.stream().filter(event -> event.getEventAt() != null && !event.getEventAt().atZone(ZoneOffset.UTC).toLocalDate().isBefore(start) && !event.getEventAt().atZone(ZoneOffset.UTC).toLocalDate().isAfter(end)).forEach(event -> addSeries(series, event.getEventAt().atZone(ZoneOffset.UTC).toLocalDate().toString(), 1));
            }
            default -> throw new com.binuwara.AssetsFlow.Exception.ApiException(HttpStatus.BAD_REQUEST, "Unknown report: " + report);
        }
        return new ReportSummaryResponse(selected, start, end, metrics, series, breakdowns);
    }

    private void addBreakdown(List<Map<String, Object>> rows, String key, String value) {
        rows.stream().filter(row -> value.equals(row.get(key))).findFirst().ifPresentOrElse(row -> row.put("count", ((Number) row.getOrDefault("count", 0)).longValue() + 1), () -> { Map<String, Object> row = new LinkedHashMap<>(); row.put(key, value); row.put("count", 1L); rows.add(row); });
    }

    private void addSeries(List<Map<String, Object>> rows, String date, Object value) {
        Map<String, Object> row = new LinkedHashMap<>(); row.put("date", date); row.put("value", value); rows.add(row);
    }

    private LifecycleEventResponse lifecycleResponse(AssetLifecycleEvent event) {
        Assignment assignment = event.getAssignment();
        Employee employee = assignment == null ? null : assignment.getEmployee();
        if (employee == null && event.getActorUser() != null) employee = event.getActorUser().getEmployee();
        return new LifecycleEventResponse(event.getId(), event.getAsset().getId(), event.getAsset().getAssetTag(), employee == null ? null : employee.getId(), employee == null ? null : employee.getEmployeeNumber(), event.getEventType(), event.getEventAt(), event.getActorUser() == null ? null : event.getActorUser().getId(), event.getFromStatus(), event.getToStatus(), event.getFromLocation() == null ? null : event.getFromLocation().getId(), event.getToLocation() == null ? null : event.getToLocation().getId(), assignment == null ? null : assignment.getId(), event.getNotes(), event.getMetadata(), event.getCreatedAt());
    }
}
