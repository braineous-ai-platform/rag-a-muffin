# A. CGO API Integration Guide

> **Audience:** Java developers integrating their application with CGO
> **Goal:** Convert domain-level JSON → CGO graph inputs → get deterministic reasoning → receive deltas/results

---

## 1. Overview

This document explains **how an external application integrates with CGO** using a **thin, JSON-first API**.
Your application never needs to understand CGO internals; it only needs to:

1. Prepare **Facts** and **Edges** (simple JSON containers)
2. Provide a **GraphView** for reasoning
3. Choose a **Rulepack**
4. Let CGO run:
   **rules → proposals → validation → mutation**

Everything else stays encapsulated within CGO.

---

## 2. The Integration Boundary (The Only Things the App Touches)

Your application interacts with only **five** CGO concepts:

### 2.1 Fact

- Represents an **atomic piece of domain state**
- Stores **id** + **payload** (opaque JSON string)

### 2.2 Edge

- Represents a **relationship between two Facts**
- Stores **id**, **fromId**, **toId**, and **payload** (opaque JSON string)

### 2.3 GraphView

- Read-only snapshot that rules see
- Usually implemented via `GraphSnapshot(Map<String,Fact>, Map<String,Edge>)`

### 2.4 Rulepack

- A container of **BusinessRule** functions
- Each rule takes a `GraphView` and produces a `Proposal`

### 2.5 Pipeline Monitor (Entry Point)

- Executes the complete flow:

  1. Run rules
  2. Validate proposals
  3. Mutate graph
  4. Return `BindResult`

Everything else (validators, mutation, scoring, history) stays internal.

---

## 3. Data Flow (App → CGO → App)

```
               +---------------------------+
               |       Your Service        |
               |  (Domain Events / JSON)   |
               +-------------+-------------+
                             |
                             | FactExtractor
                             v
                 +-----------+------------+
                 |      GraphInput        |
                 | (Facts & Edges)        |
                 +-----------+------------+
                             |
                             | GraphRepository / Snapshot
                             v
                +------------+-------------+
                |          CGO             |
                |    GraphView (Snapshot)  |
                |    Rulepack              |
                |    Pipeline Monitor       |
                +------------+-------------+
                             |
                             | BindResult / Deltas
                             v
                 +-----------+------------+
                 |     Your Service       |
                 |   (Interpret Result)   |
                 +------------------------+
```

---

## 4. Fact Extraction (Your Only Required Mapping)

You write **one function per domain event** that knows how to convert domain JSON → Fact/Edge JSON.

### 4.1 Interface

```java
@FunctionalInterface
public interface FactExtractor<D> {
    GraphInput extract(D domainEvent);
}
```

### 4.2 Example

```java
public class FlightRerouteFactExtractor
        implements FactExtractor<FlightRerouteEvent> {

    @Override
    public GraphInput extract(FlightRerouteEvent event) {

        Fact flight = new Fact(
            "FLIGHT:" + event.flight().id(),
            toJson(event.flight())
        );

        Fact fromAirport = new Fact(
            "AIRPORT:" + event.from().code(),
            toJson(event.from())
        );

        Fact toAirport = new Fact(
            "AIRPORT:" + event.to().code(),
            toJson(event.to())
        );

        Edge reroute = new Edge(
            "ROUTE:" + event.flight().id(),
            fromAirport.id(),
            toAirport.id(),
            toJson(event.routeChange())
        );

        return new GraphInput(
            Set.of(flight, fromAirport, toAirport),
            Set.of(reroute)
        );
    }
}
```

Your app is **done** after producing this.

---

## 5. Building the GraphView (Thin Snapshot)

The app typically maintains a **GraphRepository** or simply builds per-request snapshots.

```java
GraphView view = new GraphSnapshot(
    input.facts(),
    input.edges()
);
```

CGO internally treats `GraphSnapshot` as immutable.

---

## 6. Choosing a Rulepack

Rulepacks are your “business logic bundles”.

Examples:

- `REROUTE_RULEPACK`
- `ASSIGN_GATE_RULEPACK`
- `DELAY_PREDICTION_RULEPACK`

You pick one per API call:

```java
Rulepack rulepack = rulepackSelector.forContext("REROUTE");
```

Rules inside the pack all fire independently and return **Proposals**.

---

## 7. Executing CGO

This is the entire integration call:

```java
BindResult result = pipeline.process(view, rulepack);
```

Under the hood, CGO does:

1. **Execution Phase**
   Business rules run on the GraphView and produce `Set<Proposal>`

2. **Validation Phase**
   ProposalValidator checks structure of all proposals

   - If invalid → `BindResult.notOk(...)`

3. **Mutation Phase**
   Graph mutates (app may persist it)

4. **Result**
   Return `BindResult` (OK or not OK) + any deltas you want to expose

---

## 8. Mapping Results Back to Your App

Your service converts the pipeline output into your application response.

Example:

```java
public RoutingResult toRoutingResult(BindResult result) {
    if (!result.isOk()) {
        return RoutingResult.failed(result.message());
    }
    return RoutingResult.success(result.deltas());
}
```

This is **fully decoupled** from CGO internals.

---

## 9. JSON Contract (Opaque to CGO)

CGO treats all domain data as **opaque JSON strings**.
Your rules parse what they need.

### 9.1 Fact JSON

```json
{
  "id": "FLIGHT:AA123",
  "payload": {
    "flightNumber": "AA123",
    "from": "AUS",
    "to": "DFW"
  }
}
```

### 9.2 Edge JSON

```json
{
  "id": "ROUTE:AA123",
  "fromId": "AIRPORT:AUS",
  "toId": "AIRPORT:DFW",
  "payload": {
    "reason": "WEATHER",
    "newRoute": ["AUS", "IAH", "DFW"]
  }
}
```

Rules determine how these fields matter.

---

## 10. Minimal Integration Checklist

### ✔ Step 1 — Define your domain event

(e.g., `FlightRerouteEvent`)

### ✔ Step 2 — Implement a `FactExtractor`

Domain → Facts + Edges

### ✔ Step 3 — Create a snapshot (`GraphView`)

Use `GraphSnapshot`

### ✔ Step 4 — Select the right `Rulepack`

Rules your app wants to run

### ✔ Step 5 — Call the pipeline

`pipeline.process(view, rulepack)`

### ✔ Step 6 — Interpret `BindResult`

Map to your API response

That’s it.

Your integration remains **thin, JSON-only, stable, and controllable**.

---

## 11. Why This API Works

- **Zero model coupling**
  Domain models stay in your service, not in CGO.

- **Reasoning decoupled from CRUD**
  CGO evaluates structure, relationships, and rules through its own graph.

- **Replaceable Rulepacks**
  Behavior changes without changing your service code.

- **Deterministic phases**
  Execution → Validation → Mutation is consistent no matter the rule logic.

---

## 12. Appendix: Typical Service-Level Code

```java
public RoutingResult reroute(FlightRerouteEvent event) {

    // 1. Domain → Facts + Edges
    GraphInput input = factExtractor.extract(event);

    // 2. Build GraphView
    GraphView view = new GraphSnapshot(input.facts(), input.edges());

    // 3. Select Rulepack
    Rulepack rulepack = rulepackSelector.forContext("REROUTE");

    // 4. Execute
    BindResult result = pipeline.process(view, rulepack);

    // 5. Result → API response
    return toRoutingResult(result);
}
```

This is the **canonical integration pattern**.

---
