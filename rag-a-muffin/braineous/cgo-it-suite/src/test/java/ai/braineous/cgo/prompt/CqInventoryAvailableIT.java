package ai.braineous.cgo.prompt;

import ai.braineous.cgo.prompt.scenario.NexusDomainEdgeSpec;
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
import ai.braineous.rag.prompt.cgo.querygen.LlmInstructionsFieldGenerator;
import ai.braineous.rag.prompt.models.cgo.graph.BindResult;
import ai.braineous.rag.prompt.models.cgo.graph.GraphBuilder;
import ai.braineous.rag.prompt.models.cgo.graph.Input;
import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CqInventoryAvailableIT {

    private static final String CQ_RESOURCE =
            "/ai/braineous/cgo/prompt/cq/inventory-available.sql";

    @Test
    public void test_1() throws IOException {
        String sql = this.loadCq(CQ_RESOURCE);
        Console.log("cq.inventory.available.input", sql);

        JsonObject compiled = new DeclarativeQueryCompiler().compile(sql);
        Console.log("cq.inventory.available.compiled", compiled.toString());

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
                NexusDomainScenarios.inventoryAvailable();
        assertEquals(scenario.getAxisId(), task.get("factId").getAsString());
        assertEquals(scenario.getRelatedFacts(), compiledRelatedFacts);
        assertEquals(scenario.getExpression(), task.get("expression").getAsString());
        assertEquals(scenario.getDefinition(), task.get("definition").getAsString());

        Map<String, Fact> facts = this.hydrate(scenario);
        Fact axis = facts.get(scenario.getAxisId());
        QueryRequest<ValidateTask> request = this.buildRequest(scenario, facts);

        QueryPipeline pipeline = new CgoQueryPipeline(new PromptBuilder());
        String developerResponse =
                pipeline.executeDeveloper(
                        request,
                        axis,
                        compiledRelatedFacts,
                        task.get("expression").getAsString(),
                        task.get("definition").getAsString(),
                        scenario.getRuleSection(),
                        scenario.getDecisionSection());

        Console.log("cq.inventory.available.developer.response", developerResponse);
        this.assertDeveloperPrompt(developerResponse, scenario);
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
                NexusDomainScenarios.inventoryAvailable();
        Map<String, Fact> facts = this.hydrate(scenario);
        Fact axis = facts.get(scenario.getAxisId());
        QueryRequest<ValidateTask> request = this.buildRequest(scenario, facts);

        QueryPipeline pipeline = new CgoQueryPipeline(new PromptBuilder());
        int invocations = Integer.getInteger("cgo.it.invocations", 1).intValue();
        int successCount = 0;
        int noiseCount = 0;
        int i = 0;
        while (i < invocations) {
            QueryExecution<ValidateTask> execution =
                    pipeline.execute(
                            request,
                            axis,
                            compiledRelatedFacts,
                            task.get("expression").getAsString(),
                            task.get("definition").getAsString(),
                            scenario.getRuleSection(),
                            scenario.getDecisionSection());

            String developerResponse = execution.getDeveloperResponse();
            String arcInput = execution.getArcInput();
            String rawResponse = execution.getRawResponse();

            this.assertDeveloperPrompt(developerResponse, scenario);
            assertNotNull(arcInput);
            JsonObject capturedArcInput =
                    JsonParser.parseString(arcInput).getAsJsonObject();
            assertEquals(
                    developerResponse,
                    capturedArcInput.get("prompt").getAsString());
            assertTrue(capturedArcInput.has("stream"));
            assertFalse(capturedArcInput.get("stream").getAsBoolean());

            String classification =
                    this.classify(rawResponse, scenario.getExpectedOutcome());
            if ("SUCCESS".equals(classification)) {
                successCount = successCount + 1;
            } else {
                noiseCount = noiseCount + 1;
            }

            Console.log("Developer Response", developerResponse);
            Console.log(
                    "Arc Input",
                    new GsonBuilder().setPrettyPrinting().create().toJson(capturedArcInput));
            Console.log("Raw Arc response", rawResponse);
            Console.log("cq.inventory.available.classification", classification);

            i = i + 1;
        }

        Console.log("cq.inventory.available.success.count", String.valueOf(successCount));
        Console.log("cq.inventory.available.noise.count", String.valueOf(noiseCount));
        Console.log(
                "cq.inventory.available.chart.success",
                this.bar(successCount) + " " + successCount);
        Console.log(
                "cq.inventory.available.chart.noise",
                this.bar(noiseCount) + " " + noiseCount);
        assertEquals(invocations, successCount + noiseCount);
    }

    private void assertDeveloperPrompt(
            String developerResponse,
            NexusDomainScenario scenario) {
        String instructions =
                new LlmInstructionsFieldGenerator().generateNexusInstructions();
        String expectedNexus = scenario.getExpectedNexusMarker().toLowerCase(Locale.ROOT);
        String expectedRule = scenario.getRuleSection().toLowerCase(Locale.ROOT);
        String expectedDecision = scenario.getDecisionSection().toLowerCase(Locale.ROOT);
        String expectedPrompt =
                (instructions
                        + "\n\n"
                        + scenario.getExpectedNexusMarker()
                        + "\n\n"
                        + scenario.getRuleSection()
                        + "\n\n"
                        + scenario.getDecisionSection())
                        .toLowerCase(Locale.ROOT);

        assertNotNull(developerResponse);
        assertEquals(expectedPrompt, developerResponse);
        assertFalse(developerResponse.trim().startsWith("{"));
        assertFalse(developerResponse.contains("\"prompt\""));
        assertFalse(developerResponse.contains("\"stream\""));
        assertTrue(developerResponse.contains(expectedNexus));
        assertTrue(developerResponse.contains(expectedRule));
        assertTrue(developerResponse.contains(expectedDecision));
        assertTrue(
                developerResponse.indexOf(instructions.toLowerCase(Locale.ROOT))
                        < developerResponse.indexOf(expectedNexus));
        assertTrue(
                developerResponse.indexOf(expectedNexus)
                        < developerResponse.indexOf(expectedRule));
        assertTrue(
                developerResponse.indexOf(expectedRule)
                        < developerResponse.indexOf(expectedDecision));
    }

    private String classify(String rawResponse, String expectedOutcome) {
        if (rawResponse == null || rawResponse.trim().isEmpty()) {
            return "NOISE";
        }
        if ("unavailable".equals(expectedOutcome)) {
            if (this.matchesWord(rawResponse, "unavailable")
                    && !this.matchesWord(rawResponse, "available")) {
                return "SUCCESS";
            }
            return "NOISE";
        }
        if ("available".equals(expectedOutcome)) {
            if (this.matchesWord(rawResponse, "available")
                    && !this.matchesWord(rawResponse, "unavailable")) {
                return "SUCCESS";
            }
            return "NOISE";
        }
        if ("investigation".equals(expectedOutcome)) {
            if (this.containsIgnoreCase(rawResponse, "investigation")
                    && !this.containsIgnoreCase(rawResponse, "routine")) {
                return "SUCCESS";
            }
            return "NOISE";
        }
        if ("routine".equals(expectedOutcome)) {
            if (this.containsIgnoreCase(rawResponse, "routine")
                    && !this.containsIgnoreCase(rawResponse, "investigation")) {
                return "SUCCESS";
            }
            return "NOISE";
        }
        return "NOISE";
    }

    private boolean matchesWord(String rawResponse, String word) {
        return Pattern.compile("\\b" + word + "\\b", Pattern.CASE_INSENSITIVE)
                .matcher(rawResponse)
                .find();
    }

    private boolean containsIgnoreCase(String rawResponse, String value) {
        return rawResponse.toLowerCase().contains(value.toLowerCase());
    }

    private String bar(int count) {
        StringBuilder bar = new StringBuilder();
        int n = 0;
        while (n < count) {
            bar.append("#");
            n = n + 1;
        }
        return bar.toString();
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
        InputStream stream = CqInventoryAvailableIT.class.getResourceAsStream(resource);
        assertNotNull(stream, resource);
        try {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } finally {
            stream.close();
        }
    }
}
