package com.enterpriserag.domain.shared.exception;

import com.enterpriserag.domain.shared.model.DocumentId;

public class DocumentNotFoundException extends RagException {

    public DocumentNotFoundException(DocumentId id) {
        super("Document not found: " + id.value());
    }
}
