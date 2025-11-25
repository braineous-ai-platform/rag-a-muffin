# D. CGO Scoring & LLM Integration Guide

> **Audience:** Developers extending CGO with advanced reasoning using scoring, historical weights, or LLMs
> **Goal:** Understand how CGO evaluates competing proposals, integrates with scoring models (including LLMs), and preserves determinism

---

## 1. Overview

The **Scoring Layer** is responsible for deciding **which proposals should be preferred** when multiple valid reasoning paths exist.

CGO supports:

- **Rulepack-level scoring** (ranking competing proposals)
- **Historical scoring** (weights across snapshots)
- **LLM-driven scoring** (optional, pluggable)

This layer is **not required** for all Rulepacks.

- If a Rulepack produces **0 or 1** proposal, scoring is not invoked.
- If a Rulepack produces **multiple competing proposals**, CGO resolves them using the Scorer.

The Scoring Layer becomes essential for:

- Ambiguous domains (e.g., route optimization)
- Preference-sensitive behaviors (e.g., airline priorities)
- LLM-augmented reasoning

---

## 2. Position of Scoring in the CGO Pipeline

The full CGO pipeline becomes:

```
Rules → Proposals → Validation → (Scoring) → Mutation
```

Scoring executes **after** structural validation and **before** mutation.

---

## 3. When Scoring Applies

### Scoring is used when:

- The Rulepack returns **multiple proposals** that represent alternative deltas
- Proposals are **mutually exclusive** (cannot be applied simultaneously)
- Domain rules require **prioritization** or **optimality**

### Scoring is **NOT** used when:

- There is only one proposal
- All proposals are compatible and can be merged
- Rulepack semantics define no competition

This keeps scoring lightweight and avoids unnecessary complexity.

---

## 4. The Scorer Interface

The Scorer is a pluggable component:

```java
@FunctionalInterface
public interface Scorer {
    ScoringResult score(Set<Proposal> proposals, GraphView view);
}
```

Where:

- `proposals` = the set of validated proposals
- `view` = the current graph snapshot

The Scorer returns a `ScoringResult`, typically:

- a single **winning** proposal, or
- a **ranked list** of proposals

---

## 5. ScoringResult

A simple representation may look like:

```java
public class ScoringResult {
    private final Proposal winner;
    private final List<Proposal> ranked;

    // getters, constructors
}
```

CGO only needs the **winner**, but ranked lists are helpful for analytics.

---

## 6. Types of Scoring

CGO supports three levels of scoring:

### 6.1 Deterministic Rule-Based Scoring

Rules may express preference weights:

- lower fuel usage preferred
- shorter route preferred
- fewer hops preferred

The Scorer computes a numeric score per proposal:

```java
proposalScore = distanceWeight + fuelCostWeight + delayPenalty;
```

Lowest score wins.

---

### 6.2 Historical Scoring (Snapshot Memory)

CGO can consider **historical snapshot metrics**, enabling consistent behavior across time.

Examples:

- proposals that repeat past beneficial decisions receive positive weighting
- proposals that caused issues in past snapshots receive penalties

This creates **temporal continuity** in reasoning.

---

### 6.3 LLM-Based Scoring (Advanced)

LLM scoring is optional and pluggable.

In this mode, the Scorer:

1. Extracts JSON deltas from each proposal
2. Builds a **structured prompt** describing the competing proposals
3. Sends the prompt to a model (GPT, Claude, etc.)
4. Interprets the result as a ranking or direct selection

LLM scoring is appropriate for:

- complex tradeoffs
- multi-factor reasoning
- natural language constraints
- scenarios where business preferences cannot be fully coded

All LLM interactions MUST remain deterministic at CGO level:

- temperature = 0
- stable prompt format
- stable ordering of proposals

---

## 7. Ensuring Determinism in Scoring

Scoring must never introduce randomness.

To guarantee deterministic outcomes:

- Proposal set is **sorted** by stable fields (ID, hash)
- Prompt templates (for LLMs) are **frozen**
- No sampling-based randomness (temperature = 0)
- Scoring rules must not depend on external mutable state

If these rules are followed, scoring is perfectly deterministic.

---

## 8. Example Scorer (Deterministic)

```java
public class ShortestRouteScorer implements Scorer {

    @Override
    public ScoringResult score(Set<Proposal> proposals, GraphView view) {

        Proposal best = null;
        int bestDistance = Integer.MAX_VALUE;

        for (Proposal p : proposals) {
            int distance = extractDistance(p); // parse JSON
            if (distance < bestDistance) {
                best = p;
                bestDistance = distance;
            }
        }

        return new ScoringResult(best, List.of(best));
    }
}
```

This scorer simply chooses the shortest route.

---

## 9. Example LLM-Backed Scorer (Conceptual)

```java
public class LlmScorer implements Scorer {

    private final LlmClient client; // wrapper around GPT/Claude

    @Override
    public ScoringResult score(Set<Proposal> proposals, GraphView view) {

        String prompt = PromptBuilder.build(proposals, view);
        String response = client.complete(prompt, Temperature.ZERO);

        Proposal winner = parseWinnerFrom(response, proposals);

        return new ScoringResult(winner, List.of(winner));
    }
}
```

The prompt builder ensures stable JSON ordering:

- sort proposals by ID
- canonicalize JSON
- deterministic prompt format

---

## 10. Prompt Wiring Model (Stable)

LLM scoring requires a **frozen schema**:

- Unified proposal JSON format
- Stable prompt template
- Stable ordering of competing proposals

Example (conceptual prompt):

```
You are the CGO Scorer. Evaluate the following proposals.
Each proposal contains JSON describing the delta.
Choose the most operationally efficient outcome.

Context:
{{GRAPH_VIEW_JSON}}

Proposals:
{{SORTED_PROPOSAL_LIST}}

Respond with the ID of the winner.
```

This template never changes once published.

---

## 11. Integration with CGO

CGO invokes scoring only when needed:

```java
if (proposals.size() > 1) {
    ScoringResult scoring = scorer.score(proposals, view);
    Proposal winner = scoring.getWinner();
    proposals = Set.of(winner);
}
```

After scoring, CGO proceeds to the mutation phase.

---

## 12. Scoring Checklist

### ✔ Scorer interface implemented

### ✔ Proposal set is sorted before scoring

### ✔ Deterministic scoring logic

### ✔ Temperature = 0 for LLM calls

### ✔ Stable prompt schema

### ✔ Historical weighting optional but deterministic

### ✔ Only used when proposals conflict

When all of the above are satisfied, CGO scoring remains predictable, transparent, and robust.

---
