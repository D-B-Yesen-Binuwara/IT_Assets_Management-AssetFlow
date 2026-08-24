package com.binuwara.AssetsFlow.Service;

import com.binuwara.AssetsFlow.DTO.EmailTemplateRequest;
import com.binuwara.AssetsFlow.DTO.EmailTemplateResponse;
import com.binuwara.AssetsFlow.DTO.SettingsRequest;
import com.binuwara.AssetsFlow.DTO.SettingsResponse;
import com.binuwara.AssetsFlow.DTO.SystemSettingRequest;
import com.binuwara.AssetsFlow.Entity.AppUser;
import com.binuwara.AssetsFlow.Entity.EmailTemplate;
import com.binuwara.AssetsFlow.Entity.OrganizationSettings;
import com.binuwara.AssetsFlow.Entity.SystemSetting;
import com.binuwara.AssetsFlow.Repository.AppUserRepository;
import com.binuwara.AssetsFlow.Repository.AuditLogRepository;
import com.binuwara.AssetsFlow.Repository.EmailTemplateRepository;
import com.binuwara.AssetsFlow.Repository.OrganizationSettingsRepository;
import com.binuwara.AssetsFlow.Repository.SystemSettingRepository;
import com.binuwara.AssetsFlow.Security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Service
public class SettingsService {
    private static final short SETTINGS_ID = 1;
    private final OrganizationSettingsRepository settingsRepository;
    private final SystemSettingRepository systemSettingRepository;
    private final EmailTemplateRepository emailTemplateRepository;
    private final AppUserRepository appUserRepository;
    private final AuditLogRepository auditLogRepository;

    public SettingsService(OrganizationSettingsRepository settingsRepository, SystemSettingRepository systemSettingRepository, EmailTemplateRepository emailTemplateRepository, AppUserRepository appUserRepository, AuditLogRepository auditLogRepository) {
        this.settingsRepository = settingsRepository;
        this.systemSettingRepository = systemSettingRepository;
        this.emailTemplateRepository = emailTemplateRepository;
        this.appUserRepository = appUserRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public SettingsResponse get() {
        return response(settingsRepository.findById(SETTINGS_ID).orElseGet(() -> settingsRepository.save(new OrganizationSettings())));
    }

    @Transactional
    public SettingsResponse update(SettingsRequest request, AuthenticatedUser actor) {
        OrganizationSettings settings = settingsRepository.findById(SETTINGS_ID).orElseGet(() -> settingsRepository.save(new OrganizationSettings()));
        if (request.organizationName() != null) settings.setOrganizationName(DomainSupport.text(request.organizationName(), "Organization name"));
        if (request.industry() != null) settings.setIndustry(DomainSupport.optionalText(request.industry()));
        if (request.primaryContact() != null) settings.setPrimaryContact(DomainSupport.optionalText(request.primaryContact()));
        if (request.currency() != null) settings.setCurrency(DomainSupport.currency(request.currency()));
        if (request.timezone() != null) settings.setTimezone(DomainSupport.text(request.timezone(), "Timezone"));
        if (request.branding() != null) settings.setBranding(DomainSupport.json(request.branding(), "Branding"));
        if (request.notificationSettings() != null) settings.setNotificationSettings(DomainSupport.json(request.notificationSettings(), "Notification settings"));
        settings.setUpdatedBy(currentUser(actor));
        OrganizationSettings saved = settingsRepository.save(settings);
        DomainSupport.audit(auditLogRepository, appUserRepository, actor, "ORGANIZATION_SETTINGS", null, "UPDATED", null);
        return response(saved);
    }

    @Transactional(readOnly = true)
    public List<SystemSetting> listSystemSettings() {
        return systemSettingRepository.findAllByOrderBySettingKeyAsc();
    }

    @Transactional
    public SystemSetting saveSystemSetting(SystemSettingRequest request, AuthenticatedUser actor) {
        String key = DomainSupport.text(request.settingKey(), "Setting key");
        SystemSetting setting = systemSettingRepository.findById(key).orElseGet(SystemSetting::new);
        setting.setSettingKey(key);
        setting.setSettingValue(DomainSupport.json(request.settingValue(), "Setting value"));
        setting.setUpdatedBy(currentUser(actor));
        SystemSetting saved = systemSettingRepository.save(setting);
        DomainSupport.audit(auditLogRepository, appUserRepository, actor, "SYSTEM_SETTING", null, "UPDATED", null);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<EmailTemplateResponse> listTemplates() {
        return emailTemplateRepository.findAllByOrderByTemplateKeyAsc().stream().map(this::templateResponse).toList();
    }

    @Transactional
    public EmailTemplateResponse createTemplate(EmailTemplateRequest request, AuthenticatedUser actor) {
        String key = DomainSupport.text(request.templateKey(), "Template key");
        if (emailTemplateRepository.existsByTemplateKeyIgnoreCase(key)) throw DomainSupport.conflict("Template key is already in use.");
        EmailTemplate template = new EmailTemplate();
        template.setTemplateKey(key);
        template.setSubjectTemplate(DomainSupport.text(request.subjectTemplate(), "Subject template"));
        template.setBodyTemplate(DomainSupport.text(request.bodyTemplate(), "Body template"));
        if (request.active() != null) template.setActive(request.active());
        template.setUpdatedBy(currentUser(actor));
        EmailTemplate saved = emailTemplateRepository.save(template);
        DomainSupport.audit(auditLogRepository, appUserRepository, actor, "EMAIL_TEMPLATE", saved.getId(), "CREATED", null);
        return templateResponse(saved);
    }

    @Transactional
    public EmailTemplateResponse updateTemplate(java.util.UUID id, EmailTemplateRequest request, AuthenticatedUser actor) {
        EmailTemplate template = emailTemplateRepository.findById(id).orElseThrow(() -> DomainSupport.notFound("Email template"));
        if (StringUtils.hasText(request.templateKey())) {
            String key = request.templateKey().trim();
            if (emailTemplateRepository.existsByTemplateKeyIgnoreCaseAndIdNot(key, id)) throw DomainSupport.conflict("Template key is already in use.");
            template.setTemplateKey(key);
        }
        if (request.subjectTemplate() != null) template.setSubjectTemplate(DomainSupport.text(request.subjectTemplate(), "Subject template"));
        if (request.bodyTemplate() != null) template.setBodyTemplate(DomainSupport.text(request.bodyTemplate(), "Body template"));
        if (request.active() != null) template.setActive(request.active());
        template.setUpdatedBy(currentUser(actor));
        EmailTemplate saved = emailTemplateRepository.save(template);
        DomainSupport.audit(auditLogRepository, appUserRepository, actor, "EMAIL_TEMPLATE", saved.getId(), "UPDATED", null);
        return templateResponse(saved);
    }

    private AppUser currentUser(AuthenticatedUser actor) {
        return actor == null ? null : appUserRepository.getReferenceById(actor.getId());
    }

    private SettingsResponse response(OrganizationSettings settings) {
        return new SettingsResponse(settings.getOrganizationName(), settings.getIndustry(), settings.getPrimaryContact(), settings.getCurrency(), settings.getTimezone(), settings.getBranding(), settings.getNotificationSettings(), settings.getCreatedAt(), settings.getUpdatedAt());
    }

    private EmailTemplateResponse templateResponse(EmailTemplate template) {
        return new EmailTemplateResponse(template.getId(), template.getTemplateKey(), template.getSubjectTemplate(), template.getBodyTemplate(), template.isActive(), template.getCreatedAt(), template.getUpdatedAt());
    }
}
