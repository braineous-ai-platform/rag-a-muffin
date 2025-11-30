package ai.braineous.rag.prompt.cgo.query;

import ai.braineous.rag.prompt.cgo.api.ValidationResult;

import java.util.Collections;

/**
 * A no-op ValidationResultValidator implementation.
 *
 * Useful for:
 *  - testing pipeline wiring
 *  - development mode where LLM response contract validation is skipped
 *  - scenarios where upstream components already guarantee shape
 *
 * Always returns an OK ValidationResult with:
 *  - code = "OK"
 *  - message = "OK"
 *  - stage settable via constructor
 *  - anchorId = null
 *  - metadata = empty map
 */
public final class NoOpPhaseResultValidator implements PhaseResultValidator {

    private final String stage;

    public NoOpPhaseResultValidator() {
        this("llm_response_validation");
    }

    public NoOpPhaseResultValidator(String stage) {
        this.stage = stage;
    }

    @Override
    public ValidationResult validate(String rawResponse) {
        return ValidationResult.ok(
                this.stage,
                null,
                Collections.emptyMap()
        );
    }
}
