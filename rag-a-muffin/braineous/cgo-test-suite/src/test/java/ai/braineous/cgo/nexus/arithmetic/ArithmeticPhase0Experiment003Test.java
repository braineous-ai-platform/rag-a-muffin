package ai.braineous.cgo.nexus.arithmetic;

import ai.braineous.cgo.nexus.harness.Phase0GraphHarness;
import ai.braineous.cgo.nexus.model.ResolvedGraph;
import ai.braineous.cgo.nexus.orchestrator.GraphResolver;
import ai.braineous.rag.prompt.cgo.api.Edge;
import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ArithmeticPhase0Experiment003Test {

    @Test
    public void test_1() {
        Phase0GraphHarness.EstablishedGraph established =
                new Phase0GraphHarness().establish(10);
        Fact axis = established.getAxis();
        assertNotNull(axis);
        assertEquals("Inventory:INV-001", axis.getId());

        GraphSnapshot fullSnapshot = established.getSnapshot();
        Fact sale002 = fullSnapshot.nodes().get("Sales:SALE-002");
        assertNotNull(sale002);

        GraphResolver resolver = new GraphResolver();
        ResolvedGraph resolved = resolver.resolve(
                axis,
                List.of("Sales", "Vendor", "Location"));
        GraphSnapshot snapshot = resolved.getSnapshot();
        assertNotNull(snapshot);

        LinkedHashMap<String, Fact> localNodes = new LinkedHashMap<String, Fact>();
        for (Map.Entry<String, Fact> entry : snapshot.nodes().entrySet()) {
            localNodes.put(entry.getKey(), entry.getValue());
        }
        localNodes.put(sale002.getId(), sale002);

        LinkedHashMap<String, Edge> localEdges = new LinkedHashMap<String, Edge>();
        for (Map.Entry<String, Edge> entry : snapshot.edges().entrySet()) {
            localEdges.put(entry.getKey(), entry.getValue());
        }

        GraphSnapshot ambiguousSnapshot = new GraphSnapshot(localNodes, localEdges);
        ResolvedGraph ambiguous = new ResolvedGraph(axis, ambiguousSnapshot);
        assertTrue(ambiguous.getSnapshot().nodes().containsKey("Sales:SALE-001"));
        assertTrue(ambiguous.getSnapshot().nodes().containsKey("Sales:SALE-002"));

        this.observe("A", "Inventory.unknownField", snapshot);
        this.observe("B", "Customer.balance", snapshot);
        this.observe("C", "Sales.quantitySold", ambiguous.getSnapshot());
        this.observe("D", "Vendor.name", snapshot);
        this.observe("E", "Inventory.quantityOnHand +", snapshot);
        this.observe("F", "Inventory.quantityOnHand ^ 2", snapshot);
    }

    private void observe(String label, String expression, GraphSnapshot snapshot) {
        System.out.print("=== EXPERIMENT 003 ");
        System.out.print(label);
        System.out.print(" BEGIN ===\n");
        System.out.print("=== ORIGINAL EXPRESSION ===\n");
        System.out.print(expression);
        System.out.print("\n");

        System.out.print("=== RESOLVED FACT IDS ===\n");
        TreeSet<String> factIds = new TreeSet<String>();
        if (snapshot != null && snapshot.nodes() != null) {
            for (Map.Entry<String, Fact> entry : snapshot.nodes().entrySet()) {
                Fact fact = entry.getValue();
                if (fact != null && fact.getId() != null) {
                    factIds.add(fact.getId());
                }
            }
        }
        for (String factId : factIds) {
            System.out.print(factId);
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
            tokens = ArithmeticExpression.tokenize(expression);
        } catch (Exception thrown) {
            exception = thrown;
        }

        System.out.print("=== TOKENS ===\n");
        if (tokens == null) {
            System.out.print("unavailable\n");
        } else {
            int tokenIndex = 0;
            while (tokenIndex < tokens.size()) {
                System.out.print(tokens.get(tokenIndex).toString());
                System.out.print("\n");
                tokenIndex = tokenIndex + 1;
            }
        }

        if (exception == null) {
            try {
                parsed = ArithmeticExpression.parse(tokens);
            } catch (Exception thrown) {
                exception = thrown;
            }
        }

        System.out.print("=== PARSED TREE ===\n");
        if (parsed == null) {
            System.out.print("unavailable\n");
        } else {
            System.out.print(parsed.describe());
            System.out.print("\n");
        }

        if (exception == null && parsed != null) {
            try {
                result = parsed.evaluate(snapshot, operations, bindings);
                returnedValue = true;
            } catch (Exception thrown) {
                exception = thrown;
            }
        }

        System.out.print("=== FIELD BINDINGS ===\n");
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
        if (returnedValue && result != null) {
            System.out.print("=== RESULT ===\n");
            System.out.print(result.toPlainString());
            System.out.print("\n");
        }

        System.out.print("=== EXPERIMENT 003 ");
        System.out.print(label);
        System.out.print(" END ===\n");
    }
}
