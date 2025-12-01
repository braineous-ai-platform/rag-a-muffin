package ai.braineous.rag.prompt.cgo.prompt;

import com.google.gson.JsonObject;

/**
 * Internal interface for executing prompts against an LLM backend.
 *
 * API users never see this. Implementations can call local models,
 * remote APIs, gateways, etc.
 */
public interface LlmClient {

    /**
     * Execute the given prompt JSON and return the raw response as a String.
     * Response parsing/mapping will be handled in a later phase.
     */
    String executePrompt(JsonObject prompt);
}

