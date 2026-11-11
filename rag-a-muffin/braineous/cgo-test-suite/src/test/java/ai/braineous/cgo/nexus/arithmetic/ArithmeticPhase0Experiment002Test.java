package ai.braineous.cgo.nexus.arithmetic;

import ai.braineous.cgo.nexus.harness.Phase0GraphHarness;
import ai.braineous.cgo.nexus.model.ResolvedGraph;
import ai.braineous.cgo.nexus.orchestrator.GraphResolver;
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

public class ArithmeticPhase0Experiment002Test {

    @Test
    public void test_1() {
        Phase0GraphHarness.EstablishedGraph established =
                new Phase0GraphHarness().establish(10);
        Fact axis = established.getAxis();
        assertNotNull(axis);
        assertEquals("Inventory:INV-001", axis.getId());

        GraphResolver resolver = new GraphResolver();
        ResolvedGraph resolved = resolver.resolve(axis, List.of("Sales"));
        GraphSnapshot snapshot = resolved.getSnapshot();
        assertNotNull(snapshot);

        System.out.print("=== RESOLVED FACT IDS ===\n");
        TreeSet<String> factIds = new TreeSet<String>();
        for (Map.Entry<String, Fact> entry : snapshot.nodes().entrySet()) {
            Fact fact = entry.getValue();
            if (fact != null && fact.getId() != null) {
                factIds.add(fact.getId());
            }
        }
        for (String factId : factIds) {
            System.out.print(factId);
            System.out.print("\n");
        }

        this.evaluate("A", "2 + 3 * 4", snapshot);
        this.evaluate("B", "(2 + 3) * 4", snapshot);
        this.evaluate("C", "Inventory.quantityOnHand - Sales.quantitySold * 2", snapshot);
        this.evaluate(
                "D",
                "(Inventory.quantityReserved - Inventory.quantityOnHand) / Sales.quantitySold",
                snapshot);
        this.evaluate("E", "1 / 3", snapshot);
        this.evaluate(
                "F",
                "Inventory.quantityOnHand / (Sales.quantitySold - 25)",
                snapshot);
    }

    private void evaluate(String label, String expression, GraphSnapshot snapshot) {
        System.out.print("=== EXPERIMENT 002 ");
        System.out.print(label);
        System.out.print(" BEGIN ===\n");
        System.out.print("=== ORIGINAL STRING ===\n");
        System.out.print(expression);
        System.out.print("\n");

        List<ArithmeticExpression.Token> tokens = null;
        ArithmeticExpression.Node parsed = null;
        Map<String, String> bindings = new LinkedHashMap<String, String>();
        List<String> operations = new ArrayList<String>();
        BigDecimal result = null;
        Exception exception = null;

        try {
            tokens = ArithmeticExpression.tokenize(expression);
            System.out.print("=== TOKENS ===\n");
            int tokenIndex = 0;
            while (tokenIndex < tokens.size()) {
                System.out.print(tokens.get(tokenIndex).toString());
                System.out.print("\n");
                tokenIndex = tokenIndex + 1;
            }

            parsed = ArithmeticExpression.parse(tokens);
            System.out.print("=== PARSED EXPRESSION TREE ===\n");
            System.out.print(parsed.describe());
            System.out.print("\n");

            result = parsed.evaluate(snapshot, operations, bindings);

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

            System.out.print("=== ARITHMETIC OPERATIONS ===\n");
            if (operations.isEmpty()) {
                System.out.print("none\n");
            } else {
                int opIndex = 0;
                while (opIndex < operations.size()) {
                    System.out.print(operations.get(opIndex));
                    System.out.print("\n");
                    opIndex = opIndex + 1;
                }
            }

            System.out.print("=== RESULT ===\n");
            System.out.print(result.toPlainString());
            System.out.print("\n");
        } catch (Exception thrown) {
            exception = thrown;
            System.out.print("=== EXCEPTION ===\n");
            System.out.print(thrown.getClass().getName());
            System.out.print("\n");
            System.out.print(String.valueOf(thrown.getMessage()));
            System.out.print("\n");
        }

        if (exception == null) {
            System.out.print("=== EXCEPTION ===\n");
            System.out.print("none\n");
        }

        System.out.print("=== EXPERIMENT 002 ");
        System.out.print(label);
        System.out.print(" END ===\n");
    }
}
