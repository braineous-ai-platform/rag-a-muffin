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
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class OutputTemplateFieldGeneratorTest {

    @Test
    public void supports_shouldReturnTrue_forOutputTemplateField() {
        OutputTemplateFieldGenerator generator = new OutputTemplateFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("output_template");

        boolean supported = generator.supports(fieldDefinition);

        Console.log("____outputTemplate.supported.true____", String.valueOf(supported));

        assertTrue(supported);
    }

    @Test
    public void supports_shouldReturnFalse_whenFieldDefinitionIsNull() {
        OutputTemplateFieldGenerator generator = new OutputTemplateFieldGenerator();

        boolean supported = generator.supports(null);

        Console.log("____outputTemplate.supported.nullFieldDefinition____", String.valueOf(supported));

        assertFalse(supported);
    }

    @Test
    public void supports_shouldReturnFalse_whenFieldNameIsNull() {
        OutputTemplateFieldGenerator generator = new OutputTemplateFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition(null);

        boolean supported = generator.supports(fieldDefinition);

        Console.log("____outputTemplate.supported.nullFieldName____", String.valueOf(supported));

        assertFalse(supported);
    }

    @Test
    public void supports_shouldReturnFalse_forNonOutputTemplateField() {
        OutputTemplateFieldGenerator generator = new OutputTemplateFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("task");

        boolean supported = generator.supports(fieldDefinition);

        Console.log("____outputTemplate.supported.false____", String.valueOf(supported));

        assertFalse(supported);
    }

    @Test
    public void generate_shouldReturnOutputTemplateBlock() {
        OutputTemplateFieldGenerator generator = new OutputTemplateFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("output_template");
        QueryRequest request = buildRequest(
                Arrays.asList("ok", "code", "message", "anchorId")
        );

        FieldGenerationResult result = generator.generate(fieldDefinition, request);

        JsonObject expected = new JsonObject();
        JsonObject expectedResult = new JsonObject();
        expectedResult.addProperty("ok", "");
        expectedResult.addProperty("code", "");
        expectedResult.addProperty("message", "");
        expectedResult.addProperty("anchorId", "");
        expected.add("result", expectedResult);

        Console.log("____outputTemplate.generate.actual____", result.getFieldValue().toString());
        Console.log("____outputTemplate.generate.expected____", expected.toString());
        Console.log("____outputTemplate.generate.validation____", String.valueOf(result.getValidationResult()));

        assertNotNull(result);
        assertSame(fieldDefinition, result.getFieldDefinition());
        assertEquals(expected, result.getFieldValue());

        ValidationResult validationResult = result.getValidationResult();
        assertNotNull(validationResult);
        assertTrue(validationResult.isOk());
        assertEquals("field.output_template.ok", validationResult.getCode());
        assertEquals("VALID", validationResult.getMessage());
        assertEquals("field_generation", validationResult.getStage());
        assertEquals("output_template", validationResult.getAnchorId());
        assertNotNull(validationResult.getMetadata());
        assertTrue(validationResult.getMetadata().isEmpty());
    }

    @Test
    public void generate_shouldReturnOutputTemplateFields_withExpectedValues() {
        OutputTemplateFieldGenerator generator = new OutputTemplateFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("output_template");
        QueryRequest request = buildRequest(
                Arrays.asList("ok", "code", "message", "anchorId")
        );

        FieldGenerationResult result = generator.generate(fieldDefinition, request);

        JsonObject outputTemplate = result.getFieldValue();
        JsonObject resultObject = outputTemplate.getAsJsonObject("result");

        Console.log("____outputTemplate.result.size____", String.valueOf(resultObject.size()));
        Console.log("____outputTemplate.result.ok____", resultObject.get("ok").getAsString());
        Console.log("____outputTemplate.result.code____", resultObject.get("code").getAsString());
        Console.log("____outputTemplate.result.message____", resultObject.get("message").getAsString());
        Console.log("____outputTemplate.result.anchorId____", resultObject.get("anchorId").getAsString());

        assertEquals(4, resultObject.size());
        assertEquals("", resultObject.get("ok").getAsString());
        assertEquals("", resultObject.get("code").getAsString());
        assertEquals("", resultObject.get("message").getAsString());
        assertEquals("", resultObject.get("anchorId").getAsString());
    }

    @Test
    public void generate_shouldPreserveRequestedFieldOrder() {
        OutputTemplateFieldGenerator generator = new OutputTemplateFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("output_template");
        QueryRequest request = buildRequest(
                Arrays.asList("anchorId", "message", "code", "ok")
        );

        FieldGenerationResult result = generator.generate(fieldDefinition, request);

        JsonObject expected = new JsonObject();
        JsonObject expectedResult = new JsonObject();
        expectedResult.addProperty("anchorId", "");
        expectedResult.addProperty("message", "");
        expectedResult.addProperty("code", "");
        expectedResult.addProperty("ok", "");
        expected.add("result", expectedResult);

        Console.log("____outputTemplate.order.actual____", result.getFieldValue().toString());
        Console.log("____outputTemplate.order.expected____", expected.toString());

        assertEquals(expected, result.getFieldValue());
    }

    @Test
    public void generate_shouldReturnEmptyResult_whenRequestedFieldsAreNull() {
        OutputTemplateFieldGenerator generator = new OutputTemplateFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("output_template");
        QueryRequest request = buildRequest(null);

        FieldGenerationResult result = generator.generate(fieldDefinition, request);

        JsonObject outputTemplate = result.getFieldValue();
        JsonObject resultObject = outputTemplate.getAsJsonObject("result");

        Console.log("____outputTemplate.nullRequestedFields.actual____", outputTemplate.toString());

        assertNotNull(resultObject);
        assertEquals(0, resultObject.size());
    }

    @Test
    public void generate_shouldReturnEmptyResult_whenRequestedFieldsAreEmpty() {
        OutputTemplateFieldGenerator generator = new OutputTemplateFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("output_template");
        QueryRequest request = buildRequest(Collections.<String>emptyList());

        FieldGenerationResult result = generator.generate(fieldDefinition, request);

        JsonObject outputTemplate = result.getFieldValue();
        JsonObject resultObject = outputTemplate.getAsJsonObject("result");

        Console.log("____outputTemplate.emptyRequestedFields.actual____", outputTemplate.toString());

        assertNotNull(resultObject);
        assertEquals(0, resultObject.size());
    }

    @Test
    public void generate_shouldIgnoreNullRequestedFieldEntries() {
        OutputTemplateFieldGenerator generator = new OutputTemplateFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("output_template");
        QueryRequest request = buildRequest(
                Arrays.asList("ok", null, "code")
        );

        FieldGenerationResult result = generator.generate(fieldDefinition, request);

        JsonObject outputTemplate = result.getFieldValue();
        JsonObject resultObject = outputTemplate.getAsJsonObject("result");

        Console.log("____outputTemplate.nullItems.actual____", outputTemplate.toString());

        assertEquals(2, resultObject.size());
        assertEquals("", resultObject.get("ok").getAsString());
        assertEquals("", resultObject.get("code").getAsString());
        assertFalse(resultObject.has("null"));
    }

    @Test
    public void generate_shouldReturnValidationResultAnchoredToOutputTemplateField() {
        OutputTemplateFieldGenerator generator = new OutputTemplateFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("output_template");
        QueryRequest request = buildRequest(
                Arrays.asList("ok", "code")
        );

        FieldGenerationResult result = generator.generate(fieldDefinition, request);

        ValidationResult validationResult = result.getValidationResult();

        Console.log("____outputTemplate.validationResult____", String.valueOf(validationResult));

        assertNotNull(validationResult);
        assertTrue(validationResult.isOk());
        assertEquals("field.output_template.ok", validationResult.getCode());
        assertEquals("VALID", validationResult.getMessage());
        assertEquals("field_generation", validationResult.getStage());
        assertEquals("output_template", validationResult.getAnchorId());
        assertNotNull(validationResult.getMetadata());
        assertTrue(validationResult.getMetadata().isEmpty());
    }

    private QueryRequest buildRequest(java.util.List<String> requestedFields) {
        Meta meta = new Meta(
                "v1",
                "validate_flight_airports",
                "Validate departure and arrival airport codes"
        );

        GraphContext context = new GraphContext();

        ValidateTask task = new ValidateTask(
                "Validate departure and arrival airport codes",
                "Flight:F100",
                requestedFields,
                Arrays.asList("Airport:AUS", "Airport:DFW")
        );

        return new QueryRequest(meta, context, task);
    }
}