package ai.braineous.cgo.nexus.orchestrator;

import ai.braineous.cgo.nexus.model.ResolvedGraph;
import ai.braineous.rag.prompt.cgo.api.Edge;
import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.models.cgo.graph.GraphBuilder;
import ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GraphResolverTest {

    @Test
    public void test_1() {
        GraphBuilder graphBuilder = GraphBuilder.getInstance();

        Fact product = new Fact("Product:PROD-SHAMPOO-250",
                "{\"id\":\"Product:PROD-SHAMPOO-250\",\"kind\":\"Product\",\"mode\":\"atomic\",\"productId\":\"PROD-SHAMPOO-250\",\"sku\":\"SHAMPOO-250\",\"name\":\"Shampoo 250 mL\",\"unitOfMeasure\":\"bottle\"}");
        product.setMode("atomic");

        Fact location = new Fact("Location:LOC-001",
                "{\"id\":\"Location:LOC-001\",\"kind\":\"Location\",\"mode\":\"atomic\",\"locationId\":\"LOC-001\",\"name\":\"Primary Inventory Location\",\"locationType\":\"storage\"}");
        location.setMode("atomic");

        Fact inventory = new Fact("Inventory:INV-001",
                "{\"id\":\"Inventory:INV-001\",\"kind\":\"Inventory\",\"mode\":\"atomic\",\"inventoryId\":\"INV-001\",\"quantityOnHand\":500,\"quantityReserved\":0,\"product\":\"Product:PROD-SHAMPOO-250\",\"location\":\"Location:LOC-001\"}");
        inventory.setMode("atomic");

        Fact sales = new Fact("Sales:SALE-001",
                "{\"id\":\"Sales:SALE-001\",\"kind\":\"Sales\",\"mode\":\"atomic\",\"saleId\":\"SALE-001\",\"quantitySold\":25,\"soldAt\":1792584000000,\"product\":\"Product:PROD-SHAMPOO-250\"}");
        sales.setMode("atomic");

        Fact vendor = new Fact("Vendor:VENDOR-001",
                "{\"id\":\"Vendor:VENDOR-001\",\"kind\":\"Vendor\",\"mode\":\"atomic\",\"vendorId\":\"VENDOR-001\",\"name\":\"Shampoo Supplier\",\"leadTimeDays\":7,\"product\":\"Product:PROD-SHAMPOO-250\"}");
        vendor.setMode("atomic");

        graphBuilder.addNode(product);
        graphBuilder.addNode(location);
        graphBuilder.addNode(inventory);
        graphBuilder.addNode(sales);
        graphBuilder.addNode(vendor);

        Set<String> fixtureIds = new HashSet<String>();
        fixtureIds.add("Product:PROD-SHAMPOO-250");
        fixtureIds.add("Location:LOC-001");
        fixtureIds.add("Inventory:INV-001");
        fixtureIds.add("Sales:SALE-001");
        fixtureIds.add("Vendor:VENDOR-001");

        int extra = 2;
        while (extra <= 10) {
            String seq = extra < 10 ? ("00" + extra) : "010";
            String productId = "PROD-SHAMPOO-" + (249 + extra);
            String productFactId = "Product:" + productId;
            String locationId = "LOC-" + seq;
            String locationFactId = "Location:" + locationId;
            String inventoryId = "INV-" + seq;
            String inventoryFactId = "Inventory:" + inventoryId;
            String saleId = "SALE-" + seq;
            String salesFactId = "Sales:" + saleId;
            String vendorId = "VENDOR-" + seq;
            String vendorFactId = "Vendor:" + vendorId;

            Fact extraProduct = new Fact(productFactId,
                    "{\"id\":\"" + productFactId + "\",\"kind\":\"Product\",\"mode\":\"atomic\",\"productId\":\"" + productId + "\",\"sku\":\"SHAMPOO-" + (249 + extra) + "\",\"name\":\"Shampoo " + (249 + extra) + " mL\",\"unitOfMeasure\":\"bottle\"}");
            extraProduct.setMode("atomic");

            Fact extraLocation = new Fact(locationFactId,
                    "{\"id\":\"" + locationFactId + "\",\"kind\":\"Location\",\"mode\":\"atomic\",\"locationId\":\"" + locationId + "\",\"name\":\"Location " + seq + "\",\"locationType\":\"storage\"}");
            extraLocation.setMode("atomic");

            Fact extraInventory = new Fact(inventoryFactId,
                    "{\"id\":\"" + inventoryFactId + "\",\"kind\":\"Inventory\",\"mode\":\"atomic\",\"inventoryId\":\"" + inventoryId + "\",\"quantityOnHand\":" + (500 + extra) + ",\"quantityReserved\":0,\"product\":\"" + productFactId + "\",\"location\":\"" + locationFactId + "\"}");
            extraInventory.setMode("atomic");

            Fact extraSales = new Fact(salesFactId,
                    "{\"id\":\"" + salesFactId + "\",\"kind\":\"Sales\",\"mode\":\"atomic\",\"saleId\":\"" + saleId + "\",\"quantitySold\":" + (25 + extra) + ",\"soldAt\":" + (1792584000000L + extra) + ",\"product\":\"" + productFactId + "\"}");
            extraSales.setMode("atomic");

            Fact extraVendor = new Fact(vendorFactId,
                    "{\"id\":\"" + vendorFactId + "\",\"kind\":\"Vendor\",\"mode\":\"atomic\",\"vendorId\":\"" + vendorId + "\",\"name\":\"Shampoo Supplier " + seq + "\",\"leadTimeDays\":" + (7 + extra) + ",\"product\":\"" + productFactId + "\"}");
            extraVendor.setMode("atomic");

            graphBuilder.addNode(extraProduct);
            graphBuilder.addNode(extraLocation);
            graphBuilder.addNode(extraInventory);
            graphBuilder.addNode(extraSales);
            graphBuilder.addNode(extraVendor);

            fixtureIds.add(productFactId);
            fixtureIds.add(locationFactId);
            fixtureIds.add(inventoryFactId);
            fixtureIds.add(salesFactId);
            fixtureIds.add(vendorFactId);

            extra = extra + 1;
        }

        GraphSnapshot snapshot = graphBuilder.snapshot();

        assertNotNull(snapshot);
        assertNotNull(snapshot.nodes());

        assertTrue(snapshot.nodes().values().stream()
                .anyMatch(n -> n.getId().equals("Inventory:INV-001")));
        assertTrue(snapshot.nodes().values().stream()
                .anyMatch(n -> n.getId().equals("Sales:SALE-001")));
        assertTrue(snapshot.nodes().values().stream()
                .anyMatch(n -> n.getId().equals("Vendor:VENDOR-001")));
        assertTrue(snapshot.nodes().values().stream()
                .anyMatch(n -> n.getId().equals("Product:PROD-SHAMPOO-250")));
        assertTrue(snapshot.nodes().values().stream()
                .anyMatch(n -> n.getId().equals("Location:LOC-001")));

        assertEquals(inventory.getText(), snapshot.nodes().get("Inventory:INV-001").getText());
        assertEquals(sales.getText(), snapshot.nodes().get("Sales:SALE-001").getText());
        assertEquals(vendor.getText(), snapshot.nodes().get("Vendor:VENDOR-001").getText());
        assertEquals("atomic", snapshot.nodes().get("Inventory:INV-001").getMode());
        assertEquals("atomic", snapshot.nodes().get("Sales:SALE-001").getMode());
        assertEquals("atomic", snapshot.nodes().get("Vendor:VENDOR-001").getMode());

        GraphSnapshot fullSnapshot =
                GraphBuilder.getInstance().snapshot();

        for (String fixtureId : fixtureIds) {
            assertTrue(fullSnapshot.nodes().containsKey(fixtureId), fixtureId);
        }
        assertEquals(50, fixtureIds.size());
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
        int expandedNodeCount = expandedSnapshot.nodes().size();
        boolean expandedHasLocation = expandedSnapshot.nodes().containsKey("Location:LOC-001");

        ResolvedGraph contracted = resolver.resolve(
                inventory,
                List.of("Sales", "Vendor"));
        GraphSnapshot contractedSnapshot = contracted.getSnapshot();

        assertEquals(expandedNodeCount, expandedSnapshot.nodes().size());
        assertEquals(expandedHasLocation, expandedSnapshot.nodes().containsKey("Location:LOC-001"));
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

        assertEquals(31, expandedSnapshot.nodes().size());
        assertEquals(21, contractedSnapshot.nodes().size());
        assertTrue(expandedSnapshot.nodes().containsKey("Inventory:INV-001"));
        assertTrue(contractedSnapshot.nodes().containsKey("Inventory:INV-001"));
        assertFalse(expandedSnapshot.nodes().containsKey("Product:PROD-SHAMPOO-250"));
        assertFalse(contractedSnapshot.nodes().containsKey("Product:PROD-SHAMPOO-250"));

        int dimIndex = 1;
        while (dimIndex <= 10) {
            String seq = dimIndex < 10 ? ("00" + dimIndex) : "010";
            assertTrue(expandedSnapshot.nodes().containsKey("Sales:SALE-" + seq));
            assertTrue(expandedSnapshot.nodes().containsKey("Vendor:VENDOR-" + seq));
            assertTrue(expandedSnapshot.nodes().containsKey("Location:LOC-" + seq));
            assertTrue(contractedSnapshot.nodes().containsKey("Sales:SALE-" + seq));
            assertTrue(contractedSnapshot.nodes().containsKey("Vendor:VENDOR-" + seq));
            assertFalse(contractedSnapshot.nodes().containsKey("Location:LOC-" + seq));
            dimIndex = dimIndex + 1;
        }

        int inventoryIndex = 2;
        while (inventoryIndex <= 10) {
            String invSeq = inventoryIndex < 10 ? ("00" + inventoryIndex) : "010";
            assertFalse(expandedSnapshot.nodes().containsKey("Inventory:INV-" + invSeq));
            assertFalse(contractedSnapshot.nodes().containsKey("Inventory:INV-" + invSeq));
            inventoryIndex = inventoryIndex + 1;
        }

        for (String nodeId : expandedSnapshot.nodes().keySet()) {
            assertFalse(nodeId.startsWith("Product:"));
            if (nodeId.startsWith("Inventory:")) {
                assertEquals("Inventory:INV-001", nodeId);
            }
        }

        for (String nodeId : contractedSnapshot.nodes().keySet()) {
            assertFalse(nodeId.startsWith("Product:"));
            assertFalse(nodeId.startsWith("Location:"));
            if (nodeId.startsWith("Inventory:")) {
                assertEquals("Inventory:INV-001", nodeId);
            }
        }

        assertEquals(inventory.getText(), expandedSnapshot.nodes().get("Inventory:INV-001").getText());
        assertEquals(sales.getText(), expandedSnapshot.nodes().get("Sales:SALE-001").getText());
        assertEquals(vendor.getText(), expandedSnapshot.nodes().get("Vendor:VENDOR-001").getText());
        assertEquals(location.getText(), expandedSnapshot.nodes().get("Location:LOC-001").getText());
        assertEquals(inventory.getText(), contractedSnapshot.nodes().get("Inventory:INV-001").getText());
        assertEquals(sales.getText(), contractedSnapshot.nodes().get("Sales:SALE-001").getText());
        assertEquals(vendor.getText(), contractedSnapshot.nodes().get("Vendor:VENDOR-001").getText());
        assertEquals(inventory.getText(), fullSnapshot.nodes().get("Inventory:INV-001").getText());
        assertEquals(sales.getText(), fullSnapshot.nodes().get("Sales:SALE-001").getText());
        assertEquals(vendor.getText(), fullSnapshot.nodes().get("Vendor:VENDOR-001").getText());
        assertEquals(product.getText(), fullSnapshot.nodes().get("Product:PROD-SHAMPOO-250").getText());
        assertEquals(location.getText(), fullSnapshot.nodes().get("Location:LOC-001").getText());

        for (Map.Entry<String, Edge> entry : expandedSnapshot.edges().entrySet()) {
            Edge edge = entry.getValue();
            assertNotNull(edge);
            assertTrue(expandedSnapshot.nodes().containsKey(edge.getFromFactId()));
            assertTrue(expandedSnapshot.nodes().containsKey(edge.getToFactId()));
        }

        for (Map.Entry<String, Edge> entry : contractedSnapshot.edges().entrySet()) {
            Edge edge = entry.getValue();
            assertNotNull(edge);
            assertTrue(contractedSnapshot.nodes().containsKey(edge.getFromFactId()));
            assertTrue(contractedSnapshot.nodes().containsKey(edge.getToFactId()));
        }
    }
}
