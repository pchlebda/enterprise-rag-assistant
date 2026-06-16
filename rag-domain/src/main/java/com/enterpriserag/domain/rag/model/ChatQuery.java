package com.enterpriserag.domain.rag.model;

public record ChatQuery(String userMessage) {

    public ChatQuery {
        if (userMessage == null || userMessage.isBlank()) {
            throw new IllegalArgumentException("ChatQuery message must not be blank");
        }
    }
}
