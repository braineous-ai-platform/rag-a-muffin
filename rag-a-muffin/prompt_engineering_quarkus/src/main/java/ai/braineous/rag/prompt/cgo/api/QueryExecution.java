package ai.braineous.rag.prompt.cgo.api;

import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import ai.braineous.rag.prompt.cgo.query.QueryTask;

/**
 * Represents the execution of a single QueryRequest.
 *
 * v1: wraps the original request.
 * v2: also carries the raw LLM response and a typed result object.
 * v3: additionally carries generic, domain-agnostic ValidationResult objects
 *     produced by different phases:
 *       - LLM response validation (pipeline-level)
 *       - Domain-level validation (app-layer)
 *
 * The pipeline is responsible for setting the LLM response validation, while
 * domain adapters/services may set the domain validation.
 */
public final class QueryExecution<T extends QueryTask> {

    private QueryRequest<T> request;
    private String rawResponse;

    private ValidationResult promptValidation;

    /**
     * Optional domain-agnostic validation outcome for the LLM response,
     * produced by a response validator inside the pipeline (e.g. PhaseResultValidator,
     * per-request LLMResponseValidatorRule).
     *
     * May be null if:
     *  - no LLM response validator was configured, or
     *  - this query type does not use that validator.
     */
    private ValidationResult llmResponseValidation;

    /**
     * Optional domain-level validation outcome, produced outside the core
     * pipeline (e.g. domain services checking semantic correctness of the
     * mapped result against business rules / graph context).
     *
     * May be null if no domain validation has been performed.
     */
    private ValidationResult domainValidation;

    // ---- Constructors --------------------------------------------------------

    public QueryExecution(QueryRequest<T> request) {
        this.request = request;
    }

    public QueryExecution(QueryRequest<T> request,
                          String rawResponse,
                          ValidationResult promptValidation,
                          ValidationResult llmResponseValidation,
                          ValidationResult domainValidation) {
        this.request = request;
        this.rawResponse = rawResponse;
        this.promptValidation = promptValidation;
        this.llmResponseValidation = llmResponseValidation;
        this.domainValidation = domainValidation;
    }

    // ---- Accessors -----------------------------------------------------------

    /**
     * Original QueryRequest for this execution.
     */
    public QueryRequest<T> getRequest() {
        return request;
    }

    /**
     * Raw LLM response payload as a String.
     *
     * May be null if the execution failed fast before calling LLM
     * (e.g., prompt contract validation failed).
     */
    public String getRawResponse() {
        return rawResponse;
    }

    public ValidationResult getPromptValidation() {
        return promptValidation;
    }

    public boolean hasPromptValidation() {
        return promptValidation != null;
    }

    /**
     * LLM response ValidationResult, if available.
     * This reflects pipeline-level checks on the raw LLM output.
     */
    public ValidationResult getLlmResponseValidation() {
        return llmResponseValidation;
    }

    public boolean hasLlmResponseValidation() {
        return llmResponseValidation != null;
    }

    /**
     * Domain-level ValidationResult, if available.
     * This reflects semantic/business checks performed outside the core pipeline.
     */
    public ValidationResult getDomainValidation() {
        return domainValidation;
    }

    public boolean hasDomainValidation() {
        return domainValidation != null;
    }

    /**
     * Backwards-compatible alias for LLM response validation.
     * Prefer getLlmResponseValidation() going forward.
     */
    @Deprecated
    public ValidationResult getValidationResult() {
        return llmResponseValidation;
    }

    @Deprecated
    public boolean hasValidationResult() {
        return hasLlmResponseValidation();
    }
}


