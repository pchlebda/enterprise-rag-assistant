package com.enterpriserag.domain.document.model;

import com.enterpriserag.domain.shared.model.DocumentId;
import com.enterpriserag.domain.shared.model.TenantId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Note: the {@code embedding} array field gets reference equality from the generated
 * record {@code equals}/{@code hashCode} — compare it with {@code containsExactly}, not
 * whole-record {@code isEqualTo}, in tests.
 */
public record DocumentChunk(
        UUID id,
        DocumentId documentId,
        TenantId tenantId,
        int chunkIndex,
        int pageNumber,
        String content,
        int tokenCount,
        float[] embedding,
        String embeddingModelId,
        Instant createdAt
) {
    public DocumentChunk {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(documentId, "documentId must not be null");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        if (chunkIndex < 0) throw new IllegalArgumentException("chunkIndex must not be negative");
        if (pageNumber <= 0) throw new IllegalArgumentException("pageNumber must be positive");
        if (content == null || content.isBlank()) throw new IllegalArgumentException("content must not be blank");
        if (tokenCount <= 0) throw new IllegalArgumentException("tokenCount must be positive");
        Objects.requireNonNull(embedding, "embedding must not be null");
        if (embedding.length == 0) throw new IllegalArgumentException("embedding must not be empty");
        if (embeddingModelId == null || embeddingModelId.isBlank()) throw new IllegalArgumentException("embeddingModelId must not be blank");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
    }
}
