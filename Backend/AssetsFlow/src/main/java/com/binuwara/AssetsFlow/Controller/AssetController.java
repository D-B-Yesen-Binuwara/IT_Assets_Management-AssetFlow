package com.binuwara.AssetsFlow.Controller;

import com.binuwara.AssetsFlow.DTO.AssetDisposalRequest;
import com.binuwara.AssetsFlow.DTO.AssetDisposalResponse;
import com.binuwara.AssetsFlow.DTO.AssetRequest;
import com.binuwara.AssetsFlow.DTO.AssetResponse;
import com.binuwara.AssetsFlow.DTO.AssetStatusChangeRequest;
import com.binuwara.AssetsFlow.DTO.AssetTransferRequest;
import com.binuwara.AssetsFlow.DTO.AssetTransferResponse;
import com.binuwara.AssetsFlow.DTO.AssetValuationRequest;
import com.binuwara.AssetsFlow.DTO.AssetValuationResponse;
import com.binuwara.AssetsFlow.DTO.LifecycleEventRequest;
import com.binuwara.AssetsFlow.DTO.LifecycleEventResponse;
import com.binuwara.AssetsFlow.Security.AuthenticatedUser;
import com.binuwara.AssetsFlow.Service.AssetService;
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
@RequestMapping("/api/assets")
public class AssetController {
    private final AssetService assetService;

    public AssetController(AssetService assetService) { this.assetService = assetService; }

    @GetMapping
    public List<AssetResponse> list(@RequestParam(required = false) String status, @RequestParam(required = false) UUID categoryId, @RequestParam(required = false) UUID departmentId) { return assetService.list(status, categoryId, departmentId); }

    @GetMapping("/{id}")
    public AssetResponse get(@PathVariable UUID id) { return assetService.get(id); }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','DEPARTMENT_HEAD')")
    public ResponseEntity<AssetResponse> create(@RequestBody AssetRequest request, @AuthenticationPrincipal AuthenticatedUser actor) { return ResponseEntity.status(HttpStatus.CREATED).body(assetService.create(request, actor)); }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','DEPARTMENT_HEAD')")
    public AssetResponse update(@PathVariable UUID id, @RequestBody AssetRequest request, @AuthenticationPrincipal AuthenticatedUser actor) { return assetService.update(id, request, actor); }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','DEPARTMENT_HEAD')")
    public AssetResponse changeStatus(@PathVariable UUID id, @RequestBody AssetStatusChangeRequest request, @AuthenticationPrincipal AuthenticatedUser actor) { return assetService.changeStatus(id, request, actor); }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser actor) { assetService.delete(id, actor); return ResponseEntity.noContent().build(); }

    @PostMapping("/{assetId}/transfers")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','DEPARTMENT_HEAD')")
    public AssetTransferResponse transfer(@PathVariable UUID assetId, @RequestBody AssetTransferRequest request, @AuthenticationPrincipal AuthenticatedUser actor) { return assetService.transfer(assetId, request, actor); }

    @GetMapping("/{assetId}/transfers")
    public List<AssetTransferResponse> transferHistory(@PathVariable UUID assetId) { return assetService.transferHistory(assetId); }

    @PostMapping("/{assetId}/disposal")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public AssetDisposalResponse dispose(@PathVariable UUID assetId, @RequestBody AssetDisposalRequest request, @AuthenticationPrincipal AuthenticatedUser actor) { return assetService.dispose(assetId, request, actor); }

    @GetMapping("/{assetId}/disposal")
    public AssetDisposalResponse disposal(@PathVariable UUID assetId) { return assetService.getDisposal(assetId); }

    @PostMapping("/{assetId}/valuations")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','DEPARTMENT_HEAD')")
    public ResponseEntity<AssetValuationResponse> addValuation(@PathVariable UUID assetId, @RequestBody AssetValuationRequest request, @AuthenticationPrincipal AuthenticatedUser actor) { return ResponseEntity.status(HttpStatus.CREATED).body(assetService.addValuation(assetId, request, actor)); }

    @GetMapping("/{assetId}/valuations")
    public List<AssetValuationResponse> valuations(@PathVariable UUID assetId) { return assetService.valuations(assetId); }

    @GetMapping("/{assetId}/lifecycle")
    public List<LifecycleEventResponse> lifecycle(@PathVariable UUID assetId) { return assetService.lifecycle(assetId); }

    @PostMapping("/{assetId}/lifecycle")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','DEPARTMENT_HEAD')")
    public ResponseEntity<LifecycleEventResponse> addLifecycle(@PathVariable UUID assetId, @RequestBody LifecycleEventRequest request, @AuthenticationPrincipal AuthenticatedUser actor) { return ResponseEntity.status(HttpStatus.CREATED).body(assetService.addLifecycleEvent(assetId, request, actor)); }
}
