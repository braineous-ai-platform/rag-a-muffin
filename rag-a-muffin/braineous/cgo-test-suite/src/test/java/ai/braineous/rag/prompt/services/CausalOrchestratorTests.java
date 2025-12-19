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
                null,
                null,
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
                null,
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
                null,
                null,
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
                null, null, null
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
}
