package com.enterpriserag.domain.document.port.out;

import com.enterpriserag.domain.document.model.Document;
import com.enterpriserag.domain.document.model.DocumentStatus;
import com.enterpriserag.domain.shared.model.DocumentId;
import com.enterpriserag.domain.shared.model.TenantId;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface DocumentRepository {
    DocumentId save(Document document);
    Optional<Document> findById(DocumentId id, TenantId tenantId);
    List<Document> findAllByTenant(TenantId tenantId);
    void updateStatus(DocumentId id, DocumentStatus newStatus);
    void markIndexed(DocumentId id, Instant indexedAt);
    void markFailed(DocumentId id, String reason);
}
