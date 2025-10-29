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
