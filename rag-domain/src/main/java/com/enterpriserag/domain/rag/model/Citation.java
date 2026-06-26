package com.enterpriserag.domain.rag.model;

import com.enterpriserag.domain.shared.model.DocumentId;

import java.util.Objects;

public record Citation(
        DocumentId documentId,
        String filename,
        int pageNumber,
        String snippet,
        double score
) {

    public Citation {
        Objects.requireNonNull(documentId, "documentId must not be null");
        if (filename == null || filename.isBlank()) throw new IllegalArgumentException("filename must not be blank");
        if (pageNumber <= 0) throw new IllegalArgumentException("pageNumber must be positive");
        if (snippet == null || snippet.isBlank()) throw new IllegalArgumentException("snippet must not be blank");
    }
}
