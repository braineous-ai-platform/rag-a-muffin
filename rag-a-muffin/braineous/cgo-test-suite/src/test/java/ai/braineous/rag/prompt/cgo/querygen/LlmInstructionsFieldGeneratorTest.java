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
        FieldDefinition fieldDefinition = new FieldDefinition("llm_trace_instructions");

        boolean supported = generator.supports(fieldDefinition);

        Console.log("____llmInstructions.supported.false____", String.valueOf(supported));

        assertFalse(supported);
    }

    @Test
    public void generate_shouldReturnInstructionBlock() {
        LlmInstructionsFieldGenerator generator = new LlmInstructionsFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("llm_instructions");
        QueryRequest request = buildRequest();

        FieldGenerationResult result = generator.generate(fieldDefinition, request);

        JsonObject expected = new JsonObject();
        JsonArray instructions = new JsonArray();
        instructions.add("Return ONLY the output_template with values filled.");
        instructions.add("Use runtime_result as truth.");
        instructions.add("Do NOT recompute validation from context.");
        instructions.add("Do not infer control values from context.");
        instructions.add("Return compact JSON on a single line.");
        instructions.add("Do not include spaces, tabs, or newlines outside JSON syntax.");
        instructions.add("Set every value as a string.");
        instructions.add("Return exactly the output_template shape.");
        instructions.add("Do not add, remove, or rename any fields.");
        instructions.add("Return exactly one JSON object.");
        instructions.add("Do not wrap the JSON in markdown fences.");
        instructions.add("Do not include explanation before or after the JSON.");
        instructions.add("Set result.ok from runtime_result.ok.");
        instructions.add("Set result.code from runtime_result.code.");
        instructions.add("Set result.message from runtime_result.message.");
        instructions.add("Set result.anchorId from runtime_result.anchorId.");
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
        Console.log("____llmInstructions.instructions.10____", instructions.get(10).getAsString());
        Console.log("____llmInstructions.instructions.11____", instructions.get(11).getAsString());
        Console.log("____llmInstructions.instructions.12____", instructions.get(12).getAsString());
        Console.log("____llmInstructions.instructions.13____", instructions.get(13).getAsString());
        Console.log("____llmInstructions.instructions.14____", instructions.get(14).getAsString());
        Console.log("____llmInstructions.instructions.15____", instructions.get(15).getAsString());

        assertEquals(16, instructions.size());
        assertEquals("Return ONLY the output_template with values filled.", instructions.get(0).getAsString());
        assertEquals("Use runtime_result as truth.", instructions.get(1).getAsString());
        assertEquals("Do NOT recompute validation from context.", instructions.get(2).getAsString());
        assertEquals("Do not infer control values from context.", instructions.get(3).getAsString());
        assertEquals("Return compact JSON on a single line.", instructions.get(4).getAsString());
        assertEquals("Do not include spaces, tabs, or newlines outside JSON syntax.", instructions.get(5).getAsString());
        assertEquals("Set every value as a string.", instructions.get(6).getAsString());
        assertEquals("Return exactly the output_template shape.", instructions.get(7).getAsString());
        assertEquals("Do not add, remove, or rename any fields.", instructions.get(8).getAsString());
        assertEquals("Return exactly one JSON object.", instructions.get(9).getAsString());
        assertEquals("Do not wrap the JSON in markdown fences.", instructions.get(10).getAsString());
        assertEquals("Do not include explanation before or after the JSON.", instructions.get(11).getAsString());
        assertEquals("Set result.ok from runtime_result.ok.", instructions.get(12).getAsString());
        assertEquals("Set result.code from runtime_result.code.", instructions.get(13).getAsString());
        assertEquals("Set result.message from runtime_result.message.", instructions.get(14).getAsString());
        assertEquals("Set result.anchorId from runtime_result.anchorId.", instructions.get(15).getAsString());
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