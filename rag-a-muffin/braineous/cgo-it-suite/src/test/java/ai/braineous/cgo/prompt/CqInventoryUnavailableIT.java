package ai.braineous.cgo.prompt;

import ai.braineous.cgo.prompt.scenario.NexusDomainEdgeSpec;
import ai.braineous.cgo.prompt.scenario.NexusDomainExecutionHarness;
import ai.braineous.cgo.prompt.scenario.NexusDomainFactSpec;
import ai.braineous.cgo.prompt.scenario.NexusDomainScenario;
import ai.braineous.cgo.prompt.scenario.NexusDomainScenarios;
import ai.braineous.rag.prompt.cgo.api.Edge;
import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.cgo.api.GraphContext;
import ai.braineous.rag.prompt.cgo.api.Meta;
import ai.braineous.rag.prompt.cgo.api.QueryExecution;
import ai.braineous.rag.prompt.cgo.api.QueryPipeline;
import ai.braineous.rag.prompt.cgo.api.ValidateTask;
import ai.braineous.rag.prompt.cgo.prompt.PromptBuilder;
import ai.braineous.rag.prompt.cgo.query.CgoQueryPipeline;
import ai.braineous.rag.prompt.cgo.query.Node;
import ai.braineous.rag.prompt.cgo.query.QueryRequest;
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
import java.util.Arrays;
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

        int invocations = Integer.getInteger("cgo.it.invocations", 1).intValue();
        new NexusDomainExecutionHarness().run(
                scenario,
                compiledRelatedFacts,
                task.get("expression").getAsString(),
                task.get("definition").getAsString(),
                invocations);
    }

    @Test
    public void test_2() throws IOException {
        String sql = this.loadCq(CQ_RESOURCE);
        JsonObject compiled = new DeclarativeQueryCompiler().compile(sql);
        JsonObject task = compiled.getAsJsonObject("task");
        JsonArray relatedFactIds = task.getAsJsonArray("relatedFactIds");

        List<String> compiledRelatedFacts = new ArrayList<String>();
        int relatedIndex = 0;
        while (relatedIndex < relatedFactIds.size()) {
            compiledRelatedFacts.add(relatedFactIds.get(relatedIndex).getAsString());
            relatedIndex = relatedIndex + 1;
        }

        NexusDomainScenario scenario =
                NexusDomainScenarios.inventoryUnavailable();
        Map<String, Fact> facts = this.hydrate(scenario);
        Fact axis = facts.get(scenario.getAxisId());
        QueryRequest<ValidateTask> request = this.buildRequest(scenario, facts);

        QueryPipeline pipeline = new CgoQueryPipeline(new PromptBuilder());
        QueryExecution<ValidateTask> execution =
                pipeline.execute(
                        request,
                        axis,
                        compiledRelatedFacts,
                        task.get("expression").getAsString(),
                        task.get("definition").getAsString(),
                        scenario.getRuleSection(),
                        scenario.getDecisionSection());

        Console.log("cq.inventory.unavailable.execution", execution.toJsonString());
    }

    private Map<String, Fact> hydrate(NexusDomainScenario scenario) {
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
        return facts;
    }

    private QueryRequest<ValidateTask> buildRequest(
            NexusDomainScenario scenario,
            Map<String, Fact> facts) {
        Fact axis = facts.get(scenario.getAxisId());
        List<String> relatedIds = new ArrayList<String>();
        Map<String, Node> nodes = new LinkedHashMap<String, Node>();
        int factIndex = 0;
        while (factIndex < scenario.getFacts().size()) {
            NexusDomainFactSpec spec = scenario.getFacts().get(factIndex);
            Fact fact = facts.get(spec.getId());
            nodes.put(fact.getId(), this.node(fact));
            if (!fact.getId().equals(axis.getId())) {
                relatedIds.add(fact.getId());
            }
            factIndex = factIndex + 1;
        }

        ValidateTask task =
                new ValidateTask(
                        "decision",
                        axis.getId(),
                        Arrays.asList("status"),
                        relatedIds);

        return new QueryRequest<ValidateTask>(
                new Meta("v1", "decision", scenario.getName()),
                new GraphContext(nodes),
                task,
                axis.getId(),
                null);
    }

    private Node node(Fact fact) {
        return new Node(
                fact.getId(),
                fact.getText(),
                Arrays.asList(),
                Node.Mode.ATOMIC);
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
