# BraineousAI Developer Guide (Developer Edition)

> Draft under `1.0.0-alpha.2` — formalized in `alpha.3`.

---

## 1️⃣ Purpose

This document serves as the **entry point for developers** building against the BraineousAI Developer Edition.  
It outlines how to extend, integrate, and contribute to the functional reasoning pipeline:

The intent is to keep all developer-side logic **model-free, composable, and testable**.

---

## 2️⃣ Core Architecture Overview - (Still Evolving)

| Layer                       | Description                                                                           | Developer Role                                  |
| --------------------------- | ------------------------------------------------------------------------------------- | ----------------------------------------------- |
| **App / FNOOrchestrator**   | Accepts JSON, invokes extractor lambda, orchestrates rule execution.                  | Write extractors & rules.                       |
| **LLMContext**              | Snapshot boundary; holds latest `LLMFacts` for each type.                             | Do not modify; just call `.putFactsSnapshot()`. |
| **FactRule / RuleRegistry** | Functional rules; developer-authored lambdas that transform `Fact` → graph mutations. | Extend rule set via lambdas.                    |
| **Subgraph**                | In-memory graph built from rule emissions (nodes/edges).                              | Inspect or export for visualization.            |
| **CGO Bridge**              | Receives subgraph payload; performs reasoning downstream.                             | Integration handled by platform.                |

---

## 3️⃣ Development Flow

### Step 1 — JSON Extraction

Provide a lambda that maps JSON array → `List<Fact>`.

```java
private Function<String, List<Fact>> getFlightFactExtractor(){
        Function<String, List<Fact>> flightExtractor = (jsonArrayStr) -> {
            List<Fact> facts = new ArrayList<>();

            JsonArray flightsArray = JsonParser.parseString(jsonArrayStr).getAsJsonArray();
            for(int i=0; i<flightsArray.size(); i++){
                JsonObject o = flightsArray.get(i).getAsJsonObject();

                String id    = o.get("id").getAsString();        // "F102"
                String src   = o.get("origin").getAsString();    // "AUS"
                String dst   = o.get("dest").getAsString();      // "DFW"
                String depZ  = o.get("dep_utc").getAsString();   // "2025-10-22T11:30:00Z"
                String arrZ  = o.get("arr_utc").getAsString();   // "2025-10-22T12:40:00Z"

                // Airport facts (one per station)
                String srcAirportId = "Airport:" + src;
                String srcAirportText = "Airport(" + src + ", '" + src + "')";
                String dstAirportId = "Airport:" + dst;
                String dstAirportText = "Airport(" + dst + ", '" + dst + "')";
                facts.add(new Fact(srcAirportId, srcAirportText));
                facts.add(new Fact(dstAirportId, dstAirportText));


                // Flight fact (canonical)
                String flightText = "Flight(id:'" + id + "', " + src + ", " + dst + ", '" + depZ + "', '" + arrZ + "')";
                facts.add(new Fact("Flight:" + id, flightText));
            }

            return facts;
        };
        return flightExtractor;
    }

```

### Step 2 — Author Rules

```java
private Function<List<Fact>, Set<String>> getFlightFactRuleGen(){
        Function<List<Fact>, Set<String>> ruleGen = (facts) -> {
            Set<String> out = new LinkedHashSet<>();
            Set<String> airports = new LinkedHashSet<>();
            List<String> flights = new ArrayList<>();

            for (Fact f : facts) {
                String t = f.getText();
                if (t.startsWith("Airport(")) {
                    int p1 = t.indexOf('(')+1, p2 = t.indexOf(',', p1);
                    String code = t.substring(p1, p2).trim();
                    airports.add(code);
                } else if (t.startsWith("Flight(")) {
                    flights.add(t);
                }
            }

            // Emit airport nodes
            for (String code : airports) {
                out.add(("{\"id\":\"R_airport_node_%s\",\"note\":\"Create airport node.\","
                    + "\"when\":[\"Airport($code, $name)\"],"
                    + "\"then\":[{\"emit\":\"GraphNode(%s, 'airport', $name)\"}],"
                    + "\"weight\":0.8}").formatted(code, code));
            }

            // Emit flight edges (generic pattern suffices; string-only)
            if (!flights.isEmpty()) {
                out.add("{\"id\":\"R_flight_edge\",\"note\":\"Create flight edge from Flight facts.\","
                    + "\"when\":[\"Flight(id:$fid, $src, $dst, $depUtc, $arrUtc)\"],"
                    + "\"then\":[{\"emit\":\"GraphEdge($src, $dst, $depUtc, $arrUtc, 'fly', id:$fid)\"}],"
                    + "\"weight\":1.0}");
            }

            return out;
        };
        return ruleGen;
    }
```

---

### Step 3 — Orchestrate via LLMBridge

```java
public void orchestrate(JsonArray flightsJsonArray){
    try{
        LLMContext context = new LLMContext();

        Function<String, List<Fact>> factExtractor = this.getFlightFactExtractor();
        Function<List<Fact>, Set<String>> ruleGen = this.getFlightFactRuleGen();

        context.build("flights", flightsJsonArray.toString(), factExtractor, ruleGen);

        //bridge to CGO
        this.llmBridge.submit(context);

    }catch(Exception e){
        throw new RuntimeException(e.getMessage());
    }
}
```

### Testing

```
mvn -q -Dtest=LLMContextTests test
mvn -q -Dtest=FNOOrchestratorTests test
```

### Expected Output

```
____llm_context____
LLMContext{context={flights=LLMFacts{json=[{"id":"F100","number":"F100","origin":"AUS","dest":"DFW","dep_utc":"2025-10-22T10:00:00Z","arr_utc":"2025-10-22T11:10:00Z","capacity":150,"equipment":"320"},{"id":"F102","number":"F102","origin":"AUS","dest":"DFW","dep_utc":"2025-10-22T11:30:00Z","arr_utc":"2025-10-22T12:40:00Z","capacity":150,"equipment":"320"},{"id":"F110","number":"F110","origin":"SAT","dest":"DFW","dep_utc":"2025-10-22T11:10:00Z","arr_utc":"2025-10-22T12:15:00Z","capacity":150,"equipment":"320"},{"id":"F120","number":"F120","origin":"IAH","dest":"DFW","dep_utc":"2025-10-22T11:20:00Z","arr_utc":"2025-10-22T12:25:00Z","capacity":150,"equipment":"319"},{"id":"F200","number":"F200","origin":"DFW","dest":"ORD","dep_utc":"2025-10-22T13:30:00Z","arr_utc":"2025-10-22T16:50:00Z","capacity":150,"equipment":"738"},{"id":"F210","number":"F210","origin":"DFW","dest":"JFK","dep_utc":"2025-10-22T13:20:00Z","arr_utc":"2025-10-22T17:10:00Z","capacity":150,"equipment":"321"},{"id":"F220","number":"F220","origin":"DFW","dest":"LAX","dep_utc":"2025-10-22T13:45:00Z","arr_utc":"2025-10-22T15:20:00Z","capacity":150,"equipment":"73G"}], facts=[Fact [id=Airport:AUS, text=Airport(AUS, 'AUS'), feats=null], Fact [id=Airport:DFW, text=Airport(DFW, 'DFW'), feats=null], Fact [id=Flight:F100, text=Flight(id:'F100', AUS, DFW, '2025-10-22T10:00:00Z', '2025-10-22T11:10:00Z'), feats=null], Fact [id=Airport:AUS, text=Airport(AUS, 'AUS'), feats=null], Fact [id=Airport:DFW, text=Airport(DFW, 'DFW'), feats=null], Fact [id=Flight:F102, text=Flight(id:'F102', AUS, DFW, '2025-10-22T11:30:00Z', '2025-10-22T12:40:00Z'), feats=null], Fact [id=Airport:SAT, text=Airport(SAT, 'SAT'), feats=null], Fact [id=Airport:DFW, text=Airport(DFW, 'DFW'), feats=null], Fact [id=Flight:F110, text=Flight(id:'F110', SAT, DFW, '2025-10-22T11:10:00Z', '2025-10-22T12:15:00Z'), feats=null], Fact [id=Airport:IAH, text=Airport(IAH, 'IAH'), feats=null], Fact [id=Airport:DFW, text=Airport(DFW, 'DFW'), feats=null], Fact [id=Flight:F120, text=Flight(id:'F120', IAH, DFW, '2025-10-22T11:20:00Z', '2025-10-22T12:25:00Z'), feats=null], Fact [id=Airport:DFW, text=Airport(DFW, 'DFW'), feats=null], Fact [id=Airport:ORD, text=Airport(ORD, 'ORD'), feats=null], Fact [id=Flight:F200, text=Flight(id:'F200', DFW, ORD, '2025-10-22T13:30:00Z', '2025-10-22T16:50:00Z'), feats=null], Fact [id=Airport:DFW, text=Airport(DFW, 'DFW'), feats=null], Fact [id=Airport:JFK, text=Airport(JFK, 'JFK'), feats=null], Fact [id=Flight:F210, text=Flight(id:'F210', DFW, JFK, '2025-10-22T13:20:00Z', '2025-10-22T17:10:00Z'), feats=null], Fact [id=Airport:DFW, text=Airport(DFW, 'DFW'), feats=null], Fact [id=Airport:LAX, text=Airport(LAX, 'LAX'), feats=null], Fact [id=Flight:F220, text=Flight(id:'F220', DFW, LAX, '2025-10-22T13:45:00Z', '2025-10-22T15:20:00Z'), feats=null]], rules=[{"id":"R_airport_node_AUS","note":"Create airport node.","when":["Airport($code, $name)"],"then":[{"emit":"GraphNode(AUS, 'airport', $name)"}],"weight":0.8}, {"id":"R_airport_node_DFW","note":"Create airport node.","when":["Airport($code, $name)"],"then":[{"emit":"GraphNode(DFW, 'airport', $name)"}],"weight":0.8}, {"id":"R_airport_node_SAT","note":"Create airport node.","when":["Airport($code, $name)"],"then":[{"emit":"GraphNode(SAT, 'airport', $name)"}],"weight":0.8}, {"id":"R_airport_node_IAH","note":"Create airport node.","when":["Airport($code, $name)"],"then":[{"emit":"GraphNode(IAH, 'airport', $name)"}],"weight":0.8}, {"id":"R_airport_node_ORD","note":"Create airport node.","when":["Airport($code, $name)"],"then":[{"emit":"GraphNode(ORD, 'airport', $name)"}],"weight":0.8}, {"id":"R_airport_node_JFK","note":"Create airport node.","when":["Airport($code, $name)"],"then":[{"emit":"GraphNode(JFK, 'airport', $name)"}],"weight":0.8}, {"id":"R_airport_node_LAX","note":"Create airport node.","when":["Airport($code, $name)"],"then":[{"emit":"GraphNode(LAX, 'airport', $name)"}],"weight":0.8}, {"id":"R_flight_edge","note":"Create flight edge from Flight facts.","when":["Flight(id:$fid, $src, $dst, $depUtc, $arrUtc)"],"then":[{"emit":"GraphEdge($src, $dst, $depUtc, $arrUtc, 'fly', id:$fid)"}],"weight":1.0}]}}}
____llm_bridge_orchestrate____
ReasoningContext{facts=[Fact [id=Airport:AUS, text=Airport(AUS, 'AUS'), feats=null], Fact [id=Airport:DFW, text=Airport(DFW, 'DFW'), feats=null], Fact [id=Flight:F100, text=Flight(id:'F100', AUS, DFW, '2025-10-22T10:00:00Z', '2025-10-22T11:10:00Z'), feats=null], Fact [id=Airport:AUS, text=Airport(AUS, 'AUS'), feats=null], Fact [id=Airport:DFW, text=Airport(DFW, 'DFW'), feats=null], Fact [id=Flight:F102, text=Flight(id:'F102', AUS, DFW, '2025-10-22T11:30:00Z', '2025-10-22T12:40:00Z'), feats=null], Fact [id=Airport:SAT, text=Airport(SAT, 'SAT'), feats=null], Fact [id=Airport:DFW, text=Airport(DFW, 'DFW'), feats=null], Fact [id=Flight:F110, text=Flight(id:'F110', SAT, DFW, '2025-10-22T11:10:00Z', '2025-10-22T12:15:00Z'), feats=null], Fact [id=Airport:IAH, text=Airport(IAH, 'IAH'), feats=null], Fact [id=Airport:DFW, text=Airport(DFW, 'DFW'), feats=null], Fact [id=Flight:F120, text=Flight(id:'F120', IAH, DFW, '2025-10-22T11:20:00Z', '2025-10-22T12:25:00Z'), feats=null], Fact [id=Airport:DFW, text=Airport(DFW, 'DFW'), feats=null], Fact [id=Airport:ORD, text=Airport(ORD, 'ORD'), feats=null], Fact [id=Flight:F200, text=Flight(id:'F200', DFW, ORD, '2025-10-22T13:30:00Z', '2025-10-22T16:50:00Z'), feats=null], Fact [id=Airport:DFW, text=Airport(DFW, 'DFW'), feats=null], Fact [id=Airport:JFK, text=Airport(JFK, 'JFK'), feats=null], Fact [id=Flight:F210, text=Flight(id:'F210', DFW, JFK, '2025-10-22T13:20:00Z', '2025-10-22T17:10:00Z'), feats=null], Fact [id=Airport:DFW, text=Airport(DFW, 'DFW'), feats=null], Fact [id=Airport:LAX, text=Airport(LAX, 'LAX'), feats=null], Fact [id=Flight:F220, text=Flight(id:'F220', DFW, LAX, '2025-10-22T13:45:00Z', '2025-10-22T15:20:00Z'), feats=null]], state={}}

```

## 3️⃣ Extending the Platform

Add new extractor lambdas for additional data domains (Weather, Crew, Gate, etc.).

Author additional Rule lambdas for new graph semantics.

Subgraph inspection and CGO integration remain unchanged.

Future versions (alpha.3+) will support template-driven rule authoring and persistent subgraph storage.

## 6️⃣ Repository Layout (Developer Edition)

```
/docs/dev
 ├── 00_README.md
 ├── 01_DAILY_LOG/
 ├── 02_72HR_COG/
 ├── 03_ADR/
 ├── 04_RELEASE_NOTES/
 │    └── CHANGELOG-alpha.2.md
 ├── 05_BACKLOG/
 ├── 06_DESIGN/
 ├── 07_RUNBOOKS/
 ├── 08_TEMPLATES/
 ├── DEVELOPER_GUIDE.md       ← this document
 └── GLOSSARY.md
```

## 7️⃣ Commit Convention

Follow the BraineousAI commit chain pattern:

```
<scope-chain>: <concise action>
```

Examples

```
cgo->graph->subgraph->reasoning->no_pojos: JSON→Fact→Rule→Graph, zero domain baggage

fno → app_level_fact_rule_generation → llm_bridge_handoff

```

## 8️⃣ Roadmap

### Next (alpha.3)

Introduce template-based rule engine (configurable JSON/YAML rules).

Persist subgraph structures to disk or in-memory DB.

Add basic visualization CLI.

Expand documentation and CI regression suite.

### Future

Integrate subgraph → LLM prompt binding.

Distributed CGO reasoning graph.

External plugin SDK for domain-specific rulepacks.

---

Maintainer: Sohil Shah (@braineous-ai-platform)
Version: 1.0.0-alpha.2
Date: 2025-10-29
