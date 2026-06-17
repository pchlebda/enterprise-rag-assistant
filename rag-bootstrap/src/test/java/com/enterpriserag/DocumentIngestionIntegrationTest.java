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
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class DocumentIngestionIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("pgvector/pgvector:pg16")
                    .asCompatibleSubstituteFor("postgres")
    );

    @Autowired
    TestRestTemplate restTemplate;

    private static final UUID TENANT_ID = UUID.randomUUID();

    @Test
    void uploadPdfReturns202WithDocumentId() {
        var response = uploadPdf("report.pdf");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().documentId()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo("PENDING");
    }

    @Test
    void getDocumentAfterUploadReturnsPendingStatus() {
        var uploadResponse = uploadPdf("summary.pdf");
        assertThat(uploadResponse.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

        var docId = uploadResponse.getBody().documentId();
        var getResponse = restTemplate.exchange(
                "/api/v1/documents/{id}",
                HttpMethod.GET,
                new HttpEntity<>(tenantHeaders()),
                DocumentResponse.class,
                docId
        );

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().status()).isEqualTo("PENDING");
        assertThat(getResponse.getBody().filename()).isEqualTo("summary.pdf");
    }

    @Test
    void listDocumentsReturnsUploadedDocument() {
        uploadPdf("listed.pdf");

        var response = restTemplate.exchange(
                "/api/v1/documents",
                HttpMethod.GET,
                new HttpEntity<>(tenantHeaders()),
                DocumentResponse[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    void uploadNonPdfReturns400() {
        var headers = tenantHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        var body = new LinkedMultiValueMap<String, Object>();
        body.add("file", new NamedByteArrayResource("data.csv", "a,b,c".getBytes(), "text/csv"));

        var response = restTemplate.postForEntity(
                "/api/v1/documents",
                new HttpEntity<>(body, headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<DocumentUploadResponse> uploadPdf(String filename) {
        var headers = tenantHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        var body = new LinkedMultiValueMap<String, Object>();
        body.add("file", new NamedByteArrayResource(filename, "%PDF-1.4 test content".getBytes(), "application/pdf"));

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
        private final String contentType;

        NamedByteArrayResource(String filename, byte[] content, String contentType) {
            super(content);
            this.filename = filename;
            this.contentType = contentType;
        }

        @Override
        public String getFilename() {
            return filename;
        }

        public String getContentType() {
            return contentType;
        }
    }
}
