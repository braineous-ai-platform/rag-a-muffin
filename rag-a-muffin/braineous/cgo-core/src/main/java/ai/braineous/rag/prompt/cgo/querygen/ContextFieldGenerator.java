package ai.braineous.rag.prompt.cgo.querygen;

import ai.braineous.rag.prompt.cgo.api.GraphContext;
import ai.braineous.rag.prompt.cgo.api.ValidationResult;
import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import ai.braineous.rag.prompt.cgo.querygen.model.FieldDefinition;
import ai.braineous.rag.prompt.cgo.querygen.model.FieldGenerationResult;
import com.google.gson.JsonObject;

public class ContextFieldGenerator implements FieldGenerator {

    public ContextFieldGenerator() {
    }

    @Override
    public boolean supports(FieldDefinition fieldDefinition) {
        if (fieldDefinition == null) {
            return false;
        }

        if (fieldDefinition.getName() == null) {
            return false;
        }

        return "context".equals(fieldDefinition.getName());
    }

    @Override
    public FieldGenerationResult generate(FieldDefinition fieldDefinition, QueryRequest request) {
        JsonObject fieldValue = buildContextObject(request);

        ValidationResult validationResult =
                new ValidationResult(
                        true,
                        "field.context.ok",
                        "VALID",
                        "field_generation",
                        fieldDefinition.getName(),
                        null
                );

        return new FieldGenerationResult(fieldDefinition, fieldValue, validationResult);
    }

    private JsonObject buildContextObject(QueryRequest request) {
        if (request == null) {
            return new JsonObject();
        }

        GraphContext context = request.getContext();

        if (context == null) {
            return new JsonObject();
        }

        return context.toJson();
    }
}