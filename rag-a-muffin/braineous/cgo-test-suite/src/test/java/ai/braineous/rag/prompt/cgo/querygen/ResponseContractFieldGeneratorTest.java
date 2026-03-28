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

public class ResponseContractFieldGeneratorTest {

    @Test
    public void supports_shouldReturnTrue_forResponseContractField() {
        ResponseContractFieldGenerator generator = new ResponseContractFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("response_contract");

        boolean supported = generator.supports(fieldDefinition);

        Console.log("____responseContract.supported.true____", String.valueOf(supported));

        assertTrue(supported);
    }

    @Test
    public void supports_shouldReturnFalse_whenFieldDefinitionIsNull() {
        ResponseContractFieldGenerator generator = new ResponseContractFieldGenerator();

        boolean supported = generator.supports(null);

        Console.log("____responseContract.supported.nullFieldDefinition____", String.valueOf(supported));

        assertFalse(supported);
    }

    @Test
    public void supports_shouldReturnFalse_whenFieldNameIsNull() {
        ResponseContractFieldGenerator generator = new ResponseContractFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition(null);

        boolean supported = generator.supports(fieldDefinition);

        Console.log("____responseContract.supported.nullFieldName____", String.valueOf(supported));

        assertFalse(supported);
    }

    @Test
    public void supports_shouldReturnFalse_forNonResponseContractField() {
        ResponseContractFieldGenerator generator = new ResponseContractFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("llm_instructions");

        boolean supported = generator.supports(fieldDefinition);

        Console.log("____responseContract.supported.false____", String.valueOf(supported));

        assertFalse(supported);
    }

    @Test
    public void generate_shouldReturnDeterministicResponseContract_fromRequestedFields() {
        ResponseContractFieldGenerator generator = new ResponseContractFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("response_contract");
        QueryRequest request = buildRequest(
                Arrays.asList("ok", "code", "message", "anchorId"),
                Arrays.asList("Airport:AUS", "Airport:DFW")
        );

        FieldGenerationResult result = generator.generate(fieldDefinition, request);

        JsonObject expected = new JsonObject();
        expected.addProperty("type", "validation_result");
        expected.addProperty("description", "Deterministic response contract derived from selected fields.");

        JsonObject schema = new JsonObject();
        JsonObject resultJson = new JsonObject();
        JsonObject fields = new JsonObject();
        fields.addProperty("ok", "string");
        fields.addProperty("code", "string");
        fields.addProperty("message", "string");
        fields.addProperty("anchorId", "string");
        resultJson.add("fields", fields);
        schema.add("result", resultJson);
        expected.add("schema", schema);

        Console.log("____responseContract.generate.actual____", result.getFieldValue().toString());
        Console.log("____responseContract.generate.expected____", expected.toString());
        Console.log("____responseContract.generate.validation____", String.valueOf(result.getValidationResult()));

        assertNotNull(result);
        assertSame(fieldDefinition, result.getFieldDefinition());
        assertEquals(expected, result.getFieldValue());

        ValidationResult validationResult = result.getValidationResult();
        assertNotNull(validationResult);
        assertTrue(validationResult.isOk());
        assertEquals("field.response_contract.ok", validationResult.getCode());
        assertEquals("VALID", validationResult.getMessage());
        assertEquals("field_generation", validationResult.getStage());
        assertEquals("response_contract", validationResult.getAnchorId());
    }

    @Test
    public void generate_shouldReturnEmptyFieldsObject_whenRequestedFieldsAreEmpty() {
        ResponseContractFieldGenerator generator = new ResponseContractFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("response_contract");
        QueryRequest request = buildRequest(
                Collections.<String>emptyList(),
                Arrays.asList("Airport:AUS", "Airport:DFW")
        );

        FieldGenerationResult result = generator.generate(fieldDefinition, request);

        JsonObject expected = new JsonObject();
        expected.addProperty("type", "validation_result");
        expected.addProperty("description", "Deterministic response contract derived from selected fields.");

        JsonObject schema = new JsonObject();
        JsonObject resultJson = new JsonObject();
        JsonObject fields = new JsonObject();
        resultJson.add("fields", fields);
        schema.add("result", resultJson);
        expected.add("schema", schema);

        Console.log("____responseContract.emptyRequestedFields.actual____", result.getFieldValue().toString());
        Console.log("____responseContract.emptyRequestedFields.expected____", expected.toString());

        assertEquals(expected, result.getFieldValue());
    }

    @Test
    public void generate_shouldSkipNullRequestedFields() {
        ResponseContractFieldGenerator generator = new ResponseContractFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("response_contract");
        QueryRequest request = buildRequest(
                Arrays.asList("ok", null, "message"),
                Arrays.asList("Airport:AUS", "Airport:DFW")
        );

        FieldGenerationResult result = generator.generate(fieldDefinition, request);

        JsonObject actual = result.getFieldValue();

        Console.log("____responseContract.skipNullRequestedFields.actual____", actual.toString());

        JsonObject fields =
                actual.getAsJsonObject("schema")
                        .getAsJsonObject("result")
                        .getAsJsonObject("fields");

        assertTrue(fields.has("ok"));
        assertTrue(fields.has("message"));
        assertFalse(fields.has("null"));
        assertEquals(2, fields.entrySet().size());
        assertEquals("string", fields.get("ok").getAsString());
        assertEquals("string", fields.get("message").getAsString());
    }

    @Test
    public void generate_shouldReturnValidationResultAnchoredToResponseContractField() {
        ResponseContractFieldGenerator generator = new ResponseContractFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("response_contract");
        QueryRequest request = buildRequest(
                Arrays.asList("code"),
                Arrays.asList("Airport:AUS")
        );

        FieldGenerationResult result = generator.generate(fieldDefinition, request);

        ValidationResult validationResult = result.getValidationResult();

        Console.log("____responseContract.validationResult____", String.valueOf(validationResult));

        assertNotNull(validationResult);
        assertTrue(validationResult.isOk());
        assertEquals("field.response_contract.ok", validationResult.getCode());
        assertEquals("VALID", validationResult.getMessage());
        assertEquals("field_generation", validationResult.getStage());
        assertEquals("response_contract", validationResult.getAnchorId());
        assertNotNull(validationResult.getMetadata());
        assertTrue(validationResult.getMetadata().isEmpty());
    }


    //---------------------------------------------------

    private QueryRequest buildRequest(java.util.List<String> requestedFields,
                                      java.util.List<String> relatedFactIds) {
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
                relatedFactIds
        );

        return new QueryRequest(meta, context, task);
    }
}
