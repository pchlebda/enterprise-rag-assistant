package com.enterpriserag.application.document;

import com.enterpriserag.domain.document.model.Document;
import com.enterpriserag.domain.document.port.in.FindDocumentUseCase;
import com.enterpriserag.domain.document.port.out.DocumentRepository;
import com.enterpriserag.domain.shared.exception.DocumentNotFoundException;
import com.enterpriserag.domain.shared.model.DocumentId;
import com.enterpriserag.domain.shared.model.TenantId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class DocumentQueryService implements FindDocumentUseCase {

    private final DocumentRepository documentRepository;

    public DocumentQueryService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @Override
    public Document findById(DocumentId id, TenantId tenantId) {
        return documentRepository.findById(id, tenantId)
                .orElseThrow(() -> new DocumentNotFoundException(id));
    }

    @Override
    public List<Document> findAllByTenant(TenantId tenantId) {
        return documentRepository.findAllByTenant(tenantId);
    }
}
