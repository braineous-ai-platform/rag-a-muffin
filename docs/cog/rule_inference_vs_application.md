# ⚙️ CGO Execution Semantics — `inferRules()` vs `applyRules()`
**Context:**  
Within the Causal Graph Orchestrator (CGO), reasoning occurs in distinct phases — learning causal relationships (inference) and then executing those relationships (application).

## 🧩 Conceptual Difference
| Method | Layer | Purpose | Typical Input | Typical Output |
|---------|--------|----------|----------------|----------------|
| **`inferRules()`** | *Discovery / Learning layer* | Derive or update causal rules from observed facts. | Set of `Facts` | `Rule` objects |
| **`applyRules()`** | *Reasoning / Execution layer* | Use those learned rules to generate or update facts. | `Facts` + `Rules` | Derived `Facts` |

## 🔁 Reasoning Cycle Sequence
```java
inferRules(context);
applyRules(context);
```

## 🧠 Analogy
infer → learn the map  
apply → walk the map

**Tag:** `#cgo #reasoning #inferRules #applyRules #causal_graph`
