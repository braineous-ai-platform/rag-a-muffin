package ai.braineous.rag.prompt.cgo.querygen;

import ai.braineous.rag.prompt.cgo.api.ValidateTask;
import ai.braineous.rag.prompt.cgo.api.ValidationResult;
import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import ai.braineous.rag.prompt.cgo.query.QueryTask;
import ai.braineous.rag.prompt.cgo.querygen.model.FieldDefinition;
import ai.braineous.rag.prompt.cgo.querygen.model.FieldGenerationResult;
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

        List<String> selectedFields = getSelectedFields(request);

        addSelectedFields(result, selectedFields);

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

    private List<String> getSelectedFields(QueryRequest request) {
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

    private void addSelectedFields(JsonObject result, List<String> selectedFields) {
        if (selectedFields == null) {
            return;
        }

        int i = 0;
        while (i < selectedFields.size()) {
            String fieldName = selectedFields.get(i);

            if (fieldName != null) {
                result.addProperty(fieldName, "");
            }

            i++;
        }
    }
}