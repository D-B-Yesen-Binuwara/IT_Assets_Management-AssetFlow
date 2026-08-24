package com.binuwara.AssetsFlow.Controller;

import com.binuwara.AssetsFlow.DTO.LicenseAssignmentRequest;
import com.binuwara.AssetsFlow.DTO.LicenseAssignmentResponse;
import com.binuwara.AssetsFlow.DTO.LicenseRequest;
import com.binuwara.AssetsFlow.DTO.LicenseResponse;
import com.binuwara.AssetsFlow.Security.AuthenticatedUser;
import com.binuwara.AssetsFlow.Service.LicenseService;
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
@RequestMapping("/api/licenses")
public class LicenseController {
    private final LicenseService licenseService;

    public LicenseController(LicenseService licenseService) { this.licenseService = licenseService; }

    @GetMapping
    public List<LicenseResponse> list(@RequestParam(required = false) String status) { return licenseService.list(status); }

    @GetMapping("/{id}")
    public LicenseResponse get(@PathVariable UUID id) { return licenseService.get(id); }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','DEPARTMENT_HEAD')")
    public ResponseEntity<LicenseResponse> create(@RequestBody LicenseRequest request, @AuthenticationPrincipal AuthenticatedUser actor) { return ResponseEntity.status(HttpStatus.CREATED).body(licenseService.create(request, actor)); }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','DEPARTMENT_HEAD')")
    public LicenseResponse update(@PathVariable UUID id, @RequestBody LicenseRequest request, @AuthenticationPrincipal AuthenticatedUser actor) { return licenseService.update(id, request, actor); }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser actor) { licenseService.delete(id, actor); return ResponseEntity.noContent().build(); }

    @GetMapping("/assignments")
    public List<LicenseAssignmentResponse> assignments() { return licenseService.allAssignments(); }

    @GetMapping("/{licenseId}/assignments")
    public List<LicenseAssignmentResponse> licenseAssignments(@PathVariable UUID licenseId) { return licenseService.assignments(licenseId); }

    @PostMapping("/assignments")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','DEPARTMENT_HEAD')")
    public ResponseEntity<LicenseAssignmentResponse> assign(@RequestBody LicenseAssignmentRequest request, @AuthenticationPrincipal AuthenticatedUser actor) { return ResponseEntity.status(HttpStatus.CREATED).body(licenseService.assign(request, actor)); }

    @PatchMapping("/assignments/{id}/revoke")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','DEPARTMENT_HEAD')")
    public LicenseAssignmentResponse revoke(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser actor) { return licenseService.revoke(id, actor); }
}
