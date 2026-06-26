package com.enterpriserag.domain.rag.model;

import com.enterpriserag.domain.shared.model.DocumentId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AnswerTest {

    private final Citation citation = new Citation(DocumentId.generate(), "report.pdf", 1, "snippet", 0.6);

    @Test
    void createsWithValidFields() {
        var answer = new Answer("the answer", List.of(citation), "local-echo");

        assertThat(answer.text()).isEqualTo("the answer");
        assertThat(answer.citations()).containsExactly(citation);
        assertThat(answer.modelId()).isEqualTo("local-echo");
    }

    @Test
    void allowsEmptyCitations() {
        var answer = new Answer("no context found", List.of(), "no-context");

        assertThat(answer.citations()).isEmpty();
    }

    @Test
    void defensivelyCopiesCitationsList() {
        var mutable = new java.util.ArrayList<Citation>();
        mutable.add(citation);
        var answer = new Answer("text", mutable, "model");

        mutable.add(citation);

        assertThat(answer.citations()).hasSize(1);
    }

    @Test
    void rejectsBlankText() {
        assertThatThrownBy(() -> new Answer(" ", List.of(), "model"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNullCitations() {
        assertThatThrownBy(() -> new Answer("text", null, "model"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsBlankModelId() {
        assertThatThrownBy(() -> new Answer("text", List.of(), " "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
