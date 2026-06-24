package com.enterpriserag.domain.document.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChunkDraftTest {

    @Test
    void createsWithValidFields() {
        var draft = new ChunkDraft(1, "some chunk text", 3);

        assertThat(draft.pageNumber()).isEqualTo(1);
        assertThat(draft.content()).isEqualTo("some chunk text");
        assertThat(draft.tokenCount()).isEqualTo(3);
    }

    @Test
    void rejectsNonPositivePageNumber() {
        assertThatThrownBy(() -> new ChunkDraft(0, "text", 3))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsBlankContent() {
        assertThatThrownBy(() -> new ChunkDraft(1, "  ", 3))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNonPositiveTokenCount() {
        assertThatThrownBy(() -> new ChunkDraft(1, "text", 0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
