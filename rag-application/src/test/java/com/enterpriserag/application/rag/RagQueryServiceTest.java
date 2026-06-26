package com.enterpriserag.application.rag;

import com.enterpriserag.domain.document.port.out.EmbeddingModelPort;
import com.enterpriserag.domain.rag.model.ChatResponse;
import com.enterpriserag.domain.rag.model.Question;
import com.enterpriserag.domain.rag.model.RetrievedChunk;
import com.enterpriserag.domain.rag.port.out.ChatModelPort;
import com.enterpriserag.domain.rag.port.out.RetrievalPort;
import com.enterpriserag.domain.shared.model.DocumentId;
import com.enterpriserag.domain.shared.model.TenantId;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RagQueryServiceTest {

    private final TenantId tenantId = TenantId.generate();

    private final EmbeddingModelPort embeddingModelPortStub = new EmbeddingModelPort() {
        @Override
        public List<float[]> embedAll(List<String> texts) {
            return texts.stream().map(t -> new float[]{1f, 2f, 3f}).toList();
        }

        @Override
        public int dimensions() {
            return 3;
        }

        @Override
        public String modelId() {
            return "stub-embedding-model";
        }
    };

    @Test
    void returnsGroundedAnswerWithCitationsWhenChunksClearMinScore() {
        var relevantChunk = new RetrievedChunk(UUID.randomUUID(), DocumentId.generate(), "report.pdf", 3,
                "the refund window is 30 days", 0.8);
        RetrievalPort retrievalPortStub = (tenant, embedding, topK) -> List.of(relevantChunk);

        var capturedPrompts = new ArrayList<String>();
        ChatModelPort chatModelPortStub = query -> {
            capturedPrompts.add(query.userMessage());
            return new ChatResponse("the refund window is 30 days.", "local-echo");
        };

        var service = new RagQueryService(embeddingModelPortStub, retrievalPortStub, chatModelPortStub, 5, 0.3);

        var answer = service.ask(new Question(tenantId, "What is the refund window?"));

        assertThat(answer.text()).isEqualTo("the refund window is 30 days.");
        assertThat(answer.modelId()).isEqualTo("local-echo");
        assertThat(answer.citations()).hasSize(1);
        assertThat(answer.citations().get(0).documentId()).isEqualTo(relevantChunk.documentId());
        assertThat(answer.citations().get(0).filename()).isEqualTo("report.pdf");
        assertThat(answer.citations().get(0).pageNumber()).isEqualTo(3);
        assertThat(answer.citations().get(0).snippet()).isEqualTo("the refund window is 30 days");
        assertThat(capturedPrompts).hasSize(1);
        assertThat(capturedPrompts.get(0)).contains("the refund window is 30 days");
    }

    @Test
    void returnsNoContextAnswerWithoutCallingChatModelWhenNoChunkClearsMinScore() {
        var weakChunk = new RetrievedChunk(UUID.randomUUID(), DocumentId.generate(), "report.pdf", 1,
                "unrelated content", 0.1);
        RetrievalPort retrievalPortStub = (tenant, embedding, topK) -> List.of(weakChunk);

        ChatModelPort chatModelPortStub = query -> {
            throw new AssertionError("chat model must not be called when no context is grounded");
        };

        var service = new RagQueryService(embeddingModelPortStub, retrievalPortStub, chatModelPortStub, 5, 0.3);

        var answer = service.ask(new Question(tenantId, "What is the refund window?"));

        assertThat(answer.text()).isEqualTo(PromptAssembler.NO_CONTEXT_ANSWER);
        assertThat(answer.citations()).isEmpty();
        assertThat(answer.modelId()).isEqualTo(RagQueryService.NO_CONTEXT_MODEL_ID);
    }

    @Test
    void passesConfiguredTopKToRetrievalPort() {
        var capturedTopK = new int[1];
        RetrievalPort retrievalPortStub = (tenant, embedding, topK) -> {
            capturedTopK[0] = topK;
            return List.of();
        };
        ChatModelPort chatModelPortStub = query -> new ChatResponse("ok", "model");

        var service = new RagQueryService(embeddingModelPortStub, retrievalPortStub, chatModelPortStub, 7, 0.3);
        service.ask(new Question(tenantId, "question"));

        assertThat(capturedTopK[0]).isEqualTo(7);
    }
}
