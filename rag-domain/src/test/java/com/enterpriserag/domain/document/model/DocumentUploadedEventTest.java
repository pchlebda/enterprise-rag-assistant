package com.enterpriserag.domain.document.model;

import com.enterpriserag.domain.shared.model.DocumentId;
import com.enterpriserag.domain.shared.model.TenantId;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentUploadedEventTest {

    private final DocumentId docId = DocumentId.generate();
    private final TenantId tenantId = TenantId.generate();
    private final Instant now = Instant.now();

    @Test
    void createsWithValidFields() {
        var event = new DocumentUploadedEvent(docId, tenantId, "report.pdf", "application/pdf", 1024, now);

        assertThat(event.documentId()).isEqualTo(docId);
        assertThat(event.tenantId()).isEqualTo(tenantId);
        assertThat(event.filename()).isEqualTo("report.pdf");
        assertThat(event.contentType()).isEqualTo("application/pdf");
        assertThat(event.sizeBytes()).isEqualTo(1024);
        assertThat(event.occurredAt()).isEqualTo(now);
    }

    @Test
    void rejectsNullDocumentId() {
        assertThatThrownBy(() -> new DocumentUploadedEvent(null, tenantId, "f.pdf", "application/pdf", 1, now))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsNullTenantId() {
        assertThatThrownBy(() -> new DocumentUploadedEvent(docId, null, "f.pdf", "application/pdf", 1, now))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsBlankFilename() {
        assertThatThrownBy(() -> new DocumentUploadedEvent(docId, tenantId, "  ", "application/pdf", 1, now))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsZeroSizeBytes() {
        assertThatThrownBy(() -> new DocumentUploadedEvent(docId, tenantId, "f.pdf", "application/pdf", 0, now))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void recordEqualityIsValueBased() {
        var a = new DocumentUploadedEvent(docId, tenantId, "f.pdf", "application/pdf", 1, now);
        var b = new DocumentUploadedEvent(docId, tenantId, "f.pdf", "application/pdf", 1, now);
        assertThat(a).isEqualTo(b);
    }
}
