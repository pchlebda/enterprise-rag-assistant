package com.enterpriserag.domain.document.port.in;

import com.enterpriserag.domain.shared.model.DocumentId;

public interface IngestDocumentUseCase {
    DocumentId ingest(IngestDocumentCommand command);
}
