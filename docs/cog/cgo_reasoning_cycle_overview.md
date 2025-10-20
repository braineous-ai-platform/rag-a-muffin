# 🧭 CGO Reasoning Cycle Overview

**Context:**  
This overview connects the three core reasoning documents of the Causal Graph Orchestrator (CGO) and explains the full cognitive data flow—from facts to rules to confidence.

---

## 🧠 Core Docs

1. **[`fact_rule_weight_mapping.md`](./fact_rule_weight_mapping.md)**  
   → Describes how semantic‐level features (`Fact.feats`) translate into causal link weights (`Rule.weight`).

2. **[`rule_inference_vs_application.md`](./rule_inference_vs_application.md)**  
   → Defines execution semantics: learn first (`inferRules`), then reason (`applyRules`).

3. **[`reasoning_context_confidence.md`](./reasoning_context_confidence.md)**  
   → Explains how the system aggregates rule and fact weights into global contextual trust.

---

## 🧩 Cognitive Flow Diagram

```mermaid
flowchart LR
    F1[Fact A<br/>feats[…]] -->|semantic weight| R1[(Rule α<br/>weight)]
    F2[Fact B<br/>feats[…]] -->|semantic weight| R1
    R1 -->|applyRules| F3[Derived Fact C]
    F3 -->|updateContext| C[ReasoningContext<br/>confidence]
    C -->|feedback if low| IR[inferRules()]
    IR -->|learn new links| R2[(Rule β)]
```

```java
while (context.isActive()) {
    inferRules(context); // Discover new causal links
    applyRules(context); // Execute reasoning
    context.updateConfidence(); // Aggregate trust
    if (context.getConfidence() < threshold)
        context.learn.(); // Self-correct
    }
```

---

✅ **Logged & linked**

- Path → `/docs/cgo/cgo_reasoning_cycle_overview.md`
- Status → `Phase 2 Complete | Ready for Dev Guide Compilation`
- Grouped Docs →
  1. `fact_rule_weight_mapping.md`
  2. `rule_inference_vs_application.md`
  3. `reasoning_context_confidence.md`
  4. `cgo_reasoning_cycle_overview.md`

---

### 🛑 Stopping Point Summary

**End of Phase 2 → Cognitive Architecture Stabilized.**  
Officially defined:

- Core reasoning primitives
- Execution semantics
- Confidence propagation
- Developer-facing documentation system

Next session: integrate `RuleComponent` orchestration tests and finalize cognitive telemetry metrics.
