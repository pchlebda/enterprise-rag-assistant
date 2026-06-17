package com.enterpriserag.adapter.in.web.v1;

import com.enterpriserag.adapter.in.web.v1.dto.DocumentResponse;
import com.enterpriserag.adapter.in.web.v1.dto.DocumentUploadResponse;
import com.enterpriserag.domain.document.model.Document;
import com.enterpriserag.domain.document.port.in.FindDocumentUseCase;
import com.enterpriserag.domain.document.port.in.IngestDocumentCommand;
import com.enterpriserag.domain.document.port.in.IngestDocumentUseCase;
import com.enterpriserag.domain.shared.model.DocumentId;
import com.enterpriserag.domain.shared.model.TenantId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
@Tag(name = "Documents", description = "Document upload and status tracking")
public class DocumentController {

    private static final long MAX_FILE_SIZE_BYTES = 50L * 1024 * 1024;

    private final IngestDocumentUseCase ingestDocumentUseCase;
    private final FindDocumentUseCase findDocumentUseCase;

    public DocumentController(IngestDocumentUseCase ingestDocumentUseCase, FindDocumentUseCase findDocumentUseCase) {
        this.ingestDocumentUseCase = ingestDocumentUseCase;
        this.findDocumentUseCase = findDocumentUseCase;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a PDF document — returns 202 immediately, ingestion is async from M3")
    public ResponseEntity<DocumentUploadResponse> upload(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @RequestPart("file") MultipartFile file) throws IOException {

        validateFile(file);

        var command = new IngestDocumentCommand(
                TenantId.of(tenantId),
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                file.getBytes()
        );

        var documentId = ingestDocumentUseCase.ingest(command);

        return ResponseEntity.accepted()
                .body(new DocumentUploadResponse(documentId.value(), "PENDING"));
    }

    @GetMapping
    @Operation(summary = "List all documents for the tenant")
    public ResponseEntity<List<DocumentResponse>> list(
            @RequestHeader("X-Tenant-Id") UUID tenantId) {

        var docs = findDocumentUseCase.findAllByTenant(TenantId.of(tenantId))
                .stream().map(this::toResponse).toList();

        return ResponseEntity.ok(docs);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get document metadata and ingestion status")
    public ResponseEntity<DocumentResponse> getById(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @PathVariable UUID id) {

        var doc = findDocumentUseCase.findById(DocumentId.of(id), TenantId.of(tenantId));
        return ResponseEntity.ok(toResponse(doc));
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file must not be empty");
        }
        if (!"application/pdf".equals(file.getContentType())) {
            throw new IllegalArgumentException("Only PDF files are accepted (received: " + file.getContentType() + ")");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("File exceeds the 50 MB size limit");
        }
    }

    private DocumentResponse toResponse(Document doc) {
        return new DocumentResponse(
                doc.id().value(),
                doc.filename(),
                doc.contentType(),
                doc.sizeBytes(),
                doc.status().name(),
                doc.createdAt(),
                doc.indexedAt()
        );
    }
}
