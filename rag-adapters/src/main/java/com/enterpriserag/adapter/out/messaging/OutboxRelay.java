package com.enterpriserag.adapter.out.messaging;

import com.enterpriserag.adapter.out.persistence.OutboxEventFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true")
public class OutboxRelay {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);

    private final OutboxEventFacade outboxFacade;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String documentIngestTopic;

    public OutboxRelay(
            OutboxEventFacade outboxFacade,
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${kafka.topics.document-ingest}") String documentIngestTopic) {
        this.outboxFacade = outboxFacade;
        this.kafkaTemplate = kafkaTemplate;
        this.documentIngestTopic = documentIngestTopic;
    }

    @Scheduled(fixedDelayString = "${kafka.outbox.relay-interval-ms:1000}")
    void relayPendingEvents() {
        var pending = outboxFacade.findPending();
        for (var event : pending) {
            try {
                kafkaTemplate.send(documentIngestTopic, event.aggregateId().toString(), event.payload())
                        .get(5, TimeUnit.SECONDS);
                outboxFacade.markPublished(event.id(), Instant.now());
                log.debug("Relayed outbox event {} to topic {}", event.id(), documentIngestTopic);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Outbox relay interrupted");
                break;
            } catch (Exception e) {
                log.error("Failed to relay outbox event {}: {}", event.id(), e.getMessage());
            }
        }
    }
}
