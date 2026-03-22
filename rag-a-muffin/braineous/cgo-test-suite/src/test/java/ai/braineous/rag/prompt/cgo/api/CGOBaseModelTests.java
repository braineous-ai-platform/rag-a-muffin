package ai.braineous.rag.prompt.cgo.api;

import ai.braineous.rag.prompt.cgo.api.CGOBaseModel;
import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CGOBaseModelTests {

    @Test
    public void shouldSerializeBaseModelToJsonString() {

        CGOBaseModel model = new CGOBaseModel();
        model.setId("base-1");
        model.setCreatedAt("2026-03-22T15:00:00Z");
        model.setUpdatedAt("2026-03-22T15:00:01Z");
        model.setSnapshotHash("snap-base-123");

        String json = model.toJsonString();

        Console.log("_____cgo_base_model_json_string_____", json);

        assertNotNull(json);
        assertTrue(json.contains("\"id\":\"base-1\""));
        assertTrue(json.contains("\"createdAt\":\"2026-03-22T15:00:00Z\""));
        assertTrue(json.contains("\"updatedAt\":\"2026-03-22T15:00:01Z\""));
        assertTrue(json.contains("\"snapshotHash\":\"snap-base-123\""));
    }

    @Test
    public void shouldConvertBaseModelToJsonObject() {

        CGOBaseModel model = new CGOBaseModel();
        model.setId("base-2");
        model.setCreatedAt("2026-03-22T15:05:00Z");
        model.setUpdatedAt("2026-03-22T15:05:01Z");
        model.setSnapshotHash("snap-base-456");

        JsonObject json = model.toJson();

        Console.log("_____cgo_base_model_json_object_____", json);

        assertNotNull(json);
        assertEquals("base-2", json.get("id").getAsString());
        assertEquals("2026-03-22T15:05:00Z", json.get("createdAt").getAsString());
        assertEquals("2026-03-22T15:05:01Z", json.get("updatedAt").getAsString());
        assertEquals("snap-base-456", json.get("snapshotHash").getAsString());
    }

    @Test
    public void shouldDeserializeBaseModelFromJson() {

        String json =
                "{"
                        + "\"id\":\"base-3\","
                        + "\"createdAt\":\"2026-03-22T15:10:00Z\","
                        + "\"updatedAt\":\"2026-03-22T15:10:05Z\","
                        + "\"snapshotHash\":\"snap-base-789\""
                        + "}";

        Console.log("_____cgo_base_model_json_input_____", json);

        CGOBaseModel model = CGOBaseModel.fromJson(json, CGOBaseModel.class);

        Console.log("_____cgo_base_model_deserialized_____", model.toJsonString());

        assertNotNull(model);
        assertEquals("base-3", model.getId());
        assertEquals("2026-03-22T15:10:00Z", model.getCreatedAt());
        assertEquals("2026-03-22T15:10:05Z", model.getUpdatedAt());
        assertEquals("snap-base-789", model.getSnapshotHash());
    }

    @Test
    public void shouldPreserveNullFieldsInJsonString() {

        CGOBaseModel model = new CGOBaseModel();
        model.setId("base-4");
        model.setCreatedAt(null);
        model.setUpdatedAt(null);
        model.setSnapshotHash(null);

        String json = model.toJsonString();

        Console.log("_____cgo_base_model_null_json_____", json);

        assertNotNull(json);
        assertTrue(json.contains("\"id\":\"base-4\""));
        assertTrue(json.contains("\"createdAt\":null"));
        assertTrue(json.contains("\"updatedAt\":null"));
        assertTrue(json.contains("\"snapshotHash\":null"));
    }

    @Test
    public void shouldThrowWhenFromJsonGetsNullJson() {

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                new org.junit.jupiter.api.function.Executable() {
                    @Override
                    public void execute() {
                        CGOBaseModel.fromJson(null, CGOBaseModel.class);
                    }
                }
        );

        Console.log("_____cgo_base_model_null_json_exception_____", ex.getMessage());

        assertEquals("json cannot be null", ex.getMessage());
    }

    @Test
    public void shouldThrowWhenFromJsonGetsBlankJson() {

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                new org.junit.jupiter.api.function.Executable() {
                    @Override
                    public void execute() {
                        CGOBaseModel.fromJson("   ", CGOBaseModel.class);
                    }
                }
        );

        Console.log("_____cgo_base_model_blank_json_exception_____", ex.getMessage());

        assertEquals("json cannot be blank", ex.getMessage());
    }

    @Test
    public void shouldThrowWhenFromJsonGetsNullType() {

        String json = "{\"id\":\"base-5\"}";

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                new org.junit.jupiter.api.function.Executable() {
                    @Override
                    public void execute() {
                        CGOBaseModel.fromJson(json, null);
                    }
                }
        );

        Console.log("_____cgo_base_model_null_type_exception_____", ex.getMessage());

        assertEquals("type cannot be null", ex.getMessage());
    }
}
