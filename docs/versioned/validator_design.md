# 🧮 CGO Validator Design — Lambda-Based Validation Architecture

Defines the lambda-based validator system that enforces data consistency before graph reasoning layers execute.

**Author:** Sohil Shah (@braineous-engineering)  
**Date:** November 11, 2025  
**Repo:** [BraineousAI / rag-a-muffin](https://github.com/braineous-ai-platform/rag-a-muffin)

---

## 🔖 Overview

This document details the **Validator Architecture** within the **Causal Graph Orchestrator (CGO)**.  
Validators are lightweight, lambda-based functions that ensure **data consistency and domain integrity** before facts are committed to the graph substrate.

CGO separates the concept of _validation_ (what data is allowed into the graph) from _reasoning_ (how that data is later transformed or inferred).  
This ensures that the substrate remains **deterministic and predictable**, while validation logic stays modular and domain-specific.

---

## ⚙️ Validator Philosophy

| Principle           | Description                                                               |
| ------------------- | ------------------------------------------------------------------------- |
| **Lambda-first**    | Validators are pure, composable functions; no side effects.               |
| **Data-driven**     | Each validator can be configured declaratively (JSON/YAML).               |
| **Pluggable**       | Custom validators can be injected at runtime via the Rulepack loader.     |
| **Deterministic**   | Validation order and severity are explicitly defined, not inferred.       |
| **Domain-isolated** | Validators apply within their namespaced context (e.g., Airport, Flight). |

---

## 🧩 Core Interface

The validator contract within the CGO substrate is intentionally minimal:

```java
@FunctionalInterface
public interface FactValidator {
    ValidationResult validate(Fact fact, Map<String, Object> config);

    record ValidationResult(boolean ok, String code, String message, String severity) {
        public static ValidationResult ok() {
            return new ValidationResult(true, null, null, "INFO");
        }
        public static ValidationResult fail(String code, String message, String severity) {
            return new ValidationResult(false, code, message, severity);
        }
    }
}
```

### 📌 Key Points

Each validator is stateless and thread-safe.

Config parameters (e.g., regex patterns, required fields) are passed dynamically.

Severity levels (INFO, WARN, ERROR) define post-validation handling.

### 🧠 Validator Lifecycle

- Definition: Declared in the Rulepack manifest (JSON).

- Binding: Mapped to an implementation via the ValidatorRegistry.

- Execution: Invoked against each fact in deterministic order.

- Aggregation: Results collected into a ValidationSummary.

- Decision: Depending on severity and fail-fast rules, facts are accepted or rejected.

```text
Fact → [ ValidatorRegistry ] → [ FactValidator ] → ValidationResult → RulepackExecutor
```

### 🧰 Built-in Validators (Planned for v0.3.0)

| ID                 | Purpose                             | Config Example                               |
| ------------------ | ----------------------------------- | -------------------------------------------- |
| `builtin.required` | Ensures presence of required fields | `{ "fields": ["id", "attributes"] }`         |
| `builtin.regex`    | Validates format via regex          | `{ "field": "id", "pattern": "^[A-Z]{3}$" }` |
| `builtin.nonempty` | Checks lists/strings for content    | `{ "field": "attributes" }`                  |
| `builtin.unique`   | Ensures unique IDs within namespace | `{ "scope": "namespace:Airport" }`           |
| `custom.airportId` | Custom whitelist example            | `{ "allow_list": ["AUS","DFW","JFK"] }`      |

### 🔩 Validator Registry

The registry dynamically binds declarative validator IDs to implementation classes.

```java
public final class ValidatorRegistry {
    private final Map<String, FactValidator> impls = new HashMap<>();

    public void register(String key, FactValidator validator) {
        impls.put(key, validator);
    }

    public FactValidator get(String key) {
        var v = impls.get(key);
        if (v == null) throw new IllegalStateException("No validator for key: " + key);
        return v;
    }
}
```

This allows validators to be loaded from:

Static code bindings,

Dependency Injection contexts, or

Future dynamic plug-ins.

### 🧩 Integration with Rulepack Layer

Validators are declared, not hardcoded.
In RulepackTemplate.json, each validator is defined as a JSON object with:

- id

- applies_to

- use

- config

- severity

- on_fail

```json
{
  "id": "airport.id.regex",
  "applies_to": { "kind": "Airport" },
  "use": "builtin.regex",
  "config": { "field": "id", "pattern": "^[A-Z]{3}$" },
  "severity": "ERROR",
  "on_fail": "reject"
}
```

### 🔬 Deterministic Execution Model

The execution order and behavior are explicitly defined in the Rulepack manifest:

```json
"execution": {
  "order": ["airport.required", "airport.id.regex", "airport.nonempty"],
  "fail_fast": true
}
```

Properties:

- Validators always execute in the declared order.

- Fail-fast prevents cascading errors for deterministic replay.

- Every validation result is recorded for reproducibility.

### 📊 Validation Summary (concept)

```java
record ValidationSummary(List<ValidationIssue> issues) {
    static ValidationSummary from(List<ValidationIssue> list) {
        return new ValidationSummary(List.copyOf(list));
    }
}
```

Each ValidationIssue captures:

```
factId
```

```
validatorId
```

```
code
```

```
message
```

```
severity
```

### 🧭 Future Extensions

| Feature                        | Description                                                           | Target Version |
| ------------------------------ | --------------------------------------------------------------------- | -------------- |
| **Dynamic Validator Plugins**  | Load validators via reflection or SPI service loader                  | v0.4.0         |
| **Cross-namespace Validation** | Supports reasoning across related fact kinds (e.g., Flight ↔ Airport) | v0.4.0         |

### 🪶 Author Note

Validators are the first bridge between data and reasoning in CGO.
They provide guardrails without biasing the reasoning layer, allowing both deterministic and probabilistic logic to coexist gracefully in future releases.

**Author:** Sohil Shah (@braineous-engineering)  
**Branch:** main  
**Last Updated:** November 11, 2025
