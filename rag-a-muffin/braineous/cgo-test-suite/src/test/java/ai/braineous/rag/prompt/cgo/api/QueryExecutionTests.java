package ai.braineous.rag.prompt.cgo.api;

import ai.braineous.rag.prompt.cgo.query.QueryTask;
import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class QueryExecutionTests {

    @Test
    public void shouldConstructWithNullRequest() {

        QueryExecution<QueryTask> execution = new QueryExecution<QueryTask>(null);

        Console.log("_____query_execution_construct_null_request_____", execution.toString());

        assertNotNull(execution);
        assertNull(execution.getRequest());
        assertNull(execution.getRawResponse());
        assertNull(execution.getPromptValidation());
        assertNull(execution.getLlmResponseValidation());
        assertNull(execution.getDomainValidation());
        assertNull(execution.getValidationResult());
        assertNull(execution.getPrimaryValidation());
        assertNull(execution.getLlmResponse());

        assertFalse(execution.hasPromptValidation());
        assertFalse(execution.hasLlmResponseValidation());
        assertFalse(execution.hasDomainValidation());
        assertFalse(execution.hasValidationResult());

        assertTrue(execution.isOk());
        assertEquals("ok", execution.getStage());
        assertEquals("OK", execution.getStatus());
        assertFalse(execution.isInMemoryMode());
    }

    @Test
    public void shouldConstructWithFullConstructorAndNullValidations() {

        QueryExecution<QueryTask> execution =
                new QueryExecution<QueryTask>(null, "raw-hello", null, null, null);

        Console.log("_____query_execution_full_constructor_____", execution.toString());

        assertNotNull(execution);
        assertNull(execution.getRequest());
        assertEquals("raw-hello", execution.getRawResponse());
        assertNull(execution.getPromptValidation());
        assertNull(execution.getLlmResponseValidation());
        assertNull(execution.getDomainValidation());
        assertNull(execution.getValidationResult());
        assertNull(execution.getPrimaryValidation());

        assertFalse(execution.hasPromptValidation());
        assertFalse(execution.hasLlmResponseValidation());
        assertFalse(execution.hasDomainValidation());
        assertFalse(execution.hasValidationResult());

        assertTrue(execution.isOk());
        assertEquals("ok", execution.getStage());
        assertEquals("OK", execution.getStatus());
    }

    @Test
    public void shouldSetAndGetLlmResponse() {

        QueryExecution<QueryTask> execution = new QueryExecution<QueryTask>(null);

        LLMResponse llmResponse = new LLMResponse();
        llmResponse.setId("llm-resp-1");
        llmResponse.setCreatedAt("2026-03-22T16:00:00Z");
        llmResponse.setUpdatedAt("2026-03-22T16:00:01Z");
        llmResponse.setSnapshotHash("snap-llm-1");
        llmResponse.setRawResponse("{\"answer\":\"ok\"}");
        llmResponse.setSuccess(true);

        execution.setLlmResponse(llmResponse);

        Console.log("_____query_execution_llm_response_____", execution.getLlmResponse().toJsonString());

        assertNotNull(execution.getLlmResponse());
        assertEquals("llm-resp-1", execution.getLlmResponse().getId());
        assertEquals("2026-03-22T16:00:00Z", execution.getLlmResponse().getCreatedAt());
        assertEquals("2026-03-22T16:00:01Z", execution.getLlmResponse().getUpdatedAt());
        assertEquals("snap-llm-1", execution.getLlmResponse().getSnapshotHash());
        assertEquals("{\"answer\":\"ok\"}", execution.getLlmResponse().getRawResponse());
        assertTrue(execution.getLlmResponse().isSuccess());
    }

    @Test
    public void shouldToggleInMemoryMode() {

        QueryExecution<QueryTask> execution = new QueryExecution<QueryTask>(null);

        Console.log("_____query_execution_in_memory_before_____", execution.isInMemoryMode());

        assertFalse(execution.isInMemoryMode());

        execution.setInMemoryMode(true);

        Console.log("_____query_execution_in_memory_after_____", execution.isInMemoryMode());

        assertTrue(execution.isInMemoryMode());

        execution.setInMemoryMode(false);

        Console.log("_____query_execution_in_memory_reset_____", execution.isInMemoryMode());

        assertFalse(execution.isInMemoryMode());
    }

    @Test
    public void shouldReturnOkStageAndStatusWhenAllValidationsAreNull() {

        QueryExecution<QueryTask> execution =
                new QueryExecution<QueryTask>(null, "raw-response", null, null, null);

        Console.log("_____query_execution_ok_stage_status_____", execution.toString());

        assertTrue(execution.isOk());
        assertEquals("ok", execution.getStage());
        assertEquals("OK", execution.getStatus());
        assertNull(execution.getPrimaryValidation());
    }

    @Test
    public void shouldSerializeToJsonWithNullRequestAndValidations() {

        QueryExecution<QueryTask> execution =
                new QueryExecution<QueryTask>(null, "raw-json", null, null, null);

        LLMResponse llmResponse = new LLMResponse();
        llmResponse.setId("llm-resp-2");
        llmResponse.setCreatedAt("2026-03-22T16:10:00Z");
        llmResponse.setUpdatedAt("2026-03-22T16:10:01Z");
        llmResponse.setSnapshotHash("snap-llm-2");
        llmResponse.setRawResponse("{\"result\":\"accepted\"}");
        llmResponse.setSuccess(true);

        execution.setLlmResponse(llmResponse);

        JsonObject json = execution.toJson();

        Console.log("_____query_execution_json_____", json);

        assertNotNull(json);

        assertTrue(json.has("request"));
        assertTrue(json.get("request").isJsonNull());

        assertTrue(json.has("rawResponse"));
        assertEquals("raw-json", json.get("rawResponse").getAsString());

        assertTrue(json.has("promptValidation"));
        assertTrue(json.get("promptValidation").isJsonNull());

        assertTrue(json.has("llmResponseValidation"));
        assertTrue(json.get("llmResponseValidation").isJsonNull());

        assertTrue(json.has("domainValidation"));
        assertTrue(json.get("domainValidation").isJsonNull());

        assertTrue(json.has("status"));
        assertEquals("OK", json.get("status").getAsString());

        assertTrue(json.has("stage"));
        assertEquals("ok", json.get("stage").getAsString());

        assertTrue(json.has("ok"));
        assertTrue(json.get("ok").getAsBoolean());

        assertTrue(json.has("llmResponse"));
        assertFalse(json.get("llmResponse").isJsonNull());

        JsonObject llmResponseJson = json.getAsJsonObject("llmResponse");
        assertEquals("llm-resp-2", llmResponseJson.get("id").getAsString());
        assertEquals("{\"result\":\"accepted\"}", llmResponseJson.get("rawResponse").getAsString());
        assertTrue(llmResponseJson.get("success").getAsBoolean());
    }

    @Test
    public void shouldSerializeToJsonString() {

        QueryExecution<QueryTask> execution =
                new QueryExecution<QueryTask>(null, "raw-json-string", null, null, null);

        LLMResponse llmResponse = new LLMResponse();
        llmResponse.setId("llm-resp-3");
        llmResponse.setRawResponse("{\"x\":\"y\"}");
        llmResponse.setSuccess(false);

        execution.setLlmResponse(llmResponse);

        String json = execution.toJsonString();

        Console.log("_____query_execution_json_string_____", json);

        assertNotNull(json);
        assertTrue(json.contains("\"request\":null"));
        assertTrue(json.contains("\"rawResponse\":\"raw-json-string\""));
        assertTrue(json.contains("\"promptValidation\":null"));
        assertTrue(json.contains("\"llmResponseValidation\":null"));
        assertTrue(json.contains("\"domainValidation\":null"));
        assertTrue(json.contains("\"status\":\"OK\""));
        assertTrue(json.contains("\"stage\":\"ok\""));
        assertTrue(json.contains("\"ok\":true"));
        assertTrue(json.contains("\"llmResponse\""));
        assertTrue(json.contains("\"id\":\"llm-resp-3\""));
        assertTrue(json.contains("\"rawResponse\":\"{\\\"x\\\":\\\"y\\\"}\""));
        assertTrue(json.contains("\"success\":false"));
    }

    @Test
    public void shouldDeserializeFromJsonWithNullRequestAndValidations() {

        JsonObject json = new JsonObject();
        json.add("request", null);
        json.addProperty("rawResponse", "raw-from-json");
        json.add("promptValidation", null);
        json.add("llmResponseValidation", null);
        json.add("domainValidation", null);

        JsonObject llmResponseJson = new JsonObject();
        llmResponseJson.addProperty("id", "llm-resp-4");
        llmResponseJson.addProperty("createdAt", "2026-03-22T16:20:00Z");
        llmResponseJson.addProperty("updatedAt", "2026-03-22T16:20:01Z");
        llmResponseJson.addProperty("snapshotHash", "snap-llm-4");
        llmResponseJson.add("llmRequest", null);
        llmResponseJson.addProperty("rawResponse", "{\"hello\":\"world\"}");
        llmResponseJson.addProperty("success", true);

        json.add("llmResponse", llmResponseJson);

        Console.log("_____query_execution_from_json_input_____", json);

        QueryExecution<?> execution = QueryExecution.fromJson(json);

        Console.log("_____query_execution_from_json_output_____", execution.toJsonString());

        assertNotNull(execution);
        assertNull(execution.getRequest());
        assertEquals("raw-from-json", execution.getRawResponse());
        assertNull(execution.getPromptValidation());
        assertNull(execution.getLlmResponseValidation());
        assertNull(execution.getDomainValidation());

        assertFalse(execution.hasPromptValidation());
        assertFalse(execution.hasLlmResponseValidation());
        assertFalse(execution.hasDomainValidation());

        assertTrue(execution.isOk());
        assertEquals("ok", execution.getStage());
        assertEquals("OK", execution.getStatus());

        assertNotNull(execution.getLlmResponse());
        assertEquals("llm-resp-4", execution.getLlmResponse().getId());
        assertEquals("2026-03-22T16:20:00Z", execution.getLlmResponse().getCreatedAt());
        assertEquals("2026-03-22T16:20:01Z", execution.getLlmResponse().getUpdatedAt());
        assertEquals("snap-llm-4", execution.getLlmResponse().getSnapshotHash());
        assertEquals("{\"hello\":\"world\"}", execution.getLlmResponse().getRawResponse());
        assertTrue(execution.getLlmResponse().isSuccess());
    }

    @Test
    public void shouldRoundTripJsonWithLlmResponse() {

        QueryExecution<QueryTask> original =
                new QueryExecution<QueryTask>(null, "raw-roundtrip", null, null, null);

        LLMResponse llmResponse = new LLMResponse();
        llmResponse.setId("llm-resp-5");
        llmResponse.setCreatedAt("2026-03-22T16:30:00Z");
        llmResponse.setUpdatedAt("2026-03-22T16:30:01Z");
        llmResponse.setSnapshotHash("snap-llm-5");
        llmResponse.setRawResponse("{\"decision\":\"GO\"}");
        llmResponse.setSuccess(true);

        original.setLlmResponse(llmResponse);

        JsonObject json = original.toJson();

        Console.log("_____query_execution_roundtrip_json_____", json);

        QueryExecution<?> restored = QueryExecution.fromJson(json);

        Console.log("_____query_execution_roundtrip_restored_____", restored.toJsonString());

        assertNotNull(restored);
        assertNull(restored.getRequest());
        assertEquals("raw-roundtrip", restored.getRawResponse());
        assertNull(restored.getPromptValidation());
        assertNull(restored.getLlmResponseValidation());
        assertNull(restored.getDomainValidation());

        assertNotNull(restored.getLlmResponse());
        assertEquals("llm-resp-5", restored.getLlmResponse().getId());
        assertEquals("2026-03-22T16:30:00Z", restored.getLlmResponse().getCreatedAt());
        assertEquals("2026-03-22T16:30:01Z", restored.getLlmResponse().getUpdatedAt());
        assertEquals("snap-llm-5", restored.getLlmResponse().getSnapshotHash());
        assertEquals("{\"decision\":\"GO\"}", restored.getLlmResponse().getRawResponse());
        assertTrue(restored.getLlmResponse().isSuccess());

        assertTrue(restored.isOk());
        assertEquals("ok", restored.getStage());
        assertEquals("OK", restored.getStatus());
    }

    @Test
    public void shouldSerializeNullLlmResponseAsJsonNull() {

        QueryExecution<QueryTask> execution =
                new QueryExecution<QueryTask>(null, "raw-null-llm", null, null, null);

        JsonObject json = execution.toJson();

        Console.log("_____query_execution_null_llm_response_json_____", json);

        assertNotNull(json);
        assertTrue(json.has("llmResponse"));
        assertTrue(json.get("llmResponse").isJsonNull());
    }

    @Test
    public void shouldThrowWhenFromJsonGetsNullJson() {

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                new org.junit.jupiter.api.function.Executable() {
                    @Override
                    public void execute() {
                        QueryExecution.fromJson(null);
                    }
                }
        );

        Console.log("_____query_execution_null_json_exception_____", ex.getMessage());

        assertEquals("QueryExecution JSON cannot be null", ex.getMessage());
    }

    @Test
    public void shouldContainStatusStageAndRawResponseInToString() {

        QueryExecution<QueryTask> execution =
                new QueryExecution<QueryTask>(null, "raw-to-string", null, null, null);

        String str = execution.toString();

        Console.log("_____query_execution_to_string_____", str);

        assertNotNull(str);
        assertTrue(str.contains("status=OK"));
        assertTrue(str.contains("stage=ok"));
        assertTrue(str.contains("rawResponse='raw-to-string'"));
    }
}