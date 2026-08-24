package com.binuwara.AssetsFlow.Controller;

import com.binuwara.AssetsFlow.DTO.AssignmentRequest;
import com.binuwara.AssetsFlow.DTO.AssignmentResponse;
import com.binuwara.AssetsFlow.Security.AuthenticatedUser;
import com.binuwara.AssetsFlow.Service.AssignmentService;
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
@RequestMapping("/api/assignments")
public class AssignmentController {
    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) { this.assignmentService = assignmentService; }

    @GetMapping
    public List<AssignmentResponse> list(@RequestParam(required = false) String status, @RequestParam(required = false) UUID employeeId, @RequestParam(required = false) UUID assetId) { return assignmentService.list(status, employeeId, assetId); }

    @GetMapping("/{id}")
    public AssignmentResponse get(@PathVariable UUID id) { return assignmentService.get(id); }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','DEPARTMENT_HEAD')")
    public ResponseEntity<AssignmentResponse> create(@RequestBody AssignmentRequest request, @AuthenticationPrincipal AuthenticatedUser actor) { return ResponseEntity.status(HttpStatus.CREATED).body(assignmentService.create(request, actor)); }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','DEPARTMENT_HEAD')")
    public AssignmentResponse update(@PathVariable UUID id, @RequestBody AssignmentRequest request, @AuthenticationPrincipal AuthenticatedUser actor) { return assignmentService.update(id, request, actor); }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','DEPARTMENT_HEAD')")
    public ResponseEntity<Void> delete(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser actor) { assignmentService.delete(id, actor); return ResponseEntity.noContent().build(); }
}
