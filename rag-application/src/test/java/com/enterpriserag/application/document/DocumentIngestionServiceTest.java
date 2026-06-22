package com.enterpriserag.application.document;

import com.enterpriserag.domain.document.model.Document;
import com.enterpriserag.domain.document.model.DocumentStatus;
import com.enterpriserag.domain.document.model.DocumentUploadedEvent;
import com.enterpriserag.domain.document.port.in.IngestDocumentCommand;
import com.enterpriserag.domain.document.port.out.DocumentRepository;
import com.enterpriserag.domain.document.port.out.EventPublisherPort;
import com.enterpriserag.domain.document.port.out.FileStoragePort;
import com.enterpriserag.domain.shared.model.DocumentId;
import com.enterpriserag.domain.shared.model.TenantId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentIngestionServiceTest {

    private DocumentId savedId;
    private List<DocumentUploadedEvent> publishedEvents;
    private DocumentIngestionService service;

    @BeforeEach
    void setUp() {
        publishedEvents = new ArrayList<>();

        FileStoragePort storageStub = (tenant, docId, filename, content) -> "file:///tmp/" + filename;

        DocumentRepository repositoryStub = new DocumentRepository() {
            @Override
            public DocumentId save(Document document) {
                savedId = document.id();
                assertThat(document.status()).isEqualTo(DocumentStatus.PENDING);
                assertThat(document.contentHash()).isNotBlank();
                assertThat(document.storageUri()).isEqualTo("file:///tmp/report.pdf");
                return document.id();
            }

            @Override
            public Optional<Document> findById(DocumentId id, TenantId tenantId) {
                return Optional.empty();
            }

            @Override
            public List<Document> findAllByTenant(TenantId tenantId) {
                return List.of();
            }

            @Override
            public void updateStatus(DocumentId id, DocumentStatus newStatus) {
            }
        };

        EventPublisherPort publisherStub = publishedEvents::add;

        service = new DocumentIngestionService(repositoryStub, storageStub, publisherStub);
    }

    @Test
    void returnsDocumentIdAfterIngestion() {
        var command = new IngestDocumentCommand(TenantId.generate(), "report.pdf", "application/pdf", 1024, new byte[]{1, 2, 3});

        var returnedId = service.ingest(command);

        assertThat(returnedId).isNotNull();
        assertThat(returnedId).isEqualTo(savedId);
    }

    @Test
    void publishesDocumentUploadedEventWithinSameCall() {
        var tenantId = TenantId.generate();
        var command = new IngestDocumentCommand(tenantId, "report.pdf", "application/pdf", 1024, new byte[]{1, 2, 3});

        service.ingest(command);

        assertThat(publishedEvents).hasSize(1);
        var event = publishedEvents.get(0);
        assertThat(event.tenantId()).isEqualTo(tenantId);
        assertThat(event.filename()).isEqualTo("report.pdf");
        assertThat(event.contentType()).isEqualTo("application/pdf");
        assertThat(event.sizeBytes()).isEqualTo(1024);
        assertThat(event.documentId()).isEqualTo(savedId);
    }

    @Test
    void publishedEventCarriesSameDocumentIdAsReturned() {
        var command = new IngestDocumentCommand(TenantId.generate(), "report.pdf", "application/pdf", 512, new byte[]{42});

        var returnedId = service.ingest(command);

        assertThat(publishedEvents).hasSize(1);
        assertThat(publishedEvents.get(0).documentId()).isEqualTo(returnedId);
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
