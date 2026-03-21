package ai.braineous.cgo.llm;

import ai.braineous.cgo.history.HistoryRecord;
import ai.braineous.cgo.history.HistoryStore;
import ai.braineous.rag.prompt.cgo.api.*;
import ai.braineous.rag.prompt.cgo.prompt.PromptBuilder;
import ai.braineous.rag.prompt.cgo.query.CgoQueryPipeline;
import ai.braineous.rag.prompt.cgo.query.Node;
import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class LlmClientOrchestratorTests {

    @Test
    void executePrompt_nullInputs_shouldReturnEmptyString() {
        Console.log("test_start", "executePrompt_nullInputs_shouldReturnEmptyString");

        LlmClientOrchestrator orchestrator = new LlmClientOrchestrator();

        String r1 = orchestrator.executePrompt(null,null, new JsonObject());
        Console.log("result_null_adapter", r1);

        String r2 = orchestrator.executePrompt(new LlmAdapter() {
            @Override
            public String invokeLlm(QueryRequest queryRequest, JsonObject prompt) {
                return "SHOULD_NOT_HAPPEN";
            }
        }, null, null);
        Console.log("result_null_prompt", r2);

        String r3 = orchestrator.executePrompt(null, null, null);
        Console.log("result_both_null", r3);

        assertEquals("", r1, "When adapter is null, should return empty string");
        assertEquals("", r2, "When prompt is null, should return empty string");
        assertEquals("", r3, "When both are null, should return empty string");
    }

    @Test
    void executePrompt_shouldDelegateToAdapter_andReturnRawResponse() {
        Console.log("test_start", "executePrompt_shouldDelegateToAdapter_andReturnRawResponse");

        LlmClientOrchestrator orchestrator = new LlmClientOrchestrator();

        final int[] calls = {0};

        LlmAdapter adapter = new LlmAdapter() {
            @Override
            public String invokeLlm(QueryRequest queryRequest, JsonObject prompt) {
                calls[0]++;
                Console.log("adapter_invoked", "calls=" + calls[0] + ", promptNull=" + (prompt == null));
                return "{\"result\":{\"status\":\"VALID\"}}";
            }
        };

        JsonObject prompt = new JsonObject();
        prompt.addProperty("ping", "pong");

        String raw = orchestrator.executePrompt(adapter, null, prompt);

        Console.log("raw_response", raw);
        Console.log("adapter_calls", calls[0]);

        assertEquals(1, calls[0], "Adapter should be invoked exactly once");
        assertNotNull(raw, "Raw response should not be null");
        assertTrue(raw.contains("\"VALID\""), "Raw response should contain VALID");
    }

    @Test
    void pipeline_spine_shouldExecuteAndAppendHistoryRecord() {
        Console.log("test_start", "pipeline_spine_shouldExecuteAndAppendHistoryRecord");

        // arrange: clean history
        HistoryStore store = HistoryStore.getInstance();
        store.clear();

        int before = store.getAll().size();
        Console.log("history_before", before);

        // minimal request (use your existing helper)
        String factId = "Flight:F100";
        Meta meta = new Meta("v1", "validate_flight_airports", "spine");
        ValidateTask task = new ValidateTask("validate flight airports", factId);

        Node node = new Node(
                factId,
                "{\"id\":\"F100\",\"kind\":\"Flight\",\"from\":\"AUS\",\"to\":\"DFW\"}",
                List.of(),
                Node.Mode.RELATIONAL
        );

        GraphContext context = new GraphContext(Map.of(factId, node));

        QueryRequest<ValidateTask> request = QueryRequests.validateTask(meta, task, context, factId);

        // Fake adapter returns deterministic response; LlmClientOrchestrator delegates to adapter
        request.setAdapter(new LlmAdapter() {
            @Override
            public String invokeLlm(QueryRequest queryRequest, JsonObject prompt) {
                Console.log("fake_adapter_invoked", prompt == null ? "prompt=null" : "prompt=ok");
                return """
                {
                  "result": {
                    "ok": true,
                    "code": "response.contract.ok",
                    "message": "VALID",
                    "stage": "llm_response_validation",
                    "anchorId": null,
                    "metadata": { "adapter": "fake" }
                  }
                }
                """;
            }
        });

        PromptBuilder promptBuilder = new PromptBuilder();

        // KEY: pass null llmClient so pipeline uses pipeline.json wiring
        CgoQueryPipeline pipeline = new CgoQueryPipeline(promptBuilder);
        pipeline.setInMemoryMode(true);

        // act
        QueryExecution<ValidateTask> execution = pipeline.execute(request);

        // assert: execution exists + response
        assertNotNull(execution, "execution should not be null");
        Console.log("raw_response", execution.getRawResponse());
        assertNotNull(execution.getRawResponse(), "rawResponse should not be null");
        assertTrue(execution.getRawResponse().contains("\"VALID\""), "rawResponse should contain VALID");

        // assert: scorer side-effect happened
        int after = store.getAll().size();
        Console.log("history_after", after);

        assertEquals(before + 1, after, "scorer should append exactly one history record");

        HistoryRecord last = store.getAll().get(after - 1);
        Console.log("last_record", last);
        assertNotNull(last.getResult(), "ScorerResult should not be null");
    }

}
