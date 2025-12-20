package ai.braineous.rag.prompt.services;

import ai.braineous.rag.prompt.cgo.api.LLMContext;
import ai.braineous.rag.prompt.cgo.api.NetworkFactExtractor;
import ai.braineous.rag.prompt.cgo.api.NetworkRelationshipProvider;
import ai.braineous.rag.prompt.observe.Console;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CausalOrchestratorTests {

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
                new NetworkRelationshipProvider(), // substrate will bind F100 -> F200 (1 edge)
                rules
        );

        var orch = new ai.braineous.rag.prompt.services.CausalOrchestrator();
        var view = (ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot) orch.orchestrate(ctx);

        assertNotNull(view);

        Console.log("test.proposal.reject.reverse.out.edges", "" + view.edges().size());

        // Substrate edge must not exist
        assertFalse(view.edges().containsKey("Edge:Flight:F100->Flight:F200980"));

        // Substrate edge must not exist
        assertFalse(view.edges().containsKey("Edge:Flight:F200980->Flight:F100"));

        // Only the substrate edge should remain
        assertEquals(0, view.edges().size(), "edge should be rejected");
    }
}