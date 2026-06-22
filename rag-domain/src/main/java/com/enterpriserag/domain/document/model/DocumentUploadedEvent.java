package com.enterpriserag.domain.document.model;

import com.enterpriserag.domain.shared.model.DocumentId;
import com.enterpriserag.domain.shared.model.TenantId;

import java.time.Instant;
import java.util.Objects;

public record DocumentUploadedEvent(
        DocumentId documentId,
        TenantId tenantId,
        String filename,
        String contentType,
        long sizeBytes,
        Instant occurredAt
) {
    public DocumentUploadedEvent {
        Objects.requireNonNull(documentId, "documentId must not be null");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        if (filename == null || filename.isBlank()) throw new IllegalArgumentException("filename must not be blank");
        if (contentType == null || contentType.isBlank()) throw new IllegalArgumentException("contentType must not be blank");
        if (sizeBytes <= 0) throw new IllegalArgumentException("sizeBytes must be positive");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
    }
}
