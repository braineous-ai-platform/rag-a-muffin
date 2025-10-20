# 🧮 CGO Reasoning Confidence — From Weights to Contextual Trust
**Context:**  
Within the Causal Graph Orchestrator (CGO), the final stage of reasoning involves **confidence propagation** — turning raw rule weights and fact features into an interpretable measure of “how sure the system is” about its derived conclusions.

## 🧩 Conceptual Overview
| Layer | Entity | Confidence Source | Purpose |
|--------|---------|--------------------|----------|
| **Fact** | `feats[...]` | Vector similarity / semantic association | Micro-level relevance |
| **Rule** | `weight` | Causal strength between connected facts | Mid-level linkage confidence |
| **ReasoningContext** | `confidence` | Aggregation of all active rule–fact chains | Macro-level trust in reasoning state |

## ⚙️ Implementation (Java pseudocode)
```java
double computeContextConfidence(ReasoningContext ctx) {
    double totalWeight = 0.0;
    double totalInfluence = 0.0;
    for (Rule rule : ctx.getActiveRules()) {
        double w = rule.getWeight();
        totalWeight += w;
        Fact src = ctx.getFact(rule.getSource());
        Fact tgt = ctx.getFact(rule.getTarget());
        double fAvg = (src.getFeat("associated_ids_weight") + tgt.getFeat("associated_ids_weight")) / 2.0;
        totalInfluence += fAvg * w;
    }
    return totalWeight == 0 ? 0 : totalInfluence / totalWeight;
}
```

**Tag:** `#cgo #confidence #reasoning #causal_graph #contextual_trust`
