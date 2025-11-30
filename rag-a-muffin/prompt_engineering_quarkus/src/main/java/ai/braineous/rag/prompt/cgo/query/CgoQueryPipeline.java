package ai.braineous.rag.prompt.cgo.query;

import ai.braineous.rag.prompt.cgo.api.QueryExecution;
import ai.braineous.rag.prompt.cgo.api.QueryPipeline;
import ai.braineous.rag.prompt.cgo.api.ValidationResult;
import ai.braineous.rag.prompt.cgo.prompt.LlmClient;
import ai.braineous.rag.prompt.cgo.prompt.PromptBuilder;
import ai.braineous.rag.prompt.cgo.prompt.PromptRequestOutput;
import com.google.gson.JsonObject;

import java.util.Objects;

/**
 * Core, domain-agnostic implementation of the QueryPipeline.
 *
 * Responsibilities:
 *  - Take a QueryRequest<T extends QueryTask>
 *  - Use PromptBuilder to construct the LLM prompt JSON
 *  - Call LlmClient
 *  - Wrap request + raw response in QueryExecution<T>
 *
 * This class MUST remain domain-agnostic:
 *  - No references to specific tasks like "ValidateTask"
 *  - No references to domain result DTOs (e.g., ValidationResult for flights)
 */
public final class CgoQueryPipeline implements QueryPipeline {

    private final PromptBuilder promptBuilder;
    private final LlmClient llmClient;

    private final PhaseResultValidator phaseResultValidator;

    public CgoQueryPipeline(PromptBuilder promptBuilder, LlmClient llmClient, PhaseResultValidator phaseResultValidator) {
        this.promptBuilder = Objects.requireNonNull(promptBuilder, "promptBuilder must not be null");
        this.llmClient = Objects.requireNonNull(llmClient, "llmClient must not be null");
        this.phaseResultValidator = phaseResultValidator;
    }

    public CgoQueryPipeline(PromptBuilder promptBuilder, LlmClient llmClient) {
        this(promptBuilder, llmClient, null);
    }

    @Override
    public <T extends QueryTask> QueryExecution<T> execute(QueryRequest<T> request) {
        Objects.requireNonNull(request, "request must not be null");

        // 1) Build prompt from meta + task + graph context + response contract
        PromptRequestOutput requestOutput = promptBuilder.generateRequestPrompt(request);
        JsonObject prompt = requestOutput.getRequestOutput();

        // 2) Call LLM
        String rawResponse = llmClient.executePrompt(prompt);

        ValidationResult responseValidation = null;
        if(this.phaseResultValidator != null) {
            // 2b) Validate LLM response shape/contract into a generic ValidationResult
            //     (for now this is internal-only; we are not yet wiring it into QueryExecution)
            responseValidation = phaseResultValidator.validate(rawResponse);
            // TODO (next step): decide how to surface responseValidation
            // e.g., attach to QueryExecution, or fail-fast on !responseValidation.isOk()
        }

        // 3) Wrap into a generic QueryExecution; domain decides how to map it
        return new QueryExecution<>(request, null, rawResponse, responseValidation);
    }
}



