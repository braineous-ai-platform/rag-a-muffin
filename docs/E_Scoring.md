# D. CGO Scoring Layer — Architectural Preview (Refined – 2025 Roadmap)

> **Status: Not Yet Implemented (Future CGO Module)**  
> **Audience:** Developers preparing for advanced CGO reasoning features  
> **Goal:** Understand the *planned* Scoring Layer architecture that will sit above Proposal Validation and below Mutation  
>  
> This document describes the **future** Scoring Layer for CGO.  
> No scoring logic is currently active in Alpha‑2.  
> fileciteturn2file0

---

# 1. Overview — What Scoring Will Become

Today (Alpha‑2), CGO executes:

```
Rules → Proposals → Validation → Mutation
```

In future releases, CGO will optionally support an advanced **Scoring Layer**:

```
Rules → Proposals → Validation → Scoring (future) → Mutation
```

The Scoring Layer will activate **only when multiple valid proposals exist** and CGO must choose the optimal one.

Examples of future use cases:

- Choosing best reroute among alternatives  
- Selecting optimal gate assignment  
- Deciding between alternate crew pairings  
- Weighing soft constraints (weather tolerance, operational cost)  
- LLM‑backed tradeoff reasoning (optional)

Scoring will remain **deterministic, pluggable, and optional**.

---

# 2. Position of Scoring in the Future CGO Pipeline

Planned future pipeline:

```
Rulepack Execution  →  Set<Proposal>
                 ↓
      Structural Validation (existing)
                 ↓
   Domain Validation (existing optional)
                 ↓
         SCORING LAYER (future)
                 ↓
            Mutation (existing)
```

If scoring is not configured or only one proposal exists →  
**CGO skips scoring entirely.**

---

# 3. Why Scoring Is Not Implemented Yet

CGO Alpha‑2 focuses on:

- Substrate stability  
- Deterministic rule execution  
- Proposal validation  
- Query pipeline integration  

Scoring depends on:

- Proposal competition semantics  
- Stable Proposal shapes  
- Deterministic sorting rules  
- Optional LLM integration schema  

Once these are fully frozen, scoring will be implemented safely.

---

# 4. Planned Scoring Interface (Preview)

```java
@FunctionalInterface
public interface Scorer {
    ScoringResult score(Set<Proposal> proposals, GraphView view);
}
```

This interface is **not active in Alpha‑2**,  
but reflects the expected shape of the final implementation.

A Scorer will:

- Receive the **validated** proposals  
- Inspect **GraphView**  
- Return the **winning proposal** (or ranked list)

---

# 5. Planned `ScoringResult` (Preview)

```java
public class ScoringResult {
    private final Proposal winner;
    private final List<Proposal> ranked;

    // getters, constructors
}
```

Only the **winner** will be applied.  
Ranked lists enable future analytics.

---

# 6. Planned Types of Scoring

## 6.1 Deterministic Rule-Based Scoring (Primary)

Future scorers may compute numeric weights:

```
score = delayPenalty + fuelCost + distancePenalty;
```

Lowest (or highest) score wins.

This mode will be:

- fully deterministic  
- safe  
- reproducible  
- aligned with CGO’s core principles  

---

## 6.2 Historical / Temporal Scoring (Optional Future)

CGO may support:

- rewarding consistent past decisions  
- penalizing historically unstable deltas  

This would introduce *temporal coherence* into long-running graphs.

---

## 6.3 LLM-Based Scoring (Advanced / Optional)

LLM scoring **will not be part of Alpha‑2 or Alpha‑3**,  
but may appear in later versions.

Concept:

1. Extract proposal JSON deltas  
2. Build deterministic prompt  
3. Use temperature‑0 inference  
4. Parse winner into ScoringResult  

This enables complex tradeoff reasoning that is difficult to encode manually.

---

# 7. Determinism Requirements (Core Principle)

Before Scoring is officially added, CGO will enforce:

- Stable sorting of proposals before scoring  
- Frozen JSON canonicalization  
- Temperature = 0 for any model use  
- No external mutable state  
- Idempotent scoring decisions  

Determinism is mandatory.  
Scoring will **never** introduce randomness.

---

# 8. Conceptual Example: Deterministic Scorer

```java
public class ShortestRouteScorer implements Scorer {

    @Override
    public ScoringResult score(Set<Proposal> proposals, GraphView view) {

        // Pseudocode: not implemented in Alpha-2
        Proposal winner = null;
        int bestDistance = Integer.MAX_VALUE;

        for (Proposal p : proposals) {
            int d = extractDistance(p);
            if (d < bestDistance) {
                bestDistance = d;
                winner = p;
            }
        }

        return new ScoringResult(winner, List.of(winner));
    }
}
```

---

# 9. Conceptual Example: LLM Scorer (Future Only)

```java
public class LlmScorer implements Scorer {

    private final LlmClient client; // planned integration

    @Override
    public ScoringResult score(Set<Proposal> proposals, GraphView view) {

        // Not implemented in Alpha-2
        String prompt = PromptBuilder.buildForScoring(proposals, view);
        String response = client.executePrompt(prompt); // temp=0

        Proposal winner = parse(response, proposals);

        return new ScoringResult(winner, List.of(winner));
    }
}
```

This example is **architectural only**.

---

# 10. Prompt Model (Frozen After Release)

Before scoring is released, CGO will finalize:

- canonical proposal JSON  
- deterministic proposal ordering  
- stable scoring prompt template  

Example preview:

```
You are the CGO Scorer.
Evaluate these proposals and select the best candidate.
Respond ONLY with the winning proposal ID.

Context:
{{GRAPH_VIEW_JSON}}

Proposals:
{{SORTED_CANONICALIZED_PROPOSALS}}
```

---

# 11. Integration Path (Future)

Once implemented, scoring will be invoked internally:

```java
if (proposals.size() > 1 && scorer != null) {
    ScoringResult result = scorer.score(proposals, view);
    proposals = Set.of(result.getWinner());
}
```

Today: this block does **not** exist.  
It will appear once the Scoring module is shipped.

---

# 12. Roadmap Status

| Layer                        | Status         |
|------------------------------|----------------|
| Rulepacks                    | ✔ Complete     |
| Substrate Validation         | ✔ Complete     |
| Proposal Validation          | ✔ Complete     |
| QueryPipeline Validation     | ✔ Complete     |
| **Scoring Layer**            | 🚧 *Not Implemented* |
| LLM‑Scoring                  | 🚧 *Future Optional* |

---

# 13. Summary

The Scoring Layer is a **future CGO module** designed to:

- Resolve competing proposals  
- Apply deterministic decision rules  
- Support advanced optimization  
- Optionally integrate with LLM‑based tradeoff reasoning  

Alpha‑2 does **not** include scoring logic.  
This document describes the **planned architecture** that will be implemented in a future release.

