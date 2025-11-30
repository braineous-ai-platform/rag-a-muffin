package ai.braineous.rag.prompt.cgo.query;

import ai.braineous.rag.prompt.cgo.api.ValidationResult;
import ai.braineous.rag.prompt.observe.Console;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class NoOpPhaseResultValidatorTests {

    @Test
    void validate_shouldAlwaysReturnOkWithDefaultStageAndEmptyMetadata() {
        String raw = "anything at all";

        Console.log("TEST: validate_shouldAlwaysReturnOkWithDefaultStageAndEmptyMetadata - RAW", raw);

        NoOpPhaseResultValidator validator = new NoOpPhaseResultValidator();

        ValidationResult result = validator.validate(raw);

        Console.log("TEST: validate_shouldAlwaysReturnOkWithDefaultStageAndEmptyMetadata - RESULT", result);

        assertNotNull(result);
        assertTrue(result.isOk(), "NoOp should always return ok=true");

        // Fixed OK factories enforce code/message = "OK"
        assertEquals("OK", result.getCode());
        assertEquals("OK", result.getMessage());

        // Default stage from constructor
        assertEquals("llm_response_validation", result.getStage());

        // Always null anchorId
        assertNull(result.getAnchorId());

        // Always empty metadata
        Map<String, Object> metadata = result.getMetadata();
        assertNotNull(metadata);
        assertTrue(metadata.isEmpty());
    }
}
