# C. CGO Validation Guide (Refined – 2025 Architecture)

> **Audience:** Java developers using or extending CGO's safety layer  
> **Goal:** Understand the 4-phase validation model that protects CGO’s graph and reasoning pipeline  
> fileciteturn1file0

---

# 1. Overview

Validation is **CGO’s safety barrier**.  
No matter what your rules output, **CGO guarantees the graph can never be corrupted**.

CGO now uses a **4-phase validation pipeline**:

```
(1) Substrate Validation
(2) Proposal Structure Validation
(3) Domain Validation (Optional)
(4) LLM Response Validation (Query Pipeline Only)
```

Only after all phases pass does CGO apply mutations to the graph.

This prevents:

- Broken relationships  
- Missing facts  
- Malformed JSON  
- Logical contradictions  
- Unsafe deltas  
- Bad rule logic  
- Invalid LLM output  

---

# 2. Validation in the CGO Pipeline

Depending on the call (bind or query), validation sits here:

```
Domain → Facts → Input → GraphBuilder.bind(...)
                       |
                       V
         Rulepack Execution  →  Proposal Set
                       |
                       V
               Validation Phases
                       |
         OK → Apply Mutation → New GraphSnapshot  
     NOT OK → BindResult.notOk(...)
```

**No mutation occurs unless validation is perfect.**

---

# 3. Phase 1 — Substrate Validation (GraphBuilder Level)

This validates the **incoming Input**, before any rules run.

Checks include:

### 3.1 Fact-Level Safety
- `Fact.id` cannot be null  
- JSON payload must be valid  
- Fact mode must be recognized (`atomic`, `relational`, etc.)

### 3.2 Relationship Construction
For relational Facts:
- `from` and `to` Facts must exist  
- They must have stable IDs  
- Relationship Fact JSON must be valid

### 3.3 Graph Topology
- No self-loops unless explicitly allowed  
- No orphan edges  
- No malformed Input triples  

If substrate invalid → **Rulepack never runs**.

---

# 4. Phase 2 — Proposal Structure Validation (ProposalValidator)

After Rulepack execution, CGO inspects the **Set<Proposal>**.

Core responsibilities:

### 4.1 Internal Integrity
- Proposal type must be valid  
- Required fields must be present  
- No null IDs  
- JSON payload of proposed mutations must be valid

### 4.2 Cross-Proposal Consistency
- No conflicting updates  
- No contradictory deltas  
- No duplicate mutations  
- Mutations must be idempotent and well-formed

### 4.3 Topological Safety
- Mutations cannot create orphaned nodes  
- Edge updates must remain resolvable  
- Node deletions must not break relationships (unless allowed)

**This is the strongest safety layer.**  
It prevents bad rules from causing damage.

---

# 5. Phase 3 — Domain Validation (Optional Extension)

Your code may attach **domain-specific validators**.

CGO exposes two extension interfaces:

### 5.1 FactValidatorRule

```java
@FunctionalInterface
public interface FactValidatorRule {
    ValidationResult validate(Fact fact, GraphView view);
}
```

Examples:
- Flight must have `from` and `to`  
- Airport code must be IATA-valid  
- Crew must have required qualifications  

Return:
- `ValidationResult.ok()`
- or descriptive failure

---

### 5.2 RelationshipValidatorRule

```java
@FunctionalInterface
public interface RelationshipValidatorRule {
    ValidationResult validate(Relationship rel, GraphView view);
}
```

Examples:
- `from != to` for any flight  
- Turnaround time edges must include `taMinutes >= 0`  

Domain validators run **after Proposal structure validation**, ensuring the rules’ proposed changes also respect domain constraints.

---

# 6. Phase 4 — LLM Response Validation (QueryPipeline Only)

For QueryPipeline requests:

1. PromptBuilder generates a prompt  
2. LLM returns structured JSON  
3. **PromptOutputValidator** validates:
   - required fields  
   - schema correctness  
   - JSON syntax  
   - response type restrictions  

4. DomainValidation (optional) may run on the LLM output  

If invalid → CGO returns:

```
QueryExecution(promptValidationError)
```

No graph mutation occurs in Query mode.

---

# 7. BindResult (Unified Validation Result)

All validation failures across **all 4 phases** ultimately return a **BindResult.notOk(...)** or `ValidationResult` (Query mode).

Example:

```java
if (!result.isOk()) {
    log.error("CGO rejected update: " + result);
    return result;
}
```

BindResult may contain:

- error message  
- field name  
- involved Fact IDs  
- proposal IDs  
- conflict report  
- schema validation errors (LLM path)

This unified interface simplifies debugging.

---

# 8. Full Validation Flow Diagram

```
                Rulepack Output
                (Set<Proposal>)
                        |
                        V
             +----------------------+
             | ProposalValidator    |
             |  - Structure checks  |
             |  - Fact integrity    |
             |  - Topology safety   |
             +----------------------+
                        |
             OK         |        FAIL
                        |------> BindResult.notOk(...)
                        V
         +-----------------------------+
         | Domain Validators (optional)|
         +-----------------------------+
                        |
             OK         |        FAIL
                        |------> BindResult.notOk(...)
                        V
         +-----------------------------+
         |  Apply Mutation to Graph    |
         +-----------------------------+
```

---

# 9. Why CGO’s Validation Model Matters

- Guarantees **graph safety**  
- Allows **risky domain logic** without fear  
- Rules can be complex — the validator protects the system  
- Ensures **cross-rule determinism**  
- Makes CGO safe for:
  - multi-tenant environments  
  - LLM-assisted reasoning  
  - real-time ingest pipelines  
  - batch data ingestion  

Validation is the **immune system** of CGO.

---

# 10. Summary

CGO ensures correctness through 4 layers:

1. **Substrate validation** (Facts & Input)
2. **Proposal structure validation** (core safety)
3. **Domain validation** (optional, pluggable)
4. **LLM response validation** (QueryPipeline)

Only when all succeed does CGO mutate the graph.

This gives your system **deterministic**, **safe**, and **predictable** behavior every time.
