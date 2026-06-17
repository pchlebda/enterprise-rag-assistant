package com.enterpriserag.adapter.out.persistence;

import com.enterpriserag.domain.document.model.DocumentStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "documents", indexes = {
        @Index(name = "documents_tenant_id_idx", columnList = "tenant_id"),
        @Index(name = "documents_tenant_status_idx", columnList = "tenant_id, status")
})
class DocumentJpaEntity {

    @Id
    UUID id;

    @Column(name = "tenant_id", nullable = false)
    UUID tenantId;

    @Column(nullable = false)
    String filename;

    @Column(name = "content_type", nullable = false)
    String contentType;

    @Column(name = "size_bytes", nullable = false)
    long sizeBytes;

    @Column(name = "content_hash", nullable = false)
    String contentHash;

    @Column(name = "storage_uri", nullable = false, length = 1024)
    String storageUri;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    DocumentStatus status;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    String failureReason;

    @Column(name = "created_at", nullable = false)
    Instant createdAt;

    @Column(name = "indexed_at")
    Instant indexedAt;

    @Version
    int version;

    protected DocumentJpaEntity() {}
}
