package com.enterpriserag.domain.rag.model;

import com.enterpriserag.domain.shared.model.TenantId;

import java.util.Objects;

public record Question(TenantId tenantId, String text) {

    public Question {
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        if (text == null || text.isBlank()) throw new IllegalArgumentException("text must not be blank");
    }
}
