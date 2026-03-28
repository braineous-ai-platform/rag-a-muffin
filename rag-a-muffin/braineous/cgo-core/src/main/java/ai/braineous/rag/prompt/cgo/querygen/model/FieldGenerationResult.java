package ai.braineous.rag.prompt.cgo.querygen.model;

import ai.braineous.rag.prompt.cgo.api.ValidationResult;
import com.google.gson.JsonObject;

public class FieldGenerationResult {

    private final FieldDefinition fieldDefinition;
    private final JsonObject fieldValue;
    private final ValidationResult validationResult;

    public FieldGenerationResult(FieldDefinition fieldDefinition,
                                 JsonObject fieldValue,
                                 ValidationResult validationResult) {
        this.fieldDefinition = fieldDefinition;
        this.fieldValue = fieldValue;
        this.validationResult = validationResult;
    }

    public FieldDefinition getFieldDefinition() {
        return fieldDefinition;
    }

    public JsonObject getFieldValue() {
        return fieldValue;
    }

    public ValidationResult getValidationResult() {
        return validationResult;
    }
}