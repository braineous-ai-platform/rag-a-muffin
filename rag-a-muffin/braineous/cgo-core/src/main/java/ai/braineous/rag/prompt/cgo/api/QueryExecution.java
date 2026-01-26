package ai.braineous.rag.prompt.cgo.api;

import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import ai.braineous.rag.prompt.cgo.query.QueryTask;
import com.google.gson.JsonObject;

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

    private boolean inMemoryMode = false;

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

    //--------------------------------------------------------
    public boolean isOk() {
        return (promptValidation == null || promptValidation.isOk())
                && (llmResponseValidation == null || llmResponseValidation.isOk())
                && (domainValidation == null || domainValidation.isOk());
    }

    public String getStage() {
        if (promptValidation != null && !promptValidation.isOk()) return "prompt_contract";
        if (llmResponseValidation != null && !llmResponseValidation.isOk()) return "llm_response";
        if (domainValidation != null && !domainValidation.isOk()) return "domain";
        return "ok";
    }

    public String getStatus() {
        return isOk() ? "OK" : "ERROR";
    }

    public ValidationResult getPrimaryValidation() {
        if (promptValidation != null && !promptValidation.isOk()) return promptValidation;
        if (llmResponseValidation != null && !llmResponseValidation.isOk()) return llmResponseValidation;
        if (domainValidation != null && !domainValidation.isOk()) return domainValidation;
        return null;
    }

    public boolean isInMemoryMode() {
        return inMemoryMode;
    }

    public void setInMemoryMode(boolean inMemoryMode) {
        this.inMemoryMode = inMemoryMode;
    }

    //---------------------------------------------------------
    @Override
    public String toString() {
        return "QueryExecution{" +
                "status=" + getStatus() +
                ", stage=" + getStage() +
                ", request=" + request +
                ", rawResponse='" + rawResponse + '\'' +
                ", promptValidation=" + promptValidation +
                ", llmResponseValidation=" + llmResponseValidation +
                ", domainValidation=" + domainValidation +
                '}';
    }

    //----------------------------------------------------------
    public JsonObject toJson() {

        JsonObject out = new JsonObject();

        // ---- request ----
        if (this.request != null) {
            out.add("request", this.request.toJson());
        } else {
            out.add("request", null);
        }

        // ---- rawResponse ----
        if (this.rawResponse != null) {
            out.addProperty("rawResponse", this.rawResponse);
        } else {
            out.add("rawResponse", null);
        }

        // ---- validations ----
        if (this.promptValidation != null) {
            out.add("promptValidation", this.promptValidation.toJson());
        } else {
            out.add("promptValidation", null);
        }

        if (this.llmResponseValidation != null) {
            out.add("llmResponseValidation", this.llmResponseValidation.toJson());
        } else {
            out.add("llmResponseValidation", null);
        }

        if (this.domainValidation != null) {
            out.add("domainValidation", this.domainValidation.toJson());
        } else {
            out.add("domainValidation", null);
        }

        // ---- derived convenience (optional, but useful for logs) ----
        out.addProperty("status", getStatus());
        out.addProperty("stage", getStage());
        out.addProperty("ok", isOk());

        return out;
    }

    public String toJsonString() {
        return toJson().toString();
    }

    @SuppressWarnings("unchecked")
    public static QueryExecution<?> fromJson(JsonObject json) {

        if (json == null) {
            throw new IllegalArgumentException("QueryExecution JSON cannot be null");
        }

        // ---- request ----
        QueryRequest<?> req = null;
        if (json.has("request") && !json.get("request").isJsonNull()) {
            req = QueryRequest.fromJson(json.getAsJsonObject("request"));
        }

        // ---- rawResponse ----
        String rawResponse = null;
        if (json.has("rawResponse") && !json.get("rawResponse").isJsonNull()) {
            rawResponse = json.get("rawResponse").getAsString();
        }

        // ---- validations ----
        ValidationResult promptValidation = null;
        if (json.has("promptValidation") && !json.get("promptValidation").isJsonNull()) {
            promptValidation = ValidationResult.fromJson(json.getAsJsonObject("promptValidation"));
        }

        ValidationResult llmResponseValidation = null;
        if (json.has("llmResponseValidation") && !json.get("llmResponseValidation").isJsonNull()) {
            llmResponseValidation = ValidationResult.fromJson(json.getAsJsonObject("llmResponseValidation"));
        }

        ValidationResult domainValidation = null;
        if (json.has("domainValidation") && !json.get("domainValidation").isJsonNull()) {
            domainValidation = ValidationResult.fromJson(json.getAsJsonObject("domainValidation"));
        }

        QueryExecution<?> exec = new QueryExecution(req, rawResponse, promptValidation, llmResponseValidation, domainValidation);
        return exec;
    }

}


