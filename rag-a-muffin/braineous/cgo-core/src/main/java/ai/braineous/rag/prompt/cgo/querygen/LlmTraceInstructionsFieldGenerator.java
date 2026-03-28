package ai.braineous.rag.prompt.cgo.querygen;

import ai.braineous.rag.prompt.cgo.api.ValidationResult;
import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import ai.braineous.rag.prompt.cgo.querygen.model.FieldDefinition;
import ai.braineous.rag.prompt.cgo.querygen.model.FieldGenerationResult;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class LlmTraceInstructionsFieldGenerator implements FieldGenerator {

    public LlmTraceInstructionsFieldGenerator() {
    }

    @Override
    public boolean supports(FieldDefinition fieldDefinition) {
        if (fieldDefinition == null) {
            return false;
        }
        if (fieldDefinition.getName() == null) {
            return false;
        }
        return "llm_trace_instructions".equals(fieldDefinition.getName());
    }

    @Override
    public FieldGenerationResult generate(FieldDefinition fieldDefinition, QueryRequest request) {
        JsonObject fieldValue = new JsonObject();
        JsonArray instructions = new JsonArray();

        instructions.add("Use llm_trace.schema.trace.fields to construct the trace object.");
        instructions.add("Place all generated trace values under the trace field.");
        instructions.add("Do not modify the llm_trace section.");
        instructions.add("Do not add any trace fields not defined in llm_trace.schema.trace.fields.");
        instructions.add("Do not remove any trace fields defined in llm_trace.schema.trace.fields.");
        instructions.add("Do not rename any trace fields.");
        instructions.add("Set every trace field value as a string.");
        instructions.add("If a trace value cannot be determined, return an empty string for that field.");

        fieldValue.add("instructions", instructions);

        ValidationResult validationResult =
                new ValidationResult(
                        true,
                        "field.llm_trace_instructions.ok",
                        "VALID",
                        "field_generation",
                        fieldDefinition.getName(),
                        null
                );

        return new FieldGenerationResult(fieldDefinition, fieldValue, validationResult);
    }
}
