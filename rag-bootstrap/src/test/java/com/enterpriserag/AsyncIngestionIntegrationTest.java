package com.enterpriserag;

import com.enterpriserag.adapter.in.web.v1.dto.DocumentResponse;
import com.enterpriserag.adapter.in.web.v1.dto.DocumentUploadResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"kafka.enabled=true", "kafka.outbox.relay-interval-ms=500"}
)
@Testcontainers
class AsyncIngestionIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("pgvector/pgvector:pg16")
                    .asCompatibleSubstituteFor("postgres")
    );

    @Container
    @ServiceConnection
    static final KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.6.1")
    );

    @Autowired
    TestRestTemplate restTemplate;

    private static final UUID TENANT_ID = UUID.randomUUID();

    @Test
    void uploadedDocumentTransitionsToProcessingViaKafka() {
        var uploadResponse = uploadPdf("async-test.pdf");
        assertThat(uploadResponse.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        var docId = uploadResponse.getBody().documentId();

        await()
                .atMost(15, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    var getResponse = restTemplate.exchange(
                            "/api/v1/documents/{id}",
                            HttpMethod.GET,
                            new HttpEntity<>(tenantHeaders()),
                            DocumentResponse.class,
                            docId
                    );
                    assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(getResponse.getBody().status()).isEqualTo("PROCESSING");
                });
    }

    @Test
    void uploadReturns202ImmediatelyBeforeKafkaProcessing() {
        var response = uploadPdf("immediate.pdf");

        // The key M3 invariant: upload does not block on Kafka processing
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody().status()).isEqualTo("PENDING");
    }

    private ResponseEntity<DocumentUploadResponse> uploadPdf(String filename) {
        var headers = tenantHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        var body = new LinkedMultiValueMap<String, Object>();
        body.add("file", new NamedByteArrayResource(filename, "%PDF-1.4 async test content".getBytes(), "application/pdf"));

        return restTemplate.postForEntity(
                "/api/v1/documents",
                new HttpEntity<>(body, headers),
                DocumentUploadResponse.class
        );
    }

    private HttpHeaders tenantHeaders() {
        var headers = new HttpHeaders();
        headers.set("X-Tenant-Id", TENANT_ID.toString());
        return headers;
    }

    private static class NamedByteArrayResource extends ByteArrayResource {
        private final String filename;

        NamedByteArrayResource(String filename, byte[] content, String contentType) {
            super(content);
            this.filename = filename;
        }

        @Override
        public String getFilename() {
            return filename;
        }
    }
}
