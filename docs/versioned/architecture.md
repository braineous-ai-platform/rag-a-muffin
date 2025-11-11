# 🧬 CGO Architecture — System Design Overview

Describes the architectural composition of the **Causal Graph Orchestrator (CGO)**:  
how facts, validators, and rulepacks interact to form a deterministic reasoning pipeline.

**Author:** Sohil Shah (@braineous-engineering)  
**Date:** November 11, 2025  
**Repo:** [BraineousAI / rag-a-muffin](https://github.com/braineous-ai-platform/rag-a-muffin)

---

## 🔖 Purpose

The **Causal Graph Orchestrator (CGO)** is the reasoning core of the BraineousAI platform.  
It enables data systems to “think” through deterministic context assembly rather than probabilistic guesswork.

This document outlines the system’s architecture — from data ingestion to reasoning feedback — and provides a mental model for developers integrating or extending CGO.

See also: [`docs/validator_design.md`](../validator_design.md) Validator Design

See also: [`docs/rulepack_template.md`](../rulepack_template.md) Declarative rulepack schema draft

---

## 🧩 Architectural Layers

| Layer                       | Description                                                          | Key Artifact                                     |
| --------------------------- | -------------------------------------------------------------------- | ------------------------------------------------ |
| **Substrate**               | Base graph representation of atomic facts and edges.                 | `Fact`, `GraphAssembler`                         |
| **Validator Layer**         | Enforces data integrity via lambda-based validators.                 | [`validator_design.md`](./validator_design.md)   |
| **Rulepack Layer**          | Declarative reasoning contracts for validation + inference.          | [`rulepack_template.md`](./rulepack_template.md) |
| **Scoring Layer (Planned)** | Assigns weights to outcomes for comparative reasoning.               | `ScoreCombiner`                                  |
| **Outcome Loop (Planned)**  | Feedback mechanism to tune context weights via deterministic deltas. | TBD                                              |

---

## 🧠 Core Architectural Principles

| Principle                   | Description                                                                         |
| --------------------------- | ----------------------------------------------------------------------------------- |
| **Deterministic by Design** | Every run produces the same graph snapshot given identical input.                   |
| **Composable**              | Each layer (Substrate → Validator → Rulepack → Scoring) is modular and replaceable. |
| **Model-Agnostic**          | Works with both symbolic logic (rules) and statistical overlays (LLMs).             |
| **Context-Aware**           | Facts carry embedded context but no hidden state.                                   |
| **Traceable**               | Every transformation produces a verifiable audit trail via snapshot hashes.         |

---

## ⚙️ Logical Flow

```text
AppProducer
   ↓
FactExtractor → Fact → GraphAssembler
   ↓
ValidatorRegistry → FactValidator(s)
   ↓
RulepackExecutor → ValidationSummary
   ↓
(Optional) Rule Inference → Rulepack Rules
   ↓
Snapshot (deterministic hash)
   ↓
[Future] Scoring Engine → Weight Deltas
   ↓
[Future] Outcome Feedback Loop
```

### 🧮 Determinism Stack

| Component                  | Function                               | Mechanism                                 |
| -------------------------- | -------------------------------------- | ----------------------------------------- |
| **GraphAssembler**         | Builds reproducible graphs from Facts. | Stable sorting + snapshot hashing.        |
| **ValidatorRegistry**      | Executes declarative validators.       | Ordered iteration from Rulepack manifest. |
| **RulepackExecutor**       | Runs validation and rule logic.        | Fail-fast deterministic execution.        |
| **SnapshotManager**        | Serializes state to disk or cloud.     | Hash-based content addressing.            |
| **ScoreCombiner (future)** | Aggregates weighted outcomes.          | Deterministic weight deltas.              |

### 🧰 Module Integration

Each CGO module communicates via JSON contracts to preserve cross-language portability.

| Module              | Input              | Output                   | Notes                                                     |
| ------------------- | ------------------ | ------------------------ | --------------------------------------------------------- |
| `AppProducer`       | Domain data (JSON) | Facts                    | Converts application-level JSON to CGO-compatible format. |
| `FactExtractor`     | Facts              | Validated Fact objects   | Enforces minimal schema.                                  |
| `ValidatorRegistry` | Facts + Rulepack   | ValidationSummary        | Applies validators deterministically.                     |
| `RulepackExecutor`  | ValidationSummary  | Reasoning graph snapshot | Manages validator and rule sequence.                      |
| `ScoreCombiner`     | Graph snapshots    | Scored context           | Combines multiple reasoning outcomes (planned).           |

### 🧩 Data Flow Diagram (Conceptual)

```text
[ Domain Data ]
       ↓
[ AppProducer ]
       ↓
[ FactExtractor ] → [ GraphAssembler ]
       ↓
[ ValidatorRegistry ]
       ↓
[ RulepackExecutor ]
       ↓
( Optional ) [ Scoring Layer ]
       ↓
[ SnapshotManager ]
```

### 🧭 Deployment Topologies

| Topology         | Description                                  | Use Case                             |
| ---------------- | -------------------------------------------- | ------------------------------------ |
| **In-Memory**    | Local Java instance for development/testing. | Unit tests, local pipelines.         |
| **Service Mode** | Exposed via Quarkus REST API.                | Microservice integration.            |
| **Stream Mode**  | Connected via Kafka/Flink.                   | Real-time event-driven reasoning.    |
| **Batch Mode**   | Triggered via job scheduler.                 | Offline validation + rule execution. |

### 🔒 Persistence and Serialization

All graph snapshots are designed to be portable.
CGO supports pluggable serialization backends:

| Backend               | Type                  | Example                                    |
| --------------------- | --------------------- | ------------------------------------------ |
| **Local Disk**        | JSON file snapshots   | `/snapshots/cgo_snapshot_001.json`         |
| **S3**                | Object storage        | `s3://braineous-cgo/snapshots/2025-11-11/` |
| **Database (future)** | Key-value graph store | PostgreSQL / Neo4j hybrid                  |

### 🧩 Extension Points

| Hook                  | Purpose                           | Example Extension                         |
| --------------------- | --------------------------------- | ----------------------------------------- |
| **ValidatorRegistry** | Add new built-in validators.      | `builtin.jsonschema`, `builtin.reference` |
| **RulepackExecutor**  | Register new rule types.          | `derive.*`, `transform.*`                 |
| **SnapshotManager**   | Custom serialization backend.     | `S3SnapshotWriter`                        |
| **ScoreCombiner**     | Merge multiple reasoning outputs. | `WeightedOutcomeCombiner`                 |

---

### 🪶 Author Note

The CGO architecture converts traditional data pipelines into causal reasoning systems.
It treats every dataset as a knowledge graph capable of introspection — deterministic, versioned, and extensible.

**Author:** Sohil Shah (@braineous-engineering)  
**Branch:** main  
**Last Updated:** November 11, 2025
