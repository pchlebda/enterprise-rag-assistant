package com.enterpriserag.domain.rag.model;

public record ChatResponse(String answer, String modelId) {

    public ChatResponse {
        if (answer == null) {
            throw new IllegalArgumentException("ChatResponse answer must not be null");
        }
        if (modelId == null || modelId.isBlank()) {
            throw new IllegalArgumentException("ChatResponse modelId must not be blank");
        }
    }
}
