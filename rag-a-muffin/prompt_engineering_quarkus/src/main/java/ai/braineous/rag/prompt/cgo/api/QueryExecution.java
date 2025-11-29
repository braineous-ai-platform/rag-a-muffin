package ai.braineous.rag.prompt.cgo.api;

import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import ai.braineous.rag.prompt.cgo.query.QueryTask;

/**
 * Represents the execution of a single QueryRequest.
 *
 * v1: wraps the original request.
 * v2: also carries the raw LLM response and a typed result object.
 * v3: additionally carries a generic, domain-agnostic ValidationResult
 *     produced by the LLM response validator (if configured).
 */
public final class QueryExecution<T extends QueryTask> {

    private final QueryRequest<T> request;
    private final String rawResponse;
    private final Object result;

    /**
     * Optional domain-agnostic validation outcome for the LLM response,
     * produced by ValidationResultValidator inside the pipeline.
     *
     * May be null if:
     *  - no ValidationResultValidator was configured, or
     *  - this query type does not use that validator.
     */
    private final ValidationResult validationResult;

    // ---- Constructors --------------------------------------------------------

    public QueryExecution(QueryRequest<T> request) {
        this(request, null, null, null);
    }

    /**
     * Original v2-style constructor: request + result + rawResponse.
     * Kept for compatibility; delegates to the full constructor.
     */
    public QueryExecution(QueryRequest<T> request, Object result, String rawResponse) {
        this(request, result, rawResponse, null);
    }

    /**
     * Convenience: request + rawResponse only (no typed result, no validation).
     */
    public QueryExecution(QueryRequest<T> request, String rawResponse) {
        this(request, null, rawResponse, null);
    }

    /**
     * New: request + rawResponse + ValidationResult, no typed result.
     * Useful when the pipeline sets only the validator output.
     */
    public QueryExecution(QueryRequest<T> request, String rawResponse, ValidationResult validationResult) {
        this(request, null, rawResponse, validationResult);
    }

    /**
     * Full constructor.
     */
    public QueryExecution(
            QueryRequest<T> request,
            Object result,
            String rawResponse,
            ValidationResult validationResult
    ) {
        this.request = request;
        this.result = result;
        this.rawResponse = rawResponse;
        this.validationResult = validationResult;
    }

    // ---- Accessors -----------------------------------------------------------

    public QueryRequest<T> getRequest() {
        return request;
    }

    /**
     * Raw LLM response payload as a String.
     */
    public String getRawResponse() {
        return rawResponse;
    }

    /**
     * Untyped result object.
     * For domain-specific queries this may be a DTO; the pipeline itself
     * does not interpret it.
     */
    public Object getResult() {
        return result;
    }

    /**
     * Helper to retrieve the result in a type-safe way.
     */
    public <R> R getResultAs(Class<R> type) {
        if (result == null) {
            return null;
        }
        return type.cast(result);
    }

    /**
     * Domain-agnostic LLM response ValidationResult, if available.
     */
    public ValidationResult getValidationResult() {
        return validationResult;
    }

    public boolean hasValidationResult() {
        return validationResult != null;
    }
}

