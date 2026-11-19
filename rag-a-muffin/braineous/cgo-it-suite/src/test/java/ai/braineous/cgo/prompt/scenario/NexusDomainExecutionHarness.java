package ai.braineous.cgo.prompt.scenario;

import ai.braineous.rag.prompt.cgo.api.Edge;
import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.cgo.api.GraphContext;
import ai.braineous.rag.prompt.cgo.api.Meta;
import ai.braineous.rag.prompt.cgo.api.QueryExecution;
import ai.braineous.rag.prompt.cgo.api.ValidateTask;
import ai.braineous.rag.prompt.cgo.prompt.PromptBuilder;
import ai.braineous.rag.prompt.cgo.api.QueryPipeline;
import ai.braineous.rag.prompt.cgo.query.CgoQueryPipeline;
import ai.braineous.rag.prompt.cgo.query.Node;
import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import ai.braineous.rag.prompt.cgo.querygen.LlmInstructionsFieldGenerator;
import ai.braineous.rag.prompt.models.cgo.graph.BindResult;
import ai.braineous.rag.prompt.models.cgo.graph.GraphBuilder;
import ai.braineous.rag.prompt.models.cgo.graph.Input;
import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonObject;

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

public final class NexusDomainExecutionHarness {

    public void run(NexusDomainScenario scenario, int invocations) {
        this.run(
                scenario,
                scenario.getRelatedFacts(),
                scenario.getExpression(),
                scenario.getDefinition(),
                invocations);
    }

    public void run(
            NexusDomainScenario scenario,
            List<String> relatedFacts,
            String expression,
            String definition,
            int invocations) {
        assertNotNull(scenario);
        assertTrue(invocations > 0);
        assertNotNull(relatedFacts);

        Map<String, Fact> facts = this.hydrate(scenario);
        Fact axis = facts.get(scenario.getAxisId());
        assertNotNull(axis);

        String instructions =
                new LlmInstructionsFieldGenerator().generateNexusInstructions();
        String expectedInstructions = instructions.toLowerCase(Locale.ROOT);
        String expectedNexusMarker =
                scenario.getExpectedNexusMarker().toLowerCase(Locale.ROOT);
        String expectedRuleSection =
                scenario.getRuleSection().toLowerCase(Locale.ROOT);
        String expectedDecisionSection =
                scenario.getDecisionSection().toLowerCase(Locale.ROOT);
        QueryPipeline pipeline =
                new CgoQueryPipeline(new PromptBuilder());

        int successCount = 0;
        int noiseCount = 0;
        List<String> classifications = new ArrayList<String>();
        String firstPrompt = null;

        int i = 0;
        while (i < invocations) {
            QueryRequest<ValidateTask> request =
                    this.buildRequest(scenario, facts);

            QueryExecution<ValidateTask> execution =
                    pipeline.execute(
                            request,
                            axis,
                            relatedFacts,
                            expression,
                            definition,
                            scenario.getRuleSection(),
                            scenario.getDecisionSection());

            assertNotNull(execution);
            assertNotNull(execution.getLlmResponse());
            assertNotNull(execution.getRawResponse());
            assertNotNull(execution.getPromptValidation());
            assertTrue(execution.getPromptValidation().isOk());
            assertEquals(
                    "prompt.execution.ok",
                    execution.getPromptValidation().getCode());
            assertNotNull(execution.getLlmResponseValidation());

            JsonObject llmQuery =
                    execution.getLlmResponse()
                            .getLlmRequest()
                            .getLlmQuery();
            assertNotNull(llmQuery);
            assertTrue(llmQuery.has("prompt"));
            String assembledPrompt =
                    llmQuery.get("prompt").getAsString();
            assertTrue(assembledPrompt.startsWith(expectedInstructions));
            assertTrue(assembledPrompt.contains(expectedNexusMarker));
            assertTrue(assembledPrompt.contains(expectedRuleSection));
            assertTrue(assembledPrompt.contains(expectedDecisionSection));
            assertTrue(
                    assembledPrompt.indexOf(expectedInstructions)
                            < assembledPrompt.indexOf(expectedNexusMarker));
            assertTrue(
                    assembledPrompt.indexOf(expectedNexusMarker)
                            < assembledPrompt.indexOf(expectedRuleSection));
            assertTrue(
                    assembledPrompt.indexOf(expectedRuleSection)
                            < assembledPrompt.indexOf(expectedDecisionSection));
            assertFalse(assembledPrompt.contains("llm_instructions"));
            assertFalse(assembledPrompt.contains("task.controls"));

            if (firstPrompt == null) {
                firstPrompt = assembledPrompt;
            } else {
                assertEquals(firstPrompt, assembledPrompt);
            }

            String classification =
                    this.classify(execution.getRawResponse(), scenario.getExpectedOutcome());
            if ("SUCCESS".equals(classification)) {
                successCount = successCount + 1;
            } else {
                noiseCount = noiseCount + 1;
            }
            classifications.add(classification);

            Console.log("nexus.domain.scenario", scenario.getName());
            Console.log("nexus.domain.run", String.valueOf(i));
            Console.log("nexus.domain.prompt", assembledPrompt);
            Console.log("nexus.domain.raw.arc.response", execution.getRawResponse());
            Console.log("nexus.domain.promptValidation", String.valueOf(execution.getPromptValidation()));
            Console.log("nexus.domain.llmResponseValidation", String.valueOf(execution.getLlmResponseValidation()));
            Console.log("nexus.domain.status", String.valueOf(execution.getStatus()));
            Console.log("nexus.domain.stage", String.valueOf(execution.getStage()));
            Console.log("nexus.domain.classification", classification);

            i = i + 1;
        }

        Console.log("nexus.domain.success.count", String.valueOf(successCount));
        Console.log("nexus.domain.noise.count", String.valueOf(noiseCount));
        Console.log("nexus.domain.classifications", classifications.toString());
        Console.log(
                "nexus.domain.chart.success",
                this.bar(successCount) + " " + successCount);
        Console.log(
                "nexus.domain.chart.noise",
                this.bar(noiseCount) + " " + noiseCount);

        assertEquals(invocations, successCount + noiseCount);
        assertEquals(invocations, classifications.size());
        assertNotNull(firstPrompt);
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
}
