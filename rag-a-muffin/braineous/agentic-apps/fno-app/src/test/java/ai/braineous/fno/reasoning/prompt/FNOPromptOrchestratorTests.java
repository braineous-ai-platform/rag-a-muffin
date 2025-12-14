package ai.braineous.fno.reasoning.prompt;

import ai.braineous.rag.prompt.cgo.api.QueryExecution;
import ai.braineous.rag.prompt.cgo.api.ValidateTask;
import ai.braineous.rag.prompt.observe.Console;
import ai.braineous.rag.prompt.utils.Resources;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

public class FNOPromptOrchestratorTests {

    @Test
    public void testOrchestrate() throws Exception{
        FNOPromptOrchestrator orchestrator = new FNOPromptOrchestrator();

        String promptStr = Resources.getResource("prompt.json");
        JsonObject promptJson = JsonParser.parseString(promptStr).getAsJsonObject();

        QueryExecution<ValidateTask> execution = orchestrator.orchestrate(promptJson);

        Console.log("execution", execution);
    }
}
