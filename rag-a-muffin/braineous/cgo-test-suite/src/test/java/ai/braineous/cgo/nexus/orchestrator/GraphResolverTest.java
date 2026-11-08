package ai.braineous.cgo.nexus.orchestrator;

import ai.braineous.cgo.nexus.harness.Phase0GraphHarness;
import ai.braineous.cgo.nexus.model.ResolvedGraph;
import ai.braineous.rag.prompt.cgo.api.Edge;
import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.models.cgo.graph.BindResult;
import ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GraphResolverTest {

    @Test
    public void test_1() {
        Phase0GraphHarness.EstablishedGraph graph =
                new Phase0GraphHarness().establish(10);
        GraphSnapshot fullSnapshot = graph.getSnapshot();
        Fact inventory = graph.getAxis();

        assertNotNull(fullSnapshot);
        assertNotNull(fullSnapshot.nodes());
        assertNotNull(inventory);
        assertEquals("Inventory:INV-001", inventory.getId());

        Fact product = fullSnapshot.nodes().get("Product:PROD-SHAMPOO-250");
        Fact location = fullSnapshot.nodes().get("Location:LOC-001");
        Fact sales = fullSnapshot.nodes().get("Sales:SALE-001");
        Fact vendor = fullSnapshot.nodes().get("Vendor:VENDOR-001");
        assertNotNull(product);
        assertNotNull(location);
        assertNotNull(sales);
        assertNotNull(vendor);

        Set<String> fixtureIds = new HashSet<String>();
        fixtureIds.add("Product:PROD-SHAMPOO-250");
        fixtureIds.add("Location:LOC-001");
        fixtureIds.add("Inventory:INV-001");
        fixtureIds.add("Sales:SALE-001");
        fixtureIds.add("Vendor:VENDOR-001");

        int extra = 2;
        while (extra <= 10) {
            String seq = extra < 10 ? ("00" + extra) : "010";
            fixtureIds.add("Product:PROD-SHAMPOO-" + (249 + extra));
            fixtureIds.add("Location:LOC-" + seq);
            fixtureIds.add("Inventory:INV-" + seq);
            fixtureIds.add("Sales:SALE-" + seq);
            fixtureIds.add("Vendor:VENDOR-" + seq);
            extra = extra + 1;
        }

        assertEquals(50, fixtureIds.size());
        for (String fixtureId : fixtureIds) {
            assertTrue(fullSnapshot.nodes().containsKey(fixtureId), fixtureId);
        }

        int edgeInstance = 1;
        while (edgeInstance <= 10) {
            String seq = edgeInstance < 10 ? ("00" + edgeInstance) : "010";
            String productFactId = "Product:PROD-SHAMPOO-" + (249 + edgeInstance);
            String inventoryFactId = "Inventory:INV-" + seq;
            String locationFactId = "Location:LOC-" + seq;
            String salesFactId = "Sales:SALE-" + seq;
            String vendorFactId = "Vendor:VENDOR-" + seq;

            this.assertPersistedEdge(
                    fullSnapshot,
                    "Edge:" + inventoryFactId + "->" + productFactId,
                    inventoryFactId,
                    productFactId);
            this.assertPersistedEdge(
                    fullSnapshot,
                    "Edge:" + inventoryFactId + "->" + locationFactId,
                    inventoryFactId,
                    locationFactId);
            this.assertPersistedEdge(
                    fullSnapshot,
                    "Edge:" + salesFactId + "->" + productFactId,
                    salesFactId,
                    productFactId);
            this.assertPersistedEdge(
                    fullSnapshot,
                    "Edge:" + vendorFactId + "->" + productFactId,
                    vendorFactId,
                    productFactId);

            edgeInstance = edgeInstance + 1;
        }

        assertEquals(inventory.getText(), fullSnapshot.nodes().get("Inventory:INV-001").getText());
        assertEquals(sales.getText(), fullSnapshot.nodes().get("Sales:SALE-001").getText());
        assertEquals(vendor.getText(), fullSnapshot.nodes().get("Vendor:VENDOR-001").getText());
        assertEquals(product.getText(), fullSnapshot.nodes().get("Product:PROD-SHAMPOO-250").getText());
        assertEquals(location.getText(), fullSnapshot.nodes().get("Location:LOC-001").getText());

        GraphResolver resolver = new GraphResolver();
        ResolvedGraph expanded = resolver.resolve(
                inventory,
                List.of("Sales", "Vendor", "Location"));
        GraphSnapshot expandedSnapshot = expanded.getSnapshot();

        ResolvedGraph contracted = resolver.resolve(
                inventory,
                List.of("Sales", "Vendor"));
        GraphSnapshot contractedSnapshot = contracted.getSnapshot();

        assertEquals(inventory, expanded.getAxis());
        assertEquals(inventory, contracted.getAxis());

        System.out.print("=== FULL GRAPH SNAPSHOT BEGIN ===\n");
        System.out.print("Snapshot ID\n");
        System.out.print(String.valueOf(fullSnapshot.id()));
        System.out.print("\n");
        System.out.print("Node count\n");
        System.out.print(String.valueOf(fullSnapshot.nodes().size()));
        System.out.print("\n");
        System.out.print("Edge count\n");
        System.out.print(String.valueOf(fullSnapshot.edges().size()));
        System.out.print("\n");
        for (Map.Entry<String, Fact> entry : fullSnapshot.nodes().entrySet()) {
            Fact fact = entry.getValue();
            System.out.print("Fact ID\n");
            System.out.print(fact == null ? "null" : String.valueOf(fact.getId()));
            System.out.print("\n");
            System.out.print("Complete Fact text\n");
            System.out.print(fact == null ? "null" : String.valueOf(fact.getText()));
            System.out.print("\n");
            System.out.print("Fact mode\n");
            System.out.print(fact == null ? "null" : String.valueOf(fact.getMode()));
            System.out.print("\n");
            System.out.print("Fact attributes\n");
            System.out.print(fact == null ? "null" : String.valueOf(fact.getAttributes()));
            System.out.print("\n");
        }
        for (Map.Entry<String, Edge> entry : fullSnapshot.edges().entrySet()) {
            Edge edge = entry.getValue();
            System.out.print("Edge ID\n");
            System.out.print(edge == null ? "null" : String.valueOf(edge.getId()));
            System.out.print("\n");
            System.out.print("From Fact ID\n");
            System.out.print(edge == null ? "null" : String.valueOf(edge.getFromFactId()));
            System.out.print("\n");
            System.out.print("To Fact ID\n");
            System.out.print(edge == null ? "null" : String.valueOf(edge.getToFactId()));
            System.out.print("\n");
        }
        System.out.print("=== FULL GRAPH SNAPSHOT END ===\n");

        System.out.print("=== EXPANDED GRAPH SNAPSHOT BEGIN ===\n");
        System.out.print("Snapshot ID\n");
        System.out.print(String.valueOf(expandedSnapshot.id()));
        System.out.print("\n");
        System.out.print("Node count\n");
        System.out.print(String.valueOf(expandedSnapshot.nodes().size()));
        System.out.print("\n");
        System.out.print("Edge count\n");
        System.out.print(String.valueOf(expandedSnapshot.edges().size()));
        System.out.print("\n");
        for (Map.Entry<String, Fact> entry : expandedSnapshot.nodes().entrySet()) {
            Fact fact = entry.getValue();
            System.out.print("Fact ID\n");
            System.out.print(fact == null ? "null" : String.valueOf(fact.getId()));
            System.out.print("\n");
            System.out.print("Complete Fact text\n");
            System.out.print(fact == null ? "null" : String.valueOf(fact.getText()));
            System.out.print("\n");
            System.out.print("Fact mode\n");
            System.out.print(fact == null ? "null" : String.valueOf(fact.getMode()));
            System.out.print("\n");
            System.out.print("Fact attributes\n");
            System.out.print(fact == null ? "null" : String.valueOf(fact.getAttributes()));
            System.out.print("\n");
        }
        for (Map.Entry<String, Edge> entry : expandedSnapshot.edges().entrySet()) {
            Edge edge = entry.getValue();
            System.out.print("Edge ID\n");
            System.out.print(edge == null ? "null" : String.valueOf(edge.getId()));
            System.out.print("\n");
            System.out.print("From Fact ID\n");
            System.out.print(edge == null ? "null" : String.valueOf(edge.getFromFactId()));
            System.out.print("\n");
            System.out.print("To Fact ID\n");
            System.out.print(edge == null ? "null" : String.valueOf(edge.getToFactId()));
            System.out.print("\n");
        }
        System.out.print("=== EXPANDED GRAPH SNAPSHOT END ===\n");

        System.out.print("=== CONTRACTED GRAPH SNAPSHOT BEGIN ===\n");
        System.out.print("Snapshot ID\n");
        System.out.print(String.valueOf(contractedSnapshot.id()));
        System.out.print("\n");
        System.out.print("Node count\n");
        System.out.print(String.valueOf(contractedSnapshot.nodes().size()));
        System.out.print("\n");
        System.out.print("Edge count\n");
        System.out.print(String.valueOf(contractedSnapshot.edges().size()));
        System.out.print("\n");
        for (Map.Entry<String, Fact> entry : contractedSnapshot.nodes().entrySet()) {
            Fact fact = entry.getValue();
            System.out.print("Fact ID\n");
            System.out.print(fact == null ? "null" : String.valueOf(fact.getId()));
            System.out.print("\n");
            System.out.print("Complete Fact text\n");
            System.out.print(fact == null ? "null" : String.valueOf(fact.getText()));
            System.out.print("\n");
            System.out.print("Fact mode\n");
            System.out.print(fact == null ? "null" : String.valueOf(fact.getMode()));
            System.out.print("\n");
            System.out.print("Fact attributes\n");
            System.out.print(fact == null ? "null" : String.valueOf(fact.getAttributes()));
            System.out.print("\n");
        }
        for (Map.Entry<String, Edge> entry : contractedSnapshot.edges().entrySet()) {
            Edge edge = entry.getValue();
            System.out.print("Edge ID\n");
            System.out.print(edge == null ? "null" : String.valueOf(edge.getId()));
            System.out.print("\n");
            System.out.print("From Fact ID\n");
            System.out.print(edge == null ? "null" : String.valueOf(edge.getFromFactId()));
            System.out.print("\n");
            System.out.print("To Fact ID\n");
            System.out.print(edge == null ? "null" : String.valueOf(edge.getToFactId()));
            System.out.print("\n");
        }
        System.out.print("=== CONTRACTED GRAPH SNAPSHOT END ===\n");

        this.assertLockedResolution(
                inventory,
                List.of("Sales", "Vendor", "Location"),
                List.of(
                        "Inventory:INV-001",
                        "Product:PROD-SHAMPOO-250",
                        "Sales:SALE-001",
                        "Vendor:VENDOR-001",
                        "Location:LOC-001"),
                List.of(
                        "Edge:Inventory:INV-001->Product:PROD-SHAMPOO-250",
                        "Edge:Inventory:INV-001->Location:LOC-001",
                        "Edge:Sales:SALE-001->Product:PROD-SHAMPOO-250",
                        "Edge:Vendor:VENDOR-001->Product:PROD-SHAMPOO-250"),
                inventory,
                product,
                sales,
                vendor,
                location);

        this.assertLockedResolution(
                inventory,
                List.of("Sales", "Vendor"),
                List.of(
                        "Inventory:INV-001",
                        "Product:PROD-SHAMPOO-250",
                        "Sales:SALE-001",
                        "Vendor:VENDOR-001"),
                List.of(
                        "Edge:Inventory:INV-001->Product:PROD-SHAMPOO-250",
                        "Edge:Sales:SALE-001->Product:PROD-SHAMPOO-250",
                        "Edge:Vendor:VENDOR-001->Product:PROD-SHAMPOO-250"),
                inventory,
                product,
                sales,
                vendor,
                location);
    }

    @Test
    public void test_2() {
        Phase0GraphHarness.EstablishedGraph established =
                new Phase0GraphHarness().establish(10);
        GraphSnapshot fullSnapshot = established.getSnapshot();
        Fact inventory = established.getAxis();

        assertEquals(10, established.getInstanceCount());
        assertNotNull(inventory);
        assertEquals("Inventory:INV-001", inventory.getId());
        assertEquals(40, established.getBindResults().size());

        System.out.print("=== BIND RESULTS BEGIN ===\n");
        int bindResultIndex = 0;
        while (bindResultIndex < established.getBindResults().size()) {
            BindResult bindResult = established.getBindResults().get(bindResultIndex);
            System.out.print("bind ");
            System.out.print(String.valueOf(bindResultIndex + 1));
            System.out.print("\n");
            System.out.print(String.valueOf(bindResult.isOk()));
            System.out.print("\n");
            assertTrue(bindResult.isOk());
            bindResultIndex = bindResultIndex + 1;
        }
        System.out.print("=== BIND RESULTS END ===\n");

        Fact product = fullSnapshot.nodes().get("Product:PROD-SHAMPOO-250");
        Fact location = fullSnapshot.nodes().get("Location:LOC-001");
        Fact sales = fullSnapshot.nodes().get("Sales:SALE-001");
        Fact vendor = fullSnapshot.nodes().get("Vendor:VENDOR-001");
        assertNotNull(product);
        assertNotNull(location);
        assertNotNull(sales);
        assertNotNull(vendor);

        Set<String> fixtureIds = new HashSet<String>();
        fixtureIds.add("Product:PROD-SHAMPOO-250");
        fixtureIds.add("Location:LOC-001");
        fixtureIds.add("Inventory:INV-001");
        fixtureIds.add("Sales:SALE-001");
        fixtureIds.add("Vendor:VENDOR-001");

        int extra = 2;
        while (extra <= 10) {
            String seq = extra < 10 ? ("00" + extra) : "010";
            fixtureIds.add("Product:PROD-SHAMPOO-" + (249 + extra));
            fixtureIds.add("Location:LOC-" + seq);
            fixtureIds.add("Inventory:INV-" + seq);
            fixtureIds.add("Sales:SALE-" + seq);
            fixtureIds.add("Vendor:VENDOR-" + seq);
            extra = extra + 1;
        }


        assertEquals(50, fixtureIds.size());
        for (String fixtureId : fixtureIds) {
            assertTrue(fullSnapshot.nodes().containsKey(fixtureId), fixtureId);
        }

        assertEquals(inventory.getText(), fullSnapshot.nodes().get("Inventory:INV-001").getText());
        assertEquals(sales.getText(), fullSnapshot.nodes().get("Sales:SALE-001").getText());
        assertEquals(vendor.getText(), fullSnapshot.nodes().get("Vendor:VENDOR-001").getText());
        assertEquals(product.getText(), fullSnapshot.nodes().get("Product:PROD-SHAMPOO-250").getText());
        assertEquals(location.getText(), fullSnapshot.nodes().get("Location:LOC-001").getText());

        int edgeInstance = 1;
        while (edgeInstance <= 10) {
            String seq = edgeInstance < 10 ? ("00" + edgeInstance) : "010";
            String productFactId = "Product:PROD-SHAMPOO-" + (249 + edgeInstance);
            String inventoryFactId = "Inventory:INV-" + seq;
            String locationFactId = "Location:LOC-" + seq;
            String salesFactId = "Sales:SALE-" + seq;
            String vendorFactId = "Vendor:VENDOR-" + seq;

            this.assertPersistedEdge(
                    fullSnapshot,
                    "Edge:" + inventoryFactId + "->" + productFactId,
                    inventoryFactId,
                    productFactId);
            this.assertPersistedEdge(
                    fullSnapshot,
                    "Edge:" + inventoryFactId + "->" + locationFactId,
                    inventoryFactId,
                    locationFactId);
            this.assertPersistedEdge(
                    fullSnapshot,
                    "Edge:" + salesFactId + "->" + productFactId,
                    salesFactId,
                    productFactId);
            this.assertPersistedEdge(
                    fullSnapshot,
                    "Edge:" + vendorFactId + "->" + productFactId,
                    vendorFactId,
                    productFactId);

            assertFalse(fullSnapshot.edges().containsKey(
                    "Edge:" + inventoryFactId + "->" + salesFactId));
            assertFalse(fullSnapshot.edges().containsKey(
                    "Edge:" + salesFactId + "->" + inventoryFactId));

            edgeInstance = edgeInstance + 1;
        }

        System.out.print("=== FULL GRAPH SNAPSHOT BEGIN ===\n");
        System.out.print("Snapshot ID\n");
        System.out.print(String.valueOf(fullSnapshot.id()));
        System.out.print("\n");
        System.out.print("Node count\n");
        System.out.print(String.valueOf(fullSnapshot.nodes().size()));
        System.out.print("\n");
        System.out.print("Edge count\n");
        System.out.print(String.valueOf(fullSnapshot.edges().size()));
        System.out.print("\n");
        for (Map.Entry<String, Fact> entry : fullSnapshot.nodes().entrySet()) {
            Fact fact = entry.getValue();
            System.out.print("Fact ID\n");
            System.out.print(fact == null ? "null" : String.valueOf(fact.getId()));
            System.out.print("\n");
            System.out.print("Complete Fact text\n");
            System.out.print(fact == null ? "null" : String.valueOf(fact.getText()));
            System.out.print("\n");
            System.out.print("Fact mode\n");
            System.out.print(fact == null ? "null" : String.valueOf(fact.getMode()));
            System.out.print("\n");
            System.out.print("Fact attributes\n");
            System.out.print(fact == null ? "null" : String.valueOf(fact.getAttributes()));
            System.out.print("\n");
        }
        for (Map.Entry<String, Edge> entry : fullSnapshot.edges().entrySet()) {
            Edge edge = entry.getValue();
            System.out.print("Edge ID\n");
            System.out.print(edge == null ? "null" : String.valueOf(edge.getId()));
            System.out.print("\n");
            System.out.print("From Fact ID\n");
            System.out.print(edge == null ? "null" : String.valueOf(edge.getFromFactId()));
            System.out.print("\n");
            System.out.print("To Fact ID\n");
            System.out.print(edge == null ? "null" : String.valueOf(edge.getToFactId()));
            System.out.print("\n");
        }
        System.out.print("=== FULL GRAPH SNAPSHOT END ===\n");

        this.observeDimensionExperiment("A", fullSnapshot, inventory, List.of("Sales"));
        this.observeDimensionExperiment("B", fullSnapshot, inventory, List.of("Vendor"));
        this.observeDimensionExperiment("C", fullSnapshot, inventory, List.of("Location"));
        this.observeDimensionExperiment("D", fullSnapshot, inventory, List.of("Sales", "Vendor"));
        this.observeDimensionExperiment("E", fullSnapshot, inventory, List.of("Sales", "Vendor", "Location"));
        this.observeDimensionExperiment("F", fullSnapshot, inventory, List.of());

        this.observeDimensionCardinality("SALES", inventory, List.of("Sales"));
        this.observeDimensionCardinality("VENDOR", inventory, List.of("Vendor"));
        this.observeDimensionCardinality("LOCATION", inventory, List.of("Location"));
        this.observeDimensionCardinality(
                "SALES_VENDOR_LOCATION",
                inventory,
                List.of("Sales", "Vendor", "Location"));

        GraphResolver resolver = new GraphResolver();
        ResolvedGraph resolved = resolver.resolve(
                inventory,
                List.of("Sales"));
        GraphSnapshot resolvedSnapshot = resolved.getSnapshot();

        System.out.print("=== RESOLVED GRAPH SNAPSHOT BEGIN ===\n");
        System.out.print("Snapshot ID\n");
        System.out.print(String.valueOf(resolvedSnapshot.id()));
        System.out.print("\n");
        System.out.print("Node count\n");
        System.out.print(String.valueOf(resolvedSnapshot.nodes().size()));
        System.out.print("\n");
        System.out.print("Edge count\n");
        System.out.print(String.valueOf(resolvedSnapshot.edges().size()));
        System.out.print("\n");
        for (Map.Entry<String, Fact> entry : resolvedSnapshot.nodes().entrySet()) {
            Fact fact = entry.getValue();
            System.out.print("Fact ID\n");
            System.out.print(fact == null ? "null" : String.valueOf(fact.getId()));
            System.out.print("\n");
            System.out.print("Complete Fact text\n");
            System.out.print(fact == null ? "null" : String.valueOf(fact.getText()));
            System.out.print("\n");
            System.out.print("Fact mode\n");
            System.out.print(fact == null ? "null" : String.valueOf(fact.getMode()));
            System.out.print("\n");
            System.out.print("Fact attributes\n");
            System.out.print(fact == null ? "null" : String.valueOf(fact.getAttributes()));
            System.out.print("\n");
        }
        for (Map.Entry<String, Edge> entry : resolvedSnapshot.edges().entrySet()) {
            Edge edge = entry.getValue();
            System.out.print("Edge ID\n");
            System.out.print(edge == null ? "null" : String.valueOf(edge.getId()));
            System.out.print("\n");
            System.out.print("From Fact ID\n");
            System.out.print(edge == null ? "null" : String.valueOf(edge.getFromFactId()));
            System.out.print("\n");
            System.out.print("To Fact ID\n");
            System.out.print(edge == null ? "null" : String.valueOf(edge.getToFactId()));
            System.out.print("\n");
        }
        System.out.print("=== RESOLVED GRAPH SNAPSHOT END ===\n");

        assertTrue(resolvedSnapshot.nodes().containsKey("Inventory:INV-001"));
        assertTrue(resolvedSnapshot.nodes().containsKey("Sales:SALE-001"));
        assertTrue(resolvedSnapshot.nodes().containsKey("Product:PROD-SHAMPOO-250"));

        int saleIndex = 2;
        while (saleIndex <= 10) {
            String seq = saleIndex < 10 ? ("00" + saleIndex) : "010";
            assertFalse(resolvedSnapshot.nodes().containsKey("Sales:SALE-" + seq));
            saleIndex = saleIndex + 1;
        }

        assertEquals(3, resolvedSnapshot.nodes().size());

        Edge inventoryProductEdge = resolvedSnapshot.edges().get(
                "Edge:Inventory:INV-001->Product:PROD-SHAMPOO-250");
        assertNotNull(inventoryProductEdge);
        assertEquals("Inventory:INV-001", inventoryProductEdge.getFromFactId());
        assertEquals("Product:PROD-SHAMPOO-250", inventoryProductEdge.getToFactId());

        Edge salesProductEdge = resolvedSnapshot.edges().get(
                "Edge:Sales:SALE-001->Product:PROD-SHAMPOO-250");
        assertNotNull(salesProductEdge);
        assertEquals("Sales:SALE-001", salesProductEdge.getFromFactId());
        assertEquals("Product:PROD-SHAMPOO-250", salesProductEdge.getToFactId());

        assertEquals(inventory, resolved.getAxis());
        assertEquals(inventory.getText(), resolvedSnapshot.nodes().get("Inventory:INV-001").getText());
        assertEquals(product.getText(), resolvedSnapshot.nodes().get("Product:PROD-SHAMPOO-250").getText());
        assertEquals(sales.getText(), resolvedSnapshot.nodes().get("Sales:SALE-001").getText());

        this.assertLockedResolution(
                inventory,
                List.of(),
                List.of("Inventory:INV-001"),
                List.of(),
                inventory,
                product,
                sales,
                vendor,
                location);

        this.assertLockedResolution(
                inventory,
                List.of("Vendor"),
                List.of(
                        "Inventory:INV-001",
                        "Product:PROD-SHAMPOO-250",
                        "Vendor:VENDOR-001"),
                List.of(
                        "Edge:Inventory:INV-001->Product:PROD-SHAMPOO-250",
                        "Edge:Vendor:VENDOR-001->Product:PROD-SHAMPOO-250"),
                inventory,
                product,
                sales,
                vendor,
                location);

        this.assertLockedResolution(
                inventory,
                List.of("Location"),
                List.of(
                        "Inventory:INV-001",
                        "Location:LOC-001"),
                List.of("Edge:Inventory:INV-001->Location:LOC-001"),
                inventory,
                product,
                sales,
                vendor,
                location);

        this.assertLockedResolution(
                inventory,
                List.of("Sales", "Vendor"),
                List.of(
                        "Inventory:INV-001",
                        "Product:PROD-SHAMPOO-250",
                        "Sales:SALE-001",
                        "Vendor:VENDOR-001"),
                List.of(
                        "Edge:Inventory:INV-001->Product:PROD-SHAMPOO-250",
                        "Edge:Sales:SALE-001->Product:PROD-SHAMPOO-250",
                        "Edge:Vendor:VENDOR-001->Product:PROD-SHAMPOO-250"),
                inventory,
                product,
                sales,
                vendor,
                location);

        this.assertLockedResolution(
                inventory,
                List.of("Sales", "Vendor", "Location"),
                List.of(
                        "Inventory:INV-001",
                        "Product:PROD-SHAMPOO-250",
                        "Sales:SALE-001",
                        "Vendor:VENDOR-001",
                        "Location:LOC-001"),
                List.of(
                        "Edge:Inventory:INV-001->Product:PROD-SHAMPOO-250",
                        "Edge:Inventory:INV-001->Location:LOC-001",
                        "Edge:Sales:SALE-001->Product:PROD-SHAMPOO-250",
                        "Edge:Vendor:VENDOR-001->Product:PROD-SHAMPOO-250"),
                inventory,
                product,
                sales,
                vendor,
                location);
    }

    private void observeDimensionExperiment(
            String label,
            GraphSnapshot fullSnapshot,
            Fact axis,
            List<String> relatedFacts) {
        GraphResolver resolver = new GraphResolver();
        ResolvedGraph resolved = resolver.resolve(axis, relatedFacts);
        GraphSnapshot resolvedSnapshot = resolved.getSnapshot();

        System.out.print("=== DIMENSION EXPERIMENT ");
        System.out.print(label);
        System.out.print(" BEGIN ===\n");
        System.out.print("Requested axis ID\n");
        System.out.print(String.valueOf(axis.getId()));
        System.out.print("\n");
        System.out.print("Requested dimensions\n");
        System.out.print(String.valueOf(relatedFacts));
        System.out.print("\n");
        System.out.print("Full snapshot node count\n");
        System.out.print(String.valueOf(fullSnapshot.nodes().size()));
        System.out.print("\n");
        System.out.print("Full snapshot edge count\n");
        System.out.print(String.valueOf(fullSnapshot.edges().size()));
        System.out.print("\n");
        System.out.print("Resolved snapshot node count\n");
        System.out.print(String.valueOf(resolvedSnapshot.nodes().size()));
        System.out.print("\n");
        System.out.print("Resolved snapshot edge count\n");
        System.out.print(String.valueOf(resolvedSnapshot.edges().size()));
        System.out.print("\n");

        for (Map.Entry<String, Fact> entry : resolvedSnapshot.nodes().entrySet()) {
            Fact fact = entry.getValue();
            System.out.print("Fact ID\n");
            System.out.print(fact == null ? "null" : String.valueOf(fact.getId()));
            System.out.print("\n");
            System.out.print("Fact mode\n");
            System.out.print(fact == null ? "null" : String.valueOf(fact.getMode()));
            System.out.print("\n");
            System.out.print("Complete Fact text\n");
            System.out.print(fact == null ? "null" : String.valueOf(fact.getText()));
            System.out.print("\n");
        }

        for (Map.Entry<String, Edge> entry : resolvedSnapshot.edges().entrySet()) {
            Edge edge = entry.getValue();
            System.out.print("Edge ID\n");
            System.out.print(edge == null ? "null" : String.valueOf(edge.getId()));
            System.out.print("\n");
            System.out.print("From Fact ID\n");
            System.out.print(edge == null ? "null" : String.valueOf(edge.getFromFactId()));
            System.out.print("\n");
            System.out.print("To Fact ID\n");
            System.out.print(edge == null ? "null" : String.valueOf(edge.getToFactId()));
            System.out.print("\n");
        }

        boolean axisPresent = resolvedSnapshot.nodes().containsKey(axis.getId());
        System.out.print("Axis present\n");
        System.out.print(String.valueOf(axisPresent));
        System.out.print("\n");

        Set<String> instanceIds = new TreeSet<String>();
        boolean extrasPresent = false;
        int instance = 1;
        while (instance <= 10) {
            String seq = instance < 10 ? ("00" + instance) : "010";
            String productFactId = "Product:PROD-SHAMPOO-" + (249 + instance);
            boolean represented =
                    resolvedSnapshot.nodes().containsKey("Inventory:INV-" + seq)
                            || resolvedSnapshot.nodes().containsKey("Sales:SALE-" + seq)
                            || resolvedSnapshot.nodes().containsKey("Vendor:VENDOR-" + seq)
                            || resolvedSnapshot.nodes().containsKey("Location:LOC-" + seq)
                            || resolvedSnapshot.nodes().containsKey(productFactId);
            if (represented) {
                instanceIds.add(seq);
                if (instance >= 2) {
                    extrasPresent = true;
                }
            }
            instance = instance + 1;
        }

        System.out.print("Instance IDs represented\n");
        System.out.print(String.valueOf(instanceIds));
        System.out.print("\n");
        System.out.print("Facts from instances 002-010 present\n");
        System.out.print(String.valueOf(extrasPresent));
        System.out.print("\n");
        System.out.print("=== DIMENSION EXPERIMENT ");
        System.out.print(label);
        System.out.print(" END ===\n");
    }

    private void observeDimensionCardinality(
            String label,
            Fact axis,
            List<String> relatedFacts) {
        GraphResolver resolver = new GraphResolver();
        ResolvedGraph resolved = resolver.resolve(axis, relatedFacts);
        GraphSnapshot resolvedSnapshot = resolved.getSnapshot();

        System.out.print("=== DIMENSION CARDINALITY ");
        System.out.print(label);
        System.out.print(" BEGIN ===\n");
        System.out.print("Axis Fact ID\n");
        System.out.print(String.valueOf(axis.getId()));
        System.out.print("\n");
        System.out.print("Requested dimension\n");
        System.out.print(String.valueOf(relatedFacts));
        System.out.print("\n");

        for (Map.Entry<String, Fact> entry : resolvedSnapshot.nodes().entrySet()) {
            Fact fact = entry.getValue();
            System.out.print("Returned Fact ID\n");
            System.out.print(fact == null ? "null" : String.valueOf(fact.getId()));
            System.out.print("\n");
        }

        for (Map.Entry<String, Edge> entry : resolvedSnapshot.edges().entrySet()) {
            Edge edge = entry.getValue();
            System.out.print("Returned Edge ID\n");
            System.out.print(edge == null ? "null" : String.valueOf(edge.getId()));
            System.out.print("\n");
            System.out.print("From Fact ID\n");
            System.out.print(edge == null ? "null" : String.valueOf(edge.getFromFactId()));
            System.out.print("\n");
            System.out.print("To Fact ID\n");
            System.out.print(edge == null ? "null" : String.valueOf(edge.getToFactId()));
            System.out.print("\n");
        }

        int relatedIndex = 0;
        while (relatedIndex < relatedFacts.size()) {
            String dimension = relatedFacts.get(relatedIndex);
            int dimensionCount = 0;
            String matchingFactId = null;
            for (Map.Entry<String, Fact> entry : resolvedSnapshot.nodes().entrySet()) {
                Fact fact = entry.getValue();
                if (fact == null || fact.getId() == null || dimension == null) {
                    continue;
                }
                if (fact.getId().startsWith(dimension + ":")) {
                    dimensionCount = dimensionCount + 1;
                    matchingFactId = fact.getId();
                }
            }
            System.out.print("Requested dimension kind\n");
            System.out.print(String.valueOf(dimension));
            System.out.print("\n");
            System.out.print("Number of Facts belonging to requested dimension\n");
            System.out.print(String.valueOf(dimensionCount));
            System.out.print("\n");
            System.out.print("Count equals exactly 1\n");
            System.out.print(String.valueOf(dimensionCount == 1));
            System.out.print("\n");
            System.out.print("Exact matching Fact ID\n");
            if (dimensionCount == 1) {
                System.out.print(String.valueOf(matchingFactId));
            } else {
                System.out.print("none");
            }
            System.out.print("\n");
            relatedIndex = relatedIndex + 1;
        }

        boolean extrasPresent = false;
        int instance = 2;
        while (instance <= 10) {
            String seq = instance < 10 ? ("00" + instance) : "010";
            String productFactId = "Product:PROD-SHAMPOO-" + (249 + instance);
            if (resolvedSnapshot.nodes().containsKey("Inventory:INV-" + seq)
                    || resolvedSnapshot.nodes().containsKey("Sales:SALE-" + seq)
                    || resolvedSnapshot.nodes().containsKey("Vendor:VENDOR-" + seq)
                    || resolvedSnapshot.nodes().containsKey("Location:LOC-" + seq)
                    || resolvedSnapshot.nodes().containsKey(productFactId)) {
                extrasPresent = true;
            }
            instance = instance + 1;
        }
        System.out.print("Facts from instances 002-010 present\n");
        System.out.print(String.valueOf(extrasPresent));
        System.out.print("\n");
        System.out.print("=== DIMENSION CARDINALITY ");
        System.out.print(label);
        System.out.print(" END ===\n");
    }

    private void assertLockedResolution(
            Fact axis,
            List<String> relatedFacts,
            List<String> expectedFactIds,
            List<String> expectedEdgeIds,
            Fact inventory,
            Fact product,
            Fact sales,
            Fact vendor,
            Fact location) {
        GraphResolver resolver = new GraphResolver();
        ResolvedGraph resolved = resolver.resolve(axis, relatedFacts);
        GraphSnapshot snapshot = resolved.getSnapshot();

        assertEquals(axis, resolved.getAxis());
        assertEquals(expectedFactIds.size(), snapshot.nodes().size());
        assertEquals(expectedEdgeIds.size(), snapshot.edges().size());

        int factIndex = 0;
        while (factIndex < expectedFactIds.size()) {
            String factId = expectedFactIds.get(factIndex);
            assertTrue(snapshot.nodes().containsKey(factId), factId);
            factIndex = factIndex + 1;
        }

        int edgeIndex = 0;
        while (edgeIndex < expectedEdgeIds.size()) {
            String edgeId = expectedEdgeIds.get(edgeIndex);
            Edge edge = snapshot.edges().get(edgeId);
            assertNotNull(edge, edgeId);
            if (edgeId.equals("Edge:Inventory:INV-001->Product:PROD-SHAMPOO-250")) {
                assertEquals("Inventory:INV-001", edge.getFromFactId());
                assertEquals("Product:PROD-SHAMPOO-250", edge.getToFactId());
            }
            if (edgeId.equals("Edge:Sales:SALE-001->Product:PROD-SHAMPOO-250")) {
                assertEquals("Sales:SALE-001", edge.getFromFactId());
                assertEquals("Product:PROD-SHAMPOO-250", edge.getToFactId());
            }
            if (edgeId.equals("Edge:Vendor:VENDOR-001->Product:PROD-SHAMPOO-250")) {
                assertEquals("Vendor:VENDOR-001", edge.getFromFactId());
                assertEquals("Product:PROD-SHAMPOO-250", edge.getToFactId());
            }
            if (edgeId.equals("Edge:Inventory:INV-001->Location:LOC-001")) {
                assertEquals("Inventory:INV-001", edge.getFromFactId());
                assertEquals("Location:LOC-001", edge.getToFactId());
            }
            edgeIndex = edgeIndex + 1;
        }

        if (snapshot.nodes().containsKey("Inventory:INV-001")) {
            assertEquals(inventory.getText(), snapshot.nodes().get("Inventory:INV-001").getText());
        }
        if (snapshot.nodes().containsKey("Product:PROD-SHAMPOO-250")) {
            assertEquals(product.getText(), snapshot.nodes().get("Product:PROD-SHAMPOO-250").getText());
        }
        if (snapshot.nodes().containsKey("Sales:SALE-001")) {
            assertEquals(sales.getText(), snapshot.nodes().get("Sales:SALE-001").getText());
        }
        if (snapshot.nodes().containsKey("Vendor:VENDOR-001")) {
            assertEquals(vendor.getText(), snapshot.nodes().get("Vendor:VENDOR-001").getText());
        }
        if (snapshot.nodes().containsKey("Location:LOC-001")) {
            assertEquals(location.getText(), snapshot.nodes().get("Location:LOC-001").getText());
        }

        int extra = 2;
        while (extra <= 10) {
            String seq = extra < 10 ? ("00" + extra) : "010";
            assertFalse(snapshot.nodes().containsKey("Inventory:INV-" + seq));
            assertFalse(snapshot.nodes().containsKey("Sales:SALE-" + seq));
            assertFalse(snapshot.nodes().containsKey("Vendor:VENDOR-" + seq));
            assertFalse(snapshot.nodes().containsKey("Location:LOC-" + seq));
            assertFalse(snapshot.nodes().containsKey("Product:PROD-SHAMPOO-" + (249 + extra)));
            extra = extra + 1;
        }
    }

    private void assertPersistedEdge(
            GraphSnapshot snapshot,
            String edgeId,
            String fromFactId,
            String toFactId) {
        Edge edge = snapshot.edges().get(edgeId);
        assertNotNull(edge, edgeId);
        assertEquals(fromFactId, edge.getFromFactId());
        assertEquals(toFactId, edge.getToFactId());
    }
}
