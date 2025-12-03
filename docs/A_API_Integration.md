# A. CGO API Integration Guide (Refined – 2025 Architecture)

> **Audience:** Java developers integrating their application with CGO  
> **Goal:** Take domain JSON → map to CGO Facts/Input → let CGO build/validate/mutate the graph → interpret results

---

## 1. Overview

This guide explains **how an external service talks to CGO** using a **thin, JSON‑first integration style**.

Your application does **not** need to understand CGO internals. It only needs to:

1. Create **Facts** for your domain objects (as JSON strings)
2. Group those into **Input** triples (`from`, `to`, `edgeFact`)
3. Hand those inputs to the **GraphBuilder**
4. (Optionally) attach a **Rulepack** for domain reasoning
5. Read the **BindResult** and, if needed, the resulting **GraphView**

Everything else (validation, proposals, mutation, history) stays inside CGO.

---

## 2. Integration Boundary (What Your App Actually Touches)

Your application only needs a small surface area of CGO:

### 2.1 `Fact`

From `ai.braineous.rag.prompt.cgo.api.Fact`

- Represents an **atomic piece of state** in the graph  
- Carries:
  - `id` – stable key (e.g., `Flight:F100`, `Airport:AUS`)
  - `text` – opaque JSON string (your domain payload)
  - optional attributes / mode flags

CGO does **not** care about the JSON schema; your rules do.

---

### 2.2 `Edge` (Relational Facts)

From `ai.braineous.rag.prompt.cgo.api.Edge`

- Models relationships between facts (e.g., *Flight connects Airport → Airport*)  
- Internally managed by `GraphBuilder` from the **relational Fact** you pass as `edgeFact` (see `Input` below).

Most integrations don’t need to construct `Edge` directly; they only care about:

- Atomic facts (nodes)
- Relational facts (e.g., a Flight fact that links `from` and `to`)

---

### 2.3 `Input`

From `ai.braineous.rag.prompt.models.cgo.graph.Input`

Represents one logical triple:

- `from` – atomic `Fact` (e.g., origin airport)  
- `to` – atomic `Fact` (e.g., destination airport)  
- `edge` – relational `Fact` (e.g., the flight itself)

```java
Input input = new Input(fromAirportFact, toAirportFact, flightFact);
```

This is the **unit of ingestion** for `GraphBuilder.bind(...)`.

---

### 2.4 `GraphBuilder`

From `ai.braineous.rag.prompt.models.cgo.graph.GraphBuilder`

- Owns the **mutable graph under construction**
- Validates each `Input` against graph rules
- Executes Rulepacks (if provided)
- Applies mutations when everything is valid

Entry point:

```java
Validator validator = new Validator();
ProposalMonitor proposalMonitor = new ProposalMonitor();
GraphBuilder graphBuilder = new GraphBuilder(validator, proposalMonitor);
```

Then, per input:

```java
BindResult result = graphBuilder.bind(input, rulepack);
```

---

### 2.5 `Rulepack`

From `ai.braineous.rag.prompt.models.cgo.graph.Rulepack`

- Container of **BusinessRule** functions
- Each rule reads from a `GraphView` and produces a `WorldMutation`
- `GraphBuilder` converts those into `Proposal`s and validates them

Rulepacks control **what reasoning** CGO does for a given call.  
(See the separate **Rulepack Guide** for details.)

---

### 2.6 `GraphView` / `GraphSnapshot`

From:

- `ai.braineous.rag.prompt.cgo.api.GraphView`
- `ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot`

`GraphSnapshot` implements `GraphView` and gives rules a **read‑only view** of the current graph:

```java
GraphView view = graphBuilder.snapshot();   // returns GraphSnapshot
```

You typically don’t construct `GraphSnapshot` directly; `GraphBuilder` owns it.

---

### 2.7 `BindResult`

From `ai.braineous.rag.prompt.models.cgo.graph.BindResult`

Represents the outcome of a `bind(...)` call:

- `ok` / `isOk()` – did the bind succeed?
- `errors` – list of structural / rule / integrity failures
- `context` / `why` – explainability hooks

Your service only needs to interpret:

- success vs failure  
- optional error details (for logging or user feedback)

---

## 3. Data Flow (Service ↔ CGO)

```text
               +----------------------------+
               |        Your Service        |
               | (Domain Events / JSON)     |
               +--------------+-------------+
                              |
                              |  build Facts (atomic + relational)
                              v
                    +---------+---------+
                    |       Input(s)    |
                    | (from, to, edge)  |
                    +---------+---------+
                              |
                              | graphBuilder.bind(input, rulepack)
                              v
                 +------------+-------------+
                 |            CGO           |
                 |   GraphBuilder           |
                 |   Validator              |
                 |   ProposalMonitor        |
                 |   Rulepack (optional)    |
                 +------------+-------------+
                              |
                              | BindResult + snapshot (GraphView)
                              v
                 +------------+-------------+
                 |       Your Service       |
                 |  (Interpret result)      |
                 +--------------------------+
```

---

## 4. Domain → Facts → Input (Your Only Mapping Responsibility)

Your service decides **how to turn domain events into Facts**.

### 4.1 Example Domain Event

```java
public record FlightRerouteEvent(
    Flight flight,
    Airport from,
    Airport to
) {}
```

### 4.2 Mapping to Facts

```java
public class FlightRerouteMapper {

    public Input toInput(FlightRerouteEvent event) {

        Fact fromAirport = new Fact();
        fromAirport.setId("Airport:" + event.from().code());
        fromAirport.setText(toJson(event.from())); // JSON string

        Fact toAirport = new Fact();
        toAirport.setId("Airport:" + event.to().code());
        toAirport.setText(toJson(event.to()));

        Fact flight = new Fact();
        flight.setId("Flight:" + event.flight().id());
        flight.setText(toJson(event.flight()));

        // from = origin airport (atomic)
        // to   = destination airport (atomic)
        // edge = flight fact (relational-as-fact)
        return new Input(fromAirport, toAirport, flight);
    }
}
```

You can create **multiple Inputs** per request if needed (e.g., multiple flights in one batch).

---

## 5. Building and Evolving the Graph

You typically keep one `GraphBuilder` per logical graph (e.g., “today’s flight network”):

```java
Validator validator = new Validator();
ProposalMonitor proposalMonitor = new ProposalMonitor();
GraphBuilder graphBuilder = new GraphBuilder(validator, proposalMonitor);
```

For each domain event:

```java
Input input = mapper.toInput(event);
BindResult result = graphBuilder.bind(input, rulepack);

if (!result.isOk()) {
    // handle validation / rule failure
    logErrors(result);
}
```

Behind the scenes, `GraphBuilder`:

1. Validates the substrate (facts + relationship)  
2. Executes the Rulepack (if non‑null) on a `GraphView` snapshot  
3. Validates the resulting `Proposal`s  
4. Applies graph mutations if everything passes

At any point you can obtain a read‑only view:

```java
GraphView currentView = graphBuilder.snapshot();  // GraphSnapshot
```

---

## 6. Choosing a Rulepack

Examples:

- `REROUTE`
- `VALIDATE_FLIGHT`
- `ASSIGN_GATE`
- `DELAY_PROPAGATION`

You can select a Rulepack based on:

- API endpoint  
- Event type  
- Tenant / customer  
- Scenario flags

Example selector:

```java
Rulepack rulepack = rulepackSelector.forContext("REROUTE");
BindResult result = graphBuilder.bind(input, rulepack);
```

If you pass `null` as the Rulepack:

- Only **substrate validation + mutation** run  
- No higher‑level reasoning is executed

---

## 7. Executing CGO (End‑to‑End)

For most services, the integration boils down to:

```java
Validator validator = new Validator();
ProposalMonitor proposalMonitor = new ProposalMonitor();
GraphBuilder graphBuilder = new GraphBuilder(validator, proposalMonitor);

// inside your use-case method:
Input input = mapper.toInput(event);
Rulepack rulepack = rulepackSelector.forContext("REROUTE");

BindResult result = graphBuilder.bind(input, rulepack);

if (!result.isOk()) {
    // map errors to your domain error model
    return RoutingResult.failed(result.toString());
}

// optionally inspect final snapshot
GraphView view = graphBuilder.snapshot();
return RoutingResult.success(view /* or derived deltas */);
```

---

## 8. Mapping Results Back to Your Service

You decide how to surface CGO outcomes.

Example:

```java
public RoutingResult toRoutingResult(BindResult result, GraphView snapshot) {

    if (!result.isOk()) {
        return RoutingResult.failed("Validation failed: " + result.toString());
    }

    // In a real integration, you might:
    //  - read back specific Facts from snapshot
    //  - compute a diff vs previous state
    //  - or simply trust the graph as source-of-truth

    return RoutingResult.success(/* whatever your API needs */);
}
```

This keeps CGO as an **internal reasoning engine**, with your service defining the external contract.

---

## 9. JSON Contract (Opaque to CGO)

CGO treats `Fact.text` as an **opaque JSON string**.

Your domain rules parse only what they need:

```json
{
  "id": "Flight:F100",
  "payload": {
    "flightNumber": "F100",
    "from": "AUS",
    "to": "DFW",
    "status": "ON_TIME"
  }
}
```

Relational facts (like flights connecting airports) can carry whatever fields your rules require.

---

## 10. Minimal Integration Checklist

### ✔ Step 1 — Define your domain events  
E.g., `FlightRerouteEvent`, `GateChangeEvent`.

### ✔ Step 2 — Implement a mapper  
Domain event → `Input` (from, to, edge `Fact`).

### ✔ Step 3 — Create a `GraphBuilder`  
With `Validator` and `ProposalMonitor`.

### ✔ Step 4 — Pick a `Rulepack`  
Decide which behavior to run for each call.

### ✔ Step 5 — Call `graphBuilder.bind(input, rulepack)`  
Let CGO validate + reason + mutate.

### ✔ Step 6 — Interpret `BindResult` (+ optional `GraphView`)  
Map into your own response type / side-effects.

---

## 11. Why This Integration Style Works

- **JSON‑first**  
  Your domain models stay in your service; CGO only sees strings.

- **Narrow surface area**  
  Facts, Input, GraphBuilder, Rulepack, BindResult, GraphView.

- **Replaceable business logic**  
  Swap Rulepacks without changing service code.

- **Deterministic phases**  
  Substrate validation → Rulepack execution → Proposal validation → Mutation.

- **Future‑proof**  
  The same graph + Rulepack layer can later be driven by:
  - events
  - batch loaders
  - LLM‑generated Facts
  - or other upstream systems.

---

## 12. Appendix: Canonical Service Method

```java
public RoutingResult reroute(FlightRerouteEvent event) {

    // 1) Domain → Input
    Input input = mapper.toInput(event);

    // 2) Rulepack selection
    Rulepack rulepack = rulepackSelector.forContext("REROUTE");

    // 3) Execute CGO bind
    BindResult result = graphBuilder.bind(input, rulepack);

    // 4) Snapshot (optional)
    GraphView snapshot = graphBuilder.snapshot();

    // 5) Map to your response
    return toRoutingResult(result, snapshot);
}
```

This is the **canonical CGO integration pattern**:
your service owns domain, CGO owns reasoning.
