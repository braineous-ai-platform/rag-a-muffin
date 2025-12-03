# rag-a-muffin

### Retrieval-Augmented Generation with the CGO reasoning engine

![This is an image](parallax-image.jpg)

CGO (Causal Graph Orchestrator) is a deterministic reasoning engine that sits inside modern RAG pipelines.  
Instead of relying only on LLMs for reasoning, CGO uses a graph substrate, rulepacks, and a validation pipeline  
to produce reliable, safe, and explainable decisions.

### Why CGO?

- RAG retrieves facts, but cannot reason over them safely
- LLMs are non-deterministic and hard to validate
- Business logic scattered across microservices becomes brittle

CGO unifies this into a deterministic, rule-driven reasoning substrate.

### Where CGO Fits in RAG

RAG retrieves relevant context.  
CGO takes that context, applies rules, validates changes, and produces deterministic decisions.

Retriever → Documents/DB/API
↓
Retrieved Facts
↓
CGO (graph + rules + validation)
↓
Deterministic, safe, explainable results

### Quickstart — Try CGO locally

Clone the repo and run the tests:

```bash
cd rag-a-muffin
cd braineous

# Run the core CGO tests
mvn -q test
```

To see a concrete flight-network demo (FNO) using CGO:

```bash
cd rag-a-muffin
cd braineous
cd agentic-apps
cd fno-app

mvn -q -Dtest=FNOOrchestratorTests test
```

This demo:

- builds a small flight network as a graph
- applies a Rulepack over Facts (flights + airports)
- runs through the CGO reasoning cycle (Facts → Rulepacks → Validation → Graph snapshot)
- prints the final reasoning context and graph snapshot to the console.

### CGO Architecture at a Glance

CGO’s core is built around four pillars:

- **Integration (A_API_Integration.md)**  
  How your service talks to CGO: Facts, Inputs, GraphBuilder, and BindResult.

- **Rulepacks (B_Rulepack.md)**  
  How you express domain reasoning as pure BusinessRule functions over a GraphView.

- **Validation (C_Validation.md)**  
  How CGO protects the graph with multi-phase validation (substrate, proposals, domain, query/LLM).

- **Scoring (D_Scoring.md)** _(architectural preview)_  
  Planned future layer for resolving multiple valid proposals into a single winning decision.

You can find these docs under `docs/`:

- `docs/A_API_Integration.md`
- `docs/B_Rulepack.md`
- `docs/C_Validation.md`
- `docs/D_Scoring.md`

### Project Status

CGO’s core (Integration, Rulepacks, Validation) is stable and active in development.  
The Scoring layer is a planned module and not implemented yet.

**Current release:** `1.0.0-alpha.2`  
**Next milestones:**

- Query pipeline refinements
- LLM output validation
- Deterministic scoring
- Microservice runtime wrapper

### License & Contributions

Apache 2.0  
Maintainer: Sohil Shah (@braineous-ai-platform)

Contributions, issues, and discussions are welcome.
