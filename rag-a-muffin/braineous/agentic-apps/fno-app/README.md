# FNO — Flight Network Orchestrator

This module demonstrates how flight data flows through the system end-to-end:
from ingestion, to graph substrate construction, to query execution, to observation.

No prior context is required.
Run it, observe the output, then explore the code.

---

## Quick start

### 1) Start the backend

```bash
./run.sh
```

This starts the FNO backend locally (Docker).

---

### 2) Run the smoke test

```bash
./smoke.sh
```

This executes a complete black-box flow:

* data ingestion
* graph substrate construction
* query execution
* history and scoring observation

---

## What to look for in the output

When running `smoke.sh`, read the console output carefully.

### Ingest

You will see:

* the ingest request payload
* a `GraphSnapshot` response

  * nodes (e.g. `Airport:*`, `Flight:*`)
  * inferred relationships between flights

This shows how the **graph substrate is formed from data**.

---

### Query

You will see:

* a task-level request
* a structured response indicating whether the task is valid

At this stage, query execution is **deterministic and rule-based**.
LLM-backed reasoning is **not yet integrated** and will be introduced in a future release.

---

### Observe

You will see:

* a `why_snapshot`

  * total events
  * scoring aggregates

This shows how **system history accumulates over time**.

---

## Data

`flights.json` is intentionally kept at the repository root.

* it matches the ingest contract
* it is easy to modify
* changing it and re-running `smoke.sh` immediately reflects in the output

---

## Notes

* `smoke.sh` is the single source of truth for current system behavior
* advanced reasoning capabilities are intentionally out of scope for this version
* this README focuses on **observable behavior**, not internal implementation

---

## Supported environment

* macOS
* Docker Desktop
* bash + curl (preinstalled)

Windows is not supported.

---

Run it first.
Then explore the code.
