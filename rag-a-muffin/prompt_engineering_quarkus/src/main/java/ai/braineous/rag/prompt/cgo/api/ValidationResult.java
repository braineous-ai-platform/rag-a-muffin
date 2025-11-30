package ai.braineous.rag.prompt.cgo.api;

import java.util.Collections;
import java.util.Map;

/**
 * Domain-agnostic validation outcome for LLM *response validation*
 * in the CGO query phase.
 *
 * This type must NEVER depend on:
 *  - domain concepts (flight, airport, policy, etc.)
 *  - domain enums
 *  - app-level error codes
 *
 * It is purely structural: "did the LLM response pass validation" + "what, where, when".
 */
public final class ValidationResult {

    /**
     * Whether the validation step succeeded.
     *
     * For LLM responses, this typically means:
     *  - true  -> response conforms to the expected contract/schema
     *  - false -> response is malformed, partial, or contract-violating
     */
    private final boolean ok;

    /**
     * Machine-readable code for the outcome.
     *
     * Examples (all generic, not domain-bound):
     *  - "OK"
     *  - "CONTRACT_VIOLATION"
     *  - "PARSING_FAILED"
     *  - "MISSING_REQUIRED_FIELD"
     *  - "UNEXPECTED_SHAPE"
     */
    private final String code;

    /**
     * Human-readable explanation intended for logs / observability,
     * not necessarily for end-user UX.
     */
    private final String message;

    /**
     * Optional: which phase or component produced this result.
     * e.g. "LLM_RESPONSE_VALIDATION", "QUERY_RESPONSE", "POST_PROCESSING".
     */
    private final String stage;

    /**
     * Optional anchor into the request/response context.
     * Can be a query id, a field path (e.g. "result.items[0].id"), etc.
     * The validator does not interpret it further.
     */
    private final String anchorId;

    /**
     * Optional extra structured data. Completely generic.
     * Callers are free to put whatever they want here (e.g., list of missing fields),
     * but CGO core treats it as opaque.
     */
    private final Map<String, Object> metadata;

    private ValidationResult(
            boolean ok,
            String code,
            String message,
            String stage,
            String anchorId,
            Map<String, Object> metadata
    ) {
        this.ok = ok;
        this.code = code;
        this.message = message;
        this.stage = stage;
        this.anchorId = anchorId;
        this.metadata = (metadata == null)
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(metadata);
    }

    // ---------- Static factories (preferred) ----------

    public static ValidationResult ok() {
        return new ValidationResult(true, "OK", "OK", null, null, null);
    }

    public static ValidationResult ok(String stage) {
        return new ValidationResult(true, "OK", "OK", stage, null, null);
    }

    public static ValidationResult ok(
            String stage,
            String anchorId,
            Map<String, Object> metadata
    ) {
        return new ValidationResult(true, "OK", "OK", stage, anchorId, metadata);
    }

    public static ValidationResult ok(
            String code,
            String message,
            String stage,
            String anchorId,
            Map<String, Object> metadata
    ) {
        return new ValidationResult(true, "OK", "OK", stage, anchorId, metadata);
    }

    public static ValidationResult ok(
            String code,
            String message
    ) {
        // preserve the caller-provided code and message
        return createInternal(
                true,
                code,
                message,
                null,   // no stage in simple factory
                null,   // no anchorId in simple factory
                null    // no metadata in simple factory
        );
    }

    public static ValidationResult error(
            String code,
            String message
    ) {
        return new ValidationResult(false, code, message, null, null, null);
    }

    public static ValidationResult error(
            String code,
            String message,
            String stage,
            String anchorId
    ) {
        return new ValidationResult(false, code, message, stage, anchorId, null);
    }

    public static ValidationResult error(
            String code,
            String message,
            String stage,
            String anchorId,
            Map<String, Object> metadata
    ) {
        return new ValidationResult(false, code, message, stage, anchorId, metadata);
    }

    public static ValidationResult createInternal(
            boolean ok,
            String code,
            String message,
            String stage,
            String anchorId,
            Map<String, Object> metadata
    ) {
        return new ValidationResult(
                ok,
                code,
                message,
                stage,
                anchorId,
                metadata
        );
    }


    // ---------- Getters ----------

    public boolean isOk() {
        return ok;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getStage() {
        return stage;
    }

    public String getAnchorId() {
        return anchorId;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    // ---------- Convenience ----------

    @Override
    public String toString() {
        return "ValidationResult{" +
                "ok=" + ok +
                ", code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", stage='" + stage + '\'' +
                ", anchorId='" + anchorId + '\'' +
                ", metadata=" + metadata +
                '}';
    }
}

