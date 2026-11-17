package ai.braineous.cgo.prompt;

import ai.braineous.cgo.prompt.scenario.NexusDomainEdgeSpec;
import ai.braineous.cgo.prompt.scenario.NexusDomainExecutionHarness;
import ai.braineous.cgo.prompt.scenario.NexusDomainFactSpec;
import ai.braineous.cgo.prompt.scenario.NexusDomainScenario;
import ai.braineous.cgo.prompt.scenario.NexusDomainScenarios;
import ai.braineous.rag.prompt.cgo.api.Edge;
import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.cgo.querygen.DeclarativeQueryCompiler;
import ai.braineous.rag.prompt.models.cgo.graph.BindResult;
import ai.braineous.rag.prompt.models.cgo.graph.GraphBuilder;
import ai.braineous.rag.prompt.models.cgo.graph.Input;
import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CqInventoryUnavailableIT {

    private static final String CQ_RESOURCE =
            "/ai/braineous/cgo/prompt/cq/inventory-unavailable.sql";

    @Test
    public void test_1() throws IOException {
        String sql = this.loadCq(CQ_RESOURCE);
        Console.log("cq.inventory.unavailable.input", sql);

        JsonObject compiled = new DeclarativeQueryCompiler().compile(sql);
        Console.log("cq.inventory.unavailable.compiled", compiled.toString());

        JsonObject task = compiled.getAsJsonObject("task");
        assertNotNull(task);
        assertEquals("Inventory:INV-001", task.get("factId").getAsString());

        JsonArray relatedFactIds = task.getAsJsonArray("relatedFactIds");
        assertEquals(1, relatedFactIds.size());
        assertEquals("Sales", relatedFactIds.get(0).getAsString());
        assertEquals(
                "(Inventory.quantityOnHand - Inventory.quantityReserved) / Sales.quantitySold",
                task.get("expression").getAsString());
        assertEquals("inventory availability.", task.get("definition").getAsString());
        assertFalse(task.has("constraints"));
        assertFalse(task.has("ruleSection"));
        assertFalse(task.has("decisionSection"));

        List<String> compiledRelatedFacts = new ArrayList<String>();
        int relatedIndex = 0;
        while (relatedIndex < relatedFactIds.size()) {
            compiledRelatedFacts.add(relatedFactIds.get(relatedIndex).getAsString());
            relatedIndex = relatedIndex + 1;
        }

        NexusDomainScenario scenario =
                NexusDomainScenarios.inventoryUnavailable();
        assertEquals(scenario.getAxisId(), task.get("factId").getAsString());
        assertEquals(scenario.getRelatedFacts(), compiledRelatedFacts);
        assertEquals(scenario.getExpression(), task.get("expression").getAsString());
        assertEquals(scenario.getDefinition(), task.get("definition").getAsString());

        this.hydrate(scenario);

        int invocations = Integer.getInteger("cgo.it.invocations", 1).intValue();
        new NexusDomainExecutionHarness().run(
                scenario,
                compiledRelatedFacts,
                task.get("expression").getAsString(),
                task.get("definition").getAsString(),
                invocations);
    }

    private void hydrate(NexusDomainScenario scenario) {
        GraphBuilder graphBuilder = GraphBuilder.getInstance();
        Map<String, Fact> facts = new LinkedHashMap<String, Fact>();
        int factIndex = 0;
        while (factIndex < scenario.getFacts().size()) {
            NexusDomainFactSpec spec = scenario.getFacts().get(factIndex);
            Fact fact = new Fact(spec.getId(), spec.getText());
            fact.setMode("atomic");
            facts.put(spec.getId(), fact);
            graphBuilder.addNode(fact);
            factIndex = factIndex + 1;
        }

        int edgeIndex = 0;
        while (edgeIndex < scenario.getEdges().size()) {
            NexusDomainEdgeSpec spec = scenario.getEdges().get(edgeIndex);
            Fact from = facts.get(spec.getFromId());
            Fact to = facts.get(spec.getToId());
            BindResult bindResult = graphBuilder.bind(this.edgeInput(from, to), null);
            assertTrue(bindResult.isOk());
            edgeIndex = edgeIndex + 1;
        }
    }

    private Input edgeInput(Fact from, Fact to) {
        Edge edge = new Edge();
        edge.setId("Edge:" + from.getId() + "->" + to.getId());
        edge.setFromFactId(from.getId());
        edge.setToFactId(to.getId());
        edge.setMode("relational");
        return new Input(from, to, edge);
    }

    private String loadCq(String resource) throws IOException {
        InputStream stream = CqInventoryUnavailableIT.class.getResourceAsStream(resource);
        assertNotNull(stream, resource);
        try {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } finally {
            stream.close();
        }
    }
}
