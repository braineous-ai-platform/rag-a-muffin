package ai.braineous.rag.prompt.cgo.prompt;

import ai.braineous.rag.prompt.cgo.api.ValidationResult;
import com.google.gson.JsonObject;

public class PromptRequestOutput {

    private JsonObject requestOutput;

    private ValidationResult validationResult;

    public PromptRequestOutput() {
    }

    public PromptRequestOutput(JsonObject requestOutput) {
        this.requestOutput = requestOutput;
    }

    public PromptRequestOutput(JsonObject requestOutput, ValidationResult validationResult) {
        this.requestOutput = requestOutput;
        this.validationResult = validationResult;
    }

    public JsonObject getRequestOutput() {
        return requestOutput;
    }

    public void setRequestOutput(JsonObject requestOutput) {
        this.requestOutput = requestOutput;
    }

    public ValidationResult getValidationResult() {
        return validationResult;
    }

    public void setValidationResult(ValidationResult validationResult) {
        this.validationResult = validationResult;
    }

    @Override
    public String toString() {
        return "PromptRequestOutput{" +
                "requestOutput=" + requestOutput +
                ", validationResult=" + validationResult +
                '}';
    }
}
