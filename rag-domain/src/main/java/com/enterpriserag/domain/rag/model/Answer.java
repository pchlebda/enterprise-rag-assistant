package com.enterpriserag.domain.rag.model;

import java.util.List;
import java.util.Objects;

public record Answer(String text, List<Citation> citations, String modelId) {

    public Answer {
        if (text == null || text.isBlank()) throw new IllegalArgumentException("text must not be blank");
        Objects.requireNonNull(citations, "citations must not be null");
        citations = List.copyOf(citations);
        if (modelId == null || modelId.isBlank()) throw new IllegalArgumentException("modelId must not be blank");
    }
}
