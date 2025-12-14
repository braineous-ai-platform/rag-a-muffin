package ai.braineous.fno.reasoning.prompt;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

public class FNOPromptOrchestratorTests {

    @Test
    public void testOrchestrate() throws Exception{
        FNOPromptOrchestrator orchestrator = new FNOPromptOrchestrator();

        JsonObject jsonObject = new JsonObject();

        orchestrator.orchestrate(jsonObject);
    }
}
