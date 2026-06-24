package com.enterpriserag.domain.document.model;

public record ChunkDraft(
        int pageNumber,
        String content,
        int tokenCount
) {
    public ChunkDraft {
        if (pageNumber <= 0) throw new IllegalArgumentException("pageNumber must be positive");
        if (content == null || content.isBlank()) throw new IllegalArgumentException("content must not be blank");
        if (tokenCount <= 0) throw new IllegalArgumentException("tokenCount must be positive");
    }
}
