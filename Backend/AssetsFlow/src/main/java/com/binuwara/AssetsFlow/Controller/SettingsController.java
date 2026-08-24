package com.binuwara.AssetsFlow.Controller;

import com.binuwara.AssetsFlow.DTO.EmailTemplateRequest;
import com.binuwara.AssetsFlow.DTO.EmailTemplateResponse;
import com.binuwara.AssetsFlow.DTO.SettingsRequest;
import com.binuwara.AssetsFlow.DTO.SettingsResponse;
import com.binuwara.AssetsFlow.DTO.SystemSettingRequest;
import com.binuwara.AssetsFlow.Entity.SystemSetting;
import com.binuwara.AssetsFlow.Security.AuthenticatedUser;
import com.binuwara.AssetsFlow.Service.SettingsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {
    private final SettingsService settingsService;

    public SettingsController(SettingsService settingsService) { this.settingsService = settingsService; }

    @GetMapping
    public SettingsResponse get() { return settingsService.get(); }

    @PatchMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public SettingsResponse update(@RequestBody SettingsRequest request, @AuthenticationPrincipal AuthenticatedUser actor) { return settingsService.update(request, actor); }

    @GetMapping("/system")
    public Map<String, String> system() { return settingsService.listSystemSettings().stream().collect(Collectors.toMap(SystemSetting::getSettingKey, SystemSetting::getSettingValue, (first, second) -> second, LinkedHashMap::new)); }

    @PostMapping("/system")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public Map<String, String> saveSystem(@RequestBody SystemSettingRequest request, @AuthenticationPrincipal AuthenticatedUser actor) { SystemSetting saved = settingsService.saveSystemSetting(request, actor); return Map.of(saved.getSettingKey(), saved.getSettingValue()); }

    @GetMapping("/email-templates")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public List<EmailTemplateResponse> templates() { return settingsService.listTemplates(); }

    @PostMapping("/email-templates")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<EmailTemplateResponse> createTemplate(@RequestBody EmailTemplateRequest request, @AuthenticationPrincipal AuthenticatedUser actor) { return ResponseEntity.status(HttpStatus.CREATED).body(settingsService.createTemplate(request, actor)); }

    @PatchMapping("/email-templates/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public EmailTemplateResponse updateTemplate(@PathVariable UUID id, @RequestBody EmailTemplateRequest request, @AuthenticationPrincipal AuthenticatedUser actor) { return settingsService.updateTemplate(id, request, actor); }
}
