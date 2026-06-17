package com.enterpriserag.domain.document.model;

import com.enterpriserag.domain.shared.model.DocumentId;
import com.enterpriserag.domain.shared.model.TenantId;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentTest {

    private static final DocumentId ID = DocumentId.generate();
    private static final TenantId TENANT = TenantId.generate();
    private static final Instant NOW = Instant.now();

    @Test
    void createsWithValidFields() {
        var doc = validDocument();
        assertThat(doc.filename()).isEqualTo("report.pdf");
        assertThat(doc.status()).isEqualTo(DocumentStatus.PENDING);
        assertThat(doc.failureReason()).isNull();
        assertThat(doc.indexedAt()).isNull();
    }

    @Test
    void rejectsNullId() {
        assertThatThrownBy(() -> new Document(null, TENANT, "f.pdf", "application/pdf", 1024, "hash", "uri", DocumentStatus.PENDING, null, NOW, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsNullTenant() {
        assertThatThrownBy(() -> new Document(ID, null, "f.pdf", "application/pdf", 1024, "hash", "uri", DocumentStatus.PENDING, null, NOW, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsBlankFilename() {
        assertThatThrownBy(() -> new Document(ID, TENANT, "  ", "application/pdf", 1024, "hash", "uri", DocumentStatus.PENDING, null, NOW, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsZeroSizeBytes() {
        assertThatThrownBy(() -> new Document(ID, TENANT, "f.pdf", "application/pdf", 0, "hash", "uri", DocumentStatus.PENDING, null, NOW, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsBlankContentHash() {
        assertThatThrownBy(() -> new Document(ID, TENANT, "f.pdf", "application/pdf", 1024, "", "uri", DocumentStatus.PENDING, null, NOW, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void recordEqualityIsValueBased() {
        var a = validDocument();
        var b = validDocument();
        assertThat(a).isEqualTo(b);
    }

    private Document validDocument() {
        return new Document(ID, TENANT, "report.pdf", "application/pdf", 2048, "abc123hash", "file:///tmp/report.pdf", DocumentStatus.PENDING, null, NOW, null);
    }
}
