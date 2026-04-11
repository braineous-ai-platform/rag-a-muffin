package ai.braineous.rag.prompt.cgo.querygen;

import ai.braineous.rag.prompt.cgo.api.Control;
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
import java.util.List;

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
        QueryRequest request = buildRequestWithControls();

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

        JsonObject controls = new JsonObject();
        controls.addProperty("promo_mode", "spring_campaign");
        controls.addProperty("message_style", "brief");
        expected.add("controls", controls);

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
        QueryRequest request = buildRequestWithControls();

        FieldGenerationResult result = generator.generate(fieldDefinition, request);

        JsonObject task = result.getFieldValue();
        JsonObject intent = task.getAsJsonObject("intent");
        JsonArray select = task.getAsJsonArray("select");
        JsonArray relatedFactIds = task.getAsJsonArray("relatedFactIds");
        JsonObject controls = task.getAsJsonObject("controls");

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
        Console.log("____task.controls.promo_mode____", controls.get("promo_mode").getAsString());
        Console.log("____task.controls.message_style____", controls.get("message_style").getAsString());

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

        assertEquals("spring_campaign", controls.get("promo_mode").getAsString());
        assertEquals("brief", controls.get("message_style").getAsString());
    }

    @Test
    public void generate_shouldNotEmitConstraintsForCurrentPhase() {
        TaskFieldGenerator generator = new TaskFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("task");
        QueryRequest request = buildRequestWithControls();

        FieldGenerationResult result = generator.generate(fieldDefinition, request);

        JsonObject task = result.getFieldValue();

        Console.log("____task.noConstraints.actual____", task.toString());

        assertFalse(task.has("constraints"));
    }

    @Test
    public void generate_shouldReturnValidationResultAnchoredToTaskField() {
        TaskFieldGenerator generator = new TaskFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("task");
        QueryRequest request = buildRequestWithControls();

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

    @Test
    public void generate_shouldReturnEmptyControls_whenControlsIsNull() {
        TaskFieldGenerator generator = new TaskFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("task");
        QueryRequest request = buildRequestWithNullControls();

        FieldGenerationResult result = generator.generate(fieldDefinition, request);

        JsonObject task = result.getFieldValue();
        JsonObject controls = task.getAsJsonObject("controls");

        Console.log("____task.controls.null.actual____", task.toString());
        Console.log("____task.controls.null.size____", String.valueOf(controls.size()));

        assertNotNull(controls);
        assertEquals(0, controls.size());
        assertTrue(task.has("intent"));
        assertTrue(task.has("factId"));
        assertTrue(task.has("relatedFactIds"));
        assertTrue(task.has("select"));
    }

    @Test
    public void generate_shouldPreserveControlOrder() {
        TaskFieldGenerator generator = new TaskFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("task");
        QueryRequest request = buildRequestWithControls();

        FieldGenerationResult result = generator.generate(fieldDefinition, request);

        JsonObject task = result.getFieldValue();
        String actual = task.toString();

        Console.log("____task.controls.order.actual____", actual);

        assertTrue(actual.indexOf("\"promo_mode\"") < actual.indexOf("\"message_style\""));
    }

    @Test
    public void generate_shouldIgnoreNullKeys_andAllowNullValues() {
        TaskFieldGenerator generator = new TaskFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("task");
        QueryRequest request = buildRequestWithNullKeyAndNullValueControls();

        FieldGenerationResult result = generator.generate(fieldDefinition, request);

        JsonObject task = result.getFieldValue();
        JsonObject controls = task.getAsJsonObject("controls");

        Console.log("____task.controls.nullKeyNullValue.actual____", task.toString());
        Console.log("____task.controls.has.message_style____", String.valueOf(controls.has("message_style")));
        Console.log("____task.controls.message_style.isJsonNull____", String.valueOf(controls.get("message_style").isJsonNull()));

        assertFalse(controls.has("ignored_null_key"));
        assertTrue(controls.has("message_style"));
        assertTrue(controls.get("message_style").isJsonNull());
        assertEquals("spring_campaign", controls.get("promo_mode").getAsString());
    }

    private QueryRequest buildRequestWithControls() {
        Meta meta = new Meta(
                "v1",
                "validate_flight_airports",
                "Validate departure and arrival airport codes"
        );

        GraphContext context = new GraphContext();

        List<Control> controls = Arrays.asList(
                new Control("promo_mode", "spring_campaign"),
                new Control("message_style", "brief")
        );

        ValidateTask task = new ValidateTask(
                "Validate departure and arrival airport codes",
                "Flight:F100",
                Arrays.asList("ok", "code", "message", "anchorId"),
                Arrays.asList("Airport:AUS", "Airport:DFW")
        );
        task.setControls(controls);

        return new QueryRequest(meta, context, task);
    }

    private QueryRequest buildRequestWithNullControls() {
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
        task.setControls(null);

        return new QueryRequest(meta, context, task);
    }

    private QueryRequest buildRequestWithNullKeyAndNullValueControls() {
        Meta meta = new Meta(
                "v1",
                "validate_flight_airports",
                "Validate departure and arrival airport codes"
        );

        GraphContext context = new GraphContext();

        List<Control> controls = Arrays.asList(
                new Control("promo_mode", "spring_campaign"),
                new Control(null, "ignored_value"),
                new Control("message_style", null)
        );

        ValidateTask task = new ValidateTask(
                "Validate departure and arrival airport codes",
                "Flight:F100",
                Arrays.asList("ok", "code", "message", "anchorId"),
                Arrays.asList("Airport:AUS", "Airport:DFW")
        );
        task.setControls(controls);

        return new QueryRequest(meta, context, task);
    }
}