package ai.braineous.rag.prompt.services;

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
    }

}
