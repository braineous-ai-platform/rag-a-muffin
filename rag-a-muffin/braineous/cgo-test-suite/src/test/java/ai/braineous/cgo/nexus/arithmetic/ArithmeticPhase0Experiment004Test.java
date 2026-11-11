package ai.braineous.cgo.nexus.arithmetic;

import ai.braineous.cgo.nexus.harness.Phase0GraphHarness;
import ai.braineous.cgo.nexus.model.ResolvedGraph;
import ai.braineous.cgo.nexus.orchestrator.GraphResolver;
import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ArithmeticPhase0Experiment004Test {

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

        this.observe(
                "A",
                "0.1 + 0.2",
                snapshot,
                new BigDecimal("0.1").add(new BigDecimal("0.2")));

        BigDecimal factor = new BigDecimal("999999999999999999");
        BigDecimal exactB = factor.multiply(factor);
        this.observe(
                "B",
                "999999999999999999 *\n   999999999999999999",
                snapshot,
                exactB);

        this.observe(
                "C",
                "1 / 7",
                snapshot,
                BigDecimal.ONE.divide(new BigDecimal("7"), MathContext.DECIMAL128));

        this.observe(
                "D",
                "-5 * (3 + 2)",
                snapshot,
                new BigDecimal("-5").multiply(
                        new BigDecimal("3").add(new BigDecimal("2"))));

        this.observe(
                "E",
                "Inventory.quantityOnHand / 0.0001",
                snapshot,
                onHand.divide(new BigDecimal("0.0001"), MathContext.DECIMAL128));

        this.observe(
                "F",
                "Sales.quantitySold /\n   Inventory.quantityOnHand",
                snapshot,
                sold.divide(onHand, MathContext.DECIMAL128));

        String expressionG =
                "(Inventory.quantityOnHand -\n"
                        + "    Inventory.quantityReserved) /\n"
                        + "    Sales.quantitySold";
        this.observe(
                "G",
                expressionG,
                snapshot,
                onHand.subtract(reserved).divide(sold, MathContext.DECIMAL128));

        this.observeRepeatH(expressionG, snapshot);
    }

    private void observe(
            String label,
            String expression,
            GraphSnapshot snapshot,
            BigDecimal reference) {
        System.out.print("=== EXPERIMENT 004 ");
        System.out.print(label);
        System.out.print(" BEGIN ===\n");
        System.out.print("=== ORIGINAL EXPRESSION ===\n");
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

        System.out.print("=== PARSED EXPRESSION TREE ===\n");
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

        System.out.print("=== BOUND FACT FIELDS ===\n");
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

        System.out.print("=== ACTUAL BIGDECIMAL RESULT ===\n");
        if (result == null) {
            System.out.print("unavailable\n");
        } else {
            System.out.print(result.toPlainString());
            System.out.print("\n");
        }

        System.out.print("=== PRECISION AND SCALE ===\n");
        if (result == null) {
            System.out.print("unavailable\n");
        } else {
            System.out.print("precision=");
            System.out.print(String.valueOf(result.precision()));
            System.out.print("\n");
            System.out.print("scale=");
            System.out.print(String.valueOf(result.scale()));
            System.out.print("\n");
        }

        System.out.print("=== INDEPENDENT REFERENCE ===\n");
        if (reference == null) {
            System.out.print("unavailable\n");
        } else {
            System.out.print(reference.toPlainString());
            System.out.print("\n");
            System.out.print("reference precision=");
            System.out.print(String.valueOf(reference.precision()));
            System.out.print("\n");
            System.out.print("reference scale=");
            System.out.print(String.valueOf(reference.scale()));
            System.out.print("\n");
        }

        if ("B".equals(label) && result != null && reference != null) {
            System.out.print("=== B EXACT UNLIMITED REFERENCE ===\n");
            System.out.print(reference.toPlainString());
            System.out.print("\n");
            System.out.print("=== B DECIMAL128 EVALUATOR RESULT ===\n");
            System.out.print(result.toPlainString());
            System.out.print("\n");
            boolean loss = reference.compareTo(result) != 0;
            System.out.print("=== B PRECISION LOSS ===\n");
            System.out.print(String.valueOf(loss));
            System.out.print("\n");
            if (loss) {
                BigDecimal delta = reference.subtract(result).abs();
                System.out.print("absolute difference=");
                System.out.print(delta.toPlainString());
                System.out.print("\n");
            }
        }

        System.out.print("=== NUMERICALLY EQUAL TO REFERENCE ===\n");
        if (result == null || reference == null) {
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

        System.out.print("=== EXPERIMENT 004 ");
        System.out.print(label);
        System.out.print(" END ===\n");
    }

    private void observeRepeatH(String expression, GraphSnapshot snapshot) {
        System.out.print("=== EXPERIMENT 004 H BEGIN ===\n");
        System.out.print("=== ORIGINAL EXPRESSION ===\n");
        System.out.print(expression);
        System.out.print("\n");

        int completed = 0;
        int failures = 0;
        LinkedHashSet<String> distinct = new LinkedHashSet<String>();
        BigDecimal first = null;
        BigDecimal last = null;
        Exception firstException = null;
        long startNanos = System.nanoTime();

        int index = 0;
        while (index < 1000) {
            try {
                List<ArithmeticExpression.Token> tokens =
                        ArithmeticExpression.tokenize(expression);
                ArithmeticExpression.Node parsed = ArithmeticExpression.parse(tokens);
                Map<String, String> bindings = new LinkedHashMap<String, String>();
                List<String> operations = new ArrayList<String>();
                BigDecimal value = parsed.evaluate(snapshot, operations, bindings);
                if (first == null) {
                    first = value;
                }
                last = value;
                distinct.add(value.toPlainString());
                completed = completed + 1;
            } catch (Exception thrown) {
                failures = failures + 1;
                if (firstException == null) {
                    firstException = thrown;
                }
            }
            index = index + 1;
        }

        long elapsedNanos = System.nanoTime() - startNanos;

        System.out.print("=== NUMBER OF COMPLETED EVALUATIONS ===\n");
        System.out.print(String.valueOf(completed));
        System.out.print("\n");
        System.out.print("=== DISTINCT NUMERIC RESULTS ===\n");
        System.out.print(String.valueOf(distinct.size()));
        System.out.print("\n");
        for (String value : distinct) {
            System.out.print(value);
            System.out.print("\n");
        }
        System.out.print("=== FIRST RESULT ===\n");
        if (first == null) {
            System.out.print("unavailable\n");
        } else {
            System.out.print(first.toPlainString());
            System.out.print("\n");
        }
        System.out.print("=== LAST RESULT ===\n");
        if (last == null) {
            System.out.print("unavailable\n");
        } else {
            System.out.print(last.toPlainString());
            System.out.print("\n");
        }
        System.out.print("=== ANY EXCEPTIONS ===\n");
        System.out.print("failure count=");
        System.out.print(String.valueOf(failures));
        System.out.print("\n");
        if (firstException == null) {
            System.out.print("none\n");
        } else {
            System.out.print(firstException.getClass().getName());
            System.out.print("\n");
            System.out.print(String.valueOf(firstException.getMessage()));
            System.out.print("\n");
        }
        System.out.print("=== TOTAL EVALUATION WALL-CLOCK NANOS ===\n");
        System.out.print(String.valueOf(elapsedNanos));
        System.out.print("\n");
        System.out.print("=== TOTAL EVALUATION WALL-CLOCK MILLIS ===\n");
        System.out.print(String.valueOf(elapsedNanos / 1000000L));
        System.out.print("\n");
        System.out.print("=== EXPERIMENT 004 H END ===\n");
    }

    private BigDecimal field(GraphSnapshot snapshot, String factId, String fieldName) {
        Fact fact = snapshot.nodes().get(factId);
        JsonObject json = JsonParser.parseString(fact.getText()).getAsJsonObject();
        return json.get(fieldName).getAsBigDecimal();
    }
}
