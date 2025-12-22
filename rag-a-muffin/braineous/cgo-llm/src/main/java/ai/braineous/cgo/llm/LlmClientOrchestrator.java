package ai.braineous.cgo.llm;

import ai.braineous.rag.prompt.cgo.api.LlmAdapter;
import ai.braineous.rag.prompt.cgo.prompt.LlmClient;
import com.google.gson.JsonObject;

public class LlmClientOrchestrator implements LlmClient {

    public LlmClientOrchestrator() {
    }

    /**
     * Execute the given prompt JSON and return the raw response as a String.
     * Response parsing/mapping will be handled in a later phase.
     *
     * @param prompt
     */
    @Override
    public String executePrompt(LlmAdapter adapter, JsonObject prompt) {
        if(prompt == null || adapter == null){
            return "";
        }
        return adapter.invokeLlm(prompt);
    }
}
