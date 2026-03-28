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

public class LlmInstructionsFieldGeneratorTest {

    @Test
    public void supports_shouldReturnTrue_forLlmInstructionsField() {
        LlmInstructionsFieldGenerator generator = new LlmInstructionsFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("llm_instructions");

        boolean supported = generator.supports(fieldDefinition);

        Console.log("____llmInstructions.supported.true____", String.valueOf(supported));

        assertTrue(supported);
    }

    @Test
    public void supports_shouldReturnFalse_whenFieldDefinitionIsNull() {
        LlmInstructionsFieldGenerator generator = new LlmInstructionsFieldGenerator();

        boolean supported = generator.supports(null);

        Console.log("____llmInstructions.supported.nullFieldDefinition____", String.valueOf(supported));

        assertFalse(supported);
    }

    @Test
    public void supports_shouldReturnFalse_whenFieldNameIsNull() {
        LlmInstructionsFieldGenerator generator = new LlmInstructionsFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition(null);

        boolean supported = generator.supports(fieldDefinition);

        Console.log("____llmInstructions.supported.nullFieldName____", String.valueOf(supported));

        assertFalse(supported);
    }

    @Test
    public void supports_shouldReturnFalse_forNonLlmInstructionsField() {
        LlmInstructionsFieldGenerator generator = new LlmInstructionsFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("response_contract");

        boolean supported = generator.supports(fieldDefinition);

        Console.log("____llmInstructions.supported.false____", String.valueOf(supported));

        assertFalse(supported);
    }

    @Test
    public void generate_shouldReturnStaticInstructionBlock() {
        LlmInstructionsFieldGenerator generator = new LlmInstructionsFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("llm_instructions");
        QueryRequest request = buildRequest();

        FieldGenerationResult result = generator.generate(fieldDefinition, request);

        JsonObject expected = new JsonObject();
        JsonArray instructions = new JsonArray();
        instructions.add("Return exactly one JSON object.");
        instructions.add("Use response_contract.schema.result.fields to construct the result object.");
        instructions.add("Place all generated result values under the result field.");
        instructions.add("Do not modify the response_contract section.");
        instructions.add("Do not add any fields not defined in response_contract.schema.result.fields.");
        instructions.add("Do not remove any fields defined in response_contract.schema.result.fields.");
        instructions.add("Do not rename any fields.");
        instructions.add("Set every returned field value as a string.");
        instructions.add("If a value cannot be determined, return an empty string for that field.");
        instructions.add("Do not include natural language outside the JSON object.");
        expected.add("instructions", instructions);

        Console.log("____llmInstructions.generate.actual____", result.getFieldValue().toString());
        Console.log("____llmInstructions.generate.expected____", expected.toString());
        Console.log("____llmInstructions.generate.validation____", String.valueOf(result.getValidationResult()));

        assertNotNull(result);
        assertSame(fieldDefinition, result.getFieldDefinition());
        assertEquals(expected, result.getFieldValue());

        ValidationResult validationResult = result.getValidationResult();
        assertNotNull(validationResult);
        assertTrue(validationResult.isOk());
        assertEquals("field.llm_instructions.ok", validationResult.getCode());
        assertEquals("VALID", validationResult.getMessage());
        assertEquals("field_generation", validationResult.getStage());
        assertEquals("llm_instructions", validationResult.getAnchorId());
        assertNotNull(validationResult.getMetadata());
        assertTrue(validationResult.getMetadata().isEmpty());
    }

    @Test
    public void generate_shouldReturnInstructionsArray_withExpectedOrder() {
        LlmInstructionsFieldGenerator generator = new LlmInstructionsFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("llm_instructions");
        QueryRequest request = buildRequest();

        FieldGenerationResult result = generator.generate(fieldDefinition, request);

        JsonArray instructions = result.getFieldValue().getAsJsonArray("instructions");

        Console.log("____llmInstructions.instructions.size____", String.valueOf(instructions.size()));
        Console.log("____llmInstructions.instructions.0____", instructions.get(0).getAsString());
        Console.log("____llmInstructions.instructions.1____", instructions.get(1).getAsString());
        Console.log("____llmInstructions.instructions.2____", instructions.get(2).getAsString());
        Console.log("____llmInstructions.instructions.3____", instructions.get(3).getAsString());
        Console.log("____llmInstructions.instructions.4____", instructions.get(4).getAsString());
        Console.log("____llmInstructions.instructions.5____", instructions.get(5).getAsString());
        Console.log("____llmInstructions.instructions.6____", instructions.get(6).getAsString());
        Console.log("____llmInstructions.instructions.7____", instructions.get(7).getAsString());
        Console.log("____llmInstructions.instructions.8____", instructions.get(8).getAsString());
        Console.log("____llmInstructions.instructions.9____", instructions.get(9).getAsString());

        assertEquals(10, instructions.size());
        assertEquals("Return exactly one JSON object.", instructions.get(0).getAsString());
        assertEquals("Use response_contract.schema.result.fields to construct the result object.", instructions.get(1).getAsString());
        assertEquals("Place all generated result values under the result field.", instructions.get(2).getAsString());
        assertEquals("Do not modify the response_contract section.", instructions.get(3).getAsString());
        assertEquals("Do not add any fields not defined in response_contract.schema.result.fields.", instructions.get(4).getAsString());
        assertEquals("Do not remove any fields defined in response_contract.schema.result.fields.", instructions.get(5).getAsString());
        assertEquals("Do not rename any fields.", instructions.get(6).getAsString());
        assertEquals("Set every returned field value as a string.", instructions.get(7).getAsString());
        assertEquals("If a value cannot be determined, return an empty string for that field.", instructions.get(8).getAsString());
        assertEquals("Do not include natural language outside the JSON object.", instructions.get(9).getAsString());
    }

    @Test
    public void generate_shouldReturnValidationResultAnchoredToLlmInstructionsField() {
        LlmInstructionsFieldGenerator generator = new LlmInstructionsFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("llm_instructions");
        QueryRequest request = buildRequest();

        FieldGenerationResult result = generator.generate(fieldDefinition, request);

        ValidationResult validationResult = result.getValidationResult();

        Console.log("____llmInstructions.validationResult____", String.valueOf(validationResult));

        assertNotNull(validationResult);
        assertTrue(validationResult.isOk());
        assertEquals("field.llm_instructions.ok", validationResult.getCode());
        assertEquals("VALID", validationResult.getMessage());
        assertEquals("field_generation", validationResult.getStage());
        assertEquals("llm_instructions", validationResult.getAnchorId());
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