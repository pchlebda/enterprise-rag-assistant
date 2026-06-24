package com.enterpriserag.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "document_chunks", indexes = {
        @Index(name = "document_chunks_tenant_doc_idx", columnList = "tenant_id, document_id")
})
class DocumentChunkJpaEntity {

    @Id
    UUID id;

    @Column(name = "document_id", nullable = false)
    UUID documentId;

    @Column(name = "tenant_id", nullable = false)
    UUID tenantId;

    @Column(name = "chunk_index", nullable = false)
    int chunkIndex;

    @Column(name = "page_number", nullable = false)
    int pageNumber;

    @Column(nullable = false, columnDefinition = "TEXT")
    String content;

    @Column(name = "token_count", nullable = false)
    int tokenCount;

    @JdbcTypeCode(SqlTypes.VECTOR)
    @Array(length = 384)
    @Column(nullable = false)
    float[] embedding;

    @Column(name = "embedding_model_id", nullable = false, length = 100)
    String embeddingModelId;

    @Column(name = "created_at", nullable = false)
    Instant createdAt;

    protected DocumentChunkJpaEntity() {}
}
