package ai.braineous.cgo.prompt;

import ai.braineous.rag.prompt.cgo.api.Edge;
import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.cgo.api.GraphContext;
import ai.braineous.rag.prompt.cgo.api.Meta;
import ai.braineous.rag.prompt.cgo.api.QueryExecution;
import ai.braineous.rag.prompt.cgo.api.ValidateTask;
import ai.braineous.rag.prompt.cgo.prompt.PromptBuilder;
import ai.braineous.rag.prompt.cgo.query.CgoQueryPipeline;
import ai.braineous.rag.prompt.cgo.query.Node;
import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import ai.braineous.rag.prompt.cgo.querygen.LlmInstructionsFieldGenerator;
import ai.braineous.rag.prompt.models.cgo.graph.BindResult;
import ai.braineous.rag.prompt.models.cgo.graph.GraphBuilder;
import ai.braineous.rag.prompt.models.cgo.graph.Input;
import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NexusQueryPipelineIT {

    private static final String DEFINITION = "inventory availability.";
    private static final String EXPRESSION =
            "(Inventory.quantityOnHand - Inventory.quantityReserved) / Sales.quantitySold";
    private static final String EXPECTED_NEXUS =
            "inventory availability.\n\ncontext\nThe result is 0.";

    private static final String RULE_SECTION =
            "rule\n"
                    + "if the result is greater than or equal to 1,\n"
                    + "the inventory is available.\n"
                    + "otherwise, the inventory is unavailable.";

    private static final String DECISION_SECTION =
            "decision\n"
                    + "state the inventory status as either available or unavailable.";

    private Fact product;
    private Fact inventory;
    private Fact sales;
    private Fact axis;

    @BeforeEach
    public void hydrateCgoState() {
        GraphBuilder graphBuilder = GraphBuilder.getInstance();

        this.product = this.fact(
                "Product:PROD-SHAMPOO-250",
                "{\"id\":\"Product:PROD-SHAMPOO-250\",\"kind\":\"Product\","
                        + "\"mode\":\"atomic\",\"productId\":\"PROD-SHAMPOO-250\","
                        + "\"sku\":\"SHAMPOO-250\",\"name\":\"Shampoo 250 mL\","
                        + "\"unitOfMeasure\":\"bottle\"}");
        this.inventory = this.fact(
                "Inventory:INV-001",
                "{\"id\":\"Inventory:INV-001\",\"kind\":\"Inventory\","
                        + "\"mode\":\"atomic\",\"inventoryId\":\"INV-001\","
                        + "\"quantityOnHand\":0,\"quantityReserved\":0,"
                        + "\"product\":\"Product:PROD-SHAMPOO-250\"}");
        this.sales = this.fact(
                "Sales:SALE-001",
                "{\"id\":\"Sales:SALE-001\",\"kind\":\"Sales\","
                        + "\"mode\":\"atomic\",\"saleId\":\"SALE-001\","
                        + "\"quantitySold\":25,"
                        + "\"product\":\"Product:PROD-SHAMPOO-250\"}");

        graphBuilder.addNode(this.product);
        graphBuilder.addNode(this.inventory);
        graphBuilder.addNode(this.sales);

        BindResult inventoryProduct = graphBuilder.bind(
                this.edgeInput(this.inventory, this.product),
                null);
        BindResult salesProduct = graphBuilder.bind(
                this.edgeInput(this.sales, this.product),
                null);
        assertTrue(inventoryProduct.isOk());
        assertTrue(salesProduct.isOk());

        this.axis = this.inventory;
    }

    @Test
    public void test_1() {
        assertNotNull(this.axis);

        String instructions =
                new LlmInstructionsFieldGenerator().generateNexusInstructions();
        String expectedPrompt =
                instructions
                        + "\n\n"
                        + EXPECTED_NEXUS
                        + "\n\n"
                        + RULE_SECTION
                        + "\n\n"
                        + DECISION_SECTION;

        QueryRequest<ValidateTask> request =
                this.buildInventoryUnavailableRequest();

        CgoQueryPipeline pipeline =
                new CgoQueryPipeline(new PromptBuilder());

        QueryExecution<ValidateTask> execution =
                pipeline.execute(
                        request,
                        this.axis,
                        List.of("Sales"),
                        EXPRESSION,
                        DEFINITION,
                        RULE_SECTION,
                        DECISION_SECTION);

        JsonObject llmQuery = null;
        String assembledPrompt = null;
        if (execution.getLlmResponse() != null
                && execution.getLlmResponse().getLlmRequest() != null
                && execution.getLlmResponse().getLlmRequest().getLlmQuery() != null) {
            llmQuery =
                    execution.getLlmResponse()
                            .getLlmRequest()
                            .getLlmQuery();
            if (llmQuery.has("prompt")) {
                assembledPrompt = llmQuery.get("prompt").getAsString();
            }
        }

        Console.log("nexus.pipeline.assembled.prompt", assembledPrompt);
        Console.log("nexus.pipeline.raw.arc.response", execution.getRawResponse());
        Console.log("nexus.pipeline.promptValidation", String.valueOf(execution.getPromptValidation()));
        Console.log("nexus.pipeline.llmResponseValidation", String.valueOf(execution.getLlmResponseValidation()));
        Console.log("nexus.pipeline.status", String.valueOf(execution.getStatus()));
        Console.log("nexus.pipeline.stage", String.valueOf(execution.getStage()));

        assertNotNull(execution);
        assertSame(request, execution.getRequest());
        assertNotNull(execution.getLlmResponse());
        assertNotNull(execution.getRawResponse());
        assertNotNull(llmQuery);
        assertEquals(expectedPrompt, assembledPrompt);
        assertTrue(assembledPrompt.startsWith(instructions));
        assertTrue(assembledPrompt.contains(EXPECTED_NEXUS));
        assertEquals(
                assembledPrompt.indexOf(EXPECTED_NEXUS),
                instructions.length() + 2);
        assertEquals(
                assembledPrompt.indexOf("\n\nrule\n"),
                instructions.length() + 2 + EXPECTED_NEXUS.length());
        assertEquals(
                assembledPrompt.indexOf("\n\ndecision\n"),
                instructions.length() + 2 + EXPECTED_NEXUS.length() + 2 + RULE_SECTION.length());
        assertFalse(assembledPrompt.contains("llm_instructions"));
        assertFalse(llmQuery.has("llm_instructions"));
        assertFalse(llmQuery.has("task"));
        assertFalse(llmQuery.has("context"));

        assertNotNull(execution.getPromptValidation());
        assertTrue(execution.getPromptValidation().isOk());
        assertEquals(
                "prompt.execution.ok",
                execution.getPromptValidation().getCode());
        assertNotNull(execution.getLlmResponseValidation());
        assertSame(
                request,
                execution.getLlmResponse().getLlmRequest().getQueryRequest());
    }

    private QueryRequest<ValidateTask> buildInventoryUnavailableRequest() {
        Meta meta =
                new Meta(
                        "v1",
                        "decision",
                        "inventory availability"
                );

        ValidateTask task =
                new ValidateTask(
                        "decision",
                        this.inventory.getId(),
                        Arrays.asList("status"),
                        Arrays.asList(this.product.getId(), this.sales.getId())
                );

        Map<String, Node> nodes =
                new LinkedHashMap<String, Node>();
        nodes.put(this.product.getId(), this.node(this.product));
        nodes.put(this.inventory.getId(), this.node(this.inventory));
        nodes.put(this.sales.getId(), this.node(this.sales));

        return new QueryRequest<ValidateTask>(
                meta,
                new GraphContext(nodes),
                task,
                this.inventory.getId(),
                null
        );
    }

    private Node node(Fact fact) {
        return new Node(
                fact.getId(),
                fact.getText(),
                Arrays.asList(),
                Node.Mode.ATOMIC
        );
    }

    private Fact fact(String id, String text) {
        Fact fact = new Fact(id, text);
        fact.setMode("atomic");
        return fact;
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
