package ai.braineous.cgo.nexus.arithmetic;

import ai.braineous.cgo.nexus.harness.Phase0GraphHarness;
import ai.braineous.cgo.nexus.model.ResolvedGraph;
import ai.braineous.cgo.nexus.orchestrator.GraphResolver;
import ai.braineous.rag.prompt.cgo.api.Edge;
import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

public class ArithmeticPhase0Experiment006Test {

    private static final String EXPRESSION =
            "(Inventory.quantityOnHand -\n"
                    + " Inventory.quantityReserved) /\n"
                    + " Sales.quantitySold";

    @Test
    public void test_1() {
        Phase0GraphHarness.EstablishedGraph established =
                new Phase0GraphHarness().establish(10);
        Fact axis = established.getAxis();
        assertNotNull(axis);
        assertEquals("Inventory:INV-001", axis.getId());

        GraphSnapshot fullSnapshot = established.getSnapshot();
        GraphResolver resolver = new GraphResolver();
        ResolvedGraph resolved = resolver.resolve(axis, List.of("Sales"));
        GraphSnapshot original = resolved.getSnapshot();
        assertNotNull(original);

        String originalSalesText = original.nodes().get("Sales:SALE-001").getText();

        this.observe("A", this.localSalesQuantity(original, new JsonPrimitive(0)));
        this.observe("B", this.localSalesQuantity(original, new JsonPrimitive(-25)));
        this.observe("C", this.localSalesQuantity(original, new JsonPrimitive("25")));
        this.observe("D", this.localSalesQuantity(original, JsonNull.INSTANCE));
        this.observe("E", this.localSalesWithoutQuantity(original));
        this.observe("F", this.localSalesQuantity(original, new JsonPrimitive(true)));
        this.observe("G", this.localWithSecondSales(original, fullSnapshot));
        this.observe("H", original);

        assertEquals(originalSalesText, original.nodes().get("Sales:SALE-001").getText());
    }

    private GraphSnapshot localSalesQuantity(GraphSnapshot original, JsonElement quantitySold) {
        Fact originalSales = original.nodes().get("Sales:SALE-001");
        JsonObject json = JsonParser.parseString(originalSales.getText()).getAsJsonObject();
        json.add("quantitySold", quantitySold);
        Fact copiedSales = this.copyFact(originalSales, json.toString());
        assertNotSame(originalSales, copiedSales);
        return this.localSnapshot(original, copiedSales);
    }

    private GraphSnapshot localSalesWithoutQuantity(GraphSnapshot original) {
        Fact originalSales = original.nodes().get("Sales:SALE-001");
        JsonObject json = JsonParser.parseString(originalSales.getText()).getAsJsonObject();
        json.remove("quantitySold");
        Fact copiedSales = this.copyFact(originalSales, json.toString());
        assertNotSame(originalSales, copiedSales);
        return this.localSnapshot(original, copiedSales);
    }

    private GraphSnapshot localWithSecondSales(
            GraphSnapshot original,
            GraphSnapshot fullSnapshot) {
        Fact sale002Original = fullSnapshot.nodes().get("Sales:SALE-002");
        assertNotNull(sale002Original);
        Fact sale002Copy = this.copyFact(sale002Original, sale002Original.getText());
        assertNotSame(sale002Original, sale002Copy);

        LinkedHashMap<String, Fact> nodes = this.copyNodes(original);
        nodes.put(sale002Copy.getId(), sale002Copy);
        LinkedHashMap<String, Edge> edges = this.copyEdges(original);
        return new GraphSnapshot(nodes, edges);
    }

    private GraphSnapshot localSnapshot(GraphSnapshot original, Fact copiedSales) {
        LinkedHashMap<String, Fact> nodes = this.copyNodes(original);
        nodes.put(copiedSales.getId(), copiedSales);
        LinkedHashMap<String, Edge> edges = this.copyEdges(original);
        return new GraphSnapshot(nodes, edges);
    }

    private LinkedHashMap<String, Fact> copyNodes(GraphSnapshot snapshot) {
        LinkedHashMap<String, Fact> nodes = new LinkedHashMap<String, Fact>();
        for (Map.Entry<String, Fact> entry : snapshot.nodes().entrySet()) {
            nodes.put(entry.getKey(), entry.getValue());
        }
        return nodes;
    }

    private LinkedHashMap<String, Edge> copyEdges(GraphSnapshot snapshot) {
        LinkedHashMap<String, Edge> edges = new LinkedHashMap<String, Edge>();
        for (Map.Entry<String, Edge> entry : snapshot.edges().entrySet()) {
            edges.put(entry.getKey(), entry.getValue());
        }
        return edges;
    }

    private Fact copyFact(Fact original, String text) {
        Fact copy = new Fact(original.getId(), text);
        copy.setMode(original.getMode());
        return copy;
    }

    private void observe(String label, GraphSnapshot snapshot) {
        System.out.print("=== EXPERIMENT 006 ");
        System.out.print(label);
        System.out.print(" BEGIN ===\n");
        System.out.print("=== ORIGINAL EXPRESSION ===\n");
        System.out.print(EXPRESSION);
        System.out.print("\n");

        System.out.print("=== TEST-LOCAL FACT ID AND COMPLETE JSON ===\n");
        TreeSet<String> salesIds = new TreeSet<String>();
        for (Map.Entry<String, Fact> entry : snapshot.nodes().entrySet()) {
            Fact fact = entry.getValue();
            if (fact != null && fact.getId() != null && fact.getId().startsWith("Sales:")) {
                salesIds.add(fact.getId());
            }
        }
        for (String salesId : salesIds) {
            Fact fact = snapshot.nodes().get(salesId);
            System.out.print(salesId);
            System.out.print("\n");
            System.out.print(fact.getText());
            System.out.print("\n");
        }

        List<ArithmeticExpression.Token> tokens = null;
        ArithmeticExpression.Node parsed = null;
        Map<String, String> bindings = new LinkedHashMap<String, String>();
        List<String> operations = new ArrayList<String>();
        BigDecimal result = null;
        Exception exception = null;
        boolean returnedValue = false;

        try {
            tokens = ArithmeticExpression.tokenize(EXPRESSION);
            parsed = ArithmeticExpression.parse(tokens);
            result = parsed.evaluate(snapshot, operations, bindings);
            returnedValue = true;
        } catch (Exception thrown) {
            exception = thrown;
        }

        System.out.print("=== PARSED EXPRESSION TREE ===\n");
        if (parsed == null) {
            System.out.print("unavailable\n");
        } else {
            System.out.print(parsed.describe());
            System.out.print("\n");
        }

        System.out.print("=== FIELD BINDING OBSERVATIONS ===\n");
        if (bindings.isEmpty()) {
            System.out.print("none\n");
        } else {
            for (Map.Entry<String, String> binding : bindings.entrySet()) {
                System.out.print(binding.getKey());
                System.out.print("\n");
                System.out.print(binding.getValue());
                System.out.print("\n");
            }
        }

        System.out.print("=== ACTUAL NUMERICAL RESULT ===\n");
        if (result == null) {
            System.out.print("unavailable\n");
        } else {
            System.out.print(result.toPlainString());
            System.out.print("\n");
        }

        System.out.print("=== EXCEPTION CLASS ===\n");
        if (exception == null) {
            System.out.print("none\n");
        } else {
            System.out.print(exception.getClass().getName());
            System.out.print("\n");
        }

        System.out.print("=== EXCEPTION MESSAGE ===\n");
        if (exception == null) {
            System.out.print("none\n");
        } else {
            System.out.print(String.valueOf(exception.getMessage()));
            System.out.print("\n");
        }

        System.out.print("=== EVALUATION RETURNED A VALUE ===\n");
        System.out.print(String.valueOf(returnedValue));
        System.out.print("\n");

        System.out.print("=== EXPERIMENT 006 ");
        System.out.print(label);
        System.out.print(" END ===\n");
    }
}
