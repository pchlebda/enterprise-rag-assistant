package com.enterpriserag.adapter.out.persistence;

import com.enterpriserag.domain.document.model.DocumentUploadedEvent;
import com.enterpriserag.domain.document.port.out.EventPublisherPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
public class TransactionalOutboxPublisher implements EventPublisherPort {

    private final OutboxEventJpaRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public TransactionalOutboxPublisher(OutboxEventJpaRepository outboxRepository, ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(DocumentUploadedEvent event) {
        var entity = new OutboxEventJpaEntity();
        entity.id = UUID.randomUUID();
        entity.aggregateType = "Document";
        entity.aggregateId = event.documentId().value();
        entity.eventType = "DocumentUploaded";
        entity.tenantId = event.tenantId().value();
        entity.createdAt = Instant.now();
        entity.attempts = 0;
        entity.payload = toJson(event);

        outboxRepository.save(entity);
    }

    private String toJson(DocumentUploadedEvent event) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "documentId", event.documentId().value().toString(),
                    "tenantId", event.tenantId().value().toString(),
                    "filename", event.filename(),
                    "contentType", event.contentType(),
                    "sizeBytes", event.sizeBytes(),
                    "occurredAt", event.occurredAt().toString()
            ));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize DocumentUploadedEvent", e);
        }
    }
}
