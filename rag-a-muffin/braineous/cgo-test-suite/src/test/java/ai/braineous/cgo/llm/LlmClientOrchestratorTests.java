package ai.braineous.cgo.llm;

import ai.braineous.rag.prompt.cgo.api.LlmAdapter;
import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LlmClientOrchestratorTests {

    @Test
    void executePrompt_nullAdapter_shouldReturnEmptyString() {
        Console.log("test_start", "executePrompt_nullAdapter_shouldReturnEmptyString");

        LlmClientOrchestrator orchestrator = new LlmClientOrchestrator();
        String result = orchestrator.executePrompt(null, new QueryRequest(), new JsonObject());

        Console.log("result", result);

        assertEquals("", result);
    }

    @Test
    void executePrompt_nullQueryRequest_shouldReturnEmptyString() {
        Console.log("test_start", "executePrompt_nullQueryRequest_shouldReturnEmptyString");

        LlmClientOrchestrator orchestrator = new LlmClientOrchestrator();

        String result = orchestrator.executePrompt(new LlmAdapter() {
            @Override
            public String invokeLlm(QueryRequest queryRequest, JsonObject prompt) {
                return "SHOULD_NOT_HAPPEN";
            }
        }, null, new JsonObject());

        Console.log("result", result);

        assertEquals("", result);
    }

    @Test
    void executePrompt_nullPrompt_shouldReturnEmptyString() {
        Console.log("test_start", "executePrompt_nullPrompt_shouldReturnEmptyString");

        LlmClientOrchestrator orchestrator = new LlmClientOrchestrator();

        String result = orchestrator.executePrompt(new LlmAdapter() {
            @Override
            public String invokeLlm(QueryRequest queryRequest, JsonObject prompt) {
                return "SHOULD_NOT_HAPPEN";
            }
        }, new QueryRequest(), null);

        Console.log("result", result);

        assertEquals("", result);
    }

    @Test
    void executePrompt_validInputs_shouldGenerateRequestId_delegateToAdapter_andReturnRawResponse() {
        Console.log("test_start", "executePrompt_validInputs_shouldGenerateRequestId_delegateToAdapter_andReturnRawResponse");

        LlmClientOrchestrator orchestrator = new LlmClientOrchestrator();

        final int[] calls = {0};
        final QueryRequest[] capturedRequest = new QueryRequest[1];
        final JsonObject[] capturedPrompt = new JsonObject[1];

        LlmAdapter adapter = new LlmAdapter() {
            @Override
            public String invokeLlm(QueryRequest queryRequest, JsonObject prompt) {
                calls[0]++;
                capturedRequest[0] = queryRequest;
                capturedPrompt[0] = prompt;

                Console.log("adapter_invoked_calls", calls[0]);
                Console.log("adapter_request_id", queryRequest.getRequestId());
                Console.log("adapter_prompt_null", prompt == null);

                return "{\"result\":{\"status\":\"VALID\"}}";
            }
        };

        QueryRequest queryRequest = new QueryRequest();
        JsonObject prompt = new JsonObject();
        prompt.addProperty("ping", "pong");

        assertNull(queryRequest.getRequestId());

        String raw = orchestrator.executePrompt(adapter, queryRequest, prompt);

        Console.log("raw_response", raw);
        Console.log("final_request_id", queryRequest.getRequestId());

        assertEquals(1, calls[0]);
        assertSame(queryRequest, capturedRequest[0]);
        assertSame(prompt, capturedPrompt[0]);

        assertNotNull(queryRequest.getRequestId());
        assertFalse(queryRequest.getRequestId().trim().isEmpty());

        assertNotNull(raw);
        assertFalse(raw.trim().isEmpty());
        assertTrue(raw.contains("\"VALID\""));
    }
}
