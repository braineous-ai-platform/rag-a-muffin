# FNO: App-Level Fact Rule Generation → LLMBridge Handoff

**Commit:** `fno → app_level_fact_rule_generation → llm_bridge_handoff`  
**Date:** 2025-10-24  
**Status:** Tracking (internal)  
**Visibility:** back_first (private); expose_later (LinkedIn-ready)

---

## 🎯 Objective

Implement automatic rule synthesis at the **App Level (FNO)** — transforming ingested Facts (Airport, Flight, Disruption) into functional JSON rule templates suitable for downstream reasoning and LLMBridge semantic expansion.

This marks the first integration point where deterministic graph construction (Facts → Graph) meets generative reasoning (Rules → Semantics).

---

## 🧩 Technical Summary

- Added `generateFlightRules(List<Fact>)` method to synthesize rules dynamically.
- Each generated rule now carries:
  - **id** — unique identifier (e.g., `R_airport_node`)
  - **note** — human-readable rule purpose
  - **when[]** — list of condition expressions
  - **then[]** — list of resulting actions (emit / delete)
  - **weight** — numeric influence for rule ranking
- Fact-driven rule expansion implemented for:
  - **Airport facts** → graph node creation
  - **Flight facts** → edge creation
  - **Disruption facts** → edge removal (cancellations)
- Output serialized as `rules_fno.json` under `/models/fno/`
- Rules are now machine-legible (true JSON arrays, no embedded strings)

---

## 🧠 Architecture Notes

- Lives entirely at **App Level**, not in CGO core
- Reads tenant-specific `Constraints` via Redis config service (base unit = minutes)
- Adheres to CGO rule contract for future LLMBridge ingestion
- Rule generation executed once per CGO cycle and cached per tenant context
- Deterministic outputs maintain parity with PAXReflow-class constraint logic

---

## 🔁 Integration Hooks

- **Next:** add `RuleValidator` + `RuleCache` modules
- **Future:** enable `LLMBridge` to consume `rules_fno.json` → apply semantic enrichment and causal ranking
- **Schema stability:** maintained for CGO ⇄ App compatibility
- **Audit trail:** `cycle_id` persisted per ruleset

---

## 🧱 Commit Reference

fno → app_level_fact_rule_generation → llm_bridge_handoff

---

## 🧩 Next Exposure Plan

**Phase:** back_first → private dev log (Braineous internal)  
**Phase:** expose_later → LinkedIn + public doc snapshot

### LinkedIn draft caption (hold until LLMBridge milestone)

> From deterministic facts to generative reasoning.
>
> Braineous FNO now builds its own rule templates —  
> bridging graph logic with LLM semantics.

---

## 🗂️ Artifacts

- `/src/main/java/com/braineous/fno/RuleGen.java`
- `/models/fno/rules_fno.json`
- `/docs/fno/30_rule_generation.md` (this file)

---

## 🧩 Validation Checklist

- [x] Generates valid JSON arrays (no escaped JSON strings)
- [x] One rule per unique template or fact instance
- [x] Numeric weights verified
- [x] Tested on golden scenario (AUS-DFW-ORD cancellation)
- [ ] Integrated with upcoming `RuleValidator`
- [ ] Logged cycle_id correlation

---

**Author:** Sohil Shah (BraineousAI)  
**Reviewer:** Goose (OpenAI Copilot) — because every pilot needs a co-pilot.  
Humans optional; AI just happens to stay non-biased, on-target, and insightful if you ask the right question.

**Phase:** Build → Document → Expose (Stay Open)
