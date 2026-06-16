package com.enterpriserag.domain.rag.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChatQueryTest {

    @Test
    void createsWithValidMessage() {
        var query = new ChatQuery("What is RAG?");
        assertThat(query.userMessage()).isEqualTo("What is RAG?");
    }

    @Test
    void rejectsNullMessage() {
        assertThatThrownBy(() -> new ChatQuery(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsBlankMessage() {
        assertThatThrownBy(() -> new ChatQuery("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void recordEqualityIsValueBased() {
        assertThat(new ChatQuery("hello")).isEqualTo(new ChatQuery("hello"));
    }
}
