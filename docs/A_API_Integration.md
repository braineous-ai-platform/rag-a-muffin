# **Chapter A — CGO API Integration Guide (2025 Architecture)**

## **Table of Contents**

1. [Introduction](#1-introduction)
2. [Integration Boundary (Public API vs Engine Internals)](#2-integration-boundary-public-api-vs-engine-internals)
3. [CGO’s Data Model (Facts, Relationships, and the Internal Graph)](#3-cgos-data-model-facts-relationships-and-the-internal-graph)
4. [Developer Input Model (How You Provide Data to CGO)](#4-developer-input-model-how-you-provide-data-to-cgo)
5. [Ingesting Data Into CGO (How GraphBuilder Processes Your Input)](#5-ingesting-data-into-cgo-how-graphbuilder-processes-your-input)
6. [Using Rulepacks for Domain Reasoning](#6-using-rulepacks-for-domain-reasoning)
7. [Developer Workflow (How Your Service Interacts With CGO)](#7-developer-workflow-how-your-service-interacts-with-cgo)
8. [Mapping CGO Output Back Into Your Domain](#8-mapping-cgo-output-back-into-your-domain)
9. [JSON Payload Model (Opaque, Flexible, and Developer-Controlled)](#9-json-payload-model-opaque-flexible-and-developer-controlled)
10. [Putting It All Together (Canonical End-to-End Example)](#10-putting-it-all-together-canonical-end-to-end-example)

---

# **1. Introduction**

CGO provides a **deterministic graph-based reasoning engine** that your application can call using a **small, JSON-first integration surface**. You do **not** build graphs, manage edges, or orchestrate reasoning loops. Instead, you supply:

- **Facts** (your domain objects as JSON)
- **Relationships** (how those facts connect)
- **Rulepacks** (your domain reasoning logic)

CGO takes over:

- validating your data
- constructing and updating a causal graph
- applying rule-based domain logic
- performing safe mutations
- returning a consistent, versioned snapshot

From an integration perspective:

**You send Facts + Relationships → CGO validates + reasons → returns BindResult + GraphView.**

CGO becomes a drop-in, domain-agnostic reasoning substrate your service can embed anywhere.

---

# **2. Integration Boundary (Public API vs Engine Internals)**

CGO exposes four **public API concepts**:

## Public API Concepts

- **Fact** — atomic domain data
- **Relationship** — domain-level triple
- **Rulepack** — domain reasoning logic
- **GraphView** — read-only snapshot of current graph state

These form your _mental model_.

## Engine Internal Concepts

(You use them, but do not architect around them.)

- **GraphBuilder** — ingestion/validation/mutation engine
- **BindResult** — structured outcome of a submission
- **ProposalMonitor / Validators** — internal safety systems
- **SnapshotHash / VersionControl / Why** — internal integrity & tracing

This separation ensures engine evolution without breaking your integrations.

---

# **3. CGO’s Data Model (Facts, Relationships, and the Internal Graph)**

## 3.1 Facts become graph nodes

Each Fact becomes a node in CGO’s internal causal graph.  
CGO treats the JSON payload as opaque — rules interpret meaning.

## 3.2 Relationships become graph edges

A `Relationship(from, to, edgeFact)` defines the domain connection.  
CGO derives edge structure automatically.

## 3.3 Rulepacks extend and validate the graph

Rulepacks read from a `GraphView` snapshot and propose `WorldMutation`s.

## 3.4 CGO maintains a deterministic, versioned graph

Mutations update nodes, edges, attributes, and version counters.  
Snapshots ensure safe, reproducible reads.

## 3.5 Two layers of safety

- **Substrate validation**
- **Reasoning validation**

## 3.6 Developer mental model

**Facts → Relationships → Rulepack → GraphView**

---

# **4. Developer Input Model (How You Provide Data to CGO)**

## 4.1 Facts (domain objects → Facts)

```java
Fact airport = new Fact("Airport:AUS", toJson(domainAirport));
```

## 4.2 Relationships (domain logic → graph links)

```java
Relationship r = new Relationship(fromFact, toFact, edgeFact);
```

## 4.3 Grouping submissions

```java
BindResult result =
    graphAdapter.bind(graphBuilder, facts, relationships, rulepack);
```

## 4.4 Your application does not build graphs

No manual graph construction. CGO handles everything.

## 4.5 Benefits

- schema freedom
- multi-tenant flexibility
- LLM compatibility
- simple model

---

# **5. Ingesting Data Into CGO (How GraphBuilder Processes Your Input)**

Pipeline:

```
Facts + Relationships
        ↓
Structural Validation
        ↓
Rulepack (domain reasoning)
        ↓
ProposalMonitor (safety)
        ↓
Mutation
        ↓
BindResult + GraphView
```

## 5.1 Phase 1 — Substrate Validation

FactValidators + RelationshipValidators ensure structural correctness.

## 5.2 Phase 2 — Rulepack Execution

Rules read `GraphView` and propose mutations.

## 5.3 Phase 3 — ProposalMonitor

Validates all proposals.

## 5.4 Phase 4 — Mutation

Only safe proposals are applied.

## 5.5 Output

- **BindResult**
- **GraphView**

---

# **6. Using Rulepacks for Domain Reasoning**

## 6.1 Purpose

Enforce constraints, infer new data, derive relationships, compute scores.

## 6.2 BusinessRule signature

```java
WorldMutation execute(GraphView view);
```

## 6.3 Optional but powerful

Without Rulepacks → structural updates only.  
With Rulepacks → real domain intelligence.

## 6.4 Execution model

Snapshot → Rulepack → Proposals → ProposalMonitor → Mutation.

## 6.5 Dynamic selection

Often based on API context or domain event type.

## 6.6 Authoring guidelines

- use GraphView
- stay side-effect free
- small rules
- validate early

---

# **7. Developer Workflow (How Your Service Interacts With CGO)**

1. Receive domain event
2. Map to Facts
3. Map to Relationships
4. Select Rulepack
5. Submit to CGO
6. Inspect BindResult
7. Optionally inspect GraphView
8. Produce final output

---

# **8. Mapping CGO Output Back Into Your Domain**

## 8.1 BindResult

Indicates success/failure.

## 8.2 GraphView

Read-only domain state after reasoning.

## 8.3 Mapping back to domain

```java
Flight f = fromJson(view.getFactById("Flight:UA123").getText());
```

## 8.4 When to use GraphView

When inferred/enriched data matters.

## 8.5 Output Pair

- BindResult
- GraphView

---

# **9. JSON Payload Model (Opaque, Flexible, Developer-Controlled)**

## 9.1 Opaque JSON

CGO does not parse your schema.

## 9.2 Why opaque?

- schema evolution
- multi-tenant flexibility
- LLM compatibility
- domain independence

## 9.3 Edge Facts also carry JSON

Anything your rules need.

## 9.4 Semantics live in Rulepacks

Rules interpret JSON.

## 9.5 Engine checks structure, not schema.

## 9.6 LLM-driven flows supported

LLMContext can generate Facts safely.

---

# **10. Putting It All Together (Canonical End-to-End Example)**

## 10.1 Receive event

```java
FlightRerouteEvent event = parse(requestBody);
```

## 10.2 Map → Facts

```java
Fact flight = new Fact("Flight:" + event.flight().id(), toJson(event.flight()));
```

## 10.3 Map → Relationship

```java
Relationship r = new Relationship(fromAirport, toAirport, flight);
```

## 10.4 Select Rulepack

```java
Rulepack rulepack = rulepackSelector.forContext("REROUTE");
```

## 10.5 Submit

```java
BindResult result = graphAdapter.bind(graphBuilder,
                                      facts,
                                      relationships,
                                      rulepack);
```

## 10.6 Check BindResult

```java
if (!result.isOk()) return RoutingResult.failed(...);
```

## 10.7 Read snapshot

```java
GraphView view = graphBuilder.snapshot();
```

## 10.8 Produce response

```java
return RoutingResult.success(fromJson(view.getFactById("Flight:UA123").getText()));
```

## 10.9 Summary

Service view:

```
Facts → Relationships → Rulepack → CGO → BindResult → GraphView → Output
```

CGO internal:

```
Input → Validator → Rulepack → Proposals → ProposalMonitor → Mutation → Snapshot
```

## 10.10 Guarantees

- deterministic graph evolution
- domain-driven reasoning
- strict safety
- stable API
- versioned snapshots
- schema flexibility
