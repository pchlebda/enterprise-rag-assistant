package com.enterpriserag.domain.rag.model;

import com.enterpriserag.domain.shared.model.DocumentId;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RetrievedChunkTest {

    private final UUID chunkId = UUID.randomUUID();
    private final DocumentId documentId = DocumentId.generate();

    @Test
    void createsWithValidFields() {
        var chunk = new RetrievedChunk(chunkId, documentId, "report.pdf", 3, "some content", 0.42);

        assertThat(chunk.chunkId()).isEqualTo(chunkId);
        assertThat(chunk.documentId()).isEqualTo(documentId);
        assertThat(chunk.filename()).isEqualTo("report.pdf");
        assertThat(chunk.pageNumber()).isEqualTo(3);
        assertThat(chunk.content()).isEqualTo("some content");
        assertThat(chunk.score()).isEqualTo(0.42);
    }

    @Test
    void rejectsNullChunkId() {
        assertThatThrownBy(() -> new RetrievedChunk(null, documentId, "f.pdf", 1, "c", 0.1))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsNullDocumentId() {
        assertThatThrownBy(() -> new RetrievedChunk(chunkId, null, "f.pdf", 1, "c", 0.1))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsBlankFilename() {
        assertThatThrownBy(() -> new RetrievedChunk(chunkId, documentId, " ", 1, "c", 0.1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNonPositivePageNumber() {
        assertThatThrownBy(() -> new RetrievedChunk(chunkId, documentId, "f.pdf", 0, "c", 0.1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsBlankContent() {
        assertThatThrownBy(() -> new RetrievedChunk(chunkId, documentId, "f.pdf", 1, " ", 0.1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
