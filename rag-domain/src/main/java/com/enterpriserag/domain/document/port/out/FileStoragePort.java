package com.enterpriserag.domain.document.port.out;

import com.enterpriserag.domain.shared.model.DocumentId;
import com.enterpriserag.domain.shared.model.TenantId;

public interface FileStoragePort {
    String store(TenantId tenantId, DocumentId documentId, String filename, byte[] content);
    byte[] load(String storageUri);
}
