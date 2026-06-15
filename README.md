# Enterprise RAG Assistant

Production-grade Retrieval-Augmented Generation platform — Java 21, Spring Boot, LangChain4j, pgvector, Kafka.

> **Status: M0 — project skeleton.** Infrastructure boots, ArchUnit boundary guard and Testcontainers smoke test are green. Feature milestones start at M1.

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

## What exists in this commit

| Area | What's there |
|---|---|
| Maven | Multi-module parent POM (Spring Boot 3.3.6, Java 21). Version BOM for LangChain4j, MapStruct, ArchUnit, SpringDoc declared centrally. |
| `rag-domain` | `TenantId`, `DocumentId` — Java 21 records with null-guard compact constructors. `RagException` — base unchecked domain exception. |
| `rag-adapters` | POM only. Declares Spring Data JPA, PostgreSQL, Flyway as dependencies (implementations added in M2+). |
| `rag-bootstrap` | `RagAssistantApplication` — Spring Boot entry point. `application.yml` — datasource via env vars, graceful shutdown, Actuator. `application-local.yml` — local dev overrides. `V1__init_extensions.sql` — `CREATE EXTENSION IF NOT EXISTS vector`. |
| Infrastructure | `compose.yml` — PostgreSQL 16 + pgvector image, Kafka 3.7 KRaft (no Zookeeper), both with health checks. `.env.example` — all required env vars documented. |
| Tests | `HexagonalArchitectureTest` — 4 ArchUnit rules, blocks build on hexagonal boundary violation. `ApplicationSmokeTest` — full Spring context against real Testcontainers PostgreSQL (`pgvector/pgvector:pg16`), asserts `/actuator/health` returns 200 UP. |

---

## Quick start

**Prerequisites:** Docker Desktop · Java 21 · Maven 3.9+

```bash
# Start infrastructure
docker compose up -d

# Copy env (defaults work for local Docker Compose)
cp .env.example .env

# Run the application
mvn -pl rag-bootstrap spring-boot:run -Dspring-boot.run.profiles=local
```

Available now: `http://localhost:8080/actuator/health`

```bash
# Run all tests (requires Docker running)
mvn verify
```

---

## Stack

| Concern | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.3 · Maven multi-module |
| RAG / LLM | LangChain4j · OpenAI (prod) · local ONNX/Ollama (dev/CI) |
| Vector store | PostgreSQL 16 + pgvector (HNSW) |
| Schema management | Flyway |
| Async ingestion | Apache Kafka 3.7 (KRaft) + transactional outbox |
| Auth | JWT · Spring Security 6 · multi-tenant |
| Testing | JUnit 5 · Testcontainers · ArchUnit · WireMock |
| Observability | Micrometer · Prometheus · structured JSON logs |
