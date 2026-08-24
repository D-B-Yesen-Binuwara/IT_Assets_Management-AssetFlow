package com.binuwara.AssetsFlow.Service;

import com.binuwara.AssetsFlow.DTO.NotificationCreateRequest;
import com.binuwara.AssetsFlow.DTO.NotificationPreferenceRequest;
import com.binuwara.AssetsFlow.DTO.NotificationPreferenceResponse;
import com.binuwara.AssetsFlow.DTO.NotificationResponse;
import com.binuwara.AssetsFlow.Entity.AppUser;
import com.binuwara.AssetsFlow.Entity.Notification;
import com.binuwara.AssetsFlow.Entity.NotificationPreference;
import com.binuwara.AssetsFlow.Entity.NotificationPriority;
import com.binuwara.AssetsFlow.Repository.AppUserRepository;
import com.binuwara.AssetsFlow.Repository.AuditLogRepository;
import com.binuwara.AssetsFlow.Repository.NotificationPreferenceRepository;
import com.binuwara.AssetsFlow.Repository.NotificationRepository;
import com.binuwara.AssetsFlow.Security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final AppUserRepository appUserRepository;
    private final AuditLogRepository auditLogRepository;

    public NotificationService(NotificationRepository notificationRepository, NotificationPreferenceRepository preferenceRepository, AppUserRepository appUserRepository, AuditLogRepository auditLogRepository) {
        this.notificationRepository = notificationRepository;
        this.preferenceRepository = preferenceRepository;
        this.appUserRepository = appUserRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> list(String filter, AuthenticatedUser actor) {
        UUID userId = requireActor(actor).getId();
        List<Notification> notifications = "unread".equalsIgnoreCase(filter) ? notificationRepository.findAllByRecipientUser_IdAndReadAtIsNullOrderByCreatedAtDesc(userId) : notificationRepository.findAllByRecipientUser_IdOrderByCreatedAtDesc(userId);
        return notifications.stream().map(this::response).toList();
    }

    @Transactional
    public NotificationResponse create(NotificationCreateRequest request, AuthenticatedUser actor) {
        AppUser recipient = request.recipientUserId() == null ? requireActor(actor) : appUserRepository.findById(request.recipientUserId()).orElseThrow(() -> DomainSupport.notFound("User"));
        Notification notification = new Notification();
        notification.setRecipientUser(recipient);
        notification.setNotificationType(DomainSupport.text(request.notificationType(), "Notification type"));
        notification.setPriority(request.priority() == null ? NotificationPriority.MEDIUM : request.priority());
        notification.setTitle(DomainSupport.text(request.title(), "Notification title"));
        notification.setMessage(DomainSupport.text(request.message(), "Notification message"));
        Notification saved = notificationRepository.save(notification);
        DomainSupport.audit(auditLogRepository, appUserRepository, actor, "NOTIFICATION", saved.getId(), "CREATED", null);
        return response(saved);
    }

    @Transactional
    public NotificationResponse markRead(UUID id, AuthenticatedUser actor) {
        Notification notification = notificationRepository.findById(id).orElseThrow(() -> DomainSupport.notFound("Notification"));
        requireOwned(notification, actor);
        notification.setReadAt(Instant.now());
        return response(notificationRepository.save(notification));
    }

    @Transactional
    public void delete(UUID id, AuthenticatedUser actor) {
        Notification notification = notificationRepository.findById(id).orElseThrow(() -> DomainSupport.notFound("Notification"));
        requireOwned(notification, actor);
        notificationRepository.delete(notification);
    }

    @Transactional(readOnly = true)
    public List<NotificationPreferenceResponse> preferences(AuthenticatedUser actor) {
        requireActor(actor);
        return preferenceRepository.findAllByUser_IdOrderByNotificationTypeAsc(actor.getId()).stream().map(this::preferenceResponse).toList();
    }

    @Transactional
    public NotificationPreferenceResponse savePreference(NotificationPreferenceRequest request, AuthenticatedUser actor) {
        AppUser user = requireActor(actor);
        String type = DomainSupport.text(request.notificationType(), "Notification type");
        NotificationPreference preference = preferenceRepository.findByUser_IdAndNotificationTypeIgnoreCase(user.getId(), type).orElseGet(NotificationPreference::new);
        preference.setUser(user);
        preference.setNotificationType(type);
        if (request.inAppEnabled() != null) preference.setInAppEnabled(request.inAppEnabled());
        if (request.emailEnabled() != null) preference.setEmailEnabled(request.emailEnabled());
        return preferenceResponse(preferenceRepository.save(preference));
    }

    private AppUser requireActor(AuthenticatedUser actor) {
        if (actor == null) throw new com.binuwara.AssetsFlow.Exception.ApiException(HttpStatus.UNAUTHORIZED, "Authentication is required.");
        return appUserRepository.findById(actor.getId()).orElseThrow(() -> new com.binuwara.AssetsFlow.Exception.ApiException(HttpStatus.UNAUTHORIZED, "Account is no longer available."));
    }

    private void requireOwned(Notification notification, AuthenticatedUser actor) {
        if (actor == null || notification.getRecipientUser() == null || !actor.getId().equals(notification.getRecipientUser().getId())) throw new com.binuwara.AssetsFlow.Exception.ApiException(HttpStatus.FORBIDDEN, "You can manage only your own notifications.");
    }

    private NotificationResponse response(Notification notification) {
        return new NotificationResponse(notification.getId(), notification.getNotificationType(), notification.getNotificationType(), notification.getPriority(), notification.getTitle(), notification.getMessage(), notification.getCreatedAt(), notification.getCreatedAt(), notification.getReadAt(), notification.getReadAt() != null);
    }

    private NotificationPreferenceResponse preferenceResponse(NotificationPreference preference) {
        return new NotificationPreferenceResponse(preference.getId(), preference.getNotificationType(), preference.isInAppEnabled(), preference.isEmailEnabled(), preference.getCreatedAt(), preference.getUpdatedAt());
    }
}
