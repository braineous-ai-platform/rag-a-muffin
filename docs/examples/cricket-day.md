# Cricket Day — CGO Walkthrough

**Goal:** show CGO’s spine: **Facts → Rules → Reasoning Path → Closure** using a tiny cricket scenario.

## Flow (what CGO does)

1. **Facts** → load into `ReasoningContext`
2. **Rules** (causal/evidence/temporal) fire → produce `Edge`s
3. **Orchestrator** plans/walks edges → builds `ReasoningPath`
4. **(Optional) Skills** called only if path asks (LLM/tools)
5. **Closure** → pass/fail + trace (facts, rules fired, path)

## Concept → Example mapping

- **Facts:** `excellent_cricket_game`, `effort`, `prep`
- **Rules:**
  - `R1: explains` → (effort → performance)
  - `R2: implies` → (performance → win)
- **Path:** `effort, prep → performance → win → trophy → fun_evening`
- **Closure:** `accomplished_day = true`

## Run the unit test

```bash
mvn -q -Dtest=CausalOrchestratorTests test
```

{
"facts": ["excellent_cricket_game","effort","prep"],
"rulesFired": ["R1:explains","R2:implies"],
"path": ["effort","performance","win","trophy","fun_evening"],
"closure": "accomplished_day",
"pass": true
}

### Files (where things live)

1. src/main/java/.../models/Fact.java

2. src/main/java/.../models/Edge.java

3. src/main/java/.../models/ReasoningContext.java

4. src/main/java/.../models/ReasoningPath.java

5. src/main/java/.../services/CausalOrchestrator.java

6. src/test/java/.../services/CausalOrchestratorTests.java ← this test

---

## Status & daily wrap (Oct 15, 2025)

**Status:** In progress. Cricket demo proves the CGO spine (**Facts → Rules → Path → Closure**).

**Today’s wrap:** unit test passes (`CausalOrchestratorTests`), minimal trace shape documented.

---

## Next up — CTA

**Goal:** move from demo → usable API + public traction.

### 1) Wire relationships (today → tomorrow)

- Expose `/cgo/relationships/resolve` and `/cgo/relationships/example/cricket-day`
- Input: `facts[] + rules[]` → Output: `edges[] + path + trace`
- Add minimal **OpenAPI** spec at `src/main/resources/openapi.yaml`

### 2) Ship docs & example

- Link this page from `README.md` under **Examples**
- Add a short “How CGO works” diagram + one-liner
- Keep **LLM optional**; deterministic first

### 3) Publish & traction

- Tag `1.0.0-alpha.1` (docs-first drop)
- Post on LinkedIn:
  - Headline: “CGO α1 (Java/Spring): Facts → Rules → Path → Closure”
  - First comment: repo link + quickstart snippet
- Pin the post; reply to first 10 comments quickly

### 4) Prep next sink app (FNO)

- Create folder **`sink/fno/`** with `README.md` (pricing micro-moves, capacity, disruption)
- Sketch rule packs: `demand_elasticity.yaml`, `capacity_swap.yaml`, `disruption_recover.yaml`
- Define APIs: `/fn/plan | /fn/simulate | /fn/apply | /fn/measure` (spec stub only)

### 5) Guardrails & ops (lightweight)

- Add **provenance trace** fields (facts, rules fired, why[], path)
- Add **budget/caching toggles** in the gateway (env flags)
- Log costs/latency per step (simple counters)

**Definition of “done” for this phase:**  
Public endpoints live, OpenAPI checked in, docs linked, post published, FNO stub visible. No extra ceremony—ship and iterate.

> This page is the wrap-up for **Oct 15, 2025**. Further changes continue in the next daily thread/file.
