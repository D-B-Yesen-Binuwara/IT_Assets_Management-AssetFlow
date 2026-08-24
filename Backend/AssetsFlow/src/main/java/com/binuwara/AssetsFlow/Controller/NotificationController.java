package com.binuwara.AssetsFlow.Controller;

import com.binuwara.AssetsFlow.DTO.NotificationCreateRequest;
import com.binuwara.AssetsFlow.DTO.NotificationPreferenceRequest;
import com.binuwara.AssetsFlow.DTO.NotificationPreferenceResponse;
import com.binuwara.AssetsFlow.DTO.NotificationResponse;
import com.binuwara.AssetsFlow.Security.AuthenticatedUser;
import com.binuwara.AssetsFlow.Service.NotificationService;
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
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) { this.notificationService = notificationService; }

    @GetMapping
    public List<NotificationResponse> list(@RequestParam(required = false) String filter, @AuthenticationPrincipal AuthenticatedUser actor) { return notificationService.list(filter, actor); }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','DEPARTMENT_HEAD')")
    public ResponseEntity<NotificationResponse> create(@RequestBody NotificationCreateRequest request, @AuthenticationPrincipal AuthenticatedUser actor) { return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.create(request, actor)); }

    @PatchMapping("/{id}/read")
    public NotificationResponse markRead(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser actor) { return notificationService.markRead(id, actor); }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser actor) { notificationService.delete(id, actor); return ResponseEntity.noContent().build(); }

    @GetMapping("/preferences")
    public List<NotificationPreferenceResponse> preferences(@AuthenticationPrincipal AuthenticatedUser actor) { return notificationService.preferences(actor); }

    @PostMapping("/preferences")
    public ResponseEntity<NotificationPreferenceResponse> savePreference(@RequestBody NotificationPreferenceRequest request, @AuthenticationPrincipal AuthenticatedUser actor) { return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.savePreference(request, actor)); }
}
