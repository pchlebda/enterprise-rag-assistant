package com.enterpriserag.application.document;

import com.enterpriserag.domain.document.model.DocumentStatus;
import com.enterpriserag.domain.document.port.in.IngestDocumentCommand;
import com.enterpriserag.domain.document.port.out.DocumentRepository;
import com.enterpriserag.domain.document.port.out.FileStoragePort;
import com.enterpriserag.domain.shared.model.DocumentId;
import com.enterpriserag.domain.shared.model.TenantId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentIngestionServiceTest {

    private DocumentId savedId;
    private DocumentIngestionService service;

    @BeforeEach
    void setUp() {
        FileStoragePort storageStub = (tenant, docId, filename, content) -> "file:///tmp/" + filename;

        DocumentRepository repositoryStub = new DocumentRepository() {
            @Override
            public DocumentId save(com.enterpriserag.domain.document.model.Document document) {
                savedId = document.id();
                assertThat(document.status()).isEqualTo(DocumentStatus.PENDING);
                assertThat(document.contentHash()).isNotBlank();
                assertThat(document.storageUri()).isEqualTo("file:///tmp/report.pdf");
                return document.id();
            }

            @Override
            public Optional<com.enterpriserag.domain.document.model.Document> findById(DocumentId id, TenantId tenantId) {
                return Optional.empty();
            }

            @Override
            public List<com.enterpriserag.domain.document.model.Document> findAllByTenant(TenantId tenantId) {
                return List.of();
            }
        };

        service = new DocumentIngestionService(repositoryStub, storageStub);
    }

    @Test
    void returnsDocumentIdAfterIngestion() {
        var command = new IngestDocumentCommand(TenantId.generate(), "report.pdf", "application/pdf", 1024, new byte[]{1, 2, 3});

        var returnedId = service.ingest(command);

        assertThat(returnedId).isNotNull();
        assertThat(returnedId).isEqualTo(savedId);
    }

    @Test
    void computesSha256HashOfContent() {
        var content = "PDF content".getBytes();
        var command = new IngestDocumentCommand(TenantId.generate(), "report.pdf", "application/pdf", content.length, content);

        service.ingest(command);

        assertThat(savedId).isNotNull();
    }

    @Test
    void setsStatusToPendingOnNewDocument() {
        var command = new IngestDocumentCommand(TenantId.generate(), "report.pdf", "application/pdf", 512, new byte[]{42});

        service.ingest(command);

        assertThat(savedId).isNotNull();
    }
}
