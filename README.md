# Enterprise RAG Assistant

Production-grade Retrieval-Augmented Generation platform — Java 21, Spring Boot, LangChain4j, pgvector, Kafka.

> **Status: M4 — chunking + embeddings + pgvector.** Chat endpoint, document upload, async ingestion, and PDF chunking/embedding into pgvector are complete. Next: M5 (full RAG pipeline — retrieval, grounding, citations).

---

## Module structure

```
enterprise-rag-assistant/
├── rag-domain/        # Pure Java — value objects, ports (interfaces), domain services
│                      # Zero framework deps. ArchUnit enforces this.
├── rag-application/   # Use-case orchestration, transaction boundaries
├── rag-adapters/      # Inbound/outbound adapters: REST, JPA, Kafka, LLM, storage
├── rag-bootstrap/     # Spring Boot entry point, config, profiles, executable JAR
└── compose.yml        # PostgreSQL 16 + pgvector, Kafka 3.7 (KRaft)
```

Dependency direction enforced by ArchUnit (build fails on violation):
```
adapters → application → domain
```
`domain` must not import Spring, JPA, Kafka, or LangChain4j.

---

## Milestone progress

| Milestone | Status | What's there |
|---|---|---|
| M0 — Skeleton | ✅ | Maven multi-module, Spring Boot bootstrap, Docker Compose, ArchUnit guard, Testcontainers smoke test |
| M1 — Chat endpoint | ✅ | `ChatModelPort` + OpenAI adapter (profile `openai`) + local echo adapter · `POST /api/v1/chat/query` · OpenAPI/Swagger |
| M2 — Document upload | ✅ | Multipart upload, PDF validation, `LocalFileStorageAdapter`, `documents` table (Flyway V2), status lifecycle, list/get endpoints |
| M3 — Async ingestion | ✅ | `DocumentUploadedEvent` + `EventPublisherPort` · `outbox_events` table (Flyway V3) · `TransactionalOutboxPublisher` · `OutboxRelay` (scheduled) · `KafkaDocumentIngestionConsumer` · DLQ via `DefaultErrorHandler` · status → PROCESSING |
| M4 — Chunking + embeddings | ✅ | `PdfChunkerAdapter` (page-tagged, overlapping word-window chunks), `EmbeddingModelPort` (local MiniLM + OpenAI adapters), `document_chunks` table + pgvector (Flyway V4), HNSW index, status → INDEXED |
| M5 — Full RAG pipeline | 🔜 | Retrieval → prompt assembly → LLM, grounding guardrails, citations |
| M6 — AuthN/AuthZ + multi-tenancy | 🔜 | JWT, tenant claim, `@PreAuthorize`, tenant-scoped queries |

---

## What exists now (M4)

| Area | What's there |
|---|---|
| Domain ports (outbound) | `ChatModelPort`, `ChunkerPort`, `EmbeddingModelPort`, `DocumentChunkRepository`, `DocumentRepository`, `FileStoragePort`, `EventPublisherPort` |
| Domain events | `DocumentUploadedEvent` — records documentId, tenantId, filename, contentType, sizeBytes, occurredAt |
| Chat flow | `ChatUseCase` → `ChatService` → `ChatModelPort` (OpenAI or local echo) |
| Ingestion flow | `IngestDocumentUseCase` → `DocumentIngestionService` → store file + save doc + publish outbox event (atomic TX) |
| Async pipeline | `OutboxRelay` polls `outbox_events` every 1 s → publishes to `doc.ingest` Kafka topic → `KafkaDocumentIngestionConsumer` invokes `IndexDocumentUseCase` |
| Indexing flow | `IndexDocumentUseCase` → `DocumentIndexingService` → load file → `PdfChunkerAdapter` (page-tagged, overlapping chunks) → `EmbeddingModelPort` (local MiniLM by default, OpenAI behind `openai` profile) → `DocumentChunkRepository.saveAll` → status → INDEXED (or FAILED with reason, idempotent on retry) |
| DLQ | `DefaultErrorHandler` with 3 retries (2 s backoff) → `doc.ingest.DLT` dead-letter topic |
| REST API | `POST /api/v1/chat/query` · `POST /api/v1/documents` (returns 202) · `GET /api/v1/documents` · `GET /api/v1/documents/{id}` |
| OpenAPI | Swagger UI at `/swagger-ui.html`, spec at `/api-docs` |
| Error handling | RFC 7807 `application/problem+json` via `GlobalExceptionHandler` |
| Kafka control | `kafka.enabled` property (default `false`). Set to `true` only in `local` profile and Testcontainers integration tests — existing unit/slice tests run without Kafka |
| Migrations | V1 pgvector extension · V2 documents table · V3 outbox_events table · V4 document_chunks table + HNSW index |
| Tests | Domain unit tests (pure Java, no Spring) · `@WebMvcTest` slice tests · application service unit tests · `PdfChunkerAdapterTest` (real in-memory PDF via PDFBox) · `DocumentIngestionIntegrationTest` (Testcontainers Postgres) · `AsyncIngestionIntegrationTest` (Testcontainers Postgres + Kafka, Awaitility, asserts chunk rows + embedding dims) · ArchUnit boundary guard |

---

## Quick start

**Prerequisites:** Docker Desktop · Java 21 · Maven 3.9+

```bash
# Start infrastructure (Postgres + pgvector + Kafka)
docker compose up -d

# Copy env (defaults work for local Docker Compose)
cp .env.example .env

# Run the application with Kafka enabled
mvn -pl rag-bootstrap spring-boot:run -Dspring-boot.run.profiles=local
```

Available endpoints:
- `http://localhost:8080/actuator/health`
- `http://localhost:8080/swagger-ui.html`

```bash
# Run all tests (requires Docker for Testcontainers)
mvn verify
```

### Try the async ingestion flow

```bash
# Upload a PDF (returns 202 immediately)
curl -X POST http://localhost:8080/api/v1/documents \
  -H "X-Tenant-Id: 00000000-0000-0000-0000-000000000001" \
  -F "file=@/path/to/document.pdf"
# → {"documentId":"<uuid>","status":"PENDING"}

# Poll for status — transitions PENDING → PROCESSING → INDEXED
# (first request loads the local ONNX embedding model, so allow a few extra seconds)
curl http://localhost:8080/api/v1/documents/<uuid> \
  -H "X-Tenant-Id: 00000000-0000-0000-0000-000000000001"
# → {"status":"INDEXED", ...}
```

---

## Architecture

### Hexagonal boundaries

```
rag-domain       ← entities, value objects, ports (interfaces)
                   Zero deps. ArchUnit build-breaks on any Spring/JPA/Kafka import.
rag-application  ← use-case orchestration, @Transactional
rag-adapters     ← REST controllers, JPA repos, Kafka producer/consumer,
                   file storage, OpenAI LangChain4j adapter
rag-bootstrap    ← Spring Boot entry point, config, profiles
```

### Async ingestion + indexing pipeline (M3 + M4)

```
POST /documents
  │
  ├─ store file (local FS / S3 in prod)
  └─ DB transaction ──────────────────────────────────────────────┐
       INSERT documents (status=PENDING)                          │ atomic
       INSERT outbox_events (payload=DocumentUploadedEvent JSON)  │
     ────────────────────────────────────────────────────────────-┘
       → 202 Accepted immediately

OutboxRelay (@Scheduled 1s)
  └─ SELECT unpublished outbox_events (top 10)
  └─ KafkaTemplate.send("doc.ingest", documentId, payload)
  └─ UPDATE outbox_events SET published_at = now()

KafkaDocumentIngestionConsumer
  └─ @KafkaListener("doc.ingest")
  └─ IndexDocumentUseCase.index(documentId, tenantId)
       ├─ documentRepository.updateStatus(documentId, PROCESSING)
       ├─ documentChunkRepository.deleteByDocumentId(documentId)   (idempotent retry)
       ├─ fileStoragePort.load(storageUri) → PdfChunkerAdapter.chunk(...)
       │    → page-tagged ChunkDraft list (400-word window, 50-word overlap)
       ├─ embeddingModelPort.embedAll(chunkTexts)                  (local MiniLM-L6-v2, 384-dim)
       ├─ documentChunkRepository.saveAll(chunks)                  (pgvector, HNSW index)
       └─ documentRepository.markIndexed(documentId, now)          → status = INDEXED
            on any failure: documentRepository.markFailed(documentId, reason) → status = FAILED
  └─ on uncaught failure: DefaultErrorHandler → 3 retries → "doc.ingest.DLT"
```

---

## Stack

| Concern | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.3 · Maven multi-module |
| RAG / LLM | LangChain4j · OpenAI (prod) · local echo adapter (CI) |
| Vector store | PostgreSQL 16 + pgvector (HNSW) |
| Schema management | Flyway |
| Async ingestion | Apache Kafka 3.7 (KRaft) + transactional outbox |
| Auth | JWT · Spring Security 6 · multi-tenant (M6) |
| Testing | JUnit 5 · Testcontainers · ArchUnit · Awaitility |
| Observability | Micrometer · Prometheus · structured JSON logs (M9) |
