-- Enable pgvector extension for vector similarity search.
-- Requires the pgvector/pgvector:pg16 Docker image (extension pre-compiled).
-- In a production environment this would be executed by a DBA/superuser
-- before granting the application user its narrower privileges.
CREATE EXTENSION IF NOT EXISTS vector;
