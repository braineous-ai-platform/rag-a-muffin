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

public class LlmTraceFieldGeneratorTest {

    @Test
    public void supports_shouldReturnTrue_forLlmTraceField() {
        LlmTraceFieldGenerator generator = new LlmTraceFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("llm_trace");

        boolean supported = generator.supports(fieldDefinition);

        Console.log("____llmTrace.supported.true____", String.valueOf(supported));

        assertTrue(supported);
    }

    @Test
    public void supports_shouldReturnFalse_whenFieldDefinitionIsNull() {
        LlmTraceFieldGenerator generator = new LlmTraceFieldGenerator();

        boolean supported = generator.supports(null);

        Console.log("____llmTrace.supported.nullFieldDefinition____", String.valueOf(supported));

        assertFalse(supported);
    }

    @Test
    public void supports_shouldReturnFalse_whenFieldNameIsNull() {
        LlmTraceFieldGenerator generator = new LlmTraceFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition(null);

        boolean supported = generator.supports(fieldDefinition);

        Console.log("____llmTrace.supported.nullFieldName____", String.valueOf(supported));

        assertFalse(supported);
    }

    @Test
    public void supports_shouldReturnFalse_forNonLlmTraceField() {
        LlmTraceFieldGenerator generator = new LlmTraceFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("response_contract");

        boolean supported = generator.supports(fieldDefinition);

        Console.log("____llmTrace.supported.false____", String.valueOf(supported));

        assertFalse(supported);
    }

    @Test
    public void generate_shouldReturnDeterministicLlmTraceContract() {
        LlmTraceFieldGenerator generator = new LlmTraceFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("llm_trace");
        QueryRequest request = buildRequest();

        FieldGenerationResult result = generator.generate(fieldDefinition, request);

        JsonObject expected = new JsonObject();
        expected.addProperty("type", "llm_trace");
        expected.addProperty("description", "Deterministic internal trace contract for LLM execution profiling.");

        JsonObject schema = new JsonObject();
        JsonObject trace = new JsonObject();
        JsonObject fields = new JsonObject();

        fields.addProperty("finish_reason", "string");
        fields.addProperty("prompt_tokens", "string");
        fields.addProperty("response_tokens", "string");
        fields.addProperty("total_tokens", "string");
        fields.addProperty("latency_ms", "string");
        fields.addProperty("model_fingerprint", "string");

        trace.add("fields", fields);
        schema.add("trace", trace);
        expected.add("schema", schema);

        Console.log("____llmTrace.generate.actual____", result.getFieldValue().toString());
        Console.log("____llmTrace.generate.expected____", expected.toString());
        Console.log("____llmTrace.generate.validation____", String.valueOf(result.getValidationResult()));

        assertNotNull(result);
        assertSame(fieldDefinition, result.getFieldDefinition());
        assertEquals(expected, result.getFieldValue());

        ValidationResult validationResult = result.getValidationResult();
        assertNotNull(validationResult);
        assertTrue(validationResult.isOk());
        assertEquals("field.llm_trace.ok", validationResult.getCode());
        assertEquals("VALID", validationResult.getMessage());
        assertEquals("field_generation", validationResult.getStage());
        assertEquals("llm_trace", validationResult.getAnchorId());
        assertNotNull(validationResult.getMetadata());
        assertTrue(validationResult.getMetadata().isEmpty());
    }

    @Test
    public void generate_shouldContainExpectedTraceFields() {
        LlmTraceFieldGenerator generator = new LlmTraceFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("llm_trace");
        QueryRequest request = buildRequest();

        FieldGenerationResult result = generator.generate(fieldDefinition, request);

        JsonObject fields = result.getFieldValue()
                .getAsJsonObject("schema")
                .getAsJsonObject("trace")
                .getAsJsonObject("fields");

        Console.log("____llmTrace.fields____", fields.toString());

        assertEquals(6, fields.entrySet().size());
        assertEquals("string", fields.get("finish_reason").getAsString());
        assertEquals("string", fields.get("prompt_tokens").getAsString());
        assertEquals("string", fields.get("response_tokens").getAsString());
        assertEquals("string", fields.get("total_tokens").getAsString());
        assertEquals("string", fields.get("latency_ms").getAsString());
        assertEquals("string", fields.get("model_fingerprint").getAsString());
    }

    @Test
    public void generate_shouldReturnValidationResultAnchoredToLlmTraceField() {
        LlmTraceFieldGenerator generator = new LlmTraceFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("llm_trace");
        QueryRequest request = buildRequest();

        FieldGenerationResult result = generator.generate(fieldDefinition, request);

        ValidationResult validationResult = result.getValidationResult();

        Console.log("____llmTrace.validationResult____", String.valueOf(validationResult));

        assertNotNull(validationResult);
        assertTrue(validationResult.isOk());
        assertEquals("field.llm_trace.ok", validationResult.getCode());
        assertEquals("VALID", validationResult.getMessage());
        assertEquals("field_generation", validationResult.getStage());
        assertEquals("llm_trace", validationResult.getAnchorId());
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
