package com.enterpriserag.application.document;

import com.enterpriserag.domain.document.model.Document;
import com.enterpriserag.domain.document.model.DocumentStatus;
import com.enterpriserag.domain.document.port.in.IngestDocumentCommand;
import com.enterpriserag.domain.document.port.in.IngestDocumentUseCase;
import com.enterpriserag.domain.document.port.out.DocumentRepository;
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

    public DocumentIngestionService(DocumentRepository documentRepository, FileStoragePort fileStoragePort) {
        this.documentRepository = documentRepository;
        this.fileStoragePort = fileStoragePort;
    }

    @Override
    @Transactional
    public DocumentId ingest(IngestDocumentCommand command) {
        var documentId = DocumentId.generate();
        var contentHash = sha256Hex(command.content());
        var storageUri = fileStoragePort.store(command.tenantId(), documentId, command.filename(), command.content());

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
                Instant.now(),
                null
        );

        return documentRepository.save(document);
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
