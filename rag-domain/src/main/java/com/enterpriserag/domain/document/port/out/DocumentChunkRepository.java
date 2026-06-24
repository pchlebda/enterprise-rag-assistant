package com.enterpriserag.domain.document.port.out;

import com.enterpriserag.domain.document.model.DocumentChunk;
import com.enterpriserag.domain.shared.model.DocumentId;

import java.util.List;

public interface DocumentChunkRepository {
    void saveAll(List<DocumentChunk> chunks);
    void deleteByDocumentId(DocumentId id);
}
