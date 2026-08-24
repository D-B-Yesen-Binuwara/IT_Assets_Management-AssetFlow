package com.binuwara.AssetsFlow.Service;

import com.binuwara.AssetsFlow.Entity.AppUser;
import com.binuwara.AssetsFlow.Entity.AuditLog;
import com.binuwara.AssetsFlow.Exception.ApiException;
import com.binuwara.AssetsFlow.Repository.AppUserRepository;
import com.binuwara.AssetsFlow.Repository.AuditLogRepository;
import com.binuwara.AssetsFlow.Security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

final class DomainSupport {
    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

    private DomainSupport() {
    }

    static String text(String value, String field) {
        if (!StringUtils.hasText(value)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, field + " is required.");
        }
        return value.trim();
    }

    static String optionalText(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    static UUID required(UUID value, String field) {
        if (value == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, field + " is required.");
        }
        return value;
    }

    static String currency(String value) {
        if (!StringUtils.hasText(value)) {
            return "USD";
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (!normalized.matches("[A-Z]{3}")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Currency must be a three-letter ISO code.");
        }
        return normalized;
    }

    static String json(String value, String field) {
        String normalized = StringUtils.hasText(value) ? value.trim() : "{}";
        try {
            JSON_MAPPER.readTree(normalized);
            return normalized;
        } catch (Exception exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, field + " must contain valid JSON.");
        }
    }

    static String firstText(String first, String second) {
        return StringUtils.hasText(first) ? first.trim() : optionalText(second);
    }

    static <E extends Enum<E>> E enumValue(String value, Class<E> enumType, E fallback) {
        if (!StringUtils.hasText(value)) {
            return fallback;
        }
        String normalized = value.trim()
                .toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
        try {
            return Enum.valueOf(enumType, normalized);
        } catch (IllegalArgumentException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Unsupported " + enumType.getSimpleName() + ": " + value);
        }
    }

    static ApiException notFound(String resource) {
        return new ApiException(HttpStatus.NOT_FOUND, resource + " was not found.");
    }

    static ApiException conflict(String message) {
        return new ApiException(HttpStatus.CONFLICT, message);
    }

    static void audit(
            AuditLogRepository auditLogRepository,
            AppUserRepository appUserRepository,
            AuthenticatedUser actor,
            String entityType,
            UUID entityId,
            String action,
            String newValues
    ) {
        AuditLog auditLog = new AuditLog();
        if (actor != null) {
            AppUser actorReference = appUserRepository.getReferenceById(actor.getId());
            auditLog.setActorUser(actorReference);
        }
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setAction(action);
        auditLog.setNewValues(newValues);
        auditLogRepository.save(auditLog);
    }

    static String fullName(String firstName, String lastName) {
        return String.join(" ", firstName == null ? "" : firstName, lastName == null ? "" : lastName).trim();
    }

    static Instant instantOrNow(Instant instant) {
        return instant == null ? Instant.now() : instant;
    }
}
