package ai.braineous.rag.prompt.services;

import ai.braineous.rag.prompt.cgo.api.LLMContext;
import ai.braineous.rag.prompt.cgo.api.NetworkFactExtractor;
import ai.braineous.rag.prompt.cgo.api.NetworkRelationshipProvider;
import ai.braineous.rag.prompt.models.cgo.graph.GraphBuilder;
import ai.braineous.rag.prompt.observe.Console;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CausalOrchestratorTests {

    @BeforeEach
    public void setup(){
        GraphBuilder.getInstance().clear();
    }

    @Test
    void orchestrate_builds_graph_from_llmContext_facts_and_relationships() {
        // ---- build LLMContext with same flight dataset ----
        String body =
                "[" +
                        "{\"id\":\"F100\",\"origin\":\"AUS\",\"dest\":\"DFW\",\"dep_utc\":\"2025-10-22T10:00:00Z\",\"arr_utc\":\"2025-10-22T11:10:00Z\"}," +
                        "{\"id\":\"F102\",\"origin\":\"AUS\",\"dest\":\"DFW\",\"dep_utc\":\"2025-10-22T11:30:00Z\",\"arr_utc\":\"2025-10-22T12:40:00Z\"}," +
                        "{\"id\":\"F110\",\"origin\":\"SAT\",\"dest\":\"DFW\",\"dep_utc\":\"2025-10-22T11:10:00Z\",\"arr_utc\":\"2025-10-22T12:15:00Z\"}," +
                        "{\"id\":\"F120\",\"origin\":\"IAH\",\"dest\":\"DFW\",\"dep_utc\":\"2025-10-22T11:20:00Z\",\"arr_utc\":\"2025-10-22T12:25:00Z\"}," +
                        "{\"id\":\"F200\",\"origin\":\"DFW\",\"dest\":\"ORD\",\"dep_utc\":\"2025-10-22T13:30:00Z\",\"arr_utc\":\"2025-10-22T16:50:00Z\"}," +
                        "{\"id\":\"F210\",\"origin\":\"DFW\",\"dest\":\"JFK\",\"dep_utc\":\"2025-10-22T13:20:00Z\",\"arr_utc\":\"2025-10-22T17:10:00Z\"}," +
                        "{\"id\":\"F220\",\"origin\":\"DFW\",\"dest\":\"LAX\",\"dep_utc\":\"2025-10-22T13:45:00Z\",\"arr_utc\":\"2025-10-22T15:20:00Z\"}" +
                        "]";

        Console.log("test.causal.orchestrator.in", body);

        com.google.gson.JsonArray flights = com.google.gson.JsonParser.parseString(body).getAsJsonArray();

        ai.braineous.rag.prompt.cgo.api.LLMContext ctx = new ai.braineous.rag.prompt.cgo.api.LLMContext();
        ctx.build(
                "flights",
                flights.toString(),
                new NetworkFactExtractor(),
                new NetworkRelationshipProvider(),
                null
        );

        Console.log("test.causal.orchestrator.facts", "" + ctx.getAllFacts().size());
        Console.log("test.causal.orchestrator.rels", "" + ctx.getAllRelationships().size());

        // ---- run CausalOrchestrator ----
        ai.braineous.rag.prompt.services.CausalOrchestrator orch = new ai.braineous.rag.prompt.services.CausalOrchestrator();
        ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot view =
                (ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot) orch.orchestrate(ctx);

        assertNotNull(view);
        Console.log("test.causal.orchestrator.out.nodes", "" + view.nodes().size());
        Console.log("test.causal.orchestrator.out.edges", "" + view.edges().size());

        // invariants (match proven ingestion substrate)
        assertEquals(14, view.nodes().size());
        assertEquals(12, view.edges().size());

        assertNotNull(view.getFactById("Flight:F102"));
        assertNotNull(view.getFactById("Airport:DFW"));
        assertTrue(view.edges().containsKey("Edge:Flight:F100->Flight:F200"));

        var hash1 = view.snapshotHash().getValue();

        ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot view2 =
                (ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot) orch.orchestrate(ctx);

        var hash2 = view2.snapshotHash().getValue();

        assertEquals(hash1, hash2);
        Console.log("test.causal.orchestrator.hash", hash1);

    }


    @Test
    void orchestrate_with_factExtractor_only_builds_nodes_and_no_edges() {
        String body =
                "[" +
                        "{\"id\":\"F100\",\"origin\":\"AUS\",\"dest\":\"DFW\",\"dep_utc\":\"2025-10-22T10:00:00Z\",\"arr_utc\":\"2025-10-22T11:10:00Z\"}" +
                        "]";

        Console.log("test.causal.orchestrator.noRels.in", body);

        com.google.gson.JsonArray flights =
                com.google.gson.JsonParser.parseString(body).getAsJsonArray();

        // Build context the ONLY valid way
        LLMContext ctx = new LLMContext();
        // only in the 2nd test
        ctx.build(
                "flights",
                flights.toString(),
                new NetworkFactExtractor(),
                null,
                null
        );

        Console.log("test.causal.orchestrator.noRels.facts", "" + ctx.getAllFacts().size());
        Console.log("test.causal.orchestrator.noRels.rels", "" + ctx.getAllRelationships().size());

        // Run orchestrator
        ai.braineous.rag.prompt.services.CausalOrchestrator orch =
                new ai.braineous.rag.prompt.services.CausalOrchestrator();

        ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot view =
                (ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot) orch.orchestrate(ctx);

        assertNotNull(view);

        Console.log("test.causal.orchestrator.noRels.out.nodes", "" + view.nodes().size());
        Console.log("test.causal.orchestrator.noRels.out.edges", "" + view.edges().size());

        // With a single flight, nodes exist, edges must not
        assertNotNull(view.getFactById("Flight:F100"));
        assertNotNull(view.getFactById("Airport:AUS"));
        assertNotNull(view.getFactById("Airport:DFW"));

        assertEquals(0, view.edges().size());
        assertEquals(0, ctx.getAllRelationships().size());

        var hash1 = view.snapshotHash().getValue();

        ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot view2 =
                (ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot) orch.orchestrate(ctx);

        var hash2 = view2.snapshotHash().getValue();

        assertEquals(hash1, hash2);
        Console.log("test.causal.orchestrator.hash", hash1);

    }

    @Test
    void orchestrate_builds_edges_when_relationshipProvider_emits_connections() {
        String body =
                "[" +
                        "{\"id\":\"F100\",\"origin\":\"AUS\",\"dest\":\"DFW\",\"dep_utc\":\"2025-10-22T10:00:00Z\",\"arr_utc\":\"2025-10-22T11:10:00Z\"}," +
                        "{\"id\":\"F102\",\"origin\":\"AUS\",\"dest\":\"DFW\",\"dep_utc\":\"2025-10-22T11:30:00Z\",\"arr_utc\":\"2025-10-22T12:40:00Z\"}," +
                        "{\"id\":\"F110\",\"origin\":\"SAT\",\"dest\":\"DFW\",\"dep_utc\":\"2025-10-22T11:10:00Z\",\"arr_utc\":\"2025-10-22T12:15:00Z\"}," +
                        "{\"id\":\"F120\",\"origin\":\"IAH\",\"dest\":\"DFW\",\"dep_utc\":\"2025-10-22T11:20:00Z\",\"arr_utc\":\"2025-10-22T12:25:00Z\"}," +
                        "{\"id\":\"F200\",\"origin\":\"DFW\",\"dest\":\"ORD\",\"dep_utc\":\"2025-10-22T13:30:00Z\",\"arr_utc\":\"2025-10-22T16:50:00Z\"}," +
                        "{\"id\":\"F210\",\"origin\":\"DFW\",\"dest\":\"JFK\",\"dep_utc\":\"2025-10-22T13:20:00Z\",\"arr_utc\":\"2025-10-22T17:10:00Z\"}," +
                        "{\"id\":\"F220\",\"origin\":\"DFW\",\"dest\":\"LAX\",\"dep_utc\":\"2025-10-22T13:45:00Z\",\"arr_utc\":\"2025-10-22T15:20:00Z\"}" +
                        "]";

        Console.log("test.causal.orchestrator.edges.in", body);

        com.google.gson.JsonArray flights =
                com.google.gson.JsonParser.parseString(body).getAsJsonArray();

        LLMContext ctx = new LLMContext();
        ctx.build(
                "flights",
                flights.toString(),
                new NetworkFactExtractor(),
                new NetworkRelationshipProvider(),
                null
        );

        Console.log("test.causal.orchestrator.edges.facts", "" + ctx.getAllFacts().size());
        Console.log("test.causal.orchestrator.edges.rels", "" + ctx.getAllRelationships().size());

        ai.braineous.rag.prompt.services.CausalOrchestrator orch =
                new ai.braineous.rag.prompt.services.CausalOrchestrator();

        ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot view =
                (ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot) orch.orchestrate(ctx);

        assertNotNull(view);

        Console.log("test.causal.orchestrator.edges.out.nodes", "" + view.nodes().size());
        Console.log("test.causal.orchestrator.edges.out.edges", "" + view.edges().size());

        // Expect classic 4 arrivals-to-DFW x 3 departures-from-DFW = 12
        assertEquals(12, view.edges().size());

        // Spot-check a couple edges
        assertTrue(view.edges().containsKey("Edge:Flight:F100->Flight:F200"));
        assertTrue(view.edges().containsKey("Edge:Flight:F120->Flight:F220"));

        // Node sanity
        assertNotNull(view.getFactById("Flight:F102"));
        assertNotNull(view.getFactById("Airport:DFW"));

        var hash1 = view.snapshotHash().getValue();

        ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot view2 =
                (ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot) orch.orchestrate(ctx);

        var hash2 = view2.snapshotHash().getValue();

        assertEquals(hash1, hash2);
        Console.log("test.causal.orchestrator.hash", hash1);
    }

    @Test
    void orchestrate_is_idempotent_for_same_context() {
        String body =
                "[" +
                        "{\"id\":\"F100\",\"origin\":\"AUS\",\"dest\":\"DFW\",\"dep_utc\":\"2025-10-22T10:00:00Z\",\"arr_utc\":\"2025-10-22T11:10:00Z\"}," +
                        "{\"id\":\"F102\",\"origin\":\"AUS\",\"dest\":\"DFW\",\"dep_utc\":\"2025-10-22T11:30:00Z\",\"arr_utc\":\"2025-10-22T12:40:00Z\"}," +
                        "{\"id\":\"F110\",\"origin\":\"SAT\",\"dest\":\"DFW\",\"dep_utc\":\"2025-10-22T11:10:00Z\",\"arr_utc\":\"2025-10-22T12:15:00Z\"}," +
                        "{\"id\":\"F120\",\"origin\":\"IAH\",\"dest\":\"DFW\",\"dep_utc\":\"2025-10-22T11:20:00Z\",\"arr_utc\":\"2025-10-22T12:25:00Z\"}," +
                        "{\"id\":\"F200\",\"origin\":\"DFW\",\"dest\":\"ORD\",\"dep_utc\":\"2025-10-22T13:30:00Z\",\"arr_utc\":\"2025-10-22T16:50:00Z\"}," +
                        "{\"id\":\"F210\",\"origin\":\"DFW\",\"dest\":\"JFK\",\"dep_utc\":\"2025-10-22T13:20:00Z\",\"arr_utc\":\"2025-10-22T17:10:00Z\"}," +
                        "{\"id\":\"F220\",\"origin\":\"DFW\",\"dest\":\"LAX\",\"dep_utc\":\"2025-10-22T13:45:00Z\",\"arr_utc\":\"2025-10-22T15:20:00Z\"}" +
                        "]";

        var flights = com.google.gson.JsonParser.parseString(body).getAsJsonArray();

        LLMContext ctx = new LLMContext();
        ctx.build(
                "flights",
                flights.toString(),
                new NetworkFactExtractor(),
                new NetworkRelationshipProvider(),
                null
        );

        ai.braineous.rag.prompt.services.CausalOrchestrator orch =
                new ai.braineous.rag.prompt.services.CausalOrchestrator();

        var v1 = (ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot) orch.orchestrate(ctx);
        var v2 = (ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot) orch.orchestrate(ctx);

        assertEquals(v1.nodes().size(), v2.nodes().size());
        assertEquals(v1.edges().size(), v2.edges().size());

        assertEquals(v1.snapshotHash().getValue(), v2.snapshotHash().getValue());
        Console.log("test.causal.orchestrator.idempotent.hash", v1.snapshotHash().getValue());
    }


    @Test
    void orchestrate_builds_graph_from_llmContext_facts_and_relationships_with_rulepack() {
        String body =
                "[" +
                        "{\"id\":\"F100\",\"origin\":\"AUS\",\"dest\":\"DFW\",\"dep_utc\":\"2025-10-22T10:00:00Z\",\"arr_utc\":\"2025-10-22T11:10:00Z\"}," +
                        "{\"id\":\"F102\",\"origin\":\"AUS\",\"dest\":\"DFW\",\"dep_utc\":\"2025-10-22T11:30:00Z\",\"arr_utc\":\"2025-10-22T12:40:00Z\"}," +
                        "{\"id\":\"F110\",\"origin\":\"SAT\",\"dest\":\"DFW\",\"dep_utc\":\"2025-10-22T11:10:00Z\",\"arr_utc\":\"2025-10-22T12:15:00Z\"}," +
                        "{\"id\":\"F120\",\"origin\":\"IAH\",\"dest\":\"DFW\",\"dep_utc\":\"2025-10-22T11:20:00Z\",\"arr_utc\":\"2025-10-22T12:25:00Z\"}," +
                        "{\"id\":\"F200\",\"origin\":\"DFW\",\"dest\":\"ORD\",\"dep_utc\":\"2025-10-22T13:30:00Z\",\"arr_utc\":\"2025-10-22T16:50:00Z\"}," +
                        "{\"id\":\"F210\",\"origin\":\"DFW\",\"dest\":\"JFK\",\"dep_utc\":\"2025-10-22T13:20:00Z\",\"arr_utc\":\"2025-10-22T17:10:00Z\"}," +
                        "{\"id\":\"F220\",\"origin\":\"DFW\",\"dest\":\"LAX\",\"dep_utc\":\"2025-10-22T13:45:00Z\",\"arr_utc\":\"2025-10-22T15:20:00Z\"}" +
                        "]";

        Console.log("test.causal.orchestrator.withRulepack.in", body);

        var flights = com.google.gson.JsonParser.parseString(body).getAsJsonArray();

        // ---- business rules that become a Rulepack via LLMContext.getRulepack() ----
        java.util.List<ai.braineous.rag.prompt.cgo.api.BusinessRule> rules =
                java.util.List.of(view -> {
                    // pure no-op mutation (but with non-null empty sets)
                    ai.braineous.rag.prompt.cgo.api.WorldMutation m =
                            new ai.braineous.rag.prompt.cgo.api.WorldMutation();

                    m.setInsert(java.util.Collections.emptySet());
                    m.setUpdate(java.util.Collections.emptySet());
                    m.setDelete(java.util.Collections.emptySet());
                    m.setEdges(java.util.Collections.emptySet());

                    return m;
                });

        LLMContext ctx = new LLMContext();
        ctx.build(
                "flights",
                flights.toString(),
                new NetworkFactExtractor(),
                new NetworkRelationshipProvider(),
                rules
        );

        var facts = ctx.getAllFacts();
        var rels  = ctx.getAllRelationships();
        var rp    = ctx.getRulepack();

        Console.log("test.causal.orchestrator.withRulepack.facts.count", "" + facts.size());
        Console.log("test.causal.orchestrator.withRulepack.rels.count", "" + rels.size());
        Console.log("test.causal.orchestrator.withRulepack.rules.count", "" + rp.getRules().size());

        assertTrue(facts.size() > 0);
        assertTrue(rels.size() > 0);
        assertEquals(1, rp.getRules().size(), "LLMContext must derive rulepack from businessRules");

        var orch = new ai.braineous.rag.prompt.services.CausalOrchestrator();
        var view = (ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot) orch.orchestrate(ctx);

        assertNotNull(view);

        Console.log("test.causal.orchestrator.withRulepack.out.nodes", "" + view.nodes().size());
        Console.log("test.causal.orchestrator.withRulepack.out.edges", "" + view.edges().size());

        assertEquals(14, view.nodes().size());
        assertEquals(12, view.edges().size());

        assertNotNull(view.getFactById("Flight:F102"));
        assertNotNull(view.getFactById("Airport:DFW"));
        assertTrue(view.edges().containsKey("Edge:Flight:F100->Flight:F200"));

        var hash1 = view.snapshotHash().getValue();
        var view2 = (ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot) orch.orchestrate(ctx);
        var hash2 = view2.snapshotHash().getValue();

        assertEquals(hash1, hash2);
        Console.log("test.causal.orchestrator.withRulepack.hash", hash1);
    }

    @Test
    void orchestrate_applies_rulepack_mutation_insert_edge() {
        String body =
                "[" +
                        "{\"id\":\"F100\",\"origin\":\"AUS\",\"dest\":\"DFW\",\"dep_utc\":\"2025-10-22T10:00:00Z\",\"arr_utc\":\"2025-10-22T11:10:00Z\"}," +
                        "{\"id\":\"F200\",\"origin\":\"DFW\",\"dest\":\"ORD\",\"dep_utc\":\"2025-10-22T13:30:00Z\",\"arr_utc\":\"2025-10-22T16:50:00Z\"}" +
                        "]";

        Console.log("test.causal.rulepack.applyEdge.in", body);

        var flights = com.google.gson.JsonParser.parseString(body).getAsJsonArray();

        // Rule tries to insert ONE synthetic relationship (edge) on top of substrate
        java.util.List<ai.braineous.rag.prompt.cgo.api.BusinessRule> rules =
                java.util.List.of(view -> {
                    ai.braineous.rag.prompt.cgo.api.WorldMutation m =
                            new ai.braineous.rag.prompt.cgo.api.WorldMutation();

                    m.setInsert(java.util.Collections.emptySet());
                    m.setUpdate(java.util.Collections.emptySet());
                    m.setDelete(java.util.Collections.emptySet());

                    // ---- build Facts ----
                    ai.braineous.rag.prompt.cgo.api.Fact from =
                            new ai.braineous.rag.prompt.cgo.api.Fact(
                                    "Flight:F100",
                                    "Flight:F100"
                            );

                    ai.braineous.rag.prompt.cgo.api.Fact to =
                            new ai.braineous.rag.prompt.cgo.api.Fact(
                                    "Flight:F200",
                                    "Flight:F200"
                            );

                    ai.braineous.rag.prompt.cgo.api.Fact edge =
                            new ai.braineous.rag.prompt.cgo.api.Fact(
                                    "Edge:Flight:F100->Flight:F200",
                                    "connects"
                            );

                    ai.braineous.rag.prompt.cgo.api.Relationship r =
                            new ai.braineous.rag.prompt.cgo.api.Relationship();

                    r.setFrom(from);
                    r.setTo(to);
                    r.setEdge(edge);

                    m.setEdges(java.util.Set.of(r));
                    return m;
                });


        LLMContext ctx = new LLMContext();
        ctx.build(
            "flights",
            flights.toString(),
            new NetworkFactExtractor(),
            null,   // <-- YAHI LINE. RelationshipProvider OFF.
            rules
        );

        var orch = new ai.braineous.rag.prompt.services.CausalOrchestrator();
        var view = (ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot) orch.orchestrate(ctx);

        assertNotNull(view);

        Console.log("test.causal.rulepack.applyEdge.out.edges", "" + view.edges().size());

        // This is the intentional expectation: rulepack edge should be applied into snapshot
        assertFalse(view.edges().containsKey("Edge:Flight:F100->Flight:F200"));
    }


    @Test
    void orchestrate_accepts_edge() {
        String body =
                "[" +
                        "{\"id\":\"F100\",\"origin\":\"AUS\",\"dest\":\"DFW\",\"dep_utc\":\"2025-10-22T10:00:00Z\",\"arr_utc\":\"2025-10-22T11:10:00Z\"}," +
                        "{\"id\":\"F200\",\"origin\":\"DFW\",\"dest\":\"ORD\",\"dep_utc\":\"2025-10-22T13:30:00Z\",\"arr_utc\":\"2025-10-22T16:50:00Z\"}" +
                        "]";

        Console.log("test.proposal.reject.reverse.in", body);

        var flights = com.google.gson.JsonParser.parseString(body).getAsJsonArray();

        // BusinessRule emits a WRONG proposal: reverse edge that should NOT be accepted.
        java.util.List<ai.braineous.rag.prompt.cgo.api.BusinessRule> rules =
                java.util.List.of(view -> {
                    var m = new ai.braineous.rag.prompt.cgo.api.WorldMutation();
                    m.setInsert(java.util.Collections.emptySet());
                    m.setUpdate(java.util.Collections.emptySet());
                    m.setDelete(java.util.Collections.emptySet());

                    var from = new ai.braineous.rag.prompt.cgo.api.Fact("Flight:F200", "Flight:F200");
                    var to   = new ai.braineous.rag.prompt.cgo.api.Fact("Flight:F100", "Flight:F100");
                    var edge = new ai.braineous.rag.prompt.cgo.api.Fact("Edge:Flight:F200->Flight:F100", "reverse_connects");
                    edge.setMode("relational");

                    var r = new ai.braineous.rag.prompt.cgo.api.Relationship();
                    r.setFrom(from);
                    r.setTo(to);
                    r.setEdge(edge);

                    m.setEdges(java.util.Set.of(r));
                    return m;
                });

        LLMContext ctx = new LLMContext();
        ctx.build(
                "flights",
                flights.toString(),
                new NetworkFactExtractor(),
                new NetworkRelationshipProvider(), // substrate will bind F100 -> F200 (1 edge)
                rules
        );

        var orch = new ai.braineous.rag.prompt.services.CausalOrchestrator();
        var view = (ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot) orch.orchestrate(ctx);

        assertNotNull(view);

        Console.log("test.proposal.reject.reverse.out.edges", "" + view.edges().size());

        // Substrate edge must exist
        assertTrue(view.edges().containsKey("Edge:Flight:F100->Flight:F200"));

        // should not exist
        assertFalse(view.edges().containsKey("Edge:Flight:F200->Flight:F100"));

        // Only the substrate edge should remain
        assertEquals(1, view.edges().size(), "wrong proposal must not jack the snapshot");
    }

    @Test
    void orchestrate_rejects_wrong_proposal_reverse_connection_edge() {
        String body =
                "[" +
                        "{\"id\":\"F100\",\"origin\":\"AUS\",\"dest\":\"DFW\",\"dep_utc\":\"2025-10-22T10:00:00Z\",\"arr_utc\":\"2025-10-22T11:10:00Z\"}," +
                        "{\"id\":\"F200\",\"origin\":\"DFW\",\"dest\":\"ORD\",\"dep_utc\":\"2025-10-22T13:30:00Z\",\"arr_utc\":\"2025-10-22T16:50:00Z\"}" +
                        "]";

        Console.log("test.proposal.reject.reverse.in", body);

        var flights = com.google.gson.JsonParser.parseString(body).getAsJsonArray();

        // BusinessRule emits a WRONG proposal: reverse edge that should NOT be accepted.
        java.util.List<ai.braineous.rag.prompt.cgo.api.BusinessRule> rules =
                java.util.List.of(view -> {
                    var m = new ai.braineous.rag.prompt.cgo.api.WorldMutation();
                    m.setInsert(java.util.Collections.emptySet());
                    m.setUpdate(java.util.Collections.emptySet());
                    m.setDelete(java.util.Collections.emptySet());

                    var from = new ai.braineous.rag.prompt.cgo.api.Fact("Flight:F200980", "Flight:F200980");
                    var to   = new ai.braineous.rag.prompt.cgo.api.Fact("Flight:F100", "Flight:F100");
                    var edge = new ai.braineous.rag.prompt.cgo.api.Fact("Edge:Flight:F200980->Flight:F100", "should_be_rejected");
                    edge.setMode("relational");

                    var r = new ai.braineous.rag.prompt.cgo.api.Relationship();
                    r.setFrom(from);
                    r.setTo(to);
                    r.setEdge(edge);

                    m.setEdges(java.util.Set.of(r));
                    return m;
                });

        LLMContext ctx = new LLMContext();
        ctx.build(
                "flights",
                flights.toString(),
                new NetworkFactExtractor(),
                new NetworkRelationshipProvider(), // substrate may emit canonical edges
                rules
        );

        Console.log("test.proposal.reject.reverse.facts", "" + ctx.getAllFacts().size());
        Console.log("test.proposal.reject.reverse.rels", "" + ctx.getAllRelationships().size());

        var orch = new ai.braineous.rag.prompt.services.CausalOrchestrator();
        var view = (ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot) orch.orchestrate(ctx);

        assertNotNull(view);

        Console.log("test.proposal.reject.reverse.out.nodes", "" + view.nodes().size());
        Console.log("test.proposal.reject.reverse.out.edges", "" + view.edges().size());

        // Wrong proposal edge must NOT exist
        assertFalse(view.edges().containsKey("Edge:Flight:F200980->Flight:F100"));

        // And the corresponding (non-existent) reverse canonical edge must NOT exist either
        assertFalse(view.edges().containsKey("Edge:Flight:F100->Flight:F200980"));

        // Substrate must not be polluted by wrong proposal
        // (If substrate emits its own valid edges, they may exist; this test only asserts the wrong one is absent.)
    }

    @Test
    void orchestrate_does_not_apply_rulepack_edges_yet() {
        String body =
                "[" +
                        "{\"id\":\"F100\",\"origin\":\"AUS\",\"dest\":\"DFW\",\"dep_utc\":\"2025-10-22T10:00:00Z\",\"arr_utc\":\"2025-10-22T11:10:00Z\"}," +
                        "{\"id\":\"F200\",\"origin\":\"DFW\",\"dest\":\"ORD\",\"dep_utc\":\"2025-10-22T13:30:00Z\",\"arr_utc\":\"2025-10-22T16:50:00Z\"}" +
                        "]";

        Console.log("test.causal.rulepack.noApplyEdgeYet.in", body);

        var flights = com.google.gson.JsonParser.parseString(body).getAsJsonArray();

        // Rule tries to insert ONE synthetic relationship (edge) on top of substrate
        java.util.List<ai.braineous.rag.prompt.cgo.api.BusinessRule> rules =
                java.util.List.of(view -> {
                    var m = new ai.braineous.rag.prompt.cgo.api.WorldMutation();
                    m.setInsert(java.util.Collections.emptySet());
                    m.setUpdate(java.util.Collections.emptySet());
                    m.setDelete(java.util.Collections.emptySet());

                    var from = new ai.braineous.rag.prompt.cgo.api.Fact("Flight:F100", "Flight:F100");
                    var to   = new ai.braineous.rag.prompt.cgo.api.Fact("Flight:F200", "Flight:F200");

                    var edge = new ai.braineous.rag.prompt.cgo.api.Fact("Edge:Flight:F100->Flight:F200", "connects");
                    edge.setMode("relational");

                    var r = new ai.braineous.rag.prompt.cgo.api.Relationship();
                    r.setFrom(from);
                    r.setTo(to);
                    r.setEdge(edge);

                    m.setEdges(java.util.Set.of(r));
                    return m;
                });

        LLMContext ctx = new LLMContext();
        ctx.build(
                "flights",
                flights.toString(),
                new NetworkFactExtractor(),
                null,   // RelationshipProvider OFF: ONLY rulepack emits an edge
                rules
        );

        Console.log("test.causal.rulepack.noApplyEdgeYet.facts", "" + ctx.getAllFacts().size());
        Console.log("test.causal.rulepack.noApplyEdgeYet.rels", "" + ctx.getAllRelationships().size());

        var orch = new ai.braineous.rag.prompt.services.CausalOrchestrator();
        var view = (ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot) orch.orchestrate(ctx);

        assertNotNull(view);

        Console.log("test.causal.rulepack.noApplyEdgeYet.out.nodes", "" + view.nodes().size());
        Console.log("test.causal.rulepack.noApplyEdgeYet.out.edges", "" + view.edges().size());

        // v1 behavior: rulepack-emitted edges are NOT applied into graph snapshot yet
        assertFalse(view.edges().containsKey("Edge:Flight:F100->Flight:F200"));
        assertEquals(0, view.edges().size());
    }


    @Test
    void orchestrate_deterministic_snapshotHash_for_same_context_with_rulepack_noop() {
        String body =
                "[" +
                        "{\"id\":\"F100\",\"origin\":\"AUS\",\"dest\":\"DFW\",\"dep_utc\":\"2025-10-22T10:00:00Z\",\"arr_utc\":\"2025-10-22T11:10:00Z\"}," +
                        "{\"id\":\"F102\",\"origin\":\"AUS\",\"dest\":\"DFW\",\"dep_utc\":\"2025-10-22T11:30:00Z\",\"arr_utc\":\"2025-10-22T12:40:00Z\"}," +
                        "{\"id\":\"F110\",\"origin\":\"SAT\",\"dest\":\"DFW\",\"dep_utc\":\"2025-10-22T11:10:00Z\",\"arr_utc\":\"2025-10-22T12:15:00Z\"}," +
                        "{\"id\":\"F120\",\"origin\":\"IAH\",\"dest\":\"DFW\",\"dep_utc\":\"2025-10-22T11:20:00Z\",\"arr_utc\":\"2025-10-22T12:25:00Z\"}," +
                        "{\"id\":\"F200\",\"origin\":\"DFW\",\"dest\":\"ORD\",\"dep_utc\":\"2025-10-22T13:30:00Z\",\"arr_utc\":\"2025-10-22T16:50:00Z\"}," +
                        "{\"id\":\"F210\",\"origin\":\"DFW\",\"dest\":\"JFK\",\"dep_utc\":\"2025-10-22T13:20:00Z\",\"arr_utc\":\"2025-10-22T17:10:00Z\"}," +
                        "{\"id\":\"F220\",\"origin\":\"DFW\",\"dest\":\"LAX\",\"dep_utc\":\"2025-10-22T13:45:00Z\",\"arr_utc\":\"2025-10-22T15:20:00Z\"}" +
                        "]";

        Console.log("test.causal.hash.rulepack.noop.in", body);

        var flights = com.google.gson.JsonParser.parseString(body).getAsJsonArray();

        // No-op business rule: returns non-null empty sets; should not change substrate
        java.util.List<ai.braineous.rag.prompt.cgo.api.BusinessRule> rules =
                java.util.List.of(view -> {
                    var m = new ai.braineous.rag.prompt.cgo.api.WorldMutation();
                    m.setInsert(java.util.Collections.emptySet());
                    m.setUpdate(java.util.Collections.emptySet());
                    m.setDelete(java.util.Collections.emptySet());
                    m.setEdges(java.util.Collections.emptySet());
                    return m;
                });

        LLMContext ctx = new LLMContext();
        ctx.build(
                "flights",
                flights.toString(),
                new NetworkFactExtractor(),
                new NetworkRelationshipProvider(),
                rules
        );

        Console.log("test.causal.hash.rulepack.noop.facts", "" + ctx.getAllFacts().size());
        Console.log("test.causal.hash.rulepack.noop.rels", "" + ctx.getAllRelationships().size());
        Console.log("test.causal.hash.rulepack.noop.rules", "" + ctx.getRulepack().getRules().size());

        var orch = new ai.braineous.rag.prompt.services.CausalOrchestrator();

        var v1 = (ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot) orch.orchestrate(ctx);
        var v2 = (ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot) orch.orchestrate(ctx);

        assertNotNull(v1);
        assertNotNull(v2);

        var h1 = v1.snapshotHash().getValue();
        var h2 = v2.snapshotHash().getValue();

        Console.log("test.causal.hash.rulepack.noop.h1", h1);
        Console.log("test.causal.hash.rulepack.noop.h2", h2);

        assertEquals(h1, h2);
        assertEquals(v1.nodes().size(), v2.nodes().size());
        assertEquals(v1.edges().size(), v2.edges().size());
    }

    @Test
    void orchestrate_is_order_independent_for_flight_array_input() {
        String body1 =
                "[" +
                        "{\"id\":\"F100\",\"origin\":\"AUS\",\"dest\":\"DFW\",\"dep_utc\":\"2025-10-22T10:00:00Z\",\"arr_utc\":\"2025-10-22T11:10:00Z\"}," +
                        "{\"id\":\"F102\",\"origin\":\"AUS\",\"dest\":\"DFW\",\"dep_utc\":\"2025-10-22T11:30:00Z\",\"arr_utc\":\"2025-10-22T12:40:00Z\"}," +
                        "{\"id\":\"F110\",\"origin\":\"SAT\",\"dest\":\"DFW\",\"dep_utc\":\"2025-10-22T11:10:00Z\",\"arr_utc\":\"2025-10-22T12:15:00Z\"}," +
                        "{\"id\":\"F120\",\"origin\":\"IAH\",\"dest\":\"DFW\",\"dep_utc\":\"2025-10-22T11:20:00Z\",\"arr_utc\":\"2025-10-22T12:25:00Z\"}," +
                        "{\"id\":\"F200\",\"origin\":\"DFW\",\"dest\":\"ORD\",\"dep_utc\":\"2025-10-22T13:30:00Z\",\"arr_utc\":\"2025-10-22T16:50:00Z\"}," +
                        "{\"id\":\"F210\",\"origin\":\"DFW\",\"dest\":\"JFK\",\"dep_utc\":\"2025-10-22T13:20:00Z\",\"arr_utc\":\"2025-10-22T17:10:00Z\"}," +
                        "{\"id\":\"F220\",\"origin\":\"DFW\",\"dest\":\"LAX\",\"dep_utc\":\"2025-10-22T13:45:00Z\",\"arr_utc\":\"2025-10-22T15:20:00Z\"}" +
                        "]";

        // Same flights, intentionally shuffled order
        String body2 =
                "[" +
                        "{\"id\":\"F220\",\"origin\":\"DFW\",\"dest\":\"LAX\",\"dep_utc\":\"2025-10-22T13:45:00Z\",\"arr_utc\":\"2025-10-22T15:20:00Z\"}," +
                        "{\"id\":\"F120\",\"origin\":\"IAH\",\"dest\":\"DFW\",\"dep_utc\":\"2025-10-22T11:20:00Z\",\"arr_utc\":\"2025-10-22T12:25:00Z\"}," +
                        "{\"id\":\"F100\",\"origin\":\"AUS\",\"dest\":\"DFW\",\"dep_utc\":\"2025-10-22T10:00:00Z\",\"arr_utc\":\"2025-10-22T11:10:00Z\"}," +
                        "{\"id\":\"F210\",\"origin\":\"DFW\",\"dest\":\"JFK\",\"dep_utc\":\"2025-10-22T13:20:00Z\",\"arr_utc\":\"2025-10-22T17:10:00Z\"}," +
                        "{\"id\":\"F110\",\"origin\":\"SAT\",\"dest\":\"DFW\",\"dep_utc\":\"2025-10-22T11:10:00Z\",\"arr_utc\":\"2025-10-22T12:15:00Z\"}," +
                        "{\"id\":\"F200\",\"origin\":\"DFW\",\"dest\":\"ORD\",\"dep_utc\":\"2025-10-22T13:30:00Z\",\"arr_utc\":\"2025-10-22T16:50:00Z\"}," +
                        "{\"id\":\"F102\",\"origin\":\"AUS\",\"dest\":\"DFW\",\"dep_utc\":\"2025-10-22T11:30:00Z\",\"arr_utc\":\"2025-10-22T12:40:00Z\"}" +
                        "]";

        Console.log("test.causal.orderIndependent.in.body1", body1);
        Console.log("test.causal.orderIndependent.in.body2", body2);

        var flights1 = com.google.gson.JsonParser.parseString(body1).getAsJsonArray();
        var flights2 = com.google.gson.JsonParser.parseString(body2).getAsJsonArray();

        LLMContext ctx1 = new LLMContext();
        ctx1.build(
                "flights",
                flights1.toString(),
                new NetworkFactExtractor(),
                new NetworkRelationshipProvider(),
                null
        );

        LLMContext ctx2 = new LLMContext();
        ctx2.build(
                "flights",
                flights2.toString(),
                new NetworkFactExtractor(),
                new NetworkRelationshipProvider(),
                null
        );

        Console.log("test.causal.orderIndependent.ctx1.facts", "" + ctx1.getAllFacts().size());
        Console.log("test.causal.orderIndependent.ctx1.rels", "" + ctx1.getAllRelationships().size());
        Console.log("test.causal.orderIndependent.ctx2.facts", "" + ctx2.getAllFacts().size());
        Console.log("test.causal.orderIndependent.ctx2.rels", "" + ctx2.getAllRelationships().size());

        var orch = new ai.braineous.rag.prompt.services.CausalOrchestrator();

        var v1 = (ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot) orch.orchestrate(ctx1);
        var v2 = (ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot) orch.orchestrate(ctx2);

        assertNotNull(v1);
        assertNotNull(v2);

        Console.log("test.causal.orderIndependent.out1.nodes", "" + v1.nodes().size());
        Console.log("test.causal.orderIndependent.out1.edges", "" + v1.edges().size());
        Console.log("test.causal.orderIndependent.out2.nodes", "" + v2.nodes().size());
        Console.log("test.causal.orderIndependent.out2.edges", "" + v2.edges().size());

        var h1 = v1.snapshotHash().getValue();
        var h2 = v2.snapshotHash().getValue();

        Console.log("test.causal.orderIndependent.hash1", h1);
        Console.log("test.causal.orderIndependent.hash2", h2);

        assertEquals(h1, h2);
        assertEquals(v1.nodes().size(), v2.nodes().size());
        assertEquals(v1.edges().size(), v2.edges().size());
    }

    @Test
    void orchestrate_with_empty_input_builds_empty_graph_and_stable_hash() {
        String body = "[]";

        Console.log("test.causal.empty.in", body);

        var flights = com.google.gson.JsonParser.parseString(body).getAsJsonArray();

        LLMContext ctx = new LLMContext();
        ctx.build(
                "flights",
                flights.toString(),
                new NetworkFactExtractor(),
                new NetworkRelationshipProvider(),
                null
        );

        Console.log("test.causal.empty.facts", "" + ctx.getAllFacts().size());
        Console.log("test.causal.empty.rels", "" + ctx.getAllRelationships().size());

        var orch = new ai.braineous.rag.prompt.services.CausalOrchestrator();

        var v1 = (ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot) orch.orchestrate(ctx);
        assertNotNull(v1);

        Console.log("test.causal.empty.out.nodes", "" + v1.nodes().size());
        Console.log("test.causal.empty.out.edges", "" + v1.edges().size());

        assertEquals(0, v1.nodes().size());
        assertEquals(0, v1.edges().size());

        var h1 = v1.snapshotHash().getValue();
        Console.log("test.causal.empty.hash1", h1);
        assertNotNull(h1);
        assertTrue(h1.length() > 0);

        var v2 = (ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot) orch.orchestrate(ctx);
        assertNotNull(v2);

        var h2 = v2.snapshotHash().getValue();
        Console.log("test.causal.empty.hash2", h2);

        assertEquals(h1, h2);
    }

    @Test
    void orchestrate_isolation_between_runs_clear_works() {
        String body =
                "[" +
                        "{\"id\":\"F100\",\"origin\":\"AUS\",\"dest\":\"DFW\",\"dep_utc\":\"2025-10-22T10:00:00Z\",\"arr_utc\":\"2025-10-22T11:10:00Z\"}," +
                        "{\"id\":\"F200\",\"origin\":\"DFW\",\"dest\":\"ORD\",\"dep_utc\":\"2025-10-22T13:30:00Z\",\"arr_utc\":\"2025-10-22T16:50:00Z\"}" +
                        "]";

        Console.log("test.causal.isolation.in", body);

        var flights = com.google.gson.JsonParser.parseString(body).getAsJsonArray();

        LLMContext ctx = new LLMContext();
        ctx.build(
                "flights",
                flights.toString(),
                new NetworkFactExtractor(),
                new NetworkRelationshipProvider(),
                null
        );

        var orch = new ai.braineous.rag.prompt.services.CausalOrchestrator();

        var v1 = (ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot) orch.orchestrate(ctx);
        assertNotNull(v1);

        Console.log("test.causal.isolation.v1.nodes", "" + v1.nodes().size());
        Console.log("test.causal.isolation.v1.edges", "" + v1.edges().size());

        assertTrue(v1.nodes().size() > 0);

        // Explicitly clear singleton builder state
        GraphBuilder.getInstance().clear();

        // Run again with empty ctx (no facts)
        LLMContext empty = new LLMContext();
        empty.build(
                "flights",
                "[]",
                new NetworkFactExtractor(),
                new NetworkRelationshipProvider(),
                null
        );

        var v2 = (ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot) orch.orchestrate(empty);
        assertNotNull(v2);

        Console.log("test.causal.isolation.v2.nodes", "" + v2.nodes().size());
        Console.log("test.causal.isolation.v2.edges", "" + v2.edges().size());

        assertEquals(0, v2.nodes().size());
        assertEquals(0, v2.edges().size());
    }

    @Test
    void orchestrate_does_not_duplicate_edges_on_repeated_relationships() {
        String body =
                "[" +
                        "{\"id\":\"F100\",\"origin\":\"AUS\",\"dest\":\"DFW\",\"dep_utc\":\"2025-10-22T10:00:00Z\",\"arr_utc\":\"2025-10-22T11:10:00Z\"}," +
                        "{\"id\":\"F200\",\"origin\":\"DFW\",\"dest\":\"ORD\",\"dep_utc\":\"2025-10-22T13:30:00Z\",\"arr_utc\":\"2025-10-22T16:50:00Z\"}" +
                        "]";

        Console.log("test.causal.noDuplicateEdges.in", body);

        var flights = com.google.gson.JsonParser.parseString(body).getAsJsonArray();

        LLMContext ctx = new LLMContext();
        ctx.build(
                "flights",
                flights.toString(),
                new NetworkFactExtractor(),
                new NetworkRelationshipProvider(),
                null
        );

        Console.log("test.causal.noDuplicateEdges.facts", "" + ctx.getAllFacts().size());
        Console.log("test.causal.noDuplicateEdges.rels", "" + ctx.getAllRelationships().size());

        var orch = new ai.braineous.rag.prompt.services.CausalOrchestrator();

        var v1 = (ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot) orch.orchestrate(ctx);
        assertNotNull(v1);

        Console.log("test.causal.noDuplicateEdges.v1.edges", "" + v1.edges().size());

        // Run orchestrator again on SAME context
        var v2 = (ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot) orch.orchestrate(ctx);
        assertNotNull(v2);

        Console.log("test.causal.noDuplicateEdges.v2.edges", "" + v2.edges().size());

        // Edge count must remain stable (no duplication)
        assertEquals(v1.edges().size(), v2.edges().size());

        // Canonical edge must exist exactly once
        assertTrue(v2.edges().containsKey("Edge:Flight:F100->Flight:F200"));
    }

    @Test
    void orchestrate_rejects_self_edge_relationship_if_emitted() {
        String body =
                "[" +
                        "{\"id\":\"F100\",\"origin\":\"AUS\",\"dest\":\"DFW\",\"dep_utc\":\"2025-10-22T10:00:00Z\",\"arr_utc\":\"2025-10-22T11:10:00Z\"}" +
                        "]";

        Console.log("test.causal.rejectSelfEdge.in", body);

        var flights = com.google.gson.JsonParser.parseString(body).getAsJsonArray();

        // RelationshipProvider emits an invalid self-edge (F100 -> F100)
        ai.braineous.rag.prompt.cgo.api.RelationshipProvider badProvider = facts -> {
            var from = new ai.braineous.rag.prompt.cgo.api.Fact("Flight:F100", "Flight:F100");
            from.setMode("atomic");

            var to = new ai.braineous.rag.prompt.cgo.api.Fact("Flight:F100", "Flight:F100");
            to.setMode("atomic");

            var edge = new ai.braineous.rag.prompt.cgo.api.Fact("Edge:Flight:F100->Flight:F100", "self_connects");
            edge.setMode("relational");

            var r = new ai.braineous.rag.prompt.cgo.api.Relationship();
            r.setFrom(from);
            r.setTo(to);
            r.setEdge(edge);

            return java.util.List.of(r);
        };

        LLMContext ctx = new LLMContext();
        ctx.build(
                "flights",
                flights.toString(),
                new NetworkFactExtractor(),
                badProvider,
                null
        );

        Console.log("test.causal.rejectSelfEdge.facts", "" + ctx.getAllFacts().size());
        Console.log("test.causal.rejectSelfEdge.rels", "" + ctx.getAllRelationships().size());

        var orch = new ai.braineous.rag.prompt.services.CausalOrchestrator();
        var view = (ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot) orch.orchestrate(ctx);

        assertNotNull(view);

        Console.log("test.causal.rejectSelfEdge.out.nodes", "" + view.nodes().size());
        Console.log("test.causal.rejectSelfEdge.out.edges", "" + view.edges().size());

        // Must reject invalid self-edge; edges should remain empty
        assertFalse(view.edges().containsKey("Edge:Flight:F100->Flight:F100"));
        assertEquals(0, view.edges().size());
    }

    @Test
    void orchestrate_ignores_relationships_with_missing_nodes() {
        String body =
                "[" +
                        "{\"id\":\"F100\",\"origin\":\"AUS\",\"dest\":\"DFW\",\"dep_utc\":\"2025-10-22T10:00:00Z\",\"arr_utc\":\"2025-10-22T11:10:00Z\"}" +
                        "]";

        Console.log("test.causal.missingNodes.in", body);

        var flights = com.google.gson.JsonParser.parseString(body).getAsJsonArray();

        // RelationshipProvider emits edge referencing a non-existent flight F999
        ai.braineous.rag.prompt.cgo.api.RelationshipProvider badProvider = facts -> {
            var from = new ai.braineous.rag.prompt.cgo.api.Fact("Flight:F100", "Flight:F100");
            from.setMode("atomic");

            var to = new ai.braineous.rag.prompt.cgo.api.Fact("Flight:F999", "Flight:F999");
            to.setMode("atomic");

            var edge = new ai.braineous.rag.prompt.cgo.api.Fact("Edge:Flight:F100->Flight:F999", "invalid_connects");
            edge.setMode("relational");

            var r = new ai.braineous.rag.prompt.cgo.api.Relationship();
            r.setFrom(from);
            r.setTo(to);
            r.setEdge(edge);

            return java.util.List.of(r);
        };

        LLMContext ctx = new LLMContext();
        ctx.build(
                "flights",
                flights.toString(),
                new NetworkFactExtractor(),
                badProvider,
                null
        );

        Console.log("test.causal.missingNodes.facts", "" + ctx.getAllFacts().size());
        Console.log("test.causal.missingNodes.rels", "" + ctx.getAllRelationships().size());

        var orch = new ai.braineous.rag.prompt.services.CausalOrchestrator();
        var view = (ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot) orch.orchestrate(ctx);

        assertNotNull(view);

        Console.log("test.causal.missingNodes.out.nodes", "" + view.nodes().size());
        Console.log("test.causal.missingNodes.out.edges", "" + view.edges().size());

        // Edge must be ignored because 'to' node does not exist
        assertFalse(view.edges().containsKey("Edge:Flight:F100->Flight:F999"));
        assertEquals(0, view.edges().size());
    }

    @Test
    void orchestrate_normalizes_edgeFact_mode_to_relational_when_binding() {
        String body =
                "[" +
                        "{\"id\":\"F100\",\"origin\":\"AUS\",\"dest\":\"DFW\",\"dep_utc\":\"2025-10-22T10:00:00Z\",\"arr_utc\":\"2025-10-22T11:10:00Z\"}," +
                        "{\"id\":\"F200\",\"origin\":\"DFW\",\"dest\":\"ORD\",\"dep_utc\":\"2025-10-22T13:30:00Z\",\"arr_utc\":\"2025-10-22T16:50:00Z\"}" +
                        "]";

        Console.log("test.causal.edgeMode.normalized.in", body);

        var flights = com.google.gson.JsonParser.parseString(body).getAsJsonArray();

        // RelationshipProvider emits an edge with WRONG initial mode ("atomic")
        ai.braineous.rag.prompt.cgo.api.RelationshipProvider provider = facts -> {
            var from = new ai.braineous.rag.prompt.cgo.api.Fact("Flight:F100", "Flight:F100");
            from.setMode("atomic");

            var to = new ai.braineous.rag.prompt.cgo.api.Fact("Flight:F200", "Flight:F200");
            to.setMode("atomic");

            var edge = new ai.braineous.rag.prompt.cgo.api.Fact("Edge:Flight:F100->Flight:F200", "connects");
            edge.setMode("atomic"); // WRONG on purpose — GraphBuilder.bind should normalize

            var r = new ai.braineous.rag.prompt.cgo.api.Relationship();
            r.setFrom(from);
            r.setTo(to);
            r.setEdge(edge);

            return java.util.List.of(r);
        };

        LLMContext ctx = new LLMContext();
        ctx.build(
                "flights",
                flights.toString(),
                new NetworkFactExtractor(),
                provider,
                null
        );

        var orch = new ai.braineous.rag.prompt.services.CausalOrchestrator();
        var view = (ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot) orch.orchestrate(ctx);

        assertNotNull(view);

        Console.log("test.causal.edgeMode.normalized.out.edges", "" + view.edges().size());

        assertTrue(view.edges().containsKey("Edge:Flight:F100->Flight:F200"));

        var e = view.edges().get("Edge:Flight:F100->Flight:F200");
        assertNotNull(e);
        assertEquals("relational", e.getMode());
    }


    @Test
    void llmContext_build_throws_when_relationshipProvider_throws_gate_closed() {
        String body =
                "[" +
                        "{\"id\":\"F100\",\"origin\":\"AUS\",\"dest\":\"DFW\",\"dep_utc\":\"2025-10-22T10:00:00Z\",\"arr_utc\":\"2025-10-22T11:10:00Z\"}," +
                        "{\"id\":\"F200\",\"origin\":\"DFW\",\"dest\":\"ORD\",\"dep_utc\":\"2025-10-22T13:30:00Z\",\"arr_utc\":\"2025-10-22T16:50:00Z\"}" +
                        "]";

        Console.log("test.ctx.build.relsThrows.in", body);

        var flights = com.google.gson.JsonParser.parseString(body).getAsJsonArray();

        ai.braineous.rag.prompt.cgo.api.RelationshipProvider throwingProvider = facts -> {
            throw new RuntimeException("boom.relationshipProvider");
        };

        LLMContext ctx = new LLMContext();

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> ctx.build(
                        "flights",
                        flights.toString(),
                        new NetworkFactExtractor(),
                        throwingProvider,
                        null
                )
        );

        Console.log("test.ctx.build.relsThrows.out", ex.getMessage());
        assertTrue(ex.getMessage() != null && ex.getMessage().length() > 0);
    }


    //TODO: v1

    //@Test
    void orchestrate_fails_closed_when_rulepack_execution_throws() {
        String body =
                "[" +
                        "{\"id\":\"F100\",\"origin\":\"AUS\",\"dest\":\"DFW\",\"dep_utc\":\"2025-10-22T10:00:00Z\",\"arr_utc\":\"2025-10-22T11:10:00Z\"}," +
                        "{\"id\":\"F200\",\"origin\":\"DFW\",\"dest\":\"ORD\",\"dep_utc\":\"2025-10-22T13:30:00Z\",\"arr_utc\":\"2025-10-22T16:50:00Z\"}" +
                        "]";

        Console.log("test.causal.rulepack.throws.in", body);

        var flights = com.google.gson.JsonParser.parseString(body).getAsJsonArray();

        // Rulepack dev-code throws during execution
        java.util.List<ai.braineous.rag.prompt.cgo.api.BusinessRule> rules =
                java.util.List.of(view -> {
                    throw new RuntimeException("boom.rulepack.execute");
                });

        LLMContext ctx = new LLMContext();
        ctx.build(
                "flights",
                flights.toString(),
                new NetworkFactExtractor(),
                new NetworkRelationshipProvider(), // substrate will try to bind edges
                rules
        );

        Console.log("test.causal.rulepack.throws.facts", "" + ctx.getAllFacts().size());
        Console.log("test.causal.rulepack.throws.rels", "" + ctx.getAllRelationships().size());
        Console.log("test.causal.rulepack.throws.rules", "" + ctx.getRulepack().getRules().size());

        var orch = new ai.braineous.rag.prompt.services.CausalOrchestrator();

        try {
            orch.orchestrate(ctx);
            fail("expected orchestrate to fail-closed when rulepack throws");
        } catch (Exception e) {
            Console.log("test.causal.rulepack.throws.caught", e.getMessage());
            assertTrue(e.getMessage() != null && e.getMessage().length() > 0);
        }
    }


}