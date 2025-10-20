# Query–Relationship–Object (QRO) Model

Core design pattern behind how BraineousAI tracks context, relevance, and provenance across all Agentic and RAG systems.

---

## 🧩 Overview

At runtime, every interaction with data can be represented as a triple:

- **Query** — the question, intent, or trigger.
- **Object** — any retrievable unit (document, vector, record, or tool output).
- **Relationship** — the explicit connection between the two, with metadata and scores.

---

## 🔍 Why It Exists

Traditional RAG setups treat retrieval as a black box:  
prompt in → vector search → chunks out.

The QRO model **makes retrieval explainable** and **auditable**:

- Every retrieval becomes a _typed edge_.
- Every context pack is reproducible.
- Every prompt can reference its lineage.

---

## ⚙️ Schema

| Entity           | Description                                       | Key Fields                                                                      |
| ---------------- | ------------------------------------------------- | ------------------------------------------------------------------------------- |
| **Query**        | Natural language input or task signal.            | `id`, `text`, `intent`, `actor`, `session_id`, `context_hash`, `budget(tokens)` |
| **Object**       | Source of truth (doc, chunk, DB row, API result). | `id`, `type`, `uri`, `version`, `content_hash`, `embedding_ref`                 |
| **Relationship** | Scored edge between Query and Object.             | `id`, `src_id`, `dst_id`, `kind`, `score`, `method`, `span_refs[]`, `ts`        |

**Kinds of Relationship:**

- `retrieves` — object fetched by retriever
- `supports` / `contradicts` — semantic alignment or opposition
- `derived_from` — generated answer or output lineage
- `uses_tool` — external system invoked
- `compresses` — compressed prompt or summary reuse

---

## 🧠 Token Optimization

Each Query creates a **context_hash** for the top-K retrieved objects.  
Future queries with the same hash reuse that context without recomputation.

Result →

- lower token spend,
- deterministic context,
- faster downstream generation.

---

## 🕸 Integration with Fact Graph

When paired with the `Fact Graph with Rules as Edges`,  
the QRO graph feeds into the causal layer:

This bridges short-term RAG memory and long-term reasoning.

---

## 🧾 Event Topics (Kafka)

| Topic          | Event                                     | Purpose                    |
| -------------- | ----------------------------------------- | -------------------------- |
| `query.v1`     | `QueryCreated`, `QueryRefined`            | new or refined questions   |
| `retrieval.v1` | `RetrievalScored`                         | ranked results and methods |
| `answer.v1`    | `AnswerProposed`, `AnswerFinalized`       | persisted responses        |
| `edge.v1`      | `RelationshipCreated`                     | relationship metadata      |
| `telemetry.v1` | `TokenSpend`, `CacheHit`, `RouteDecision` | runtime insights           |

---

## 📦 Example API

```bash
POST /queries
GET  /queries/{id}/context
POST /answers
GET  /lineage/{answer_id}
```

---
