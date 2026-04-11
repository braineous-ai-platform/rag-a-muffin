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

public class OutputTemplateFieldGenerator implements FieldGenerator {

    public OutputTemplateFieldGenerator() {
    }

    @Override
    public boolean supports(FieldDefinition fieldDefinition) {
        if (fieldDefinition == null) {
            return false;
        }
        if (fieldDefinition.getName() == null) {
            return false;
        }
        return "output_template".equals(fieldDefinition.getName());
    }

    @Override
    public FieldGenerationResult generate(FieldDefinition fieldDefinition, QueryRequest request) {
        JsonObject fieldValue = new JsonObject();
        JsonObject result = new JsonObject();

        QueryTask queryTask = request.getTask();
        List<String> selectedFields = null;

        if (queryTask instanceof ValidateTask) {
            ValidateTask validateTask = (ValidateTask) queryTask;
            selectedFields = validateTask.getRequestedFields();
        }

        JsonArray orderedFields = toJsonArray(selectedFields);
        for (int i = 0; i < orderedFields.size(); i++) {
            if (!orderedFields.get(i).isJsonNull()) {
                result.addProperty(orderedFields.get(i).getAsString(), "");
            }
        }

        fieldValue.add("result", result);

        ValidationResult validationResult =
                new ValidationResult(
                        true,
                        "field.output_template.ok",
                        "VALID",
                        "field_generation",
                        fieldDefinition.getName(),
                        null
                );

        return new FieldGenerationResult(fieldDefinition, fieldValue, validationResult);
    }

    private JsonArray toJsonArray(List<String> items) {
        JsonArray array = new JsonArray();

        if (items == null) {
            return array;
        }

        for (String item : items) {
            if (item != null) {
                array.add(item);
            } else {
                array.add((String) null);
            }
        }

        return array;
    }
}
