package com.enterpriserag.domain.document.port.in;

import com.enterpriserag.domain.shared.model.DocumentId;
import com.enterpriserag.domain.shared.model.TenantId;

import java.util.Objects;

public record IndexDocumentCommand(
        DocumentId documentId,
        TenantId tenantId
) {
    public IndexDocumentCommand {
        Objects.requireNonNull(documentId, "documentId must not be null");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
    }
}
