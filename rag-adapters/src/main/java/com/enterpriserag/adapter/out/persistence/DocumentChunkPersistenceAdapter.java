package com.enterpriserag.adapter.out.persistence;

import com.enterpriserag.domain.document.model.DocumentChunk;
import com.enterpriserag.domain.document.port.out.DocumentChunkRepository;
import com.enterpriserag.domain.shared.model.DocumentId;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DocumentChunkPersistenceAdapter implements DocumentChunkRepository {

    private final DocumentChunkJpaRepository jpaRepository;

    public DocumentChunkPersistenceAdapter(DocumentChunkJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void saveAll(List<DocumentChunk> chunks) {
        jpaRepository.saveAll(chunks.stream().map(this::toEntity).toList());
    }

    @Override
    public void deleteByDocumentId(DocumentId id) {
        jpaRepository.deleteByDocumentId(id.value());
    }

    private DocumentChunkJpaEntity toEntity(DocumentChunk chunk) {
        var e = new DocumentChunkJpaEntity();
        e.id = chunk.id();
        e.documentId = chunk.documentId().value();
        e.tenantId = chunk.tenantId().value();
        e.chunkIndex = chunk.chunkIndex();
        e.pageNumber = chunk.pageNumber();
        e.content = chunk.content();
        e.tokenCount = chunk.tokenCount();
        e.embedding = chunk.embedding();
        e.embeddingModelId = chunk.embeddingModelId();
        e.createdAt = chunk.createdAt();
        return e;
    }
}
