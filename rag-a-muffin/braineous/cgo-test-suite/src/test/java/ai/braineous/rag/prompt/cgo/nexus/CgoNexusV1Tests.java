package ai.braineous.rag.prompt.cgo.nexus;

import ai.braineous.rag.prompt.cgo.api.Edge;
import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.cgo.nexus.arithmetic.ArithmeticEngine;
import ai.braineous.rag.prompt.cgo.nexus.map.MapEngine;
import ai.braineous.rag.prompt.cgo.nexus.model.ResolvedGraph;
import ai.braineous.rag.prompt.cgo.nexus.orchestrator.GraphResolver;
import ai.braineous.rag.prompt.cgo.nexus.orchestrator.NexusOrchestrator;
import ai.braineous.rag.prompt.cgo.nexus.template.TemplateGenerator;
import ai.braineous.rag.prompt.models.cgo.graph.BindResult;
import ai.braineous.rag.prompt.models.cgo.graph.GraphBuilder;
import ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot;
import ai.braineous.rag.prompt.models.cgo.graph.Input;
import ai.braineous.rag.prompt.observe.Console;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CgoNexusV1Tests {

    private static final String INVENTORY_EXPRESSION =
            "(Inventory.quantityOnHand - Inventory.quantityReserved) / Sales.quantitySold";

    @Test
    public void test_1() {
        Fact inventory = this.hydrateInventoryUnavailable();
        ResolvedGraph resolved =
                new GraphResolver().resolve(inventory, List.of("Sales"));

        Console.log("cgo.nexus.resolver.axis", inventory.getId());

        Assertions.assertNotNull(resolved);
        Assertions.assertSame(inventory, resolved.getAxis());
        Assertions.assertNotNull(resolved.getSnapshot());
        Assertions.assertTrue(resolved.getSnapshot().nodes().containsKey("Inventory:INV-001"));
        Assertions.assertTrue(resolved.getSnapshot().nodes().containsKey("Sales:SALE-001"));
        Assertions.assertTrue(resolved.getSnapshot().nodes().containsKey("Product:PROD-SHAMPOO-250"));
    }

    @Test
    public void test_2() {
        Fact inventory = this.hydrateInventoryUnavailable();
        ResolvedGraph resolved =
                new GraphResolver().resolve(inventory, List.of("Sales"));
        BigDecimal actual =
                new ArithmeticEngine().evaluate(INVENTORY_EXPRESSION, resolved);

        Console.log("cgo.nexus.arithmetic.unavailable", actual.toPlainString());

        Assertions.assertEquals(0, BigDecimal.ZERO.compareTo(actual));
    }

    @Test
    public void test_3() {
        Map<String, Fact> nodes = new LinkedHashMap<String, Fact>();
        Fact account = this.fact(
                "Account:emp-042",
                "{\"id\":\"Account:emp-042\",\"kind\":\"Account\","
                        + "\"mode\":\"atomic\",\"account\":\"emp-042\"}");
        Fact location = this.fact(
                "Location:germany",
                "{\"id\":\"Location:germany\",\"kind\":\"Location\","
                        + "\"mode\":\"atomic\",\"location\":\"germany\"}");
        nodes.put(account.getId(), account);
        nodes.put(location.getId(), location);
        ResolvedGraph resolved =
                new ResolvedGraph(account, new GraphSnapshot(nodes, new LinkedHashMap<String, Edge>()));

        Map<String, String> actual = new MapEngine().evaluate(resolved);

        Console.log("cgo.nexus.map", String.valueOf(actual));

        Assertions.assertEquals("emp-042", actual.get("account"));
        Assertions.assertEquals("germany", actual.get("location"));
        Assertions.assertFalse(actual.containsKey("id"));
        Assertions.assertFalse(actual.containsKey("kind"));
        Assertions.assertFalse(actual.containsKey("mode"));
    }

    @Test
    public void test_4() {
        String actual =
                new TemplateGenerator().generate("inventory availability.", BigDecimal.ZERO);

        Console.log("cgo.nexus.template.arithmetic", actual);

        Assertions.assertEquals(
                "inventory availability.\n\ncontext\nThe result is 0.",
                actual);
    }

    @Test
    public void test_5() {
        Map<String, String> values = new LinkedHashMap<String, String>();
        values.put("account", "emp-042");
        values.put("location", "germany");
        String actual =
                new TemplateGenerator().generate("security investigation", values);

        Console.log("cgo.nexus.template.map", actual);

        Assertions.assertEquals(
                "security investigation\n\ncontext\naccount: emp-042\nlocation: germany",
                actual);
        Assertions.assertFalse(actual.contains("The result is"));
    }

    @Test
    public void test_6() {
        Fact axis = this.fact("Inventory:INV-001", "{\"id\":\"Inventory:INV-001\"}");
        GraphSnapshot snapshot =
                new GraphSnapshot(new LinkedHashMap<String, Fact>(), new LinkedHashMap<String, Edge>());
        ResolvedGraph resolved = new ResolvedGraph(axis, snapshot);

        Assertions.assertSame(axis, resolved.getAxis());
        Assertions.assertSame(snapshot, resolved.getSnapshot());
    }

    @Test
    public void test_7() {
        Fact inventory = this.hydrateInventoryUnavailable();
        String actual = new NexusOrchestrator().execute(
                inventory,
                List.of("Sales"),
                INVENTORY_EXPRESSION,
                "inventory availability.");

        Console.log("cgo.nexus.inventory.unavailable", actual);

        Assertions.assertEquals(
                "inventory availability.\n\ncontext\nThe result is 0.",
                actual);
    }

    @Test
    public void test_8() {
        Fact inventory = this.hydrateInventoryAvailable();
        String actual = new NexusOrchestrator().execute(
                inventory,
                List.of("Sales"),
                INVENTORY_EXPRESSION,
                "inventory availability.");

        Console.log("cgo.nexus.inventory.available", actual);

        Assertions.assertEquals(
                "inventory availability.\n\ncontext\nThe result is 1.",
                actual);
    }

    @Test
    public void test_9() {
        Fact account = this.hydrateSecurity(3, 1);
        String actual = new NexusOrchestrator().execute(
                account,
                List.of("Location", "Device", "FailedLogin", "SuccessfulLogin"),
                null,
                "security investigation");

        Console.log("cgo.nexus.security.investigation", actual);

        Assertions.assertTrue(actual.startsWith("security investigation\n\ncontext"));
        Assertions.assertFalse(actual.contains("The result is"));
        this.assertContextContains(actual, "account", "Account:emp-042");
        this.assertContextContains(actual, "location", "germany");
        this.assertContextContains(actual, "device", "macbook pro");
        this.assertContextContains(actual, "failedLoginAttempts", "3");
        this.assertContextContains(actual, "successfulLoginAttempts", "1");
    }

    @Test
    public void test_10() {
        Fact account = this.hydrateSecurity(0, 0);
        String actual = new NexusOrchestrator().execute(
                account,
                List.of("Location", "Device", "FailedLogin", "SuccessfulLogin"),
                null,
                "security investigation");

        Console.log("cgo.nexus.security.routine", actual);

        Assertions.assertTrue(actual.startsWith("security investigation\n\ncontext"));
        Assertions.assertFalse(actual.contains("The result is"));
        this.assertContextContains(actual, "account", "Account:emp-042");
        this.assertContextContains(actual, "failedLoginAttempts", "0");
        this.assertContextContains(actual, "successfulLoginAttempts", "0");
    }

    private void assertContextContains(String template, String key, String value) {
        Assertions.assertTrue(
                template.contains(key + ": " + value),
                key + ": " + value);
    }

    private Fact hydrateInventoryUnavailable() {
        return this.hydrateInventory(0, 0, 25);
    }

    private Fact hydrateInventoryAvailable() {
        return this.hydrateInventory(26, 1, 25);
    }

    private Fact hydrateInventory(int quantityOnHand, int quantityReserved, int quantitySold) {
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
                        + "\"quantityOnHand\":" + quantityOnHand
                        + ",\"quantityReserved\":" + quantityReserved
                        + ",\"product\":\"Product:PROD-SHAMPOO-250\"}");
        Fact sales = this.fact(
                "Sales:SALE-001",
                "{\"id\":\"Sales:SALE-001\",\"kind\":\"Sales\","
                        + "\"mode\":\"atomic\",\"saleId\":\"SALE-001\","
                        + "\"quantitySold\":" + quantitySold
                        + ",\"product\":\"Product:PROD-SHAMPOO-250\"}");
        graphBuilder.addNode(product);
        graphBuilder.addNode(inventory);
        graphBuilder.addNode(sales);
        BindResult inventoryProduct = graphBuilder.bind(this.edgeInput(inventory, product), null);
        BindResult salesProduct = graphBuilder.bind(this.edgeInput(sales, product), null);
        Assertions.assertTrue(inventoryProduct.isOk());
        Assertions.assertTrue(salesProduct.isOk());
        return inventory;
    }

    private Fact hydrateSecurity(int failedLoginAttempts, int successfulLoginAttempts) {
        GraphBuilder graphBuilder = GraphBuilder.getInstance();
        Fact account = this.fact(
                "Account:emp-042",
                "{\"id\":\"Account:emp-042\",\"kind\":\"Account\","
                        + "\"mode\":\"atomic\",\"account\":\"emp-042\"}");
        Fact location = this.fact(
                "Location:germany",
                "{\"id\":\"Location:germany\",\"kind\":\"Location\","
                        + "\"mode\":\"atomic\",\"location\":\"germany\"}");
        Fact device = this.fact(
                "Device:macbook-pro",
                "{\"id\":\"Device:macbook-pro\",\"kind\":\"Device\","
                        + "\"mode\":\"atomic\",\"device\":\"macbook pro\"}");
        Fact failedLogin = this.fact(
                "FailedLogin:FL-001",
                "{\"id\":\"FailedLogin:FL-001\",\"kind\":\"FailedLogin\","
                        + "\"mode\":\"atomic\",\"failedLoginAttempts\":" + failedLoginAttempts
                        + ",\"account\":\"Account:emp-042\"}");
        Fact successfulLogin = this.fact(
                "SuccessfulLogin:SL-001",
                "{\"id\":\"SuccessfulLogin:SL-001\",\"kind\":\"SuccessfulLogin\","
                        + "\"mode\":\"atomic\",\"successfulLoginAttempts\":" + successfulLoginAttempts
                        + ",\"account\":\"Account:emp-042\"}");
        graphBuilder.addNode(account);
        graphBuilder.addNode(location);
        graphBuilder.addNode(device);
        graphBuilder.addNode(failedLogin);
        graphBuilder.addNode(successfulLogin);
        Assertions.assertTrue(graphBuilder.bind(this.edgeInput(account, location), null).isOk());
        Assertions.assertTrue(graphBuilder.bind(this.edgeInput(account, device), null).isOk());
        Assertions.assertTrue(graphBuilder.bind(this.edgeInput(failedLogin, account), null).isOk());
        Assertions.assertTrue(graphBuilder.bind(this.edgeInput(successfulLogin, account), null).isOk());
        return account;
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
