# **Chapter D — Full Pipeline Architecture**

This chapter explains how CGO transforms a structured request into a validated reasoning result. It connects the foundational concepts introduced earlier with the internal orchestration that powers CGO’s deterministic reasoning cycle.

---

# **Table of Contents**

1. [Introduction](#1-introduction)
2. [The Pipeline at a Glance](#2-the-pipeline-at-a-glance)
3. [Anatomy of a QueryRequest](#3-anatomy-of-a-queryrequest)
4. [Phase 1: Preparation](#4-phase-1-preparation--interpreting-the-task-and-building-the-prompt)
5. [Phase 2: Execution](#5-phase-2-execution--calling-the-reasoning-engine)
6. [Phase 3: Validation](#6-phase-3-validation--ensuring-structural-and-domain-correctness)
7. [Phase 4: Final Assembly](#7-phase-4-final-assembly--producing-the-queryexecution)
8. [End-to-End Example](#8-a-high-level-end-to-end-example-walkthrough)
9. [How This Architecture Connects to Other Chapters](#9-how-this-architecture-connects-to-the-rest-of-cgo)

---

# **1. Introduction**

The CGO pipeline is the deterministic engine that receives a structured query, prepares it, invokes a reasoning model, validates the result, and assembles a transparent lifecycle record. While earlier chapters introduced the building blocks — API integration, the rulepack system, and validation contracts — this chapter explains how those parts fit together inside the execution pipeline.

For background:

- **Chapter A — API Integration**: How requests enter CGO
- **Chapter B — Rulepack**: How domain knowledge and contracts attach to requests
- **Chapter C — Validation**: How correctness is enforced

This chapter builds directly on those foundations.

---

# **2. The Pipeline at a Glance**

The CGO pipeline transforms a `QueryRequest<T>` into a `QueryExecution<T>` through four stages:

1. **Preparation** — Interpret the request and construct a structured prompt
2. **Execution** — Invoke the configured LLM orchestrator
3. **Validation** — Apply structural and domain correctness checks
4. **Final Assembly** — Produce a complete `QueryExecution` record

Each stage is deterministic, pluggable, and isolated. The pipeline itself contains no domain knowledge — all domain-specific rules arrive through metadata, rulepacks, and optional domain validators.

To understand the pipeline, we begin with the object that initiates it: the `QueryRequest`.

---

# **3. Anatomy of a QueryRequest**

Every reasoning task in CGO begins with a `QueryRequest<T>`. It contains the essential information CGO needs before it can build a prompt or evaluate domain rules.

A `QueryRequest` has four elements:

### **1. Metadata (`Meta`)**

Describes the type of reasoning task being requested — including the task name, version, and expected response shape.

### **2. The Task (`QueryTask`)**

A specific instruction such as validating an object, resolving a conflict, or answering a structured question. Tasks include human-readable intent and reference the facts involved.

### **3. Graph Substrate (`GraphContext`)**

The domain state assembled by the application. It contains all nodes relevant to the reasoning task. CGO does not fetch data on its own; it relies entirely on this substrate.

### **4. Optional Domain Rule**

Applications may attach a domain-specific validator that inspects the LLM’s raw response to enforce semantics beyond structural correctness.

The `QueryRequest` defines **what is being asked**, **what information is available**, and **how correctness should be enforced**. The pipeline transforms this input into a deterministic reasoning operation.

---

# **4. Phase 1: Preparation — Interpreting the Task and Building the Prompt**

The pipeline begins by formalizing the reasoning task into a structured prompt. Preparation ensures the system fully understands the request before invoking any external computation.

This phase has three activities:

### **1. Interpreting the Request**

Metadata identifies the reasoning task and expected output contract. The task provides human-readable intent. Together, they define the problem.

### **2. Anchoring to the Graph Substrate**

The pipeline locates the referenced facts within the `GraphContext`. These nodes contain the domain data required to ground the reasoning operation.

### **3. Constructing the Prompt**

The pipeline assembles:

- the task definition,
- the relevant nodes from the graph substrate,
- the constraints defined in the rulepack.

The result is a structured, machine-generated prompt contract designed for deterministic, repeatable LLM behavior.

---

# **5. Phase 2: Execution — Calling the Reasoning Engine**

With the prompt prepared, the pipeline invokes the configured LLM client. CGO uses a pluggable architecture:

### **1. Selecting the LLM Client**

The implementation is determined at runtime via configuration (e.g., `pipeline.json`). This keeps CGO model-agnostic.

### **2. Sending the Prompt**

The pipeline forwards the structured prompt to the orchestrator. All communication details remain isolated within the adapter layer.

### **3. Receiving the Raw Response**

The orchestrator returns an unvalidated JSON response. CGO treats this purely as data — correctness is determined in the next phase.

Execution is intentionally narrow: obtain the raw result, nothing more.

---

# **6. Phase 3: Validation — Ensuring Structural and Domain Correctness**

Validation confirms that the LLM output is structurally sound and semantically valid. CGO applies three layers:

### **1. Prompt Validation**

Occurs before execution. If the prompt fails schema checks, the pipeline halts and returns a failed `QueryExecution`.

### **2. Core Response Validation**

After the LLM returns a raw response, CGO verifies that the structure matches the expected schema defined by metadata and rulepack.

### **3. Domain Rule Validation (Optional)**

Applications may apply domain-specific validators to enforce business semantics. This runs only if earlier layers succeed.

Together, these layers guarantee that CGO’s outputs are trustworthy, deterministic, and grounded in domain rules.

---

# **7. Phase 4: Final Assembly — Producing the QueryExecution**

After validation, the pipeline produces a `QueryExecution<T>` that encapsulates the entire lifecycle:

### **1. The Original Request**

Returned unchanged for traceability.

### **2. The Raw LLM Response**

Captured as-is for inspection and auditing.

### **3. Validation Outcomes**

Includes prompt validation, core response validation, and optional domain rule validation.

### **4. Final Execution Status**

While CGO does not impose a single interpretation of success, the combination of validations and raw output allows applications to determine correctness.

The `QueryExecution` becomes the artifact consumed by scoring, history, observability, and downstream workflows.

---

# **8. A High-Level End-to-End Example (Walkthrough)**

Consider a simple airline-domain example: validating that a flight uses valid airport codes.

### **1. Ingestion (Application-Level)**

The application loads airports and flights, converts them to `Node` objects, and builds a `GraphContext`:

- Airport nodes: `AUS`, `DFW`
- Flight node: `F100` with `from = AUS`, `to = DFW`

### **2. Building the QueryRequest**

The request includes:

- `Meta("validate_flight_airports")`
- a `ValidateTask` for flight `F100`
- the graph substrate
- an optional domain rule verifying airport existence

### **3. Preparation**

The pipeline:

- interprets the metadata
- anchors to the graph
- constructs a structured prompt describing the validation problem

### **4. Execution**

The prompt is sent to the configured LLM orchestrator, which returns raw JSON.

### **5. Validation and Assembly**

CGO:

- checks structural correctness
- applies the domain rule
- builds a `QueryExecution` containing the full lifecycle

The application now has a validated, explainable reasoning result.

---

# **9. How This Architecture Connects to the Rest of CGO**

This chapter shows how CGO’s building blocks operate together inside the execution pipeline.

- **[Chapter A — API Integration](./A_API_Integration.md)** — how requests enter CGO
- **[Chapter B — Rulepack](./B_Rulepack.md)** — how domain knowledge shapes prompts and validation
- **[Chapter C — Validation](./C_Validation.md)** — how correctness is enforced

This chapter (D) reveals the internal pipeline that unifies them.

Next chapters build on this architecture:

- **Chapter E — Scorer**: how CGO computes signals, scores, and WHY() explanations.
- **Chapter F — LLM Plugin Architecture**: how orchestrators and adapters integrate cleanly.
- **History & Observability**: how reasoning is recorded, audited, and monitored.

With the architecture now established, we move from _how reasoning is executed_ to _how reasoning is interpreted and evolved_.

---

**End of Chapter D.**
