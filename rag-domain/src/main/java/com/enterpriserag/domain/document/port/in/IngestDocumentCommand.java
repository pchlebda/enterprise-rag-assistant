package com.enterpriserag.domain.document.port.in;

import com.enterpriserag.domain.shared.model.TenantId;

import java.util.Objects;

public record IngestDocumentCommand(
        TenantId tenantId,
        String filename,
        String contentType,
        long sizeBytes,
        byte[] content
) {
    public IngestDocumentCommand {
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        if (filename == null || filename.isBlank()) throw new IllegalArgumentException("filename must not be blank");
        if (contentType == null || contentType.isBlank()) throw new IllegalArgumentException("contentType must not be blank");
        if (sizeBytes <= 0) throw new IllegalArgumentException("sizeBytes must be positive");
        Objects.requireNonNull(content, "content must not be null");
    }
}
