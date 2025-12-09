package ai.braineous.cgo.llm;

import ai.braineous.rag.prompt.cgo.prompt.LlmClient;
import com.google.gson.JsonObject;

public class LlmClientOrchestrator implements LlmClient {
    private final LlmAdapter adapter;

    public LlmClientOrchestrator() {
        this.adapter = new OpenAILlmAdapter();
    }

    public LlmClientOrchestrator(LlmAdapter adapter) {
        this.adapter = adapter;
    }

    /**
     * Execute the given prompt JSON and return the raw response as a String.
     * Response parsing/mapping will be handled in a later phase.
     *
     * @param prompt
     */
    @Override
    public String executePrompt(JsonObject prompt) {
        if(prompt == null){
            return "";
        }

        return this.adapter.invokeLlm(prompt);
    }
}
