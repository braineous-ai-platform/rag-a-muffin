package ai.braineous.rag.prompt.cgo.querygen.model;

import ai.braineous.rag.prompt.cgo.api.ValidationResult;

public class FieldGenerationResult {

    private final FieldDefinition fieldDefinition;
    private final FieldValue fieldValue;
    private final ValidationResult validationResult;

    public FieldGenerationResult(FieldDefinition fieldDefinition,
                                 FieldValue fieldValue,
                                 ValidationResult validationResult) {
        this.fieldDefinition = fieldDefinition;
        this.fieldValue = fieldValue;
        this.validationResult = validationResult;
    }

    public FieldDefinition getFieldDefinition() {
        return fieldDefinition;
    }

    public FieldValue getFieldValue() {
        return fieldValue;
    }

    public ValidationResult getValidationResult() {
        return validationResult;
    }
}