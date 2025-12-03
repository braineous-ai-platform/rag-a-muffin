package ai.braineous.rag.prompt.cgo.prompt;

import ai.braineous.rag.prompt.cgo.prompt.LlmClient;
import com.google.gson.JsonObject;

/**
 * Simple test double for LlmClient.
 * Captures the last prompt and returns a fixed response string.
 */
public final class FakeLlmClient implements LlmClient {

    private JsonObject lastPrompt;
    private final String response;

    public FakeLlmClient(String response) {
        this.response = response;
    }

    @Override
    public String executePrompt(JsonObject prompt) {
        this.lastPrompt = prompt;
        return response;
    }

    public JsonObject getLastPrompt() {
        return lastPrompt;
    }
}
