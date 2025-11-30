package ai.braineous.rag.prompt.cgo.query;

import ai.braineous.rag.prompt.cgo.api.ValidationResult;

public interface PhaseResultValidator {

    /**
     * Validate the raw LLM response against the expected ValidationResult contract.
     *
     * Implementations typically:
     *  - parse rawResponse JSON
     *  - check required fields (ok, code, message, etc.)
     *  - return a ValidationResult representing:
     *      - contract OK, or
     *      - contract violation / parsing error
     */
    ValidationResult validate(String rawResponse);
}
