package ai.braineous.rag.prompt.cgo.api;

import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import com.google.gson.JsonObject;

public abstract class LlmAdapter {

    private JsonObject config;

    public LlmAdapter() {
    }

    public LlmAdapter(JsonObject config) {
        this.config = config;
    }

    public JsonObject getConfig() {
        return config;
    }

    public void setConfig(JsonObject config) {
        this.config = config;
    }

    public abstract String invokeLlm(QueryRequest queryRequest, JsonObject prompt);

    @Override
    public String toString() {
        return "LlmAdapter{" +
                "config=" + config +
                '}';
    }
}
