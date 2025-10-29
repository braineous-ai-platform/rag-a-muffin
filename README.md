# rag-a-muffin

⭐️ If you found this through the CGO post — welcome!  
The reasoning cycle docs live under `/docs/cgo/`.

![This is an image](parallax-image.jpg)

Retrieval-Augmented Generation (RAG) made powerful and simple

## Description

Retrieval-Augmented Generation (RAG) made powerful and simple. This project lets you connect a large language model to your own knowledge sources — documents, databases, APIs — so it can retrieve relevant facts before generating an answer. By separating retrieval (finding the right context) from generation (crafting the response), it produces more accurate, up-to-date, and transparent outputs than standalone LLMs. Built to be fast, modular, and easy to extend, it’s perfect for building chatbots, Q&A systems, research assistants, and any AI app that needs to “know what it’s talking about.”

## Current Status

### BraineousAI — Developer Edition (`rag-a-muffin`)

> **Version:** `1.0.0-alpha.2`  
> **Pipeline:** `JSON → Fact → Rule → Subgraph → CGO`

BraineousAI Developer Edition demonstrates a **no-model**, lambda-driven reasoning pipeline.  
Developers provide extractors and rules as functions; the platform turns them into executable graphs.

---

### Quickstart

```bash
cd <repo_root>/rag-a-muffin/prompt_engineering_quarkus

mvn -q -Dtest=FNOOrchestratorTests test
```

Expected output:

```
____llm_context____
LLMContext{context={flights=LLMFacts{json=[{"id":"F100","number":"F100","origin":"AUS","dest":"DFW","dep_utc":"2025-10-22T10:00:00Z","arr_utc":"2025-10-22T11:10:00Z","capacity":150,"equipment":"320"},{"id":"F102","number":"F102","origin":"AUS","dest":"DFW","dep_utc":"2025-10-22T11:30:00Z","arr_utc":"2025-10-22T12:40:00Z","capacity":150,"equipment":"320"},{"id":"F110","number":"F110","origin":"SAT","dest":"DFW","dep_utc":"2025-10-22T11:10:00Z","arr_utc":"2025-10-22T12:15:00Z","capacity":150,"equipment":"320"},{"id":"F120","number":"F120","origin":"IAH","dest":"DFW","dep_utc":"2025-10-22T11:20:00Z","arr_utc":"2025-10-22T12:25:00Z","capacity":150,"equipment":"319"},{"id":"F200","number":"F200","origin":"DFW","dest":"ORD","dep_utc":"2025-10-22T13:30:00Z","arr_utc":"2025-10-22T16:50:00Z","capacity":150,"equipment":"738"},{"id":"F210","number":"F210","origin":"DFW","dest":"JFK","dep_utc":"2025-10-22T13:20:00Z","arr_utc":"2025-10-22T17:10:00Z","capacity":150,"equipment":"321"},{"id":"F220","number":"F220","origin":"DFW","dest":"LAX","dep_utc":"2025-10-22T13:45:00Z","arr_utc":"2025-10-22T15:20:00Z","capacity":150,"equipment":"73G"}], facts=[Fact [id=Airport:AUS, text=Airport(AUS, 'AUS'), feats=null], Fact [id=Airport:DFW, text=Airport(DFW, 'DFW'), feats=null], Fact [id=Flight:F100, text=Flight(id:'F100', AUS, DFW, '2025-10-22T10:00:00Z', '2025-10-22T11:10:00Z'), feats=null], Fact [id=Airport:AUS, text=Airport(AUS, 'AUS'), feats=null], Fact [id=Airport:DFW, text=Airport(DFW, 'DFW'), feats=null], Fact [id=Flight:F102, text=Flight(id:'F102', AUS, DFW, '2025-10-22T11:30:00Z', '2025-10-22T12:40:00Z'), feats=null], Fact [id=Airport:SAT, text=Airport(SAT, 'SAT'), feats=null], Fact [id=Airport:DFW, text=Airport(DFW, 'DFW'), feats=null], Fact [id=Flight:F110, text=Flight(id:'F110', SAT, DFW, '2025-10-22T11:10:00Z', '2025-10-22T12:15:00Z'), feats=null], Fact [id=Airport:IAH, text=Airport(IAH, 'IAH'), feats=null], Fact [id=Airport:DFW, text=Airport(DFW, 'DFW'), feats=null], Fact [id=Flight:F120, text=Flight(id:'F120', IAH, DFW, '2025-10-22T11:20:00Z', '2025-10-22T12:25:00Z'), feats=null], Fact [id=Airport:DFW, text=Airport(DFW, 'DFW'), feats=null], Fact [id=Airport:ORD, text=Airport(ORD, 'ORD'), feats=null], Fact [id=Flight:F200, text=Flight(id:'F200', DFW, ORD, '2025-10-22T13:30:00Z', '2025-10-22T16:50:00Z'), feats=null], Fact [id=Airport:DFW, text=Airport(DFW, 'DFW'), feats=null], Fact [id=Airport:JFK, text=Airport(JFK, 'JFK'), feats=null], Fact [id=Flight:F210, text=Flight(id:'F210', DFW, JFK, '2025-10-22T13:20:00Z', '2025-10-22T17:10:00Z'), feats=null], Fact [id=Airport:DFW, text=Airport(DFW, 'DFW'), feats=null], Fact [id=Airport:LAX, text=Airport(LAX, 'LAX'), feats=null], Fact [id=Flight:F220, text=Flight(id:'F220', DFW, LAX, '2025-10-22T13:45:00Z', '2025-10-22T15:20:00Z'), feats=null]], rules=[{"id":"R_airport_node_AUS","note":"Create airport node.","when":["Airport($code, $name)"],"then":[{"emit":"GraphNode(AUS, 'airport', $name)"}],"weight":0.8}, {"id":"R_airport_node_DFW","note":"Create airport node.","when":["Airport($code, $name)"],"then":[{"emit":"GraphNode(DFW, 'airport', $name)"}],"weight":0.8}, {"id":"R_airport_node_SAT","note":"Create airport node.","when":["Airport($code, $name)"],"then":[{"emit":"GraphNode(SAT, 'airport', $name)"}],"weight":0.8}, {"id":"R_airport_node_IAH","note":"Create airport node.","when":["Airport($code, $name)"],"then":[{"emit":"GraphNode(IAH, 'airport', $name)"}],"weight":0.8}, {"id":"R_airport_node_ORD","note":"Create airport node.","when":["Airport($code, $name)"],"then":[{"emit":"GraphNode(ORD, 'airport', $name)"}],"weight":0.8}, {"id":"R_airport_node_JFK","note":"Create airport node.","when":["Airport($code, $name)"],"then":[{"emit":"GraphNode(JFK, 'airport', $name)"}],"weight":0.8}, {"id":"R_airport_node_LAX","note":"Create airport node.","when":["Airport($code, $name)"],"then":[{"emit":"GraphNode(LAX, 'airport', $name)"}],"weight":0.8}, {"id":"R_flight_edge","note":"Create flight edge from Flight facts.","when":["Flight(id:$fid, $src, $dst, $depUtc, $arrUtc)"],"then":[{"emit":"GraphEdge($src, $dst, $depUtc, $arrUtc, 'fly', id:$fid)"}],"weight":1.0}]}}}
____llm_bridge_orchestrate____
ReasoningContext{facts=[Fact [id=Airport:AUS, text=Airport(AUS, 'AUS'), feats=null], Fact [id=Airport:DFW, text=Airport(DFW, 'DFW'), feats=null], Fact [id=Flight:F100, text=Flight(id:'F100', AUS, DFW, '2025-10-22T10:00:00Z', '2025-10-22T11:10:00Z'), feats=null], Fact [id=Airport:AUS, text=Airport(AUS, 'AUS'), feats=null], Fact [id=Airport:DFW, text=Airport(DFW, 'DFW'), feats=null], Fact [id=Flight:F102, text=Flight(id:'F102', AUS, DFW, '2025-10-22T11:30:00Z', '2025-10-22T12:40:00Z'), feats=null], Fact [id=Airport:SAT, text=Airport(SAT, 'SAT'), feats=null], Fact [id=Airport:DFW, text=Airport(DFW, 'DFW'), feats=null], Fact [id=Flight:F110, text=Flight(id:'F110', SAT, DFW, '2025-10-22T11:10:00Z', '2025-10-22T12:15:00Z'), feats=null], Fact [id=Airport:IAH, text=Airport(IAH, 'IAH'), feats=null], Fact [id=Airport:DFW, text=Airport(DFW, 'DFW'), feats=null], Fact [id=Flight:F120, text=Flight(id:'F120', IAH, DFW, '2025-10-22T11:20:00Z', '2025-10-22T12:25:00Z'), feats=null], Fact [id=Airport:DFW, text=Airport(DFW, 'DFW'), feats=null], Fact [id=Airport:ORD, text=Airport(ORD, 'ORD'), feats=null], Fact [id=Flight:F200, text=Flight(id:'F200', DFW, ORD, '2025-10-22T13:30:00Z', '2025-10-22T16:50:00Z'), feats=null], Fact [id=Airport:DFW, text=Airport(DFW, 'DFW'), feats=null], Fact [id=Airport:JFK, text=Airport(JFK, 'JFK'), feats=null], Fact [id=Flight:F210, text=Flight(id:'F210', DFW, JFK, '2025-10-22T13:20:00Z', '2025-10-22T17:10:00Z'), feats=null], Fact [id=Airport:DFW, text=Airport(DFW, 'DFW'), feats=null], Fact [id=Airport:LAX, text=Airport(LAX, 'LAX'), feats=null], Fact [id=Flight:F220, text=Flight(id:'F220', DFW, LAX, '2025-10-22T13:45:00Z', '2025-10-22T15:20:00Z'), feats=null]], state={}}

```

---

## Docs

- [Developer Guide](./docs/dev/DEVELOPER_GUIDE.md)
- [Changelog](./docs/dev/04_RELEASE_NOTES/CHANGELOG-alpha.2.md)

---

**Maintainer:** Sohil Shah (@braineous-ai-platform)  
**License:** Apache 2.0
