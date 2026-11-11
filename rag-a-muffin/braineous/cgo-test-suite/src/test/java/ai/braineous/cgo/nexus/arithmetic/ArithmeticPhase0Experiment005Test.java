package ai.braineous.cgo.nexus.arithmetic;

import ai.braineous.cgo.nexus.harness.Phase0GraphHarness;
import ai.braineous.cgo.nexus.model.ResolvedGraph;
import ai.braineous.cgo.nexus.orchestrator.GraphResolver;
import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ArithmeticPhase0Experiment005Test {

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

        BigDecimal onHand = this.field(snapshot, "Inventory:INV-001", "quantityOnHand");
        BigDecimal reserved = this.field(snapshot, "Inventory:INV-001", "quantityReserved");
        BigDecimal sold = this.field(snapshot, "Sales:SALE-001", "quantitySold");
        MathContext decimal = ArithmeticExpression.DECIMAL;

        this.observe(
                "A",
                "-5 * (3 + 2)",
                snapshot,
                new BigDecimal("-5").multiply(
                        new BigDecimal("3").add(new BigDecimal("2")),
                        decimal));
        this.observe(
                "B",
                "+5 * (3 + 2)",
                snapshot,
                new BigDecimal("5").multiply(
                        new BigDecimal("3").add(new BigDecimal("2")),
                        decimal));
        this.observe(
                "C",
                "--5",
                snapshot,
                new BigDecimal("5").negate(decimal).negate(decimal));
        this.observe(
                "D",
                "-(-5)",
                snapshot,
                new BigDecimal("5").negate(decimal).negate(decimal));
        this.observe(
                "E",
                "-(Inventory.quantityOnHand -\n     Inventory.quantityReserved)",
                snapshot,
                onHand.subtract(reserved, decimal).negate(decimal));
        this.observe(
                "F",
                "Inventory.quantityOnHand * -2",
                snapshot,
                onHand.multiply(new BigDecimal("2").negate(decimal), decimal));
        this.observe(
                "G",
                "10 - -5",
                snapshot,
                new BigDecimal("10").subtract(new BigDecimal("5").negate(decimal), decimal));
        this.observe(
                "H",
                "-(2 + 3) * -4",
                snapshot,
                new BigDecimal("2").add(new BigDecimal("3"), decimal)
                        .negate(decimal)
                        .multiply(new BigDecimal("4").negate(decimal), decimal));
        this.observe(
                "I",
                "Inventory.quantityOnHand / -0.0001",
                snapshot,
                onHand.divide(new BigDecimal("0.0001").negate(decimal), decimal));
        this.observe(
                "J",
                "-Sales.quantitySold",
                snapshot,
                sold.negate(decimal));
    }

    private void observe(
            String label,
            String expression,
            GraphSnapshot snapshot,
            BigDecimal reference) {
        System.out.print("=== EXPERIMENT 005 ");
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

        System.out.print("=== PARSED AST ===\n");
        if (parsed == null) {
            System.out.print("unavailable\n");
        } else {
            System.out.print(parsed.describe());
            System.out.print("\n");
        }

        if (exception == null && parsed != null) {
            try {
                result = parsed.evaluate(snapshot, operations, bindings);
            } catch (Exception thrown) {
                exception = thrown;
            }
        }

        System.out.print("=== BOUND FACT VALUES ===\n");
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

        System.out.print("=== EVALUATION OPERATIONS ===\n");
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

        System.out.print("=== NUMERIC RESULT ===\n");
        if (result == null) {
            System.out.print("unavailable\n");
        } else {
            System.out.print(result.toPlainString());
            System.out.print("\n");
        }

        System.out.print("=== INDEPENDENT REFERENCE ===\n");
        System.out.print(reference.toPlainString());
        System.out.print("\n");

        System.out.print("=== NUMERICALLY EQUAL TO REFERENCE ===\n");
        if (result == null) {
            System.out.print("unavailable\n");
        } else {
            System.out.print(String.valueOf(result.compareTo(reference) == 0));
            System.out.print("\n");
        }

        System.out.print("=== EXCEPTION ===\n");
        if (exception == null) {
            System.out.print("none\n");
        } else {
            System.out.print(exception.getClass().getName());
            System.out.print("\n");
            System.out.print(String.valueOf(exception.getMessage()));
            System.out.print("\n");
        }

        System.out.print("=== EXPERIMENT 005 ");
        System.out.print(label);
        System.out.print(" END ===\n");
    }

    private BigDecimal field(GraphSnapshot snapshot, String factId, String fieldName) {
        Fact fact = snapshot.nodes().get(factId);
        return JsonParser.parseString(fact.getText()).getAsJsonObject()
                .get(fieldName)
                .getAsBigDecimal();
    }
}
