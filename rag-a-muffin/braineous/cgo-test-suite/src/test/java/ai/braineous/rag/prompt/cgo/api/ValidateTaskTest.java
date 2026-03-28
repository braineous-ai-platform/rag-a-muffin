package ai.braineous.rag.prompt.cgo.api;

import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class ValidateTaskTest {

    @Test
    public void shouldExposeConstructorFields() {
        ValidateTask task = new ValidateTask(
                "Validate departure and arrival airport codes",
                "Flight:F100",
                Arrays.asList("ok", "code", "message", "anchorId"),
                Arrays.asList("Airport:AUS", "Airport:DFW")
        );

        Console.log("____validateTask.description____", task.getDescription());
        Console.log("____validateTask.factId____", task.getFactId());
        Console.log("____validateTask.requestedFields____", String.valueOf(task.getRequestedFields()));
        Console.log("____validateTask.relatedFactIds____", String.valueOf(task.getRelatedFactIds()));

        assertEquals("Validate departure and arrival airport codes", task.getDescription());
        assertEquals("Flight:F100", task.getFactId());
        assertEquals(4, task.getRequestedFields().size());
        assertEquals("ok", task.getRequestedFields().get(0));
        assertEquals("code", task.getRequestedFields().get(1));
        assertEquals("message", task.getRequestedFields().get(2));
        assertEquals("anchorId", task.getRequestedFields().get(3));
        assertEquals(2, task.getRelatedFactIds().size());
        assertEquals("Airport:AUS", task.getRelatedFactIds().get(0));
        assertEquals("Airport:DFW", task.getRelatedFactIds().get(1));
    }

    @Test
    public void toJson_shouldSerializeAllFields() {
        ValidateTask task = new ValidateTask(
                "Validate departure and arrival airport codes",
                "Flight:F100",
                Arrays.asList("ok", "code", "message", "anchorId"),
                Arrays.asList("Airport:AUS", "Airport:DFW")
        );

        JsonObject actual = task.toJson();

        JsonObject expected = new JsonObject();
        expected.addProperty("description", "Validate departure and arrival airport codes");
        expected.addProperty("factId", "Flight:F100");

        JsonArray requestedFields = new JsonArray();
        requestedFields.add("ok");
        requestedFields.add("code");
        requestedFields.add("message");
        requestedFields.add("anchorId");
        expected.add("requestedFields", requestedFields);

        JsonArray relatedFactIds = new JsonArray();
        relatedFactIds.add("Airport:AUS");
        relatedFactIds.add("Airport:DFW");
        expected.add("relatedFactIds", relatedFactIds);

        Console.log("____validateTask.toJson.actual____", actual.toString());
        Console.log("____validateTask.toJson.expected____", expected.toString());

        assertEquals(expected, actual);
    }

    @Test
    public void toJson_shouldWriteNullDescriptionAndFactId() {
        ValidateTask task = new ValidateTask(
                null,
                null,
                Collections.singletonList("code"),
                Collections.singletonList("Airport:AUS")
        );

        JsonObject actual = task.toJson();

        Console.log("____validateTask.nulls.actual____", actual.toString());

        assertTrue(actual.has("description"));
        assertTrue(actual.get("description").isJsonNull());
        assertTrue(actual.has("factId"));
        assertTrue(actual.get("factId").isJsonNull());

        assertTrue(actual.has("requestedFields"));
        assertEquals(1, actual.getAsJsonArray("requestedFields").size());
        assertEquals("code", actual.getAsJsonArray("requestedFields").get(0).getAsString());

        assertTrue(actual.has("relatedFactIds"));
        assertEquals(1, actual.getAsJsonArray("relatedFactIds").size());
        assertEquals("Airport:AUS", actual.getAsJsonArray("relatedFactIds").get(0).getAsString());
    }

    @Test
    public void toJson_shouldWriteEmptyArraysWhenListsAreNull() {
        ValidateTask task = new ValidateTask(
                "Validate",
                "Flight:F100",
                null,
                null
        );

        JsonObject actual = task.toJson();

        Console.log("____validateTask.nullLists.actual____", actual.toString());

        assertTrue(actual.has("requestedFields"));
        assertTrue(actual.get("requestedFields").isJsonArray());
        assertEquals(0, actual.getAsJsonArray("requestedFields").size());

        assertTrue(actual.has("relatedFactIds"));
        assertTrue(actual.get("relatedFactIds").isJsonArray());
        assertEquals(0, actual.getAsJsonArray("relatedFactIds").size());
    }

    @Test
    public void toJsonString_shouldMatchToJsonStringRepresentation() {
        ValidateTask task = new ValidateTask(
                "Validate departure and arrival airport codes",
                "Flight:F100",
                Arrays.asList("ok", "code"),
                Arrays.asList("Airport:AUS")
        );

        String actual = task.toJsonString();
        String expected = task.toJson().toString();

        Console.log("____validateTask.toJsonString.actual____", actual);
        Console.log("____validateTask.toJsonString.expected____", expected);

        assertEquals(expected, actual);
    }

    @Test
    public void fromJson_shouldReconstructValidateTask() {
        JsonObject json = new JsonObject();
        json.addProperty("description", "Validate departure and arrival airport codes");
        json.addProperty("factId", "Flight:F100");

        JsonArray requestedFields = new JsonArray();
        requestedFields.add("ok");
        requestedFields.add("code");
        requestedFields.add("message");
        requestedFields.add("anchorId");
        json.add("requestedFields", requestedFields);

        JsonArray relatedFactIds = new JsonArray();
        relatedFactIds.add("Airport:AUS");
        relatedFactIds.add("Airport:DFW");
        json.add("relatedFactIds", relatedFactIds);

        ValidateTask task = ValidateTask.fromJson(json);

        Console.log("____validateTask.fromJson.description____", task.getDescription());
        Console.log("____validateTask.fromJson.factId____", task.getFactId());
        Console.log("____validateTask.fromJson.requestedFields____", String.valueOf(task.getRequestedFields()));
        Console.log("____validateTask.fromJson.relatedFactIds____", String.valueOf(task.getRelatedFactIds()));

        assertNotNull(task);
        assertEquals("Validate departure and arrival airport codes", task.getDescription());
        assertEquals("Flight:F100", task.getFactId());
        assertEquals(4, task.getRequestedFields().size());
        assertEquals("ok", task.getRequestedFields().get(0));
        assertEquals("code", task.getRequestedFields().get(1));
        assertEquals("message", task.getRequestedFields().get(2));
        assertEquals("anchorId", task.getRequestedFields().get(3));
        assertEquals(2, task.getRelatedFactIds().size());
        assertEquals("Airport:AUS", task.getRelatedFactIds().get(0));
        assertEquals("Airport:DFW", task.getRelatedFactIds().get(1));
    }

    @Test
    public void fromJson_shouldReturnNullWhenInputIsNull() {
        ValidateTask task = ValidateTask.fromJson(null);

        Console.log("____validateTask.fromJson.null____", String.valueOf(task));

        assertNull(task);
    }

    @Test
    public void fromJson_shouldReturnEmptyListsWhenArraysAbsent() {
        JsonObject json = new JsonObject();
        json.addProperty("description", "Validate departure and arrival airport codes");
        json.addProperty("factId", "Flight:F100");

        ValidateTask task = ValidateTask.fromJson(json);

        Console.log("____validateTask.fromJson.noArrays.description____", task.getDescription());
        Console.log("____validateTask.fromJson.noArrays.factId____", task.getFactId());
        Console.log("____validateTask.fromJson.noArrays.requestedFields____", String.valueOf(task.getRequestedFields()));
        Console.log("____validateTask.fromJson.noArrays.relatedFactIds____", String.valueOf(task.getRelatedFactIds()));

        assertNotNull(task);
        assertEquals("Validate departure and arrival airport codes", task.getDescription());
        assertEquals("Flight:F100", task.getFactId());
        assertNotNull(task.getRequestedFields());
        assertTrue(task.getRequestedFields().isEmpty());
        assertNotNull(task.getRelatedFactIds());
        assertTrue(task.getRelatedFactIds().isEmpty());
    }

    @Test
    public void fromJson_shouldPreserveNullItemsInsideArrays() {
        JsonObject json = new JsonObject();
        json.addProperty("description", "Validate departure and arrival airport codes");
        json.addProperty("factId", "Flight:F100");

        JsonArray requestedFields = new JsonArray();
        requestedFields.add("ok");
        requestedFields.add((String) null);
        requestedFields.add("message");
        json.add("requestedFields", requestedFields);

        JsonArray relatedFactIds = new JsonArray();
        relatedFactIds.add("Airport:AUS");
        relatedFactIds.add((String) null);
        relatedFactIds.add("Airport:DFW");
        json.add("relatedFactIds", relatedFactIds);

        ValidateTask task = ValidateTask.fromJson(json);

        Console.log("____validateTask.fromJson.nullItems.requestedFields____", String.valueOf(task.getRequestedFields()));
        Console.log("____validateTask.fromJson.nullItems.relatedFactIds____", String.valueOf(task.getRelatedFactIds()));

        assertEquals(3, task.getRequestedFields().size());
        assertEquals("ok", task.getRequestedFields().get(0));
        assertNull(task.getRequestedFields().get(1));
        assertEquals("message", task.getRequestedFields().get(2));

        assertEquals(3, task.getRelatedFactIds().size());
        assertEquals("Airport:AUS", task.getRelatedFactIds().get(0));
        assertNull(task.getRelatedFactIds().get(1));
        assertEquals("Airport:DFW", task.getRelatedFactIds().get(2));
    }

    @Test
    public void toString_shouldIncludeNewFields() {
        ValidateTask task = new ValidateTask(
                "Validate departure and arrival airport codes",
                "Flight:F100",
                Arrays.asList("ok", "code"),
                Arrays.asList("Airport:AUS", "Airport:DFW")
        );

        String actual = task.toString();

        Console.log("____validateTask.toString____", actual);

        assertTrue(actual.contains("description='Validate departure and arrival airport codes'"));
        assertTrue(actual.contains("factId='Flight:F100'"));
        assertTrue(actual.contains("requestedFields=[ok, code]"));
        assertTrue(actual.contains("relatedFactIds=[Airport:AUS, Airport:DFW]"));
    }
}