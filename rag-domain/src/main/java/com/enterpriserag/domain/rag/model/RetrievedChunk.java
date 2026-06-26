package com.enterpriserag.domain.rag.model;

import com.enterpriserag.domain.shared.model.DocumentId;

import java.util.Objects;
import java.util.UUID;

public record RetrievedChunk(
        UUID chunkId,
        DocumentId documentId,
        String filename,
        int pageNumber,
        String content,
        double score
) {

    public RetrievedChunk {
        Objects.requireNonNull(chunkId, "chunkId must not be null");
        Objects.requireNonNull(documentId, "documentId must not be null");
        if (filename == null || filename.isBlank()) throw new IllegalArgumentException("filename must not be blank");
        if (pageNumber <= 0) throw new IllegalArgumentException("pageNumber must be positive");
        if (content == null || content.isBlank()) throw new IllegalArgumentException("content must not be blank");
    }
}
