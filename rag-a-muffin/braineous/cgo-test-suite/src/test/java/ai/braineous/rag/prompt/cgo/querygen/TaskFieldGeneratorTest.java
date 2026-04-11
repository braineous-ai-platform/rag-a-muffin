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
    public void generate_shouldReturnLatestTaskBlock() {
        TaskFieldGenerator generator = new TaskFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("task");
        QueryRequest request = buildRequest();

        FieldGenerationResult result = generator.generate(fieldDefinition, request);

        JsonObject expected = new JsonObject();

        JsonObject intent = new JsonObject();
        intent.addProperty("goal", "Validate departure and arrival airport codes");
        expected.add("intent", intent);

        expected.addProperty("factId", "Flight:F100");

        JsonArray relatedFactIds = new JsonArray();
        relatedFactIds.add("Airport:AUS");
        relatedFactIds.add("Airport:DFW");
        expected.add("relatedFactIds", relatedFactIds);

        JsonArray select = new JsonArray();
        select.add("ok");
        select.add("code");
        select.add("message");
        select.add("anchorId");
        expected.add("select", select);

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
        JsonObject intent = task.getAsJsonObject("intent");
        JsonArray select = task.getAsJsonArray("select");
        JsonArray relatedFactIds = task.getAsJsonArray("relatedFactIds");

        Console.log("____task.intent.goal____", intent.get("goal").getAsString());
        Console.log("____task.factId____", task.get("factId").getAsString());
        Console.log("____task.select.size____", String.valueOf(select.size()));
        Console.log("____task.select.0____", select.get(0).getAsString());
        Console.log("____task.select.1____", select.get(1).getAsString());
        Console.log("____task.select.2____", select.get(2).getAsString());
        Console.log("____task.select.3____", select.get(3).getAsString());
        Console.log("____task.relatedFactIds.size____", String.valueOf(relatedFactIds.size()));
        Console.log("____task.relatedFactIds.0____", relatedFactIds.get(0).getAsString());
        Console.log("____task.relatedFactIds.1____", relatedFactIds.get(1).getAsString());

        assertEquals("Validate departure and arrival airport codes", intent.get("goal").getAsString());
        assertEquals("Flight:F100", task.get("factId").getAsString());

        assertEquals(4, select.size());
        assertEquals("ok", select.get(0).getAsString());
        assertEquals("code", select.get(1).getAsString());
        assertEquals("message", select.get(2).getAsString());
        assertEquals("anchorId", select.get(3).getAsString());

        assertEquals(2, relatedFactIds.size());
        assertEquals("Airport:AUS", relatedFactIds.get(0).getAsString());
        assertEquals("Airport:DFW", relatedFactIds.get(1).getAsString());
    }

    @Test
    public void generate_shouldNotEmitConstraintsForCurrentPhase() {
        TaskFieldGenerator generator = new TaskFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("task");
        QueryRequest request = buildRequest();

        FieldGenerationResult result = generator.generate(fieldDefinition, request);

        JsonObject task = result.getFieldValue();

        Console.log("____task.noConstraints.actual____", task.toString());

        assertFalse(task.has("constraints"));
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
