package ai.braineous.rag.prompt.cgo.prompt;

import com.google.gson.JsonObject;

public class PromptRequestOutput {

    private JsonObject requestOutput;

    public PromptRequestOutput() {
    }

    public PromptRequestOutput(JsonObject requestOutput) {
        this.requestOutput = requestOutput;
    }

    public JsonObject getRequestOutput() {
        return requestOutput;
    }

    public void setRequestOutput(JsonObject requestOutput) {
        this.requestOutput = requestOutput;
    }

    @Override
    public String toString() {
        return "PromptRequestOutput{" +
                "requestOutput=" + requestOutput +
                '}';
    }
}
