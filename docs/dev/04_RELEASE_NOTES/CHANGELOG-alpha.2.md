# CHANGELOG — v1.0.0-alpha.2

### Summary

BraineousAI Developer Edition reaches **functional reasoning pipeline** maturity:  
`JSON → Fact → Rule → Subgraph → CGO`.  
This marks the transition from data-model prototypes to a fully lambda-driven,
no-POJO architecture.

---

### 🚀 What’s New

- **FNOOrchestrator** — single developer-side entrypoint:
  - Accepts raw JSON arrays and an extractor lambda.
  - Hands data to `LLMContext` for snapshoting.
  - Executes functional `FactRule` set → produces a `Subgraph`.
  - Hands off results to `LLMBridge` (non-blocking).
- **LLMContext / LLMBridge interfaces** finalized as the app boundary (snapshot-only, fire-and-forget).
- **Functional Rule Engine** — developers author pure functions:
  ```java
  FactRule airportRule = (fact,g) -> {
      if(!fact.getText().startsWith("Airport(")) return;
      g.upsertNode("Airport:AUS","airport", Map.of("code","AUS"));
  };
  ```

# From project root

```
cd rag-a-muffin/prompt_engineering_quarkus

mvn -q -Dtest=LLMContextTests test

mvn -q -Dtest=FNOOrchestratorTests test
```

\_**\_extracted_facts\_\_**
[Fact [id=Airport:AUS, ...], ...]
\_**\_llm_context\_\_**
LLMContext{context={flights=LLMFacts{...}}}
\_**\_subgraph_nodes\_\_**
[Airport:AUS, Airport:DFW, Flight:F100]
\_**\_subgraph_edges\_\_**
[E:ROUTE:AUS->DFW]

---

### 🧩 Not Yet Included

| Area                          | Status                 | Notes                                                                             |
| ----------------------------- | ---------------------- | --------------------------------------------------------------------------------- |
| **Merge & De-dupe Logic**     | ❌ Not yet implemented | Will be handled in downstream CGO ingestion layer.                                |
| **Template / DSL Rule Layer** | ❌ Missing             | Functional lambdas only; template-based rules planned for `alpha.3`.              |
| **Subgraph Persistence**      | ❌ In-memory only      | Will be externalized to disk or store in `alpha.3`.                               |
| **Graph Visualization**       | ❌ Pending             | Basic render planned via CLI or Web UI prototype.                                 |
| **Developer Documentation**   | ⚙️ Partial             | `DEVELOPER_GUIDE.md` stub created under `docs/dev/`, to be expanded in `alpha.3`. |
| **CI / Regression Suite**     | ❌ Absent              | CI pipeline will be configured in next cycle.                                     |
| **Performance Benchmarks**    | ⚙️ Upcoming            | Load/perf tests once CGO integration stabilizes.                                  |

---

### 🗺️ Next Up

| Milestone     | Objective                                                  | Target                |
| ------------- | ---------------------------------------------------------- | --------------------- |
| **`alpha.3`** | Integrate Subgraph → LLM prompt binding.                   | Core milestone.       |
|               | Introduce rule template DSL & JSON-based rule definitions. |                       |
|               | Add persistent subgraph storage and visualization.         |                       |
|               | Developer doc expansion + contributor templates.           |                       |
|               | CI regression pipeline (GitHub Actions).                   |                       |
| **`alpha.4`** | Add cross-domain rule sets (`Weather`, `Crew`, `Gate`).    |                       |
|               | Start live CGO ingestion tests.                            |                       |
|               | Establish basic REST hooks for app-side orchestration.     |                       |
| **Long-term** | Open telemetry hooks, distributed context propagation.     | Post-`alpha` release. |

---

**Commit Reference:** [`1.0.0-alpha.2`](https://github.com/braineous-ai-platform/rag-a-muffin/commits/1.0.0-alpha.2)  
**Author:** Sohil Shah (@braineous-ai-platform)  
**Date:** 2025-10-29
