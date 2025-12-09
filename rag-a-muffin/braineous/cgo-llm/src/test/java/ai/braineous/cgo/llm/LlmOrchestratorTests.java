package ai.braineous.cgo.llm;

import ai.braineous.rag.prompt.cgo.api.*;
import ai.braineous.rag.prompt.cgo.prompt.PromptBuilder;
import ai.braineous.rag.prompt.cgo.prompt.SimpleResponseContractRegistry;
import ai.braineous.rag.prompt.cgo.query.CgoQueryPipeline;
import ai.braineous.rag.prompt.cgo.query.Node;
import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class LlmOrchestratorTests {

    @Test
    void executePrompt_withNullPrompt_returnsEmptyString() {
        LlmClientOrchestrator orchestrator = new LlmClientOrchestrator(new OpenAILlmAdapter());

        String result = orchestrator.executePrompt(null);

        assertNotNull(result);
        assertEquals("", result);
    }

    @Test
    void executePrompt_delegatesToAdapter() {
        LlmAdapter fake = prompt -> "STUBBED";
        LlmClientOrchestrator orchestrator = new LlmClientOrchestrator(fake);

        String result = orchestrator.executePrompt(new JsonObject());

        assertEquals("STUBBED", result);
    }


    @Test
    void execute_withNullLlmClient_shouldUseConfiguredLlmClientFromPipelineJson() {
        // arrange
        String factId = "Flight:F100";

        Meta meta = new Meta(
                "v1",
                "validate_flight_airports",
                "Validate that the selected flight fact has valid departure and arrival airport codes using graph context."
        );

        String taskDescription =
                "Validate that the selected flight has valid departure and arrival airport codes based on the airport nodes in the graph. " +
                        "A valid flight must have: (1) 'from' matching one Airport:* code, (2) 'to' matching one Airport:* code, (3) 'from' != 'to'.";

        ValidateTask task = new ValidateTask(taskDescription, factId);

        Node node = new Node(
                factId,
                "{\"id\":\"F100\",\"kind\":\"Flight\",\"mode\":\"relational\",\"from\":\"AUS\",\"to\":\"DFW\"}",
                List.of(),
                Node.Mode.RELATIONAL
        );

        GraphContext context = new GraphContext(Map.of(factId, node));

        QueryRequest<ValidateTask> request =
                QueryRequests.validateTask(meta, task, context, factId);

        // PromptBuilder with no prompt/core/domain validators – pure happy path
        PromptBuilder promptBuilder = new PromptBuilder(new SimpleResponseContractRegistry());

        // IMPORTANT: pass null for LlmClient so pipeline uses pipeline.json -> llm_client
        CgoQueryPipeline pipeline = new CgoQueryPipeline(promptBuilder, null);

        // act
        QueryExecution<ValidateTask> execution = pipeline.execute(request);

        // observe: log what actually happened
        Console.log("pipelineJson.rawResponse", execution != null ? execution.getRawResponse() : null);
        Console.log("pipelineJson.promptValidation", execution != null ? execution.getPromptValidation() : null);
        Console.log("pipelineJson.llmResponseValidation", execution != null ? execution.getLlmResponseValidation() : null);
        Console.log("pipelineJson.domainValidation", execution != null ? execution.getDomainValidation() : null);

        // assert
        assertNotNull(execution, "QueryExecution should not be null when using configured LLM client from pipeline.json");
        assertSame(request, execution.getRequest(), "Execution should wrap the same QueryRequest instance");

        // Because OpenAILlmAdapter stub currently returns:
        //   {"result":{"status":"VALID"}}
        // we at least expect some non-empty response containing VALID.
        String rawResponse = execution.getRawResponse();
        assertNotNull(rawResponse, "rawResponse should not be null");
        assertFalse(rawResponse.isEmpty(), "rawResponse should not be empty");
        assertTrue(rawResponse.contains("\"VALID\""),
                "rawResponse should contain VALID as returned by the stubbed OpenAILlmAdapter");
    }
}
