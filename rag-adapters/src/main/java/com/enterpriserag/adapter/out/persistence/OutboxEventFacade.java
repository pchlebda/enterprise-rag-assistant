package com.enterpriserag.adapter.out.persistence;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;


@Component
public class OutboxEventFacade {

    private final OutboxEventJpaRepository outboxRepository;

    public OutboxEventFacade(OutboxEventJpaRepository outboxRepository) {
        this.outboxRepository = outboxRepository;
    }

    public List<PendingOutboxEvent> findPending() {
        return outboxRepository.findTop10ByPublishedAtIsNullOrderByCreatedAtAsc().stream()
                .map(e -> new PendingOutboxEvent(e.id, e.aggregateId, e.payload))
                .toList();
    }

    public void markPublished(UUID id, Instant now) {
        outboxRepository.markPublished(id, now);
    }

    public record PendingOutboxEvent(UUID id, UUID aggregateId, String payload) {}
}
