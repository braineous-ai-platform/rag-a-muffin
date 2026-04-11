package ai.braineous.rag.prompt.cgo.querygen;

import ai.braineous.rag.prompt.cgo.api.ValidateTask;
import ai.braineous.rag.prompt.cgo.api.ValidationResult;
import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import ai.braineous.rag.prompt.cgo.query.QueryTask;
import ai.braineous.rag.prompt.cgo.querygen.model.FieldDefinition;
import ai.braineous.rag.prompt.cgo.querygen.model.FieldGenerationResult;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

public class LlmInstructionsFieldGenerator implements FieldGenerator {

    public LlmInstructionsFieldGenerator() {
    }

    @Override
    public boolean supports(FieldDefinition fieldDefinition) {
        if (fieldDefinition == null) {
            return false;
        }
        if (fieldDefinition.getName() == null) {
            return false;
        }
        return "llm_instructions".equals(fieldDefinition.getName());
    }

    @Override
    public FieldGenerationResult generate(FieldDefinition fieldDefinition, QueryRequest request) {
        JsonObject fieldValue = new JsonObject();
        JsonArray instructions = new JsonArray();

        addStaticInstructions(instructions);
        addDynamicResultBindings(instructions, request);

        fieldValue.add("instructions", instructions);

        ValidationResult validationResult =
                new ValidationResult(
                        true,
                        "field.llm_instructions.ok",
                        "VALID",
                        "field_generation",
                        fieldDefinition.getName(),
                        null
                );

        return new FieldGenerationResult(fieldDefinition, fieldValue, validationResult);
    }

    private void addStaticInstructions(JsonArray instructions) {
        instructions.add("Return ONLY the output_template with values filled.");
        instructions.add("Use runtime_result as truth.");
        instructions.add("Do NOT recompute validation from context.");
        instructions.add("Do not evaluate constraints from scratch.");
        instructions.add("Return compact JSON on a single line.");
        instructions.add("Do not include spaces, tabs, or newlines outside JSON syntax.");
        instructions.add("Set every value as a string.");
        instructions.add("Return exactly the output_template shape.");
        instructions.add("Do not add, remove, or rename any fields.");
        instructions.add("Return exactly one JSON object.");
        instructions.add("Do not wrap the JSON in markdown fences.");
        instructions.add("Do not include explanation before or after the JSON.");
    }

    private void addDynamicResultBindings(JsonArray instructions, QueryRequest request) {
        List<String> requestedFields = getRequestedFields(request);

        if (requestedFields == null) {
            return;
        }

        for (int i = 0; i < requestedFields.size(); i++) {
            String fieldName = requestedFields.get(i);

            if (fieldName == null) {
                continue;
            }

            instructions.add("Set result." + fieldName + " from runtime_result." + fieldName + ".");
        }
    }

    private List<String> getRequestedFields(QueryRequest request) {
        if (request == null) {
            return null;
        }

        QueryTask queryTask = request.getTask();
        if (!(queryTask instanceof ValidateTask)) {
            return null;
        }

        ValidateTask validateTask = (ValidateTask) queryTask;
        return validateTask.getRequestedFields();
    }
}