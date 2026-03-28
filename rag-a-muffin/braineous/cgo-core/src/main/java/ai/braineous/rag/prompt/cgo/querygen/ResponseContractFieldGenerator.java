package ai.braineous.rag.prompt.cgo.querygen;

import ai.braineous.rag.prompt.cgo.api.ValidateTask;
import ai.braineous.rag.prompt.cgo.api.ValidationResult;
import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import ai.braineous.rag.prompt.cgo.querygen.model.FieldDefinition;
import ai.braineous.rag.prompt.cgo.querygen.model.FieldGenerationResult;
import com.google.gson.JsonObject;

/**
 * {
 *   "type": "validation_result",
 *   "description": "Deterministic response contract derived from selected fields.",
 *   "schema": {
 *     "result": {
 *       "fields": {
 *         "ok": "string",
 *         "code": "string",
 *         "message": "string",
 *         "anchorId": "string"
 *       }
 *     }
 *   }
 * }
 */
public class ResponseContractFieldGenerator implements FieldGenerator {

    public ResponseContractFieldGenerator() {
    }

    @Override
    public boolean supports(FieldDefinition fieldDefinition) {
        if (fieldDefinition == null) {
            return false;
        }
        if (fieldDefinition.getName() == null) {
            return false;
        }
        return "response_contract".equals(fieldDefinition.getName());
    }

    @Override
    public FieldGenerationResult generate(FieldDefinition fieldDefinition, QueryRequest request) {
        ValidateTask task = (ValidateTask) request.getTask();

        JsonObject responseContract = new JsonObject();
        responseContract.addProperty("type", "validation_result");
        responseContract.addProperty("description", "Deterministic response contract derived from selected fields.");

        JsonObject schema = new JsonObject();
        JsonObject result = new JsonObject();
        JsonObject fields = new JsonObject();

        if (task != null && task.getRequestedFields() != null) {
            for (String requestedField : task.getRequestedFields()) {
                if (requestedField != null) {
                    fields.addProperty(requestedField, "string");
                }
            }
        }

        result.add("fields", fields);
        schema.add("result", result);
        responseContract.add("schema", schema);

        ValidationResult validationResult =
                new ValidationResult(
                        true,
                        "field.response_contract.ok",
                        "VALID",
                        "field_generation",
                        fieldDefinition.getName(),
                        null
                );

        return new FieldGenerationResult(fieldDefinition, responseContract, validationResult);
    }
}