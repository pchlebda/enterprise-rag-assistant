package com.enterpriserag;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ChatEndpointIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("pgvector/pgvector:pg16")
                    .asCompatibleSubstituteFor("postgres")
    );

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void chatQueryReturnsEchoResponseFromLocalAdapter() {
        var request = Map.of("message", "What is RAG?");

        var response = restTemplate.postForEntity("/api/v1/chat/query", request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsKey("answer");
        assertThat(response.getBody()).containsKey("modelId");
        assertThat((String) response.getBody().get("answer")).contains("What is RAG?");
        assertThat(response.getBody().get("modelId")).isEqualTo("local-echo");
    }

    @Test
    void blankMessageReturns400() {
        var request = Map.of("message", "");

        var response = restTemplate.postForEntity("/api/v1/chat/query", request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
