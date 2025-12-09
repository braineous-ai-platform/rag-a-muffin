# **rag-a-muffin**

### **Retrieval-Augmented Generation with the CGO reasoning engine**

![This is an image](parallax-image.jpg)

**CGO (Causal Graph Orchestrator)** is a **deterministic reasoning engine** designed to sit inside modern RAG pipelines and agentic systems.  
Where LLMs provide language understanding, CGO provides:

- a **graph substrate**
- **rule-driven reasoning**
- **multi-phase validation**
- **safe, explainable mutations**

This produces **predictable**, **trustworthy** decisions that LLMs alone cannot guarantee.

---

# **Why CGO?**

### RAG retrieves context — but cannot _reason_ over it safely.

### LLMs reason — but cannot guarantee _correctness_ or _consistency_.

### Business logic spreads across microservices — and becomes _brittle_.

CGO unifies these concerns into a **single deterministic substrate**.

It turns **facts → relationships → validated graph → deterministic decisions**.

---

# **Where CGO Fits in RAG**

RAG handles _retrieval_.  
CGO handles _reasoning_.

```
Retriever → Documents / DB / API
            ↓
         Retrieved Facts
            ↓
   CGO (Graph + Rulepacks + Validation)
            ↓
   Deterministic, safe, explainable results
```

CGO ensures the reasoning loop is **stable, reproducible, safe**, and **verifiable**.

---

# **Quickstart — Try CGO Locally**

Clone and run the core tests:

```bash
cd rag-a-muffin/braineous
mvn -q test
```

Run the **Flight Network Optimizer (FNO) demo**:

```bash
cd rag-a-muffin/braineous/agentic-apps/fno-app
mvn -q -Dtest=FNOOrchestratorTests test
```

This demo:

- builds a small **flight network graph**
- applies a **Rulepack** (flights + airports)
- runs through the full **CGO reasoning cycle**
- prints the final reasoning context & GraphSnapshot

---

# **CGO Architecture at a Glance (Alpha2)**

CGO’s core is built around **four pillars**, corresponding to Chapters A–D.

---

## **1. Integration Layer (A_API_Integration.md)**

How your service talks to CGO:

- Facts
- Relationships
- GraphBuilder
- BindResult
- GraphView (read-only snapshots)

---

## **2. Rulepack Engine (B_Rulepack.md)**

Domain reasoning defined as **pure BusinessRule functions**:

```
GraphView → Proposal
```

CGO executes all rules in parallel and validates the proposal set.

---

## **3. Validation Layer (C_Validation.md)**

CGO’s safety system — four phases:

1. Substrate validation (Facts + Relationships)
2. Proposal structure validation
3. Domain validation (optional, pluggable)
4. LLM output validation (QueryPipeline only)

Mutations occur **only** if all phases pass.

---

## **4. Pipeline Architecture (D_Pipeline.md)**

End-to-end reasoning flow:

```
Request
 → Input mapping
 → Rulepack execution
 → Validation
 → Mutation (if safe)
 → GraphSnapshot
 → QueryExecution
```

CGO ensures deterministic, explainable outcomes.

---

# **Documentation**

| Module          | File                                              |
| --------------- | ------------------------------------------------- |
| API Integration | [A_API_Integration.md](docs/A_API_Integration.md) |
| Rulepacks       | [B_Rulepack.md](docs/B_Rulepack.md)               |
| Validation      | [C_Validation.md](docs/C_Validation.md)           |
| Pipeline        | [D_Pipeline.md](docs/D_Pipeline.md)               |

---

# **Evolution of the Repository**

This project began as RAG experimentation across:

- `rag_fast_api/`
- `prompt_engineering_quarkus/`
- `llm_orchestration_fast_api/`
- `prototyping/`

These folders are intentionally preserved.  
They show the research journey:

- early RAG attempts
- prompt-engineering experiments
- LLM orchestration prototypes
- schema validation tests
- abandoned designs

From these experiments emerged the consolidated **CGO monorepo** under `braineous/`:

- `cgo-core` — graph substrate + reasoning engine + public/developer api
- `cgo-scorer` — deterministic scoring (upcoming)
- `cgo-history` — event sourcing & memory (upcoming)
- `cgo-llm` — abstract LLM/vector interfaces
- `cgo-observer` — observability hooks
- `agentic-apps` — demos such as the FNO flight optimizer

This visible evolution shows the transition from **“just another RAG repo”** →  
**“a new reasoning substrate category.”**

---

# **Project Status**

CGO Alpha2 contains:

- stable Integration model
- deterministic Rulepack engine
- multi-phase Validation
- full Pipeline documentation (Chapters A–D)

**Current release:** `1.0.0-alpha.2`

### **Next Milestones**

- Query pipeline refinements
- LLM output validation
- Deterministic scoring engine
- History/event recording
- Microservice runtime wrapper
- Observability extensions

---

# **License & Contributions**

Apache 2.0  
Maintainer: Sohil Shah (@braineous-ai-platform)

Contributions, issues, and discussions are welcome.
