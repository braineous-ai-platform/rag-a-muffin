# 🧠 CGO Example — Mapping `Fact.feats` to `Rule.weight`
**Context:**  
Within the Causal Graph Orchestrator (CGO), both `Fact` and `Rule` objects carry quantitative attributes that describe their relative influence inside a reasoning chain.

## 🧩 Conceptual Mapping
| Layer | Entity | Purpose | Data Source | Notes |
|--------|---------|----------|--------------|-------|
| **Semantic Layer** | `Fact` | Represents an observed or derived atomic statement | `feats[associated_ids_weight]` | Vector-level contextual relevance — derived from embeddings or attention scores |
| **Reasoning Layer** | `Rule` | Describes a logical or causal relationship between two Facts | `weight` | Quantifies the causal strength between the linked Facts |

> **Relationship:**  
> `Rule.weight` is derived from (or influenced by) the relevant `Fact.feats[...]` values participating in the rule.  
> It acts as a higher-order confidence score for how strongly Fact A → Fact B co-activate within reasoning space.

## ⚙️ Operational View
```java
rule.weight = mean(
    factA.feats["associated_ids_weight"],
    factB.feats["associated_ids_weight"]
);
```

## 🧠 Reasoning Analogy
Facts = Neurons firing (with features)  
Rules = Synapses (with causal weights)  
Context = Cognitive state formed by their activation pattern

## 🧰 Implementation Snippet
```java
double computeRuleWeight(Fact source, Fact target) {
    double srcWeight = source.getFeat("associated_ids_weight");
    double tgtWeight = target.getFeat("associated_ids_weight");
    return (srcWeight + tgtWeight) / 2.0;
}
```

**Tag:** `#cgo #reasoning #facts #rules #causal_graph`
