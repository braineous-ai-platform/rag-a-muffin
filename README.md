# **rag-a-muffin**

### **Retrieval-Augmented Generation with the CGO reasoning engine**

![This is an image](parallax-image.jpg)

**CGO (Causal Graph Orchestrator)** is a **deterministic reasoning engine** designed to sit inside modern RAG pipelines and agentic systems.  
Where LLMs provide language understanding, CGO provides:

- a **graph substrate**
- **rule-driven reasoning**
- **multi-phase validation**
- **safe, explainable state changes**

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

## Documentation guide

This repository is designed to be explored in stages.
Start with runnable examples before reading deeper documentation.

### Recommended reading order

If you are new:

1. **[FNO application (runnable)](./rag-a-muffin/braineous/agentic-apps/fno-app/README.md)**

   - Start with the FNO module.
   - After cloning the repo, from the project root:
     `cd rag-a-muffin/braineous/agentic-apps/fno-app`
   - Run `run.sh`, then `smoke.sh`.
   - Observe how data is ingested into a graph, how queries execute, and how results are observed.

   This provides a concrete mental model before diving into any abstractions.

2. **[Chapter A — API Integration](./docs/A_API_Integration.md)**

   - Explains the public CGO API surface.
   - Introduces core concepts such as Facts, Relationships, Rulepacks, and GraphView.
   - Start here if you plan to integrate CGO into a service.

3. **[Chapter B — Rulepack](./docs/B_Rulepack.md)**

   - Shows how domain reasoning is expressed.
   - Focuses on deterministic, rule-based decision logic.
   - Best read after understanding the API contract.

4. **[Chapter D — Pipeline Architecture](./docs/D_Pipeline_Architecture.md)**

   - Describes how CGO executes reasoning end-to-end.
   - Covers request handling, execution flow, and orchestration.
   - Intended for deeper understanding, not first use.

5. **[Chapter C — Validation](./docs/C_Validation.md)**

   - Explains safety, correctness, and validation guarantees.
   - Covers how invalid states are detected and prevented.
   - Read once you are comfortable with the core model.

6. **[Chapter E — Scoring](./docs/E_Scoring.md)**

   - Describes planned scoring capabilities.
   - Not implemented in the current release.

---

### Notes

- File names are stable for linking and SEO.
- Chapters are intentionally detailed; start with the runnable FNO flow first.
- Documentation focuses on **observable behavior and contracts**, not internal mutation mechanics.

Run the system first.
Then read the chapters with context.

---

# **CGO Architecture at a Glance (Alpha2)**

CGO’s core is built around **four pillars**, corresponding to Chapters A–D.

---

## **1. Integration Layer**

How your service talks to CGO:

- Facts
- Relationships
- GraphBuilder
- BindResult
- GraphView (read-only snapshots)

---

## **2. Rulepack Engine**

Domain reasoning defined as **pure BusinessRule functions**:

```
GraphView → Proposal
```

CGO executes all rules in parallel and validates the proposal set.

---

## **3. Validation Layer**

CGO’s safety system — four phases:

1. Substrate validation (Facts + Relationships)
2. Proposal structure validation
3. Domain validation (optional, pluggable)
4. LLM output validation (QueryPipeline only)

Changes are applied only after all validation phases succeed.

---

## **4. Pipeline Architecture**

End-to-end reasoning flow:

```
Request
 → Input mapping
 → Rulepack execution
 → Validation
 → State update (if permitted)
 → GraphSnapshot
 → QueryExecution
```

CGO ensures deterministic, explainable outcomes.

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
Maintainer: Sohil Shah (@braineous-engineering)

Contributions, issues, and discussions are welcome.
