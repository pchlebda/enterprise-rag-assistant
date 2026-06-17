package com.enterpriserag.domain.document.model;

import com.enterpriserag.domain.shared.model.DocumentId;
import com.enterpriserag.domain.shared.model.TenantId;

import java.time.Instant;
import java.util.Objects;

public record Document(
        DocumentId id,
        TenantId tenantId,
        String filename,
        String contentType,
        long sizeBytes,
        String contentHash,
        String storageUri,
        DocumentStatus status,
        String failureReason,
        Instant createdAt,
        Instant indexedAt
) {
    public Document {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        if (filename == null || filename.isBlank()) throw new IllegalArgumentException("filename must not be blank");
        if (contentType == null || contentType.isBlank()) throw new IllegalArgumentException("contentType must not be blank");
        if (sizeBytes <= 0) throw new IllegalArgumentException("sizeBytes must be positive");
        if (contentHash == null || contentHash.isBlank()) throw new IllegalArgumentException("contentHash must not be blank");
        if (storageUri == null || storageUri.isBlank()) throw new IllegalArgumentException("storageUri must not be blank");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
    }
}
