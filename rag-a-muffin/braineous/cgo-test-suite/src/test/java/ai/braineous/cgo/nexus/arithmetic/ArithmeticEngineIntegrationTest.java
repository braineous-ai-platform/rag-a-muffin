package ai.braineous.cgo.nexus.arithmetic;

import ai.braineous.cgo.nexus.harness.Phase0GraphHarness;
import ai.braineous.cgo.nexus.model.ResolvedGraph;
import ai.braineous.cgo.nexus.orchestrator.GraphResolver;
import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ArithmeticEngineIntegrationTest {

    @Test
    public void test_1() {
        Phase0GraphHarness.EstablishedGraph established =
                new Phase0GraphHarness().establish(10);
        Fact axis = established.getAxis();
        assertNotNull(axis);
        assertEquals("Inventory:INV-001", axis.getId());

        GraphResolver resolver = new GraphResolver();
        ResolvedGraph resolvedGraph = resolver.resolve(axis, List.of("Sales"));
        assertNotNull(resolvedGraph);
        GraphSnapshot snapshot = resolvedGraph.getSnapshot();
        assertNotNull(snapshot);
        assertNotNull(snapshot.nodes());

        TreeSet<String> factIds = new TreeSet<String>();
        for (Map.Entry<String, Fact> entry : snapshot.nodes().entrySet()) {
            Fact fact = entry.getValue();
            if (fact != null && fact.getId() != null) {
                factIds.add(fact.getId());
            }
        }

        System.out.print("=== RESOLVED FACT IDS ===\n");
        for (String factId : factIds) {
            System.out.print(factId);
            System.out.print("\n");
        }

        assertEquals(3, snapshot.nodes().size());
        assertTrue(snapshot.nodes().containsKey("Inventory:INV-001"));
        assertTrue(snapshot.nodes().containsKey("Product:PROD-SHAMPOO-250"));
        assertTrue(snapshot.nodes().containsKey("Sales:SALE-001"));

        int extra = 2;
        while (extra <= 10) {
            String seq = extra < 10 ? ("00" + extra) : "010";
            assertFalse(snapshot.nodes().containsKey("Inventory:INV-" + seq));
            assertFalse(snapshot.nodes().containsKey("Sales:SALE-" + seq));
            assertFalse(snapshot.nodes().containsKey("Vendor:VENDOR-" + seq));
            assertFalse(snapshot.nodes().containsKey("Location:LOC-" + seq));
            assertFalse(snapshot.nodes().containsKey(
                    "Product:PROD-SHAMPOO-" + (249 + extra)));
            extra = extra + 1;
        }

        String expression =
                "(Inventory.quantityOnHand - "
                        + "Inventory.quantityReserved) / "
                        + "Sales.quantitySold";
        System.out.print("=== EXPRESSION ===\n");
        System.out.print(expression);
        System.out.print("\n");

        ArithmeticEngine engine = new ArithmeticEngine();
        BigDecimal actual = engine.evaluate(expression, resolvedGraph);

        System.out.print("=== RESULT ===\n");
        System.out.print(actual == null ? "null" : actual.toPlainString());
        System.out.print("\n");
        System.out.print("=== NUMERICALLY EQUAL TO 20 ===\n");
        System.out.print(String.valueOf(
                actual != null && new BigDecimal("20").compareTo(actual) == 0));
        System.out.print("\n");

        assertNotNull(actual);
        assertEquals(0, new BigDecimal("20").compareTo(actual));
    }
}
