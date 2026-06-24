package com.enterpriserag.application.document;

import com.enterpriserag.domain.document.model.ChunkDraft;
import com.enterpriserag.domain.document.model.Document;
import com.enterpriserag.domain.document.model.DocumentChunk;
import com.enterpriserag.domain.document.model.DocumentStatus;
import com.enterpriserag.domain.document.port.in.IndexDocumentCommand;
import com.enterpriserag.domain.document.port.out.ChunkerPort;
import com.enterpriserag.domain.document.port.out.DocumentChunkRepository;
import com.enterpriserag.domain.document.port.out.DocumentRepository;
import com.enterpriserag.domain.document.port.out.EmbeddingModelPort;
import com.enterpriserag.domain.document.port.out.FileStoragePort;
import com.enterpriserag.domain.shared.exception.DocumentNotFoundException;
import com.enterpriserag.domain.shared.model.DocumentId;
import com.enterpriserag.domain.shared.model.TenantId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentIndexingServiceTest {

    private final DocumentId docId = DocumentId.generate();
    private final TenantId tenantId = TenantId.generate();

    private Document storedDocument;
    private List<DocumentStatus> statusUpdates;
    private List<DocumentId> deletedForDocumentIds;
    private List<DocumentChunk> savedChunks;
    private DocumentId indexedDocumentId;
    private String failureReason;
    private boolean throwOnChunk;

    private DocumentIndexingService service;

    @BeforeEach
    void setUp() {
        statusUpdates = new ArrayList<>();
        deletedForDocumentIds = new ArrayList<>();
        savedChunks = new ArrayList<>();
        indexedDocumentId = null;
        failureReason = null;
        throwOnChunk = false;

        storedDocument = new Document(docId, tenantId, "report.pdf", "application/pdf", 1024, "abc123",
                "file:///tmp/report.pdf", DocumentStatus.PENDING, null, Instant.now(), null);

        DocumentRepository documentRepositoryStub = new DocumentRepository() {
            @Override
            public DocumentId save(Document document) {
                return document.id();
            }

            @Override
            public Optional<Document> findById(DocumentId id, TenantId tenant) {
                if (id.equals(docId) && tenant.equals(tenantId)) return Optional.of(storedDocument);
                return Optional.empty();
            }

            @Override
            public List<Document> findAllByTenant(TenantId tenant) {
                return List.of();
            }

            @Override
            public void updateStatus(DocumentId id, DocumentStatus newStatus) {
                statusUpdates.add(newStatus);
            }

            @Override
            public void markIndexed(DocumentId id, Instant indexedAt) {
                indexedDocumentId = id;
            }

            @Override
            public void markFailed(DocumentId id, String reason) {
                failureReason = reason;
            }
        };

        DocumentChunkRepository chunkRepositoryStub = new DocumentChunkRepository() {
            @Override
            public void saveAll(List<DocumentChunk> chunks) {
                savedChunks.addAll(chunks);
            }

            @Override
            public void deleteByDocumentId(DocumentId id) {
                deletedForDocumentIds.add(id);
            }
        };

        FileStoragePort fileStoragePortStub = new FileStoragePort() {
            @Override
            public String store(TenantId tenant, DocumentId docId, String filename, byte[] content) {
                throw new UnsupportedOperationException("not used by DocumentIndexingService");
            }

            @Override
            public byte[] load(String storageUri) {
                return "%PDF-1.4 fake content".getBytes();
            }
        };

        ChunkerPort chunkerPortStub = (fileContent, contentType) -> {
            if (throwOnChunk) throw new IllegalStateException("boom");
            return List.of(
                    new ChunkDraft(1, "first chunk", 2),
                    new ChunkDraft(1, "second chunk", 2)
            );
        };

        EmbeddingModelPort embeddingModelPortStub = new EmbeddingModelPort() {
            @Override
            public List<float[]> embedAll(List<String> texts) {
                return texts.stream().map(t -> new float[]{1f, 2f, 3f}).toList();
            }

            @Override
            public int dimensions() {
                return 3;
            }

            @Override
            public String modelId() {
                return "stub-model";
            }
        };

        service = new DocumentIndexingService(documentRepositoryStub, chunkRepositoryStub, fileStoragePortStub,
                chunkerPortStub, embeddingModelPortStub);
    }

    @Test
    void indexesDocumentAndSavesChunksWithEmbeddings() {
        service.index(new IndexDocumentCommand(docId, tenantId));

        assertThat(statusUpdates).containsExactly(DocumentStatus.PROCESSING);
        assertThat(deletedForDocumentIds).containsExactly(docId);
        assertThat(savedChunks).hasSize(2);
        assertThat(savedChunks.get(0).chunkIndex()).isEqualTo(0);
        assertThat(savedChunks.get(0).pageNumber()).isEqualTo(1);
        assertThat(savedChunks.get(0).content()).isEqualTo("first chunk");
        assertThat(savedChunks.get(0).embedding()).containsExactly(1f, 2f, 3f);
        assertThat(savedChunks.get(0).embeddingModelId()).isEqualTo("stub-model");
        assertThat(savedChunks.get(1).chunkIndex()).isEqualTo(1);
        assertThat(indexedDocumentId).isEqualTo(docId);
        assertThat(failureReason).isNull();
    }

    @Test
    void isNoOpWhenDocumentAlreadyIndexed() {
        storedDocument = new Document(docId, tenantId, "report.pdf", "application/pdf", 1024, "abc123",
                "file:///tmp/report.pdf", DocumentStatus.INDEXED, null, Instant.now(), Instant.now());

        service.index(new IndexDocumentCommand(docId, tenantId));

        assertThat(statusUpdates).isEmpty();
        assertThat(savedChunks).isEmpty();
        assertThat(deletedForDocumentIds).isEmpty();
    }

    @Test
    void marksFailedAndRethrowsWhenChunkingFails() {
        throwOnChunk = true;

        assertThatThrownBy(() -> service.index(new IndexDocumentCommand(docId, tenantId)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(failureReason).isEqualTo("boom");
        assertThat(indexedDocumentId).isNull();
        assertThat(savedChunks).isEmpty();
    }

    @Test
    void throwsWhenDocumentNotFound() {
        assertThatThrownBy(() -> service.index(new IndexDocumentCommand(DocumentId.generate(), tenantId)))
                .isInstanceOf(DocumentNotFoundException.class);
    }
}
