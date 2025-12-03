package ai.braineous.rag.prompt.cgo.query;

import ai.braineous.rag.prompt.cgo.api.LLMResponseValidatorRule;
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

    private final PhaseResultValidator llmResponseValidator;

    public CgoQueryPipeline(PromptBuilder promptBuilder, LlmClient llmClient, PhaseResultValidator llmResponseValidator) {
        this.promptBuilder = Objects.requireNonNull(promptBuilder, "promptBuilder must not be null");
        this.llmClient = Objects.requireNonNull(llmClient, "llmClient must not be null");
        this.llmResponseValidator = llmResponseValidator;
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

        // 1b) Fail-fast if prompt contract validation failed
        ValidationResult promptValidation = requestOutput.getValidationResult();
        if (promptValidation != null && !promptValidation.isOk()) {
            // Prompt is invalid; do NOT call LLM. Surface this as the pipeline's ValidationResult.
            // We store it in the LLM validation slot; the stage identifies that this came
            // from the prompt contract phase ("prompt_contract_validation").
            return new QueryExecution<>(request, null, promptValidation, null, null);
        }

        // 2) Call LLM
        String rawResponse = llmClient.executePrompt(prompt);

        // 2a) Global/core LLM response validation (if configured)
        ValidationResult responseValidation = null;
        if (this.llmResponseValidator != null) {
            responseValidation = llmResponseValidator.validate(rawResponse);
            if (responseValidation != null && !responseValidation.isOk()) {
                // Core response contract failed; return with this ValidationResult
                return new QueryExecution<>(request, rawResponse, promptValidation, responseValidation, null);
            }
        }

        // 2b) Per-request LLM response rule (API-level, from QueryRequest)
        LLMResponseValidatorRule rule = request.getRule();
        ValidationResult domainValidation = null;
        if (rule != null) {
            domainValidation = rule.validate(rawResponse);
            if (domainValidation != null && !domainValidation.isOk()) {
                // Per-request rule failed; return with this ValidationResult
                return new QueryExecution<>(request, rawResponse, promptValidation, responseValidation, domainValidation);
            }
        }


        // 4) Wrap into a generic QueryExecution; domain decides how to map rawResponse → domain DTO
        // Domain-level validation is not performed here yet, so domainValidation = null.
        return new QueryExecution<>(request, rawResponse, promptValidation, responseValidation, domainValidation);
    }
}



