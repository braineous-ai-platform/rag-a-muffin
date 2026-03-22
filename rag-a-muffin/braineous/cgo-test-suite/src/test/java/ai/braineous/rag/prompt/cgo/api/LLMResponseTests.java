package ai.braineous.rag.prompt.cgo.api;

import ai.braineous.rag.prompt.cgo.api.LLMResponse;
import ai.braineous.rag.prompt.observe.Console;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class LLMResponseTests {

    @Test
    public void shouldSerializeLlmResponseToJsonString() {

        LLMResponse response = new LLMResponse();
        response.setId("llm-resp-1");
        response.setCreatedAt("2026-03-22T14:00:00Z");
        response.setUpdatedAt("2026-03-22T14:00:01Z");
        response.setSnapshotHash("snap-resp-123");
        response.setRawResponse("{\"answer\":\"ok\"}");
        response.setSuccess(true);

        String json = response.toJsonString();

        Console.log("_____llm_response_json_string_____", json);

        assertNotNull(json);
        assertTrue(json.contains("\"id\":\"llm-resp-1\""));
        assertTrue(json.contains("\"createdAt\":\"2026-03-22T14:00:00Z\""));
        assertTrue(json.contains("\"updatedAt\":\"2026-03-22T14:00:01Z\""));
        assertTrue(json.contains("\"snapshotHash\":\"snap-resp-123\""));
        assertTrue(json.contains("\"rawResponse\":\"{\\\"answer\\\":\\\"ok\\\"}\""));
        assertTrue(json.contains("\"success\":true"));
    }

    @Test
    public void shouldConvertLlmResponseToJsonObject() {

        LLMResponse response = new LLMResponse();
        response.setId("llm-resp-2");
        response.setCreatedAt("2026-03-22T14:05:00Z");
        response.setUpdatedAt("2026-03-22T14:05:01Z");
        response.setSnapshotHash("snap-resp-456");
        response.setRawResponse("{\"status\":\"done\"}");
        response.setSuccess(false);

        com.google.gson.JsonObject json = response.toJson();

        Console.log("_____llm_response_json_object_____", json);

        assertNotNull(json);
        assertEquals("llm-resp-2", json.get("id").getAsString());
        assertEquals("2026-03-22T14:05:00Z", json.get("createdAt").getAsString());
        assertEquals("2026-03-22T14:05:01Z", json.get("updatedAt").getAsString());
        assertEquals("snap-resp-456", json.get("snapshotHash").getAsString());
        assertEquals("{\"status\":\"done\"}", json.get("rawResponse").getAsString());
        assertFalse(json.get("success").getAsBoolean());
    }

    @Test
    public void shouldDeserializeLlmResponseFromJson() {

        String json =
                "{"
                        + "\"id\":\"llm-resp-3\","
                        + "\"createdAt\":\"2026-03-22T14:10:00Z\","
                        + "\"updatedAt\":\"2026-03-22T14:10:05Z\","
                        + "\"snapshotHash\":\"snap-resp-789\","
                        + "\"llmRequest\":null,"
                        + "\"rawResponse\":\"{\\\"result\\\":\\\"accepted\\\"}\","
                        + "\"success\":true"
                        + "}";

        Console.log("_____llm_response_json_input_____", json);

        LLMResponse response = LLMResponse.fromJson(json, LLMResponse.class);

        Console.log("_____llm_response_deserialized_____", response.toJsonString());

        assertNotNull(response);
        assertEquals("llm-resp-3", response.getId());
        assertEquals("2026-03-22T14:10:00Z", response.getCreatedAt());
        assertEquals("2026-03-22T14:10:05Z", response.getUpdatedAt());
        assertEquals("snap-resp-789", response.getSnapshotHash());
        assertEquals("{\"result\":\"accepted\"}", response.getRawResponse());
        assertTrue(response.isSuccess());
    }

    @Test
    public void shouldPreserveNullFieldsInJsonString() {

        LLMResponse response = new LLMResponse();
        response.setId("llm-resp-4");
        response.setCreatedAt("2026-03-22T14:20:00Z");
        response.setUpdatedAt("2026-03-22T14:20:01Z");
        response.setSnapshotHash("snap-resp-000");
        response.setLlmRequest(null);
        response.setRawResponse(null);
        response.setSuccess(false);

        String json = response.toJsonString();

        Console.log("_____llm_response_null_json_____", json);

        assertNotNull(json);
        assertTrue(json.contains("\"llmRequest\":null"));
        assertTrue(json.contains("\"rawResponse\":null"));
        assertTrue(json.contains("\"success\":false"));
    }

    @Test
    public void shouldThrowWhenFromJsonGetsNullJson() {

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                new org.junit.jupiter.api.function.Executable() {
                    @Override
                    public void execute() {
                        LLMResponse.fromJson(null, LLMResponse.class);
                    }
                }
        );

        Console.log("_____llm_response_null_json_exception_____", ex.getMessage());

        assertEquals("json cannot be null", ex.getMessage());
    }

    @Test
    public void shouldThrowWhenFromJsonGetsBlankJson() {

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                new org.junit.jupiter.api.function.Executable() {
                    @Override
                    public void execute() {
                        LLMResponse.fromJson("   ", LLMResponse.class);
                    }
                }
        );

        Console.log("_____llm_response_blank_json_exception_____", ex.getMessage());

        assertEquals("json cannot be blank", ex.getMessage());
    }

    @Test
    public void shouldThrowWhenFromJsonGetsNullType() {

        String json = "{\"id\":\"llm-resp-5\"}";

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                new org.junit.jupiter.api.function.Executable() {
                    @Override
                    public void execute() {
                        LLMResponse.fromJson(json, null);
                    }
                }
        );

        Console.log("_____llm_response_null_type_exception_____", ex.getMessage());

        assertEquals("type cannot be null", ex.getMessage());
    }
}
