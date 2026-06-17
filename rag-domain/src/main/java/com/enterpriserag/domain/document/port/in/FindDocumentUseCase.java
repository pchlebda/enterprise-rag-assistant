package com.enterpriserag.domain.document.port.in;

import com.enterpriserag.domain.document.model.Document;
import com.enterpriserag.domain.shared.model.DocumentId;
import com.enterpriserag.domain.shared.model.TenantId;

import java.util.List;

public interface FindDocumentUseCase {
    Document findById(DocumentId id, TenantId tenantId);
    List<Document> findAllByTenant(TenantId tenantId);
}
