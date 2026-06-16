package com.enterpriserag.domain.rag.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChatResponseTest {

    @Test
    void createsWithValidFields() {
        var response = new ChatResponse("RAG stands for...", "gpt-4o-mini");
        assertThat(response.answer()).isEqualTo("RAG stands for...");
        assertThat(response.modelId()).isEqualTo("gpt-4o-mini");
    }

    @Test
    void rejectsNullAnswer() {
        assertThatThrownBy(() -> new ChatResponse(null, "model"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsBlankModelId() {
        assertThatThrownBy(() -> new ChatResponse("answer", "  "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
