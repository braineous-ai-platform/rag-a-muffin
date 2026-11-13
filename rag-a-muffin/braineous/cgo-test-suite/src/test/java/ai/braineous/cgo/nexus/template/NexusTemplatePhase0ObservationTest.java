package ai.braineous.cgo.nexus.template;

import ai.braineous.cgo.nexus.orchestrator.NexusOrchestrator;
import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.models.cgo.graph.BindResult;
import ai.braineous.rag.prompt.models.cgo.graph.GraphBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NexusTemplatePhase0ObservationTest {

    private final NexusTemplateObservationSupport support =
            new NexusTemplateObservationSupport();

    private Fact axis;

    @BeforeEach
    public void hydrateCgoState() {
        GraphBuilder graphBuilder = GraphBuilder.getInstance();

        Fact product = this.support.fact(
                "Product:PROD-SHAMPOO-250",
                "{\"id\":\"Product:PROD-SHAMPOO-250\",\"kind\":\"Product\","
                        + "\"mode\":\"atomic\",\"productId\":\"PROD-SHAMPOO-250\","
                        + "\"sku\":\"SHAMPOO-250\",\"name\":\"Shampoo 250 mL\","
                        + "\"unitOfMeasure\":\"bottle\"}");
        Fact inventory = this.support.fact(
                "Inventory:INV-001",
                "{\"id\":\"Inventory:INV-001\",\"kind\":\"Inventory\","
                        + "\"mode\":\"atomic\",\"inventoryId\":\"INV-001\","
                        + "\"quantityOnHand\":0,\"quantityReserved\":0,"
                        + "\"product\":\"Product:PROD-SHAMPOO-250\"}");
        Fact sales = this.support.fact(
                "Sales:SALE-001",
                "{\"id\":\"Sales:SALE-001\",\"kind\":\"Sales\","
                        + "\"mode\":\"atomic\",\"saleId\":\"SALE-001\","
                        + "\"quantitySold\":25,"
                        + "\"product\":\"Product:PROD-SHAMPOO-250\"}");

        graphBuilder.addNode(product);
        graphBuilder.addNode(inventory);
        graphBuilder.addNode(sales);

        BindResult inventoryProduct = graphBuilder.bind(
                this.support.edgeInput(inventory, product),
                null);
        BindResult salesProduct = graphBuilder.bind(
                this.support.edgeInput(sales, product),
                null);
        assertTrue(inventoryProduct.isOk());
        assertTrue(salesProduct.isOk());

        this.axis = inventory;
    }

    @Test
    public void test_1() {
        assertNotNull(this.axis);

        String generated = new NexusOrchestrator().execute(
                this.axis,
                List.of("Sales"),
                "(Inventory.quantityOnHand - Inventory.quantityReserved) / Sales.quantitySold",
                this.support.loadResource("inventory-unavailable.definition.txt"));
        String reference = this.support.loadResource("inventory-unavailable.nexus.txt");
        this.support.observe(generated, reference);
    }
}
