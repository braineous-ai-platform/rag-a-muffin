package ai.braineous.rag.prompt.cgo.querygen.model;

import com.google.gson.JsonObject;

public class AssemblyResult {

    private final JsonObject result;

    public AssemblyResult(JsonObject result) {
        this.result = result;
    }

    public JsonObject getResult() {
        return result;
    }
}
