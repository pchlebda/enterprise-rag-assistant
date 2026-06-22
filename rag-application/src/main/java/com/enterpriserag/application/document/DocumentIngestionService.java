package com.enterpriserag.application.document;

import com.enterpriserag.domain.document.model.Document;
import com.enterpriserag.domain.document.model.DocumentStatus;
import com.enterpriserag.domain.document.model.DocumentUploadedEvent;
import com.enterpriserag.domain.document.port.in.IngestDocumentCommand;
import com.enterpriserag.domain.document.port.in.IngestDocumentUseCase;
import com.enterpriserag.domain.document.port.out.DocumentRepository;
import com.enterpriserag.domain.document.port.out.EventPublisherPort;
import com.enterpriserag.domain.document.port.out.FileStoragePort;
import com.enterpriserag.domain.shared.model.DocumentId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

@Service
public class DocumentIngestionService implements IngestDocumentUseCase {

    private final DocumentRepository documentRepository;
    private final FileStoragePort fileStoragePort;
    private final EventPublisherPort eventPublisherPort;

    public DocumentIngestionService(
            DocumentRepository documentRepository,
            FileStoragePort fileStoragePort,
            EventPublisherPort eventPublisherPort) {
        this.documentRepository = documentRepository;
        this.fileStoragePort = fileStoragePort;
        this.eventPublisherPort = eventPublisherPort;
    }

    @Override
    @Transactional
    public DocumentId ingest(IngestDocumentCommand command) {
        var documentId = DocumentId.generate();
        var contentHash = sha256Hex(command.content());
        var storageUri = fileStoragePort.store(command.tenantId(), documentId, command.filename(), command.content());
        var now = Instant.now();

        var document = new Document(
                documentId,
                command.tenantId(),
                command.filename(),
                command.contentType(),
                command.sizeBytes(),
                contentHash,
                storageUri,
                DocumentStatus.PENDING,
                null,
                now,
                null
        );

        documentRepository.save(document);

        eventPublisherPort.publish(new DocumentUploadedEvent(
                documentId,
                command.tenantId(),
                command.filename(),
                command.contentType(),
                command.sizeBytes(),
                now
        ));

        return documentId;
    }

    private String sha256Hex(byte[] content) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(content));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
