package com.enterpriserag.adapter.in.messaging;

import com.enterpriserag.domain.document.model.DocumentStatus;
import com.enterpriserag.domain.document.port.out.DocumentRepository;
import com.enterpriserag.domain.shared.model.DocumentId;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true")
public class KafkaDocumentIngestionConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaDocumentIngestionConsumer.class);
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final DocumentRepository documentRepository;
    private final ObjectMapper objectMapper;

    public KafkaDocumentIngestionConsumer(DocumentRepository documentRepository, ObjectMapper objectMapper) {
        this.documentRepository = documentRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${kafka.topics.document-ingest}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(String payload) {
        try {
            Map<String, Object> event = objectMapper.readValue(payload, MAP_TYPE);
            var documentId = DocumentId.of(UUID.fromString((String) event.get("documentId")));
            var filename = (String) event.get("filename");

            log.info("Ingestion worker received DocumentUploaded event for document {} ({})", documentId, filename);

            documentRepository.updateStatus(documentId, DocumentStatus.PROCESSING);

            log.debug("Document {} status set to PROCESSING", documentId);
        } catch (Exception e) {
            log.error("Failed to process ingestion event: {}", e.getMessage(), e);
            throw new IllegalStateException("Ingestion event processing failed", e);
        }
    }
}
