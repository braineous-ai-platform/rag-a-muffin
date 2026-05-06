package ai.braineous.rag.prompt.cgo.querygen;

import ai.braineous.rag.prompt.cgo.api.ValidateTask;
import ai.braineous.rag.prompt.cgo.api.ValidationResult;
import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import ai.braineous.rag.prompt.cgo.query.QueryTask;
import ai.braineous.rag.prompt.cgo.querygen.model.FieldDefinition;
import ai.braineous.rag.prompt.cgo.querygen.model.FieldGenerationResult;
import com.google.gson.JsonObject;

import java.util.List;

/**
 * {
 *   "type": "execution_result",
 *   "description": "Deterministic execution response contract derived from selected fields.",
 *   "schema": {
 *     "result": {
 *       "fields": {
 *         "decision": "string",
 *         "reason": "string",
 *         "code": "string"
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
        JsonObject responseContract = new JsonObject();

        responseContract.addProperty("type", "execution_result");
        responseContract.addProperty(
                "description",
                "Deterministic execution response contract derived from selected fields."
        );

        JsonObject schema = new JsonObject();
        JsonObject result = new JsonObject();
        JsonObject fields = new JsonObject();

        List<String> requestedFields = getRequestedFields(request);

        if (requestedFields != null) {
            int i = 0;
            while (i < requestedFields.size()) {
                String requestedField = requestedFields.get(i);

                if (requestedField != null) {
                    fields.addProperty(requestedField, "string");
                }

                i++;
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

    private List<String> getRequestedFields(QueryRequest request) {
        if (request == null) {
            return null;
        }

        QueryTask queryTask = request.getTask();

        if (!(queryTask instanceof ValidateTask)) {
            return null;
        }

        ValidateTask task = (ValidateTask) queryTask;

        return task.getRequestedFields();
    }
}