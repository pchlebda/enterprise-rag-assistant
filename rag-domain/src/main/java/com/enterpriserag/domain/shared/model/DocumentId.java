package com.enterpriserag.domain.shared.model;

import java.util.UUID;

public record DocumentId(UUID value) {

    public DocumentId {
        if (value == null) {
            throw new IllegalArgumentException("DocumentId value must not be null");
        }
    }

    public static DocumentId of(UUID value) {
        return new DocumentId(value);
    }

    public static DocumentId generate() {
        return new DocumentId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
