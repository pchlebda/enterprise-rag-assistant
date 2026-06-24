package com.enterpriserag.domain.document.model;

import com.enterpriserag.domain.shared.model.DocumentId;
import com.enterpriserag.domain.shared.model.TenantId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentChunkTest {

    private final DocumentId docId = DocumentId.generate();
    private final TenantId tenantId = TenantId.generate();
    private final Instant now = Instant.now();

    @Test
    void createsWithValidFields() {
        var embedding = new float[]{0.1f, 0.2f, 0.3f};
        var chunk = new DocumentChunk(UUID.randomUUID(), docId, tenantId, 0, 1, "chunk text", 2, embedding, "local-minilm-l6-v2", now);

        assertThat(chunk.documentId()).isEqualTo(docId);
        assertThat(chunk.tenantId()).isEqualTo(tenantId);
        assertThat(chunk.chunkIndex()).isEqualTo(0);
        assertThat(chunk.pageNumber()).isEqualTo(1);
        assertThat(chunk.content()).isEqualTo("chunk text");
        assertThat(chunk.embedding()).containsExactly(0.1f, 0.2f, 0.3f);
        assertThat(chunk.embeddingModelId()).isEqualTo("local-minilm-l6-v2");
    }

    @Test
    void rejectsNegativeChunkIndex() {
        assertThatThrownBy(() -> new DocumentChunk(UUID.randomUUID(), docId, tenantId, -1, 1, "text", 2, new float[]{0.1f}, "model", now))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsEmptyEmbedding() {
        assertThatThrownBy(() -> new DocumentChunk(UUID.randomUUID(), docId, tenantId, 0, 1, "text", 2, new float[]{}, "model", now))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsBlankEmbeddingModelId() {
        assertThatThrownBy(() -> new DocumentChunk(UUID.randomUUID(), docId, tenantId, 0, 1, "text", 2, new float[]{0.1f}, "  ", now))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsBlankContent() {
        assertThatThrownBy(() -> new DocumentChunk(UUID.randomUUID(), docId, tenantId, 0, 1, " ", 2, new float[]{0.1f}, "model", now))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
