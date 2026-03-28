package ai.braineous.rag.prompt.cgo.querygen;

import ai.braineous.rag.prompt.cgo.api.ValidationResult;
import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import ai.braineous.rag.prompt.cgo.querygen.model.FieldDefinition;
import ai.braineous.rag.prompt.cgo.querygen.model.FieldGenerationResult;
import com.google.gson.JsonObject;

public class LlmTraceFieldGenerator implements FieldGenerator {

    public LlmTraceFieldGenerator() {
    }

    @Override
    public boolean supports(FieldDefinition fieldDefinition) {
        if (fieldDefinition == null) {
            return false;
        }
        if (fieldDefinition.getName() == null) {
            return false;
        }
        return "llm_trace".equals(fieldDefinition.getName());
    }

    @Override
    public FieldGenerationResult generate(FieldDefinition fieldDefinition, QueryRequest request) {
        JsonObject fieldValue = new JsonObject();
        fieldValue.addProperty("type", "llm_trace");
        fieldValue.addProperty("description", "Deterministic internal trace contract for LLM execution profiling.");

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
        fieldValue.add("schema", schema);

        ValidationResult validationResult =
                new ValidationResult(
                        true,
                        "field.llm_trace.ok",
                        "VALID",
                        "field_generation",
                        fieldDefinition.getName(),
                        null
                );

        return new FieldGenerationResult(fieldDefinition, fieldValue, validationResult);
    }
}
