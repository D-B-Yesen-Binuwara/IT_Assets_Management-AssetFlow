package com.binuwara.AssetsFlow.Controller;

import com.binuwara.AssetsFlow.DTO.MaintenanceRequest;
import com.binuwara.AssetsFlow.DTO.MaintenanceResponse;
import com.binuwara.AssetsFlow.Security.AuthenticatedUser;
import com.binuwara.AssetsFlow.Service.MaintenanceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/maintenance")
public class MaintenanceController {
    private final MaintenanceService maintenanceService;

    public MaintenanceController(MaintenanceService maintenanceService) { this.maintenanceService = maintenanceService; }

    @GetMapping
    public List<MaintenanceResponse> list(@RequestParam(required = false) String status, @RequestParam(required = false) String priority, @RequestParam(required = false) UUID assetId) { return maintenanceService.list(status, priority, assetId); }

    @GetMapping("/{id}")
    public MaintenanceResponse get(@PathVariable UUID id) { return maintenanceService.get(id); }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','DEPARTMENT_HEAD')")
    public ResponseEntity<MaintenanceResponse> create(@RequestBody MaintenanceRequest request, @AuthenticationPrincipal AuthenticatedUser actor) { return ResponseEntity.status(HttpStatus.CREATED).body(maintenanceService.create(request, actor)); }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','DEPARTMENT_HEAD')")
    public MaintenanceResponse update(@PathVariable UUID id, @RequestBody MaintenanceRequest request, @AuthenticationPrincipal AuthenticatedUser actor) { return maintenanceService.update(id, request, actor); }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','DEPARTMENT_HEAD')")
    public ResponseEntity<Void> delete(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser actor) { maintenanceService.delete(id, actor); return ResponseEntity.noContent().build(); }
}
