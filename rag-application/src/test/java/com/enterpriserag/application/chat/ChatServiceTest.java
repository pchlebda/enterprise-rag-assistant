package com.enterpriserag.application.chat;

import com.enterpriserag.domain.rag.model.ChatQuery;
import com.enterpriserag.domain.rag.model.ChatResponse;
import com.enterpriserag.domain.rag.port.out.ChatModelPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChatServiceTest {

    private ChatService service;

    @BeforeEach
    void setUp() {
        ChatModelPort stub = query -> new ChatResponse("stubbed answer", "stub-model");
        service = new ChatService(stub);
    }

    @Test
    void delegatesQueryToPortAndReturnsItsResponse() {
        var response = service.ask(new ChatQuery("What is RAG?"));

        assertThat(response.answer()).isEqualTo("stubbed answer");
        assertThat(response.modelId()).isEqualTo("stub-model");
    }

    @Test
    void passesQueryUnchangedToPort() {
        var captured = new ChatQuery[1];
        ChatModelPort capturingStub = q -> {
            captured[0] = q;
            return new ChatResponse("ok", "stub");
        };
        new ChatService(capturingStub).ask(new ChatQuery("hello"));

        assertThat(captured[0].userMessage()).isEqualTo("hello");
    }
}
