package com.enterpriserag;

import com.enterpriserag.adapter.in.web.v1.dto.DocumentResponse;
import com.enterpriserag.adapter.in.web.v1.dto.DocumentUploadResponse;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
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

    @Autowired
    JdbcTemplate jdbcTemplate;

    private static final UUID TENANT_ID = UUID.randomUUID();

    @Test
    void uploadedDocumentIsChunkedAndEmbeddedViaKafka() {
        var uploadResponse = uploadPdf("async-test.pdf");
        assertThat(uploadResponse.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        var docId = uploadResponse.getBody().documentId();

        // First use of the local ONNX embedding model loads it from disk, so allow generous time.
        await()
                .atMost(30, TimeUnit.SECONDS)
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
                    assertThat(getResponse.getBody().status()).isEqualTo("INDEXED");
                });

        var chunkRows = jdbcTemplate.queryForList(
                "SELECT embedding_model_id, vector_dims(embedding) AS dims FROM document_chunks WHERE document_id = ?",
                docId
        );
        assertThat(chunkRows).isNotEmpty();
        assertThat(chunkRows).allSatisfy(row -> {
            assertThat(row.get("embedding_model_id")).isEqualTo("local-minilm-l6-v2");
            assertThat(row.get("dims")).isEqualTo(384);
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
        body.add("file", new NamedByteArrayResource(filename, buildPdf(), "application/pdf"));

        return restTemplate.postForEntity(
                "/api/v1/documents",
                new HttpEntity<>(body, headers),
                DocumentUploadResponse.class
        );
    }

    private byte[] buildPdf() {
        try (var document = new PDDocument()) {
            var page = new PDPage();
            document.addPage(page);
            try (var contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(25, 700);
                contentStream.showText("This is a minimal real PDF used for async ingestion integration testing.");
                contentStream.endText();
            }
            var out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
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
