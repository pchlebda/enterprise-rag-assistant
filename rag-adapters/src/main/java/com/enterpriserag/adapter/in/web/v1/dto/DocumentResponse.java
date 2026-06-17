package com.enterpriserag.adapter.in.web.v1.dto;

import java.time.Instant;
import java.util.UUID;

public record DocumentResponse(
        UUID id,
        String filename,
        String contentType,
        long sizeBytes,
        String status,
        Instant createdAt,
        Instant indexedAt
) {}
