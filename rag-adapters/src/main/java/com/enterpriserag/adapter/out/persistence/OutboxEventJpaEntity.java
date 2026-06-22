package com.enterpriserag.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
class OutboxEventJpaEntity {

    @Id
    UUID id;

    @Column(name = "aggregate_type", nullable = false, length = 100)
    String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    UUID aggregateId;

    @Column(name = "event_type", nullable = false, length = 100)
    String eventType;

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    String payload;

    @Column(name = "tenant_id", nullable = false)
    UUID tenantId;

    @Column(name = "created_at", nullable = false)
    Instant createdAt;

    @Column(name = "published_at")
    Instant publishedAt;

    @Column(name = "attempts", nullable = false)
    int attempts;

    protected OutboxEventJpaEntity() {}
}
