package ai.braineous.rag.prompt.cgo.querygen;

import ai.braineous.rag.prompt.cgo.api.GraphContext;
import ai.braineous.rag.prompt.cgo.api.Meta;
import ai.braineous.rag.prompt.cgo.api.ValidateTask;
import ai.braineous.rag.prompt.cgo.api.ValidationResult;
import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import ai.braineous.rag.prompt.cgo.querygen.model.FieldDefinition;
import ai.braineous.rag.prompt.cgo.querygen.model.FieldGenerationResult;
import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class MetaFieldGeneratorTest {

    @Test
    public void supports_shouldReturnTrue_forMetaField() {
        MetaFieldGenerator generator = new MetaFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("meta");

        boolean supported = generator.supports(fieldDefinition);

        Console.log("____meta.supported.true____", String.valueOf(supported));

        assertTrue(supported);
    }

    @Test
    public void supports_shouldReturnFalse_whenFieldDefinitionIsNull() {
        MetaFieldGenerator generator = new MetaFieldGenerator();

        boolean supported = generator.supports(null);

        Console.log("____meta.supported.nullFieldDefinition____", String.valueOf(supported));

        assertFalse(supported);
    }

    @Test
    public void supports_shouldReturnFalse_whenFieldNameIsNull() {
        MetaFieldGenerator generator = new MetaFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition(null);

        boolean supported = generator.supports(fieldDefinition);

        Console.log("____meta.supported.nullFieldName____", String.valueOf(supported));

        assertFalse(supported);
    }

    @Test
    public void supports_shouldReturnFalse_forNonMetaField() {
        MetaFieldGenerator generator = new MetaFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("task");

        boolean supported = generator.supports(fieldDefinition);

        Console.log("____meta.supported.false____", String.valueOf(supported));

        assertFalse(supported);
    }

    @Test
    public void generate_shouldReturnMetaBlock() {
        MetaFieldGenerator generator = new MetaFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("meta");
        QueryRequest request = buildRequest();

        FieldGenerationResult result = generator.generate(fieldDefinition, request);

        JsonObject expected = new JsonObject();
        expected.addProperty("version", "v1");
        expected.addProperty("queryKind", "validate_flight_airports");
        expected.addProperty("description", "Validate departure and arrival airport codes");

        Console.log("____meta.generate.actual____", result.getFieldValue().toString());
        Console.log("____meta.generate.expected____", expected.toString());
        Console.log("____meta.generate.validation____", String.valueOf(result.getValidationResult()));

        assertNotNull(result);
        assertSame(fieldDefinition, result.getFieldDefinition());
        assertEquals(expected, result.getFieldValue());

        ValidationResult validationResult = result.getValidationResult();
        assertNotNull(validationResult);
        assertTrue(validationResult.isOk());
        assertEquals("field.meta.ok", validationResult.getCode());
        assertEquals("VALID", validationResult.getMessage());
        assertEquals("field_generation", validationResult.getStage());
        assertEquals("meta", validationResult.getAnchorId());
        assertNotNull(validationResult.getMetadata());
        assertTrue(validationResult.getMetadata().isEmpty());
    }

    @Test
    public void generate_shouldReturnMetaFields_withExpectedValues() {
        MetaFieldGenerator generator = new MetaFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("meta");
        QueryRequest request = buildRequest();

        FieldGenerationResult result = generator.generate(fieldDefinition, request);

        JsonObject meta = result.getFieldValue();

        Console.log("____meta.version____", meta.get("version").getAsString());
        Console.log("____meta.queryKind____", meta.get("queryKind").getAsString());
        Console.log("____meta.description____", meta.get("description").getAsString());

        assertEquals("v1", meta.get("version").getAsString());
        assertEquals("validate_flight_airports", meta.get("queryKind").getAsString());
        assertEquals("Validate departure and arrival airport codes", meta.get("description").getAsString());
    }

    @Test
    public void generate_shouldReturnValidationResultAnchoredToMetaField() {
        MetaFieldGenerator generator = new MetaFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("meta");
        QueryRequest request = buildRequest();

        FieldGenerationResult result = generator.generate(fieldDefinition, request);

        ValidationResult validationResult = result.getValidationResult();

        Console.log("____meta.validationResult____", String.valueOf(validationResult));

        assertNotNull(validationResult);
        assertTrue(validationResult.isOk());
        assertEquals("field.meta.ok", validationResult.getCode());
        assertEquals("VALID", validationResult.getMessage());
        assertEquals("field_generation", validationResult.getStage());
        assertEquals("meta", validationResult.getAnchorId());
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