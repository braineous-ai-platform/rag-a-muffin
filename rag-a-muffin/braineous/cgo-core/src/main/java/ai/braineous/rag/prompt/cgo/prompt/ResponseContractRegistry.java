package ai.braineous.rag.prompt.cgo.prompt;

import com.google.gson.JsonObject;

import java.util.List;

/**
 * Internal registry for operator-specific LLM contract data.
 *
 * API users never see this. It is used by PromptBuilder
 * to enrich the QueryRequest with response_contract and
 * llm_instructions for the LLM.
 */
public interface ResponseContractRegistry {

    /**
     * Return the response_contract JSON block for a given queryKind.
     */
    JsonObject responseContractFor(String queryKind);

    /**
     * Return the operator-specific llm_instructions for a given queryKind.
     */
    List<String> llmInstructionsFor(String queryKind);
}

