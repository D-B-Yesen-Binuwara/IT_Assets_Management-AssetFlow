package com.binuwara.AssetsFlow.Controller;

import com.binuwara.AssetsFlow.DTO.WarrantyClaimRequest;
import com.binuwara.AssetsFlow.DTO.WarrantyClaimResponse;
import com.binuwara.AssetsFlow.DTO.WarrantyRequest;
import com.binuwara.AssetsFlow.DTO.WarrantyResponse;
import com.binuwara.AssetsFlow.Security.AuthenticatedUser;
import com.binuwara.AssetsFlow.Service.WarrantyService;
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
@RequestMapping("/api/warranty")
public class WarrantyController {
    private final WarrantyService warrantyService;

    public WarrantyController(WarrantyService warrantyService) { this.warrantyService = warrantyService; }

    @GetMapping
    public List<WarrantyResponse> list(@RequestParam(required = false) UUID assetId, @RequestParam(required = false) String status) { return warrantyService.list(assetId, status); }

    @GetMapping("/{id}")
    public WarrantyResponse get(@PathVariable UUID id) { return warrantyService.get(id); }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','DEPARTMENT_HEAD')")
    public ResponseEntity<WarrantyResponse> create(@RequestBody WarrantyRequest request, @AuthenticationPrincipal AuthenticatedUser actor) { return ResponseEntity.status(HttpStatus.CREATED).body(warrantyService.create(request, actor)); }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','DEPARTMENT_HEAD')")
    public WarrantyResponse update(@PathVariable UUID id, @RequestBody WarrantyRequest request, @AuthenticationPrincipal AuthenticatedUser actor) { return warrantyService.update(id, request, actor); }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser actor) { warrantyService.delete(id, actor); return ResponseEntity.noContent().build(); }

    @GetMapping("/claims")
    public List<WarrantyClaimResponse> claims(@RequestParam(required = false) UUID assetId) { return warrantyService.claims(assetId); }

    @PostMapping("/claims")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','DEPARTMENT_HEAD')")
    public ResponseEntity<WarrantyClaimResponse> createClaim(@RequestBody WarrantyClaimRequest request, @AuthenticationPrincipal AuthenticatedUser actor) { return ResponseEntity.status(HttpStatus.CREATED).body(warrantyService.createClaim(request, actor)); }

    @PatchMapping("/claims/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','DEPARTMENT_HEAD')")
    public WarrantyClaimResponse updateClaim(@PathVariable UUID id, @RequestBody WarrantyClaimRequest request, @AuthenticationPrincipal AuthenticatedUser actor) { return warrantyService.updateClaim(id, request, actor); }
}
