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

public class LlmTraceInstructionsFieldGeneratorTest {

    @Test
    public void supports_shouldReturnTrue_forLlmTraceInstructionsField() {
        LlmTraceInstructionsFieldGenerator generator = new LlmTraceInstructionsFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("llm_trace_instructions");

        boolean supported = generator.supports(fieldDefinition);

        Console.log("____llmTraceInstructions.supported.true____", String.valueOf(supported));

        assertTrue(supported);
    }

    @Test
    public void supports_shouldReturnFalse_whenFieldDefinitionIsNull() {
        LlmTraceInstructionsFieldGenerator generator = new LlmTraceInstructionsFieldGenerator();

        boolean supported = generator.supports(null);

        Console.log("____llmTraceInstructions.supported.nullFieldDefinition____", String.valueOf(supported));

        assertFalse(supported);
    }

    @Test
    public void supports_shouldReturnFalse_whenFieldNameIsNull() {
        LlmTraceInstructionsFieldGenerator generator = new LlmTraceInstructionsFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition(null);

        boolean supported = generator.supports(fieldDefinition);

        Console.log("____llmTraceInstructions.supported.nullFieldName____", String.valueOf(supported));

        assertFalse(supported);
    }

    @Test
    public void supports_shouldReturnFalse_forNonLlmTraceInstructionsField() {
        LlmTraceInstructionsFieldGenerator generator = new LlmTraceInstructionsFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("llm_trace");

        boolean supported = generator.supports(fieldDefinition);

        Console.log("____llmTraceInstructions.supported.false____", String.valueOf(supported));

        assertFalse(supported);
    }

    @Test
    public void generate_shouldReturnStaticInstructionBlock() {
        LlmTraceInstructionsFieldGenerator generator = new LlmTraceInstructionsFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("llm_trace_instructions");
        QueryRequest request = buildRequest();

        FieldGenerationResult result = generator.generate(fieldDefinition, request);

        JsonObject expected = new JsonObject();
        JsonArray instructions = new JsonArray();
        instructions.add("Use llm_trace.schema.trace.fields to construct the trace object.");
        instructions.add("Place all generated trace values under the trace field.");
        instructions.add("Do not modify the llm_trace section.");
        instructions.add("Do not add any trace fields not defined in llm_trace.schema.trace.fields.");
        instructions.add("Do not remove any trace fields defined in llm_trace.schema.trace.fields.");
        instructions.add("Do not rename any trace fields.");
        instructions.add("Set every trace field value as a string.");
        instructions.add("If a trace value cannot be determined, return an empty string for that field.");
        expected.add("instructions", instructions);

        Console.log("____llmTraceInstructions.generate.actual____", result.getFieldValue().toString());
        Console.log("____llmTraceInstructions.generate.expected____", expected.toString());
        Console.log("____llmTraceInstructions.generate.validation____", String.valueOf(result.getValidationResult()));

        assertNotNull(result);
        assertSame(fieldDefinition, result.getFieldDefinition());
        assertEquals(expected, result.getFieldValue());

        ValidationResult validationResult = result.getValidationResult();
        assertNotNull(validationResult);
        assertTrue(validationResult.isOk());
        assertEquals("field.llm_trace_instructions.ok", validationResult.getCode());
        assertEquals("VALID", validationResult.getMessage());
        assertEquals("field_generation", validationResult.getStage());
        assertEquals("llm_trace_instructions", validationResult.getAnchorId());
        assertNotNull(validationResult.getMetadata());
        assertTrue(validationResult.getMetadata().isEmpty());
    }

    @Test
    public void generate_shouldReturnInstructionsArray_withExpectedOrder() {
        LlmTraceInstructionsFieldGenerator generator = new LlmTraceInstructionsFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("llm_trace_instructions");
        QueryRequest request = buildRequest();

        FieldGenerationResult result = generator.generate(fieldDefinition, request);

        JsonArray instructions = result.getFieldValue().getAsJsonArray("instructions");

        Console.log("____llmTraceInstructions.instructions.size____", String.valueOf(instructions.size()));
        Console.log("____llmTraceInstructions.instructions.0____", instructions.get(0).getAsString());
        Console.log("____llmTraceInstructions.instructions.1____", instructions.get(1).getAsString());
        Console.log("____llmTraceInstructions.instructions.2____", instructions.get(2).getAsString());
        Console.log("____llmTraceInstructions.instructions.3____", instructions.get(3).getAsString());
        Console.log("____llmTraceInstructions.instructions.4____", instructions.get(4).getAsString());
        Console.log("____llmTraceInstructions.instructions.5____", instructions.get(5).getAsString());
        Console.log("____llmTraceInstructions.instructions.6____", instructions.get(6).getAsString());
        Console.log("____llmTraceInstructions.instructions.7____", instructions.get(7).getAsString());

        assertEquals(8, instructions.size());
        assertEquals("Use llm_trace.schema.trace.fields to construct the trace object.", instructions.get(0).getAsString());
        assertEquals("Place all generated trace values under the trace field.", instructions.get(1).getAsString());
        assertEquals("Do not modify the llm_trace section.", instructions.get(2).getAsString());
        assertEquals("Do not add any trace fields not defined in llm_trace.schema.trace.fields.", instructions.get(3).getAsString());
        assertEquals("Do not remove any trace fields defined in llm_trace.schema.trace.fields.", instructions.get(4).getAsString());
        assertEquals("Do not rename any trace fields.", instructions.get(5).getAsString());
        assertEquals("Set every trace field value as a string.", instructions.get(6).getAsString());
        assertEquals("If a trace value cannot be determined, return an empty string for that field.", instructions.get(7).getAsString());
    }

    @Test
    public void generate_shouldReturnValidationResultAnchoredToLlmTraceInstructionsField() {
        LlmTraceInstructionsFieldGenerator generator = new LlmTraceInstructionsFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("llm_trace_instructions");
        QueryRequest request = buildRequest();

        FieldGenerationResult result = generator.generate(fieldDefinition, request);

        ValidationResult validationResult = result.getValidationResult();

        Console.log("____llmTraceInstructions.validationResult____", String.valueOf(validationResult));

        assertNotNull(validationResult);
        assertTrue(validationResult.isOk());
        assertEquals("field.llm_trace_instructions.ok", validationResult.getCode());
        assertEquals("VALID", validationResult.getMessage());
        assertEquals("field_generation", validationResult.getStage());
        assertEquals("llm_trace_instructions", validationResult.getAnchorId());
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
