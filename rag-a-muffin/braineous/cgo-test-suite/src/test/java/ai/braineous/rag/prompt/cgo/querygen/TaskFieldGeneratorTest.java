package ai.braineous.rag.prompt.cgo.querygen;

import ai.braineous.rag.prompt.cgo.api.GraphContext;
import ai.braineous.rag.prompt.cgo.api.Meta;
import ai.braineous.rag.prompt.cgo.api.ValidateTask;
import ai.braineous.rag.prompt.cgo.api.ValidationResult;
import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import ai.braineous.rag.prompt.cgo.querygen.model.FieldDefinition;
import ai.braineous.rag.prompt.cgo.querygen.model.FieldGenerationResult;
import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class TaskFieldGeneratorTest {

    @Test
    public void supports_shouldReturnTrue_forTaskField() {
        TaskFieldGenerator generator = new TaskFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("task");

        boolean supported = generator.supports(fieldDefinition);

        Console.log("____task.supported.true____", String.valueOf(supported));

        assertTrue(supported);
    }

    @Test
    public void supports_shouldReturnFalse_whenFieldDefinitionIsNull() {
        TaskFieldGenerator generator = new TaskFieldGenerator();

        boolean supported = generator.supports(null);

        Console.log("____task.supported.nullFieldDefinition____", String.valueOf(supported));

        assertFalse(supported);
    }

    @Test
    public void supports_shouldReturnFalse_whenFieldNameIsNull() {
        TaskFieldGenerator generator = new TaskFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition(null);

        boolean supported = generator.supports(fieldDefinition);

        Console.log("____task.supported.nullFieldName____", String.valueOf(supported));

        assertFalse(supported);
    }

    @Test
    public void supports_shouldReturnFalse_forNonTaskField() {
        TaskFieldGenerator generator = new TaskFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("meta");

        boolean supported = generator.supports(fieldDefinition);

        Console.log("____task.supported.false____", String.valueOf(supported));

        assertFalse(supported);
    }

    @Test
    public void generate_shouldReturnTaskBlock() {
        TaskFieldGenerator generator = new TaskFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("task");
        QueryRequest request = buildRequest();

        FieldGenerationResult result = generator.generate(fieldDefinition, request);

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

        Console.log("____task.generate.actual____", result.getFieldValue().toString());
        Console.log("____task.generate.expected____", expected.toString());
        Console.log("____task.generate.validation____", String.valueOf(result.getValidationResult()));

        assertNotNull(result);
        assertSame(fieldDefinition, result.getFieldDefinition());
        assertEquals(expected, result.getFieldValue());

        ValidationResult validationResult = result.getValidationResult();
        assertNotNull(validationResult);
        assertTrue(validationResult.isOk());
        assertEquals("field.task.ok", validationResult.getCode());
        assertEquals("VALID", validationResult.getMessage());
        assertEquals("field_generation", validationResult.getStage());
        assertEquals("task", validationResult.getAnchorId());
        assertNotNull(validationResult.getMetadata());
        assertTrue(validationResult.getMetadata().isEmpty());
    }

    @Test
    public void generate_shouldReturnTaskFields_withExpectedValues() {
        TaskFieldGenerator generator = new TaskFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("task");
        QueryRequest request = buildRequest();

        FieldGenerationResult result = generator.generate(fieldDefinition, request);

        JsonObject task = result.getFieldValue();
        JsonArray requestedFields = task.getAsJsonArray("requestedFields");
        JsonArray relatedFactIds = task.getAsJsonArray("relatedFactIds");

        Console.log("____task.description____", task.get("description").getAsString());
        Console.log("____task.factId____", task.get("factId").getAsString());
        Console.log("____task.requestedFields.size____", String.valueOf(requestedFields.size()));
        Console.log("____task.requestedFields.0____", requestedFields.get(0).getAsString());
        Console.log("____task.requestedFields.1____", requestedFields.get(1).getAsString());
        Console.log("____task.requestedFields.2____", requestedFields.get(2).getAsString());
        Console.log("____task.requestedFields.3____", requestedFields.get(3).getAsString());
        Console.log("____task.relatedFactIds.size____", String.valueOf(relatedFactIds.size()));
        Console.log("____task.relatedFactIds.0____", relatedFactIds.get(0).getAsString());
        Console.log("____task.relatedFactIds.1____", relatedFactIds.get(1).getAsString());

        assertEquals("Validate departure and arrival airport codes", task.get("description").getAsString());
        assertEquals("Flight:F100", task.get("factId").getAsString());

        assertEquals(4, requestedFields.size());
        assertEquals("ok", requestedFields.get(0).getAsString());
        assertEquals("code", requestedFields.get(1).getAsString());
        assertEquals("message", requestedFields.get(2).getAsString());
        assertEquals("anchorId", requestedFields.get(3).getAsString());

        assertEquals(2, relatedFactIds.size());
        assertEquals("Airport:AUS", relatedFactIds.get(0).getAsString());
        assertEquals("Airport:DFW", relatedFactIds.get(1).getAsString());
    }

    @Test
    public void generate_shouldReturnValidationResultAnchoredToTaskField() {
        TaskFieldGenerator generator = new TaskFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("task");
        QueryRequest request = buildRequest();

        FieldGenerationResult result = generator.generate(fieldDefinition, request);

        ValidationResult validationResult = result.getValidationResult();

        Console.log("____task.validationResult____", String.valueOf(validationResult));

        assertNotNull(validationResult);
        assertTrue(validationResult.isOk());
        assertEquals("field.task.ok", validationResult.getCode());
        assertEquals("VALID", validationResult.getMessage());
        assertEquals("field_generation", validationResult.getStage());
        assertEquals("task", validationResult.getAnchorId());
        assertNotNull(validationResult.getMetadata());
        assertTrue(validationResult.getMetadata().isEmpty());
    }

    private QueryRequest buildRequest() {
        Meta meta = new Meta(
                "v1",
                "validate_flight_airports",
                "Validate departure and arrival airport codes"
        );

        GraphContext context = new GraphContext();

        ValidateTask task = new ValidateTask(
                "Validate departure and arrival airport codes",
                "Flight:F100",
                Arrays.asList("ok", "code", "message", "anchorId"),
                Arrays.asList("Airport:AUS", "Airport:DFW")
        );

        return new QueryRequest(meta, context, task);
    }
}
