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

public class ArithmeticPhase0Experiment007Test {

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

        MathContext decimal = ArithmeticExpression.DECIMAL;
        BigDecimal onHand = this.field(snapshot, "Inventory:INV-001", "quantityOnHand");
        BigDecimal reserved = this.field(snapshot, "Inventory:INV-001", "quantityReserved");
        BigDecimal sold = this.field(snapshot, "Sales:SALE-001", "quantitySold");

        this.observe("A", "(((2 + 3)))", snapshot, new BigDecimal("5"));
        this.observe(
                "B",
                "2 * (3 + (4 * (5 - 1)))",
                snapshot,
                new BigDecimal("2").multiply(
                        new BigDecimal("3").add(
                                new BigDecimal("4").multiply(
                                        new BigDecimal("5").subtract(new BigDecimal("1"), decimal),
                                        decimal),
                                decimal),
                        decimal));
        this.observe(
                "C",
                "-(-(-5))",
                snapshot,
                new BigDecimal("5").negate(decimal).negate(decimal).negate(decimal));
        this.observe(
                "D",
                ".5 + .25",
                snapshot,
                new BigDecimal("0.5").add(new BigDecimal("0.25")));
        this.observe(
                "E",
                "5. + 2.",
                snapshot,
                new BigDecimal("5").add(new BigDecimal("2")));
        this.observe(
                "F",
                "0005 + 0002",
                snapshot,
                new BigDecimal("5").add(new BigDecimal("2")));
        this.observe(
                "G",
                "1 + 2 * 3 - 4 / 2",
                snapshot,
                new BigDecimal("1").add(
                        new BigDecimal("2").multiply(new BigDecimal("3"), decimal),
                        decimal)
                        .subtract(
                                new BigDecimal("4").divide(new BigDecimal("2"), decimal),
                                decimal));
        this.observe(
                "H",
                "( Inventory.quantityOnHand\n"
                        + "      - Inventory.quantityReserved )\n"
                        + "      / Sales.quantitySold",
                snapshot,
                onHand.subtract(reserved, decimal).divide(sold, decimal));

        this.observe("I", "1..2", snapshot, null);
        this.observe("J", "1.2.3", snapshot, null);
        this.observe("K", "(2 + 3", snapshot, null);
        this.observe("L", "2 + 3)", snapshot, null);
        this.observe("M", "2 ** 3", snapshot, null);
        this.observe("N", "2 3", snapshot, null);
        this.observe("O", "Inventory.", snapshot, null);
        this.observe("P", "", snapshot, null);
        this.observe("Q", "   \n\t  ", snapshot, null);
        this.observe("R", "1e3", snapshot, null);
        this.observe("S", "1 / / 2", snapshot, null);
    }

    private void observe(
            String label,
            String expression,
            GraphSnapshot snapshot,
            BigDecimal reference) {
        System.out.print("=== EXPERIMENT 007 ");
        System.out.print(label);
        System.out.print(" BEGIN ===\n");
        System.out.print("=== ORIGINAL EXPRESSION ===\n");
        if (expression.length() == 0) {
            System.out.print("<empty>\n");
        } else {
            System.out.print(expression);
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
        } else if (tokens.isEmpty()) {
            System.out.print("<none>\n");
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

        System.out.print("=== AST ===\n");
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

        System.out.print("=== NUMERIC RESULT ===\n");
        if (result == null) {
            System.out.print("unavailable\n");
        } else {
            System.out.print(result.toPlainString());
            System.out.print("\n");
        }

        if (reference != null) {
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

        System.out.print("=== EXPERIMENT 007 ");
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
