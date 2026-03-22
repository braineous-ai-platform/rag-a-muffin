package ai.braineous.rag.prompt.cgo.api;

import ai.braineous.rag.prompt.cgo.api.LLMRequest;
import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LLMRequestTests {

    @Test
    public void shouldSerializeLlmRequestToJsonString() {

        LLMRequest request = new LLMRequest();
        request.setId("llm-req-1");
        request.setCreatedAt("2026-03-22T10:15:30Z");
        request.setUpdatedAt("2026-03-22T10:15:31Z");
        request.setSnapshotHash("snap-123");

        JsonObject llmQuery = new JsonObject();
        llmQuery.addProperty("queryKind", "WHY");
        llmQuery.addProperty("factId", "flight:123");
        request.setLlmQuery(llmQuery);

        String json = request.toJsonString();

        Console.log("_____llm_request_json_string_____", json);

        assertNotNull(json);
        assertTrue(json.contains("\"id\":\"llm-req-1\""));
        assertTrue(json.contains("\"createdAt\":\"2026-03-22T10:15:30Z\""));
        assertTrue(json.contains("\"updatedAt\":\"2026-03-22T10:15:31Z\""));
        assertTrue(json.contains("\"snapshotHash\":\"snap-123\""));
        assertTrue(json.contains("\"llmQuery\""));
        assertTrue(json.contains("\"queryKind\":\"WHY\""));
        assertTrue(json.contains("\"factId\":\"flight:123\""));
    }

    @Test
    public void shouldConvertLlmRequestToJsonObject() {

        LLMRequest request = new LLMRequest();
        request.setId("llm-req-2");
        request.setCreatedAt("2026-03-22T11:00:00Z");
        request.setUpdatedAt("2026-03-22T11:00:01Z");
        request.setSnapshotHash("snap-456");

        JsonObject llmQuery = new JsonObject();
        llmQuery.addProperty("queryKind", "WHAT");
        llmQuery.addProperty("factId", "airport:AUS");
        request.setLlmQuery(llmQuery);

        JsonObject json = request.toJson();

        Console.log("_____llm_request_json_object_____", json);

        assertNotNull(json);
        assertEquals("llm-req-2", json.get("id").getAsString());
        assertEquals("2026-03-22T11:00:00Z", json.get("createdAt").getAsString());
        assertEquals("2026-03-22T11:00:01Z", json.get("updatedAt").getAsString());
        assertEquals("snap-456", json.get("snapshotHash").getAsString());

        assertTrue(json.has("llmQuery"));
        assertEquals("WHAT", json.getAsJsonObject("llmQuery").get("queryKind").getAsString());
        assertEquals("airport:AUS", json.getAsJsonObject("llmQuery").get("factId").getAsString());
    }

    @Test
    public void shouldDeserializeLlmRequestFromJson() {

        String json =
                "{"
                        + "\"id\":\"llm-req-3\","
                        + "\"createdAt\":\"2026-03-22T12:00:00Z\","
                        + "\"updatedAt\":\"2026-03-22T12:00:05Z\","
                        + "\"snapshotHash\":\"snap-789\","
                        + "\"queryRequest\":null,"
                        + "\"llmQuery\":{"
                        +     "\"queryKind\":\"HOW\","
                        +     "\"factId\":\"pnr:999\""
                        + "}"
                        + "}";

        Console.log("_____llm_request_json_input_____", json);

        LLMRequest request = LLMRequest.fromJson(json, LLMRequest.class);

        Console.log("_____llm_request_deserialized_____", request.toJsonString());

        assertNotNull(request);
        assertEquals("llm-req-3", request.getId());
        assertEquals("2026-03-22T12:00:00Z", request.getCreatedAt());
        assertEquals("2026-03-22T12:00:05Z", request.getUpdatedAt());
        assertEquals("snap-789", request.getSnapshotHash());

        assertNotNull(request.getLlmQuery());
        assertEquals("HOW", request.getLlmQuery().get("queryKind").getAsString());
        assertEquals("pnr:999", request.getLlmQuery().get("factId").getAsString());
    }

    @Test
    public void shouldPreserveNullFieldsInJsonString() {

        LLMRequest request = new LLMRequest();
        request.setId("llm-req-4");
        request.setCreatedAt("2026-03-22T13:00:00Z");
        request.setUpdatedAt("2026-03-22T13:00:01Z");
        request.setSnapshotHash("snap-000");
        request.setQueryRequest(null);
        request.setLlmQuery(null);

        String json = request.toJsonString();

        Console.log("_____llm_request_null_json_____", json);

        assertNotNull(json);
        assertTrue(json.contains("\"queryRequest\":null"));
        assertTrue(json.contains("\"llmQuery\":null"));
    }

    @Test
    public void shouldThrowWhenFromJsonGetsNullJson() {

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                new org.junit.jupiter.api.function.Executable() {
                    @Override
                    public void execute() {
                        LLMRequest.fromJson(null, LLMRequest.class);
                    }
                }
        );

        Console.log("_____llm_request_null_json_exception_____", ex.getMessage());

        assertEquals("json cannot be null", ex.getMessage());
    }

    @Test
    public void shouldThrowWhenFromJsonGetsBlankJson() {

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                new org.junit.jupiter.api.function.Executable() {
                    @Override
                    public void execute() {
                        LLMRequest.fromJson("   ", LLMRequest.class);
                    }
                }
        );

        Console.log("_____llm_request_blank_json_exception_____", ex.getMessage());

        assertEquals("json cannot be blank", ex.getMessage());
    }

    @Test
    public void shouldThrowWhenFromJsonGetsNullType() {

        String json = "{\"id\":\"llm-req-5\"}";

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                new org.junit.jupiter.api.function.Executable() {
                    @Override
                    public void execute() {
                        LLMRequest.fromJson(json, null);
                    }
                }
        );

        Console.log("_____llm_request_null_type_exception_____", ex.getMessage());

        assertEquals("type cannot be null", ex.getMessage());
    }
}


