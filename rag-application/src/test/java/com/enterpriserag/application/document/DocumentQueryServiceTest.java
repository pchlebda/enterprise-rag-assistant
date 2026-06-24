package com.enterpriserag.application.document;

import com.enterpriserag.domain.document.model.Document;
import com.enterpriserag.domain.document.model.DocumentStatus;
import com.enterpriserag.domain.document.port.out.DocumentRepository;
import com.enterpriserag.domain.shared.exception.DocumentNotFoundException;
import com.enterpriserag.domain.shared.model.DocumentId;
import com.enterpriserag.domain.shared.model.TenantId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentQueryServiceTest {

    private DocumentId docId;
    private TenantId tenantId;
    private Document storedDoc;
    private DocumentQueryService service;

    @BeforeEach
    void setUp() {
        docId = DocumentId.generate();
        tenantId = TenantId.generate();
        storedDoc = new Document(docId, tenantId, "report.pdf", "application/pdf", 1024, "abc123", "file:///tmp/report.pdf", DocumentStatus.PENDING, null, Instant.now(), null);

        DocumentRepository repositoryStub = new DocumentRepository() {
            @Override
            public DocumentId save(Document document) {
                return document.id();
            }

            @Override
            public Optional<Document> findById(DocumentId id, TenantId tenant) {
                if (id.equals(docId) && tenant.equals(tenantId)) return Optional.of(storedDoc);
                return Optional.empty();
            }

            @Override
            public List<Document> findAllByTenant(TenantId tenant) {
                if (tenant.equals(tenantId)) return List.of(storedDoc);
                return List.of();
            }

            @Override
            public void updateStatus(DocumentId id, DocumentStatus newStatus) {
            }

            @Override
            public void markIndexed(DocumentId id, Instant indexedAt) {
            }

            @Override
            public void markFailed(DocumentId id, String reason) {
            }
        };

        service = new DocumentQueryService(repositoryStub);
    }

    @Test
    void findsByIdSuccessfully() {
        var result = service.findById(docId, tenantId);
        assertThat(result).isEqualTo(storedDoc);
    }

    @Test
    void throwsWhenDocumentNotFound() {
        assertThatThrownBy(() -> service.findById(DocumentId.generate(), tenantId))
                .isInstanceOf(DocumentNotFoundException.class);
    }

    @Test
    void returnsAllDocumentsForTenant() {
        var results = service.findAllByTenant(tenantId);
        assertThat(results).hasSize(1).contains(storedDoc);
    }

    @Test
    void returnsEmptyListForUnknownTenant() {
        var results = service.findAllByTenant(TenantId.generate());
        assertThat(results).isEmpty();
    }
}
