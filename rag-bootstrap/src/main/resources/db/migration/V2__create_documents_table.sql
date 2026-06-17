CREATE TABLE documents
(
    id             UUID         NOT NULL PRIMARY KEY,
    tenant_id      UUID         NOT NULL,
    filename       VARCHAR(255) NOT NULL,
    content_type   VARCHAR(100) NOT NULL,
    size_bytes     BIGINT       NOT NULL,
    content_hash   VARCHAR(64)  NOT NULL,
    storage_uri    VARCHAR(1024) NOT NULL,
    status         VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    failure_reason TEXT,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    indexed_at     TIMESTAMPTZ,
    version        INT          NOT NULL DEFAULT 0
);

CREATE INDEX documents_tenant_id_idx ON documents (tenant_id);
CREATE INDEX documents_tenant_status_idx ON documents (tenant_id, status);
