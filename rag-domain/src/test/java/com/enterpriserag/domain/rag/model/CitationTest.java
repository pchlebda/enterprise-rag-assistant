package com.enterpriserag.domain.rag.model;

import com.enterpriserag.domain.shared.model.DocumentId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CitationTest {

    private final DocumentId documentId = DocumentId.generate();

    @Test
    void createsWithValidFields() {
        var citation = new Citation(documentId, "report.pdf", 2, "snippet text", 0.55);

        assertThat(citation.documentId()).isEqualTo(documentId);
        assertThat(citation.filename()).isEqualTo("report.pdf");
        assertThat(citation.pageNumber()).isEqualTo(2);
        assertThat(citation.snippet()).isEqualTo("snippet text");
        assertThat(citation.score()).isEqualTo(0.55);
    }

    @Test
    void rejectsNullDocumentId() {
        assertThatThrownBy(() -> new Citation(null, "f.pdf", 1, "s", 0.1))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsBlankFilename() {
        assertThatThrownBy(() -> new Citation(documentId, " ", 1, "s", 0.1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNonPositivePageNumber() {
        assertThatThrownBy(() -> new Citation(documentId, "f.pdf", 0, "s", 0.1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsBlankSnippet() {
        assertThatThrownBy(() -> new Citation(documentId, "f.pdf", 1, " ", 0.1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
