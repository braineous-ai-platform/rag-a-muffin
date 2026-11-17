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
import static org.junit.jupiter.api.Assertions.assertNull;

public class CqSecurityInvestigationIT {

    private static final String CQ_RESOURCE =
            "/ai/braineous/cgo/prompt/cq/security-investigation.sql";

    @Test
    public void test_1() throws IOException {
        String sql = this.loadCq(CQ_RESOURCE);
        Console.log("cq.security.investigation.input", sql);

        JsonObject compiled = new DeclarativeQueryCompiler().compile(sql);
        Console.log("cq.security.investigation.compiled", compiled.toString());

        JsonObject task = compiled.getAsJsonObject("task");
        assertNotNull(task);
        assertEquals("Account:emp-042", task.get("factId").getAsString());

        JsonArray relatedFactIds = task.getAsJsonArray("relatedFactIds");
        assertEquals(4, relatedFactIds.size());
        assertEquals("Location", relatedFactIds.get(0).getAsString());
        assertEquals("Device", relatedFactIds.get(1).getAsString());
        assertEquals("FailedLogin", relatedFactIds.get(2).getAsString());
        assertEquals("SuccessfulLogin", relatedFactIds.get(3).getAsString());
        assertEquals("", task.get("expression").getAsString());
        assertEquals("security investigation", task.get("definition").getAsString());
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
                NexusDomainScenarios.securityInvestigation();
        assertEquals(scenario.getAxisId(), task.get("factId").getAsString());
        assertEquals(scenario.getRelatedFacts(), compiledRelatedFacts);
        assertNull(scenario.getExpression());
        assertEquals(scenario.getDefinition(), task.get("definition").getAsString());
        assertEquals("failedLoginAttempts: 3", scenario.getExpectedNexusMarker());

        int invocations = Integer.getInteger("cgo.it.invocations", 1).intValue();
        new NexusDomainExecutionHarness().run(
                scenario,
                compiledRelatedFacts,
                task.get("expression").getAsString(),
                task.get("definition").getAsString(),
                invocations);
    }

    private String loadCq(String resource) throws IOException {
        InputStream stream = CqSecurityInvestigationIT.class.getResourceAsStream(resource);
        assertNotNull(stream, resource);
        try {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } finally {
            stream.close();
        }
    }
}
