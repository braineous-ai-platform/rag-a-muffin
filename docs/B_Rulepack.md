# B. CGO Rulepack Guide

> **Audience:** Java developers defining business logic for CGO
> **Goal:** Understand how to structure, write, and integrate Rulepacks that drive CGO’s reasoning behavior

---

## 1. Overview

A **Rulepack** is the core behavioral unit in CGO.
It defines **what reasoning CGO performs** for a given API call or domain context.

Your application selects which Rulepack to use (e.g., `REROUTE_RULEPACK`), then CGO executes:

```
Rules → Proposals → Validation → Mutation
```

Rulepacks are intentionally **simple, declarative, and independent**:

- A Rulepack is just a **set of rules**.
- Each rule fires independently.
- No sequencing or ordering.
- Each rule produces **Proposals**.
- CGO validates & applies the combined proposals.

This ensures deterministic reasoning without hidden pipelines or implicit flow control.

---

## 2. Role of Rulepacks at Integration Boundary

From an application point of view, Rulepacks serve two purposes:

### 2.1 Behavior Selection

Choosing a Rulepack defines _which reasoning_ CGO performs:

- Route selection
- Gate assignment
- Delay propagation
- Conflict detection
- etc.

### 2.2 Stability

You can change rule logic or add new rules inside a Rulepack **without modifying the application**.

The service simply says:

```java
Rulepack pack = rulepackSelector.forContext("REROUTE");
```

and CGO performs the required reasoning.

---

## 3. The Rule Interface

At the lowest level, a CGO rule is a pure function:

```java
@FunctionalInterface
public interface BusinessRule {
    Proposal execute(GraphView view);
}
```

### Key points:

- Rules must be **pure** (no graph mutation)
- Rules **only read** from `GraphView`
- Rules always return a **Proposal**
- Rules operate on JSON stored in Facts/Edges

All rules follow this shape:

```
GraphView → Proposal
```

CGO later aggregates proposals across rules.

---

## 4. Rulepack Structure

A Rulepack is simply:

```java
public class Rulepack {
    private Set<BusinessRule> rules = new HashSet<>();

    public Set<BusinessRule> getRules() { return rules; }
    public void setRules(Set<BusinessRule> rules) { this.rules = rules; }

    public Set<Proposal> execute(GraphView view){
        Set<Proposal> results = new HashSet<>();
        for(BusinessRule rule : rules){
            results.add(rule.execute(view));
        }
        return results;
    }
}
```

### Why Set?

- **Order must not matter**
- Rules are independent
- Execution order cannot encode logic
- Enforces architectural purity

### Determinism

CGO guarantees determinism through:

- Pure rule functions
- Stable proposal validation
- Graph mutation only after validation

Rulepacks maintain this consistency.

---

## 5. Execution Model

When a Rulepack is executed:

1. **Every rule fires**
2. Each rule returns a **Proposal**
3. CGO combines all proposals
4. Structure validator ensures they form a coherent change-set
5. If valid → mutations are applied
6. If invalid → CGO returns an error result

### No Short-Circuiting

Rulepacks are NOT pipelines.

- No sequencing
- No early termination
- No hidden ordering that changes output

Every rule is treated as **parallel reasoning** contributing to the final delta.

---

## 6. Example Rulepack

```java
public class RerouteRulepack {

    public static Rulepack build(){
        Rulepack pack = new Rulepack();

        pack.setRules(Set.of(
            new WeatherAvoidanceRule(),
            new CapacityBalancingRule(),
            new AirportClosureRule()
        ));

        return pack;
    }
}
```

Your application may register this Rulepack as:

```java
rulepackRegistry.put("REROUTE", RerouteRulepack.build());
```

---

## 7. Example BusinessRule

Below is a simplified rule that checks if a flight should reroute due to weather:

```java
public class WeatherAvoidanceRule implements BusinessRule {

    @Override
    public Proposal execute(GraphView view) {

        // 1. Extract the flight fact
        Fact flight = view.getFactById("FLIGHT:AA123");
        JsonNode flightJson = parse(flight.payload());

        // 2. Extract weather info
        Fact weather = view.getFactById("WEATHER:" + flightJson.get("from").asText());
```
