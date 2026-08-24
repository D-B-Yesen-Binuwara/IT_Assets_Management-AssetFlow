package com.binuwara.AssetsFlow.DTO;

public record EmailTemplateRequest(String templateKey, String subjectTemplate, String bodyTemplate, Boolean active) {
}
