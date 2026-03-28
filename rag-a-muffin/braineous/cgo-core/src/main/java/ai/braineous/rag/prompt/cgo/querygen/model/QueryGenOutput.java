package ai.braineous.rag.prompt.cgo.querygen.model;

import ai.braineous.rag.prompt.cgo.api.ValidationResult;
import com.google.gson.JsonObject;

public class QueryGenOutput {

    private final JsonObject payload;
    private final ValidationResult validationResult;

    public QueryGenOutput(JsonObject payload, ValidationResult validationResult) {
        this.payload = payload;
        this.validationResult = validationResult;
    }

    public JsonObject getPayload() {
        return payload;
    }

    public ValidationResult getValidationResult() {
        return validationResult;
    }
}