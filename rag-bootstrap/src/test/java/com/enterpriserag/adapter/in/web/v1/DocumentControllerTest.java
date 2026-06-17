package com.enterpriserag.adapter.in.web.v1;

import com.enterpriserag.domain.document.model.Document;
import com.enterpriserag.domain.document.model.DocumentStatus;
import com.enterpriserag.domain.document.port.in.FindDocumentUseCase;
import com.enterpriserag.domain.document.port.in.IngestDocumentUseCase;
import com.enterpriserag.domain.shared.exception.DocumentNotFoundException;
import com.enterpriserag.domain.shared.model.DocumentId;
import com.enterpriserag.domain.shared.model.TenantId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocumentController.class)
class DocumentControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    IngestDocumentUseCase ingestDocumentUseCase;

    @MockBean
    FindDocumentUseCase findDocumentUseCase;

    private static final UUID TENANT_ID = UUID.randomUUID();

    @Test
    void uploadValidPdfReturns202WithDocumentId() throws Exception {
        var docId = DocumentId.generate();
        when(ingestDocumentUseCase.ingest(any())).thenReturn(docId);

        mockMvc.perform(multipart("/api/v1/documents")
                        .file(new MockMultipartFile("file", "report.pdf", "application/pdf", "PDF content".getBytes()))
                        .header("X-Tenant-Id", TENANT_ID))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.documentId").value(docId.value().toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void uploadNonPdfReturns400() throws Exception {
        mockMvc.perform(multipart("/api/v1/documents")
                        .file(new MockMultipartFile("file", "data.csv", "text/csv", "a,b,c".getBytes()))
                        .header("X-Tenant-Id", TENANT_ID))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadEmptyFileReturns400() throws Exception {
        mockMvc.perform(multipart("/api/v1/documents")
                        .file(new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]))
                        .header("X-Tenant-Id", TENANT_ID))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadMissingTenantHeaderReturns400() throws Exception {
        mockMvc.perform(multipart("/api/v1/documents")
                        .file(new MockMultipartFile("file", "report.pdf", "application/pdf", "PDF".getBytes())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listDocumentsReturns200WithEmptyArray() throws Exception {
        when(findDocumentUseCase.findAllByTenant(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/documents")
                        .header("X-Tenant-Id", TENANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void listDocumentsReturnsDocumentEntries() throws Exception {
        var doc = sampleDocument();
        when(findDocumentUseCase.findAllByTenant(any())).thenReturn(List.of(doc));

        mockMvc.perform(get("/api/v1/documents")
                        .header("X-Tenant-Id", TENANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].filename").value("report.pdf"))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void getDocumentByIdReturns200() throws Exception {
        var doc = sampleDocument();
        when(findDocumentUseCase.findById(any(), any())).thenReturn(doc);

        mockMvc.perform(get("/api/v1/documents/{id}", doc.id().value())
                        .header("X-Tenant-Id", TENANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(doc.id().value().toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getDocumentByIdReturns404WhenNotFound() throws Exception {
        when(findDocumentUseCase.findById(any(), any()))
                .thenThrow(new DocumentNotFoundException(DocumentId.generate()));

        mockMvc.perform(get("/api/v1/documents/{id}", UUID.randomUUID())
                        .header("X-Tenant-Id", TENANT_ID))
                .andExpect(status().isNotFound());
    }

    private Document sampleDocument() {
        return new Document(
                DocumentId.generate(),
                TenantId.of(TENANT_ID),
                "report.pdf",
                "application/pdf",
                2048,
                "abc123hash",
                "file:///tmp/report.pdf",
                DocumentStatus.PENDING,
                null,
                Instant.now(),
                null
        );
    }
}
