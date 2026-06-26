package com.enterpriserag.application.rag;

import com.enterpriserag.domain.rag.model.RetrievedChunk;
import com.enterpriserag.domain.shared.model.DocumentId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PromptAssemblerTest {

    @Test
    void includesChunkContentAndPageNumbers() {
        var chunks = List.of(
                new RetrievedChunk(UUID.randomUUID(), DocumentId.generate(), "report.pdf", 3, "alpha content", 0.9),
                new RetrievedChunk(UUID.randomUUID(), DocumentId.generate(), "report.pdf", 7, "beta content", 0.8)
        );

        var prompt = PromptAssembler.assemble("What is in the report?", chunks);

        assertThat(prompt)
                .contains("page 3")
                .contains("alpha content")
                .contains("page 7")
                .contains("beta content")
                .contains("What is in the report?");
    }

    @Test
    void embedsTheGuardrailSentenceAsAnInstruction() {
        var prompt = PromptAssembler.assemble("question", List.of(
                new RetrievedChunk(UUID.randomUUID(), DocumentId.generate(), "f.pdf", 1, "content", 0.9)
        ));

        assertThat(prompt).contains(PromptAssembler.NO_CONTEXT_ANSWER);
    }
}
