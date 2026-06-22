package com.enterpriserag.adapter.out.persistence;

import com.enterpriserag.domain.document.model.Document;
import com.enterpriserag.domain.document.model.DocumentStatus;
import com.enterpriserag.domain.document.port.out.DocumentRepository;
import com.enterpriserag.domain.shared.model.DocumentId;
import com.enterpriserag.domain.shared.model.TenantId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class DocumentPersistenceAdapter implements DocumentRepository {

    private final DocumentJpaRepository jpaRepository;

    public DocumentPersistenceAdapter(DocumentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public DocumentId save(Document document) {
        jpaRepository.save(toEntity(document));
        return document.id();
    }

    @Override
    public Optional<Document> findById(DocumentId id, TenantId tenantId) {
        return jpaRepository.findByIdAndTenantId(id.value(), tenantId.value())
                .map(this::toDomain);
    }

    @Override
    public List<Document> findAllByTenant(TenantId tenantId) {
        return jpaRepository.findAllByTenantId(tenantId.value()).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void updateStatus(DocumentId id, DocumentStatus newStatus) {
        jpaRepository.updateStatusById(id.value(), newStatus);
    }

    private Document toDomain(DocumentJpaEntity e) {
        return new Document(
                DocumentId.of(e.id),
                TenantId.of(e.tenantId),
                e.filename,
                e.contentType,
                e.sizeBytes,
                e.contentHash,
                e.storageUri,
                e.status,
                e.failureReason,
                e.createdAt,
                e.indexedAt
        );
    }

    private DocumentJpaEntity toEntity(Document d) {
        var e = new DocumentJpaEntity();
        e.id = d.id().value();
        e.tenantId = d.tenantId().value();
        e.filename = d.filename();
        e.contentType = d.contentType();
        e.sizeBytes = d.sizeBytes();
        e.contentHash = d.contentHash();
        e.storageUri = d.storageUri();
        e.status = d.status();
        e.failureReason = d.failureReason();
        e.createdAt = d.createdAt();
        e.indexedAt = d.indexedAt();
        return e;
    }
}
