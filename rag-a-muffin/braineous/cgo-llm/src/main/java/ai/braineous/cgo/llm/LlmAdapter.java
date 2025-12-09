package ai.braineous.cgo.llm;

import com.google.gson.JsonObject;

public interface LlmAdapter {

    public String invokeLlm(JsonObject prompt);
}
