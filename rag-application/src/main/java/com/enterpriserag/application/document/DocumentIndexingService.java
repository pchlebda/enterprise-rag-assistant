package com.enterpriserag.application.document;

import com.enterpriserag.domain.document.model.ChunkDraft;
import com.enterpriserag.domain.document.model.DocumentChunk;
import com.enterpriserag.domain.document.model.DocumentStatus;
import com.enterpriserag.domain.document.port.in.IndexDocumentCommand;
import com.enterpriserag.domain.document.port.in.IndexDocumentUseCase;
import com.enterpriserag.domain.document.port.out.ChunkerPort;
import com.enterpriserag.domain.document.port.out.DocumentChunkRepository;
import com.enterpriserag.domain.document.port.out.DocumentRepository;
import com.enterpriserag.domain.document.port.out.EmbeddingModelPort;
import com.enterpriserag.domain.document.port.out.FileStoragePort;
import com.enterpriserag.domain.shared.exception.DocumentNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

/**
 * No method here is wrapped in a single {@code @Transactional} boundary on purpose: an outer
 * transaction would mark itself rollback-only on the exception thrown from the catch block below,
 * silently discarding the {@code markFailed} write. Each repository call commits independently.
 */
@Service
public class DocumentIndexingService implements IndexDocumentUseCase {

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final FileStoragePort fileStoragePort;
    private final ChunkerPort chunkerPort;
    private final EmbeddingModelPort embeddingModelPort;

    public DocumentIndexingService(
            DocumentRepository documentRepository,
            DocumentChunkRepository documentChunkRepository,
            FileStoragePort fileStoragePort,
            ChunkerPort chunkerPort,
            EmbeddingModelPort embeddingModelPort) {
        this.documentRepository = documentRepository;
        this.documentChunkRepository = documentChunkRepository;
        this.fileStoragePort = fileStoragePort;
        this.chunkerPort = chunkerPort;
        this.embeddingModelPort = embeddingModelPort;
    }

    @Override
    public void index(IndexDocumentCommand command) {
        var document = documentRepository.findById(command.documentId(), command.tenantId())
                .orElseThrow(() -> new DocumentNotFoundException(command.documentId()));

        if (document.status() == DocumentStatus.INDEXED) {
            return;
        }

        documentRepository.updateStatus(command.documentId(), DocumentStatus.PROCESSING);

        try {
            documentChunkRepository.deleteByDocumentId(command.documentId());

            var fileContent = fileStoragePort.load(document.storageUri());
            var drafts = chunkerPort.chunk(fileContent, document.contentType());

            var embeddings = embeddingModelPort.embedAll(drafts.stream().map(ChunkDraft::content).toList());

            var now = Instant.now();
            var chunks = new ArrayList<DocumentChunk>(drafts.size());
            for (int i = 0; i < drafts.size(); i++) {
                var draft = drafts.get(i);
                chunks.add(new DocumentChunk(
                        UUID.randomUUID(),
                        command.documentId(),
                        command.tenantId(),
                        i,
                        draft.pageNumber(),
                        draft.content(),
                        draft.tokenCount(),
                        embeddings.get(i),
                        embeddingModelPort.modelId(),
                        now
                ));
            }

            documentChunkRepository.saveAll(chunks);
            documentRepository.markIndexed(command.documentId(), now);
        } catch (Exception e) {
            documentRepository.markFailed(command.documentId(), e.getMessage());
            throw e;
        }
    }
}
