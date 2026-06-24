CREATE TABLE document_chunks
(
    id                 UUID         NOT NULL PRIMARY KEY,
    document_id        UUID         NOT NULL REFERENCES documents (id),
    tenant_id          UUID         NOT NULL,
    chunk_index        INT          NOT NULL,
    page_number        INT          NOT NULL,
    content            TEXT         NOT NULL,
    token_count        INT          NOT NULL,
    embedding           vector(384) NOT NULL,
    embedding_model_id VARCHAR(100) NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX document_chunks_tenant_doc_idx ON document_chunks (tenant_id, document_id);
CREATE INDEX document_chunks_embedding_hnsw_idx ON document_chunks USING hnsw (embedding vector_cosine_ops);
