package ai.braineous.cgo.prompt;

import ai.braineous.cgo.nexus.orchestrator.NexusOrchestrator;
import ai.braineous.rag.prompt.cgo.api.Edge;
import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.cgo.prompt.PromptBuilder;
import ai.braineous.rag.prompt.cgo.prompt.PromptRequestOutput;
import ai.braineous.rag.prompt.models.cgo.graph.BindResult;
import ai.braineous.rag.prompt.models.cgo.graph.GraphBuilder;
import ai.braineous.rag.prompt.models.cgo.graph.Input;
import ai.braineous.rag.prompt.observe.Console;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NexusPromptBuilderIT {

    private static final String DEFINITION = "inventory availability.";

    private static final String RULE_SECTION =
            "rule\n"
                    + "if the result is greater than or equal to 1,\n"
                    + "the inventory is available.\n"
                    + "otherwise, the inventory is unavailable.";

    private static final String DECISION_SECTION =
            "decision\n"
                    + "state the inventory status as either available or unavailable.";

    private Fact axis;

    @BeforeEach
    public void hydrateCgoState() {
        GraphBuilder graphBuilder = GraphBuilder.getInstance();

        Fact product = this.fact(
                "Product:PROD-SHAMPOO-250",
                "{\"id\":\"Product:PROD-SHAMPOO-250\",\"kind\":\"Product\","
                        + "\"mode\":\"atomic\",\"productId\":\"PROD-SHAMPOO-250\","
                        + "\"sku\":\"SHAMPOO-250\",\"name\":\"Shampoo 250 mL\","
                        + "\"unitOfMeasure\":\"bottle\"}");
        Fact inventory = this.fact(
                "Inventory:INV-001",
                "{\"id\":\"Inventory:INV-001\",\"kind\":\"Inventory\","
                        + "\"mode\":\"atomic\",\"inventoryId\":\"INV-001\","
                        + "\"quantityOnHand\":0,\"quantityReserved\":0,"
                        + "\"product\":\"Product:PROD-SHAMPOO-250\"}");
        Fact sales = this.fact(
                "Sales:SALE-001",
                "{\"id\":\"Sales:SALE-001\",\"kind\":\"Sales\","
                        + "\"mode\":\"atomic\",\"saleId\":\"SALE-001\","
                        + "\"quantitySold\":25,"
                        + "\"product\":\"Product:PROD-SHAMPOO-250\"}");

        graphBuilder.addNode(product);
        graphBuilder.addNode(inventory);
        graphBuilder.addNode(sales);

        BindResult inventoryProduct = graphBuilder.bind(
                this.edgeInput(inventory, product),
                null);
        BindResult salesProduct = graphBuilder.bind(
                this.edgeInput(sales, product),
                null);
        assertTrue(inventoryProduct.isOk());
        assertTrue(salesProduct.isOk());

        this.axis = inventory;
    }

    @Test
    public void test_1() {
        assertNotNull(this.axis);

        String nexusTitleAndContext = new NexusOrchestrator().execute(
                this.axis,
                List.of("Sales"),
                "(Inventory.quantityOnHand - Inventory.quantityReserved) / Sales.quantitySold",
                DEFINITION);

        Console.log("nexus.title.context", nexusTitleAndContext);

        PromptRequestOutput output =
                new PromptBuilder().generateExecutionPrompt(
                        nexusTitleAndContext,
                        RULE_SECTION,
                        DECISION_SECTION);

        String prompt =
                output.getRequestOutput().get("prompt").getAsString();

        Console.log("assembled.prompt", prompt);

        String expected =
                nexusTitleAndContext
                        + "\n\n"
                        + RULE_SECTION
                        + "\n\n"
                        + DECISION_SECTION;

        assertEquals(
                "inventory availability.\n\ncontext\nThe result is 0.",
                nexusTitleAndContext);
        assertEquals(expected, prompt);
        assertTrue(prompt.startsWith(nexusTitleAndContext));
        assertEquals(
                prompt.indexOf("\n\nrule\n"),
                nexusTitleAndContext.length());
        assertEquals(
                prompt.indexOf("\n\ndecision\n"),
                nexusTitleAndContext.length() + 2 + RULE_SECTION.length());
        assertEquals("prompt.execution.ok", output.getValidationResult().getCode());
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
