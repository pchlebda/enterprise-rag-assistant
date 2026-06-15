package com.enterpriserag.domain.shared.model;

import java.util.UUID;

public record TenantId(UUID value) {

    public TenantId {
        if (value == null) {
            throw new IllegalArgumentException("TenantId value must not be null");
        }
    }

    public static TenantId of(UUID value) {
        return new TenantId(value);
    }

    public static TenantId generate() {
        return new TenantId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
