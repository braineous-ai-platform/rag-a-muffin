# 🧱 CGO Rulepack Template — Declarative Reasoning Layer

Defines the declarative JSON-based schema for validators and rule logic in the **Causal Graph Orchestrator (CGO)**.  
The Rulepack acts as the **reasoning contract**, providing deterministic sequencing, validation, and transformation definitions that operate atop the stable CGO substrate.

**Author:** Sohil Shah (@braineous-engineering)  
**Date:** November 11, 2025  
**Repo:** [BraineousAI / rag-a-muffin](https://github.com/braineous-ai-platform/rag-a-muffin)

---

## 🔖 Overview

See also: [`docs/validator_design.md`](../validator_design.md) Validator Design

A **Rulepack** is a self-contained JSON manifest describing how a domain should be validated and reasoned over.  
It defines:

1. Which validators to apply,
2. How facts are transformed into new edges or rules, and
3. What deterministic order and severity policies govern execution.

Each Rulepack is **namespaced** — meaning it applies only within a bounded domain (e.g., `Airport`, `Flight`, or `Policy`).

---

## 🧩 Rulepack Structure

| Field        | Type   | Description                                                                       |
| ------------ | ------ | --------------------------------------------------------------------------------- |
| `meta`       | object | Metadata such as name, version, description, namespace, and determinism strategy. |
| `bindings`   | object | Maps logical validator IDs to their implementation handles.                       |
| `validators` | array  | Declares validator rules and configurations.                                      |
| `execution`  | object | Defines execution order and fail-fast policies.                                   |
| `rules`      | array  | (Optional) Declarative logic for generating derived edges or facts.               |

---

### 🧱 Example: Core Template

```json
{
  "meta": {
    "name": "core-airport-substrate",
    "version": "0.2.0",
    "namespace": "cgo.rulepack.airport",
    "description": "Validator and rule definitions for Airport domain.",
    "determinism": {
      "stable_sort_keys": ["kind", "id"],
      "snapshot_hash": "sha256"
    }
  },
  "bindings": {
    "validator_impls": {
      "builtin.required": "class:com.cgo.validators.RequiredFieldsValidator",
      "builtin.regex": "class:com.cgo.validators.RegexValidator",
      "builtin.nonempty": "class:com.cgo.validators.NonEmptyValidator",
      "custom.airportId": "class:com.example.validators.AirportIdValidator"
    }
  },
  "validators": [
    {
      "id": "airport.required",
      "applies_to": { "kind": "Airport" },
      "use": "builtin.required",
      "config": { "fields": ["id", "attributes"] },
      "severity": "ERROR",
      "on_fail": "reject"
    },
    {
      "id": "airport.id.regex",
      "applies_to": { "kind": "Airport" },
      "use": "builtin.regex",
      "config": { "field": "id", "pattern": "^[A-Z]{3}$" },
      "severity": "ERROR",
      "on_fail": "reject"
    }
  ],
  "execution": {
    "order": ["airport.required", "airport.id.regex"],
    "fail_fast": true
  },
  "rules": []
}
```

### 🧠 Execution Semantics

| Concept                 | Description                                                            |
| ----------------------- | ---------------------------------------------------------------------- |
| **Fail-Fast**           | Stops validation after first critical failure (`on_fail: reject`).     |
| **Determinism**         | Execution order is explicitly declared and reproducible.               |
| **Severity Hierarchy**  | `ERROR` > `WARN` > `INFO`.                                             |
| **Namespace Isolation** | A rulepack cannot affect other namespaces; no side effects.            |
| **Versioned Evolution** | Rulepacks are immutable snapshots — a new file is created per version. |

### 🧩 Declarative Rules (Preview for v0.3.0)

In later versions, the "rules" array will support inference and transformation logic, expressed declaratively.

Example (pseudo-spec):

```json
"rules": [
  {
    "id": "derive.flightEdges",
    "applies_to": { "kind": "Flight" },
    "when": [
      { "field": "from", "exists": true },
      { "field": "to", "exists": true }
    ],
    "emit": [
      { "edge": "connects", "from": "$from", "to": "$to" }
    ],
    "severity": "INFO",
    "on_fail": "skip"
  }
]
```

### 🧮 Schema Overview

| Element                    | Type    | Description                                         |
| -------------------------- | ------- | --------------------------------------------------- |
| `meta`                     | object  | Identifies rulepack scope and determinism strategy. |
| `bindings.validator_impls` | object  | Maps identifiers to implementation handles.         |
| `validators`               | array   | Declarative list of validators with configs.        |
| `execution.order`          | array   | Ordered list of validator IDs.                      |
| `execution.fail_fast`      | boolean | Whether to stop after first critical failure.       |
| `rules`                    | array   | Declarative logic for derived edges or facts.       |

### 🧩 Deterministic Rulepack Execution Flow

```text
Facts
  ↓
ValidatorRegistry
  ↓
Validation → ValidationSummary
  ↓
RulepackExecutor
  ↓
(Optionally) Rule generation and transformation
  ↓
Snapshot (deterministic hash)
```

---

### 🧭 Future Work

| Feature                         | Description                                              | Target |
| ------------------------------- | -------------------------------------------------------- | ------ |
| **Declarative Rule Definition** | Extend schema to handle rule-based transformations       | v0.3.0 |
| **Cross-namespace Inference**   | Allow rulepacks to reference facts across domains        | v0.4.0 |
| **Weighted Rule Outcomes**      | Introduce scoring and weight deltas for reasoning layers | v0.5.0 |
| **Serialization Hooks**         | Persist rulepacks and execution results (S3, DB)         | v0.5.0 |
| **AI Overlay Integration**      | Attach reasoning adapters (LLM + symbolic hybrid)        | v0.6.0 |

### 🪶 Author Note

Rulepacks transform CGO from a validation substrate into a deterministic reasoning engine.
They define what “thinking” means for a domain — not through hardcoded logic, but through transparent, versioned contracts.

**Author:** Sohil Shah (@braineous-engineering)  
**Branch:** main  
**Last Updated:** November 11, 2025
