# B. CGO Rulepack Guide (Refined – 2025 Architecture)

> **Audience:** Java developers defining business logic for CGO  
> **Goal:** Learn how Rulepacks express domain reasoning in a deterministic, modular, graph-driven way

---

# **1. Overview**

A **Rulepack** defines **the reasoning behavior** CGO should perform for a given request or domain scenario.

Your application chooses *which* Rulepack applies (e.g., `validate_flight`, `reroute`, `conflict_detection`), and CGO executes a deterministic sequence:

```
Rules → Proposals → ProposalMonitor → Validation → Mutation
```

**Rulepacks are:**

- Declarative  
- Isolated  
- Order-independent  
- Focused on *behavior*, not control flow  
- Safe (pure functions, no side-effects)

A Rulepack simply contains **a set of rules**, each rule producing a **Proposal**.  
CGO later evaluates, validates, and applies the combined proposals.

This gives you **predictable behavior**, **clean separation**, and **modular domain evolution** without application rework.

---

# **2. Why Rulepacks Exist**

## **2.1 Behavior Selection**
Your application picks “which reasoning to run” by picking a Rulepack:

- Flight validation  
- Reroute suggestions  
- Turnaround-time adjustments  
- Gate assignment  
- Crew coverage detection  
- Any custom domain logic

Rulepacks = “domain behaviors” packaged into isolated logical units.

## **2.2 Stability Across App Changes**

You can:

- Add new rules  
- Update logic  
- Remove rules  
- Expand domain behavior  

**…without touching your application code.**

The app simply says:

```java
Rulepack pack = rulepackSelector.forContext("REROUTE");
```

CGO handles the entire reasoning phase internally.

---

# **3. The BusinessRule Interface**

A **BusinessRule** is the smallest unit of CGO reasoning.

```java
@FunctionalInterface
public interface BusinessRule {
    Proposal execute(GraphView view);
}
```

### **Rule Constraints (Very Important)**

- **Pure function**  
  No mutations, no state, no randomness.

- **Read-only access to `GraphView`**  
  GraphView exposes:
  - Facts (JSON payloads)
  - Edges (relationships)
  - Attributes

- **Always returns a Proposal**  
  Proposal = candidate change  
  (not applied yet, just proposed)

- **Does not know about:**
  - Other rules  
  - Validation  
  - Order  
  - Application context  
  - Mutation logic  

A rule has one job:

```
GraphView → Proposal
```

CGO orchestrates everything else.

---

# **4. Rulepack Structure**

```java
public class Rulepack {

    private Set<BusinessRule> rules = new HashSet<>();

    public Set<BusinessRule> getRules() { return rules; }
    public void setRules(Set<BusinessRule> rules) { this.rules = rules; }

    public Set<Proposal> execute(GraphView view) {
        Set<Proposal> out = new HashSet<>();
        for (BusinessRule rule : rules) {
            out.add(rule.execute(view));
        }
        return out;
    }
}
```

### **Why Set, not List?**

- Order must never encode logic  
- Prevents accidental sequencing  
- Reinforces **parallel reasoning**  
- Encourages pure declarative behavior  

### **Determinism Guarantee**

CGO ensures determinism via:

- Pure rules  
- Stable ProposalMonitor  
- Validation layers  
- Mutations only after successful validation  
- Snapshot-based graph updates  

Rulepacks stay clean and deterministic.

---

# **5. Execution Model**

When CGO executes a Rulepack:

### **1. Every rule fires**  
No skipping, no conditions here.

### **2. Each rule returns a Proposal**

Examples:
- “Change airport DFW → ORD”  
- “Add delay: +30 minutes”  
- “Mark flight invalid”  

### **3. ProposalMonitor runs**  
It inspects all proposals:
- Detects conflicts  
- Normalizes  
- Aggregates compatible changes  
- Flags contradictory proposals

### **4. Validation phases run**
Depending on the Rulepack:
- Proposal structure validation  
- Domain invariants  
- Graph-level safety rules  

### **5. If valid → mutation**
GraphSnapshot updates.  
New facts, new edges, updated attributes.

### **6. If invalid → CGO returns a ValidationResult**
No partial changes.

---

## **Important: No Pipelines in Rulepacks**
Rulepacks **are not pipelines**.

- No ordering  
- No short-circuit  
- No conditional flow  
- No chaining  
- No “this rule after that rule”

Every rule is independent.  
CGO is responsible for combining reasoning outputs.

This design is what makes CGO **modular, explainable, testable, and deterministic**.

---

# **6. Example Rulepack**

```java
public class RerouteRulepack {

    public static Rulepack build() {

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

### Registry usage:

```java
rulepackRegistry.put("REROUTE", RerouteRulepack.build());
```

Your app doesn’t need to care what’s inside —  
it just selects **behavior**.

---

# **7. Example BusinessRule**

Below is a simplified rule that evaluates whether a flight should reroute due to weather:

```java
public class WeatherAvoidanceRule implements BusinessRule {

    @Override
    public Proposal execute(GraphView view) {

        // 1. Extract the flight fact
        Fact flight = view.getFactById("Flight:F100");
        JsonNode flightJson = parse(flight.text());

        // 2. Extract weather fact for the departure airport
        String from = flightJson.get("from").asText();
        Fact weather = view.getFactById("Weather:" + from);
        JsonNode weatherJson = parse(weather.text());

        // 3. Simple logic: if "storm" → propose reroute
        if ("STORM".equals(weatherJson.get("condition").asText())) {
            return Proposal.of("reroute", Map.of(
                "flightId", flight.getId(),
                "action", "AVOID_WEATHER",
                "reason", "storm_at_origin"
            ));
        }

        // 4. Default "no change" proposal
        return Proposal.noop();
    }
}
```

### Notes:
- Reads only from facts  
- No mutation  
- Returns a clean Proposal  
- Easy to test  
- Easy to combine with others  

---

# **8. What Makes Rulepacks Powerful**

- Add/remove rules without touching the app  
- Rules stay pure + testable  
- CGO ensures consistency  
- Rules reflect **domain thinking**, not infrastructure  
- Easy debugging → each rule logs its Proposal  
- Deterministic outcomes across runs  
- Supports multi-domain graphs (flights, weather, gates, crew, etc.)

---

# **9. Summary**

Rulepacks are the **behavioral heart** of CGO.

They let you:

- Encode domain reasoning cleanly  
- Keep application code minimal  
- Change logic safely  
- Maintain deterministic graph evolution  
- Scale behavior without rewriting your service  

If Graph is the **substrate**,  
then Rulepack is the **brainstem** of CGO’s reasoning.
