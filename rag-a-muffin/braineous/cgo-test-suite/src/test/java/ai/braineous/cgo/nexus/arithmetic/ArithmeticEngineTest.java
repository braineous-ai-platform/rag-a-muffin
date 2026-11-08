package ai.braineous.cgo.nexus.arithmetic;

import ai.braineous.cgo.nexus.harness.Phase0GraphHarness;
import ai.braineous.cgo.nexus.model.ResolvedGraph;
import ai.braineous.cgo.nexus.orchestrator.GraphResolver;
import ai.braineous.rag.prompt.cgo.api.Edge;
import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ArithmeticEngineTest {

    @Test
    public void test_1() {
        ResolvedGraph resolved = this.resolveSales();
        ArithmeticEngine engine = new ArithmeticEngine();

        assertEquals(
                0,
                new BigDecimal("5").compareTo(
                        engine.evaluate("1 + 2 * 3 - 4 / 2", resolved)));
        assertEquals(
                0,
                new BigDecimal("14").compareTo(
                        engine.evaluate("2 + 3 * 4", resolved)));
    }

    @Test
    public void test_2() {
        ResolvedGraph resolved = this.resolveSales();
        ArithmeticEngine engine = new ArithmeticEngine();

        assertEquals(
                0,
                new BigDecimal("20").compareTo(
                        engine.evaluate("(2 + 3) * 4", resolved)));
        assertEquals(
                0,
                new BigDecimal("38").compareTo(
                        engine.evaluate("2 * (3 + (4 * (5 - 1)))", resolved)));
        assertEquals(
                0,
                new BigDecimal("5").compareTo(
                        engine.evaluate("(((2 + 3)))", resolved)));
    }

    @Test
    public void test_3() {
        ResolvedGraph resolved = this.resolveSales();
        ArithmeticEngine engine = new ArithmeticEngine();

        assertEquals(
                0,
                new BigDecimal("-25").compareTo(
                        engine.evaluate("-5 * (3 + 2)", resolved)));
        assertEquals(
                0,
                new BigDecimal("25").compareTo(
                        engine.evaluate("+5 * (3 + 2)", resolved)));
        assertEquals(
                0,
                new BigDecimal("5").compareTo(
                        engine.evaluate("--5", resolved)));
        assertEquals(
                0,
                new BigDecimal("-5").compareTo(
                        engine.evaluate("-(-(-5))", resolved)));
        assertEquals(
                0,
                new BigDecimal("15").compareTo(
                        engine.evaluate("10 - -5", resolved)));
        assertEquals(
                0,
                new BigDecimal("20").compareTo(
                        engine.evaluate("-(2 + 3) * -4", resolved)));
    }

    @Test
    public void test_4() {
        ResolvedGraph resolved = this.resolveSales();
        ArithmeticEngine engine = new ArithmeticEngine();

        assertEquals(
                0,
                new BigDecimal("0.3").compareTo(
                        engine.evaluate("0.1 + 0.2", resolved)));
        assertEquals(
                0,
                new BigDecimal("7").compareTo(
                        engine.evaluate("0005 + 0002", resolved)));
        assertEquals(
                0,
                new BigDecimal("7").compareTo(
                        engine.evaluate("5. + 2.", resolved)));
    }

    @Test
    public void test_5() {
        ResolvedGraph resolved = this.resolveSales();
        ArithmeticEngine engine = new ArithmeticEngine();

        BigDecimal result = engine.evaluate(
                "(Inventory.quantityOnHand -\n"
                        + "    Inventory.quantityReserved) /\n"
                        + "    Sales.quantitySold",
                resolved);
        assertEquals(0, new BigDecimal("20").compareTo(result));
        assertEquals(
                0,
                new BigDecimal("-25").compareTo(
                        engine.evaluate("-Sales.quantitySold", resolved)));
        assertEquals(
                0,
                new BigDecimal("-500").compareTo(
                        engine.evaluate(
                                "-(Inventory.quantityOnHand - Inventory.quantityReserved)",
                                resolved)));
    }

    @Test
    public void test_6() {
        ResolvedGraph resolved = this.resolveSales();
        ArithmeticEngine engine = new ArithmeticEngine();

        IllegalStateException thrown = assertThrows(
                IllegalStateException.class,
                () -> engine.evaluate("Inventory.unknownField", resolved));
        assertEquals(
                "field Inventory.unknownField missing on Inventory:INV-001",
                thrown.getMessage());

        ResolvedGraph missingSold = this.localSalesWithoutQuantity(resolved);
        IllegalStateException missing = assertThrows(
                IllegalStateException.class,
                () -> engine.evaluate("Sales.quantitySold", missingSold));
        assertEquals(
                "field Sales.quantitySold missing on Sales:SALE-001",
                missing.getMessage());
    }

    @Test
    public void test_7() {
        Phase0GraphHarness.EstablishedGraph established =
                new Phase0GraphHarness().establish(10);
        ResolvedGraph resolved = this.resolveSales(established);
        Fact sale002 = established.getSnapshot().nodes().get("Sales:SALE-002");
        assertNotNull(sale002);

        Fact sale002Copy = new Fact(sale002.getId(), sale002.getText());
        sale002Copy.setMode(sale002.getMode());

        LinkedHashMap<String, Fact> nodes = new LinkedHashMap<String, Fact>();
        for (Map.Entry<String, Fact> entry : resolved.getSnapshot().nodes().entrySet()) {
            nodes.put(entry.getKey(), entry.getValue());
        }
        nodes.put(sale002Copy.getId(), sale002Copy);

        LinkedHashMap<String, Edge> edges = new LinkedHashMap<String, Edge>();
        for (Map.Entry<String, Edge> entry : resolved.getSnapshot().edges().entrySet()) {
            edges.put(entry.getKey(), entry.getValue());
        }

        ResolvedGraph ambiguous = new ResolvedGraph(
                resolved.getAxis(),
                new GraphSnapshot(nodes, edges));
        ArithmeticEngine engine = new ArithmeticEngine();
        IllegalStateException thrown = assertThrows(
                IllegalStateException.class,
                () -> engine.evaluate("Sales.quantitySold", ambiguous));
        assertEquals("multiple Facts for kind Sales", thrown.getMessage());
    }

    @Test
    public void test_8() {
        ResolvedGraph resolved = this.resolveSales();
        ArithmeticEngine engine = new ArithmeticEngine();

        ArithmeticException thrown = assertThrows(
                ArithmeticException.class,
                () -> engine.evaluate("1 / 0", resolved));
        assertEquals("Division by zero", thrown.getMessage());
    }

    @Test
    public void test_9() {
        ResolvedGraph resolved = this.resolveSales();
        ArithmeticEngine engine = new ArithmeticEngine();

        BigDecimal result = engine.evaluate(
                "999999999999999999 * 999999999999999999",
                resolved);
        BigDecimal exact = new BigDecimal("999999999999999999")
                .multiply(new BigDecimal("999999999999999999"));
        assertEquals(
                0,
                new BigDecimal("999999999999999998000000000000000000")
                        .compareTo(result));
        assertTrue(exact.compareTo(result) != 0);
        assertEquals(0, exact.subtract(result).abs().compareTo(BigDecimal.ONE));

        BigDecimal oneSeventh = engine.evaluate("1 / 7", resolved);
        assertEquals(
                0,
                BigDecimal.ONE.divide(new BigDecimal("7"), MathContext.DECIMAL128)
                        .compareTo(oneSeventh));
        assertEquals(34, oneSeventh.precision());
    }

    @Test
    public void test_10() {
        ResolvedGraph resolved = this.resolveSales();
        ArithmeticEngine engine = new ArithmeticEngine();

        assertThrows(IllegalArgumentException.class, () -> engine.evaluate(".5 + .25", resolved));
        assertThrows(NumberFormatException.class, () -> engine.evaluate("1..2", resolved));
        assertThrows(IllegalArgumentException.class, () -> engine.evaluate("(2 + 3", resolved));
        assertThrows(IllegalArgumentException.class, () -> engine.evaluate("2 + 3)", resolved));
        assertThrows(IllegalArgumentException.class, () -> engine.evaluate("2 ** 3", resolved));
        assertThrows(IllegalArgumentException.class, () -> engine.evaluate("2 3", resolved));
        assertThrows(IllegalArgumentException.class, () -> engine.evaluate("Inventory.", resolved));
        assertThrows(IllegalArgumentException.class, () -> engine.evaluate("", resolved));
        assertThrows(IllegalArgumentException.class, () -> engine.evaluate("   ", resolved));
        assertThrows(IllegalArgumentException.class, () -> engine.evaluate("1e3", resolved));
        assertThrows(IllegalArgumentException.class, () -> engine.evaluate("1 / / 2", resolved));
        assertThrows(IllegalArgumentException.class, () -> engine.evaluate("2 ^ 3", resolved));
    }

    @Test
    public void test_11() {
        ResolvedGraph resolved = this.resolveSales();
        ArithmeticEngine engine = new ArithmeticEngine();
        String expression =
                "(Inventory.quantityOnHand - Inventory.quantityReserved) / Sales.quantitySold";

        LinkedHashSet<String> distinct = new LinkedHashSet<String>();
        BigDecimal first = null;
        BigDecimal last = null;
        int index = 0;
        while (index < 1000) {
            BigDecimal value = engine.evaluate(expression, resolved);
            if (first == null) {
                first = value;
            }
            last = value;
            distinct.add(value.toPlainString());
            index = index + 1;
        }

        assertEquals(1, distinct.size());
        assertEquals(0, new BigDecimal("20").compareTo(first));
        assertEquals(0, new BigDecimal("20").compareTo(last));
    }

    private ResolvedGraph resolveSales() {
        Phase0GraphHarness.EstablishedGraph established =
                new Phase0GraphHarness().establish(10);
        return this.resolveSales(established);
    }

    private ResolvedGraph resolveSales(Phase0GraphHarness.EstablishedGraph established) {
        Fact axis = established.getAxis();
        assertNotNull(axis);
        assertEquals("Inventory:INV-001", axis.getId());
        GraphResolver resolver = new GraphResolver();
        return resolver.resolve(axis, List.of("Sales"));
    }

    private ResolvedGraph localSalesWithoutQuantity(ResolvedGraph original) {
        Fact originalSales = original.getSnapshot().nodes().get("Sales:SALE-001");
        JsonObject json = JsonParser.parseString(originalSales.getText()).getAsJsonObject();
        json.remove("quantitySold");
        Fact copiedSales = new Fact(originalSales.getId(), json.toString());
        copiedSales.setMode(originalSales.getMode());

        LinkedHashMap<String, Fact> nodes = new LinkedHashMap<String, Fact>();
        for (Map.Entry<String, Fact> entry : original.getSnapshot().nodes().entrySet()) {
            nodes.put(entry.getKey(), entry.getValue());
        }
        nodes.put(copiedSales.getId(), copiedSales);

        LinkedHashMap<String, Edge> edges = new LinkedHashMap<String, Edge>();
        for (Map.Entry<String, Edge> entry : original.getSnapshot().edges().entrySet()) {
            edges.put(entry.getKey(), entry.getValue());
        }

        return new ResolvedGraph(
                original.getAxis(),
                new GraphSnapshot(nodes, edges));
    }
}
