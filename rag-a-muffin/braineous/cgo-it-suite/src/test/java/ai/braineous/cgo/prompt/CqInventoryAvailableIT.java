package ai.braineous.cgo.prompt;

import ai.braineous.cgo.prompt.scenario.NexusDomainExecutionHarness;
import ai.braineous.cgo.prompt.scenario.NexusDomainScenario;
import ai.braineous.cgo.prompt.scenario.NexusDomainScenarios;
import ai.braineous.rag.prompt.cgo.querygen.DeclarativeQueryCompiler;
import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CqInventoryAvailableIT {

    private static final String CQ_RESOURCE =
            "/ai/braineous/cgo/prompt/cq/inventory-available.sql";

    @Test
    public void test_1() throws IOException {
        String sql = this.loadCq(CQ_RESOURCE);
        Console.log("cq.inventory.available.input", sql);

        JsonObject compiled = new DeclarativeQueryCompiler().compile(sql);
        Console.log("cq.inventory.available.compiled", compiled.toString());

        JsonObject task = compiled.getAsJsonObject("task");
        assertNotNull(task);
        assertEquals("Inventory:INV-001", task.get("factId").getAsString());

        JsonArray relatedFactIds = task.getAsJsonArray("relatedFactIds");
        assertEquals(1, relatedFactIds.size());
        assertEquals("Sales", relatedFactIds.get(0).getAsString());
        assertEquals(
                "(Inventory.quantityOnHand - Inventory.quantityReserved) / Sales.quantitySold",
                task.get("expression").getAsString());
        assertEquals("inventory availability.", task.get("definition").getAsString());
        assertFalse(task.has("constraints"));
        assertFalse(task.has("ruleSection"));
        assertFalse(task.has("decisionSection"));

        List<String> compiledRelatedFacts = new ArrayList<String>();
        int relatedIndex = 0;
        while (relatedIndex < relatedFactIds.size()) {
            compiledRelatedFacts.add(relatedFactIds.get(relatedIndex).getAsString());
            relatedIndex = relatedIndex + 1;
        }

        NexusDomainScenario scenario =
                NexusDomainScenarios.inventoryAvailable();
        assertEquals(scenario.getAxisId(), task.get("factId").getAsString());
        assertEquals(scenario.getRelatedFacts(), compiledRelatedFacts);
        assertEquals(scenario.getExpression(), task.get("expression").getAsString());
        assertEquals(scenario.getDefinition(), task.get("definition").getAsString());

        int invocations = Integer.getInteger("cgo.it.invocations", 1).intValue();
        new NexusDomainExecutionHarness().run(
                scenario,
                compiledRelatedFacts,
                task.get("expression").getAsString(),
                task.get("definition").getAsString(),
                invocations);
    }

    private String loadCq(String resource) throws IOException {
        InputStream stream = CqInventoryAvailableIT.class.getResourceAsStream(resource);
        assertNotNull(stream, resource);
        try {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } finally {
            stream.close();
        }
    }
}
