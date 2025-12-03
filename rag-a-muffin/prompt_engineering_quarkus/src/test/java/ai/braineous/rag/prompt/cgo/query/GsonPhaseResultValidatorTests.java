package ai.braineous.rag.prompt.cgo.query;

import ai.braineous.rag.prompt.cgo.api.ValidationResult;
import ai.braineous.rag.prompt.observe.Console;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class GsonPhaseResultValidatorTests {

    @Test
    void validate_wrappedOkResult_shouldReturnOkValidationResult() {
        String rawJson = """
            {
              "result": {
                "ok": true,
                "code": "response.contract.ok",
                "message": "All good",
                "stage": "llm_response_validation",
                "anchorId": "Fact:123",
                "metadata": {
                  "raw_length": 123,
                  "model": "gpt-x"
                }
              }
            }
            """;

        Console.log("TEST: validate_wrappedOkResult_shouldReturnOkValidationResult - RAW_JSON", rawJson);

        GsonPhaseResultValidator validator = new GsonPhaseResultValidator();

        ValidationResult result = validator.validate(rawJson);

        Console.log("TEST: validate_wrappedOkResult_shouldReturnOkValidationResult - RESULT", result);

        assertNotNull(result, "ValidationResult must not be null");
        assertTrue(result.isOk(), "ok flag must be true for a valid wrapped result");
        assertEquals("response.contract.ok", result.getCode(), "code should come from JSON");
        assertEquals("All good", result.getMessage(), "message should come from JSON");
        assertEquals("llm_response_validation", result.getStage(), "stage should come from JSON");
        assertEquals("Fact:123", result.getAnchorId(), "anchorId should come from JSON");

        Map<String, Object> metadata = result.getMetadata();
        assertNotNull(metadata, "metadata must never be null");
        assertEquals(2, metadata.size(), "metadata should contain both entries from JSON");
        assertEquals(123.0, metadata.get("raw_length")); // Gson parses numbers as Double by default
        assertEquals("gpt-x", metadata.get("model"));
    }

    @Test
    void validate_flatOkResult_shouldReturnOkValidationResult() {
        String rawJson = """
            {
              "ok": true,
              "code": "response.contract.ok",
              "message": "All good (flat)",
              "stage": "llm_response_validation",
              "anchorId": "Fact:456",
              "metadata": {
                "raw_length": 456,
                "model": "gpt-y"
              }
            }
            """;

        Console.log("TEST: validate_flatOkResult_shouldReturnOkValidationResult - RAW_JSON", rawJson);

        GsonPhaseResultValidator validator = new GsonPhaseResultValidator();

        ValidationResult result = validator.validate(rawJson);

        Console.log("TEST: validate_flatOkResult_shouldReturnOkValidationResult - RESULT", result);

        assertNotNull(result, "ValidationResult must not be null");
        assertTrue(result.isOk(), "ok flag must be true for a valid flat result");
        assertEquals("response.contract.ok", result.getCode(), "code should come from JSON");
        assertEquals("All good (flat)", result.getMessage(), "message should come from JSON");
        assertEquals("llm_response_validation", result.getStage(), "stage should come from JSON");
        assertEquals("Fact:456", result.getAnchorId(), "anchorId should come from JSON");

        Map<String, Object> metadata = result.getMetadata();
        assertNotNull(metadata, "metadata must never be null");
        assertEquals(2, metadata.size(), "metadata should contain both entries from JSON");
        assertEquals(456.0, metadata.get("raw_length")); // Gson uses Double for numbers
        assertEquals("gpt-y", metadata.get("model"));
    }

    @Test
    void validate_nullRawResponse_shouldReturnEmptyContractError() {
        String rawJson = null;

        Console.log("TEST: validate_nullRawResponse_shouldReturnEmptyContractError - RAW_JSON", rawJson);

        GsonPhaseResultValidator validator = new GsonPhaseResultValidator();

        ValidationResult result = validator.validate(rawJson);

        Console.log("TEST: validate_nullRawResponse_shouldReturnEmptyContractError - RESULT", result);

        assertNotNull(result, "ValidationResult must not be null");
        assertFalse(result.isOk(), "ok flag must be false for empty/null rawResponse");
        assertEquals("response.contract.empty", result.getCode(), "code should indicate empty contract");
        assertEquals("Raw LLM response is empty", result.getMessage(), "message should describe empty input");
        assertEquals("llm_response_validation", result.getStage(), "stage should be the default validation stage");
        assertNull(result.getAnchorId(), "anchorId should be null for empty input");

        Map<String, Object> metadata = result.getMetadata();
        assertNotNull(metadata, "metadata must never be null");
        assertEquals(1, metadata.size(), "metadata should contain exactly one entry for rawResponse");
        assertTrue(metadata.containsKey("rawResponse"), "metadata must contain 'rawResponse' key");
        assertNull(metadata.get("rawResponse"), "rawResponse value in metadata should be null for null input");
    }

    @Test
    void validate_invalidJson_shouldReturnInvalidJsonError() {
        String rawJson = "{ not-valid-json ";

        Console.log("TEST: validate_invalidJson_shouldReturnInvalidJsonError - RAW_JSON", rawJson);

        GsonPhaseResultValidator validator = new GsonPhaseResultValidator();

        ValidationResult result = validator.validate(rawJson);

        Console.log("TEST: validate_invalidJson_shouldReturnInvalidJsonError - RESULT", result);

        assertNotNull(result, "ValidationResult must not be null");
        assertFalse(result.isOk(), "ok flag must be false for invalid JSON");
        assertEquals("response.contract.invalid_json", result.getCode(), "code should indicate invalid JSON");

        String message = result.getMessage();
        assertNotNull(message, "message must not be null");
        assertTrue(message.startsWith("Failed to parse LLM response as JSON:"),
                "message should start with parse failure prefix");

        assertEquals("llm_response_validation", result.getStage(), "stage should be the default validation stage");
        assertNull(result.getAnchorId(), "anchorId should be null for invalid JSON");

        Map<String, Object> metadata = result.getMetadata();
        assertNotNull(metadata, "metadata must never be null");
        assertEquals(1, metadata.size(), "metadata should contain exactly one entry for rawResponse");
        assertEquals(rawJson, metadata.get("rawResponse"), "metadata.rawResponse should echo the invalid JSON input");
    }

    @Test
    void validate_nonObjectRoot_shouldReturnRootNotObjectError() {
        // root is a JSON array instead of an object
        String rawJson = """
            [
              {
                "ok": true
              }
            ]
            """;

        Console.log("TEST: validate_nonObjectRoot_shouldReturnRootNotObjectError - RAW_JSON", rawJson);

        GsonPhaseResultValidator validator = new GsonPhaseResultValidator();

        ValidationResult result = validator.validate(rawJson);

        Console.log("TEST: validate_nonObjectRoot_shouldReturnRootNotObjectError - RESULT", result);

        assertNotNull(result, "ValidationResult must not be null");
        assertFalse(result.isOk(), "ok flag must be false when root is not an object");
        assertEquals("response.contract.root_not_object", result.getCode(), "code should indicate non-object root");
        assertEquals("Expected JSON object at root", result.getMessage(), "message should describe the problem");
        assertEquals("llm_response_validation", result.getStage(), "stage should be the default validation stage");
        assertNull(result.getAnchorId(), "anchorId should be null for this error");

        Map<String, Object> metadata = result.getMetadata();
        assertNotNull(metadata, "metadata must never be null");
        assertEquals(1, metadata.size(), "metadata should contain exactly one entry for rawResponse");
        assertEquals(rawJson, metadata.get("rawResponse"), "metadata.rawResponse should echo the input");
    }

    @Test
    void validate_resultFieldNotObject_shouldReturnGenericContractError() {
        // "result" exists but is NOT an object → currently treated as a generic contract error
        String rawJson = """
            {
              "result": 123
            }
            """;

        Console.log("TEST: validate_resultFieldNotObject_shouldReturnGenericContractError - RAW_JSON", rawJson);

        GsonPhaseResultValidator validator = new GsonPhaseResultValidator();

        ValidationResult result = validator.validate(rawJson);

        Console.log("TEST: validate_resultFieldNotObject_shouldReturnGenericContractError - RESULT", result);

        assertNotNull(result, "ValidationResult must not be null");
        assertFalse(result.isOk(), "ok must be false when 'ok' field is missing");

        // Current implementation falls back to a generic contract error
        assertEquals("response.contract.error", result.getCode());
        assertEquals("", result.getMessage());
        assertEquals("llm_response_validation", result.getStage());
        assertNull(result.getAnchorId());

        Map<String, Object> metadata = result.getMetadata();
        assertNotNull(metadata, "metadata must never be null");
        assertTrue(metadata.isEmpty(), "metadata should be empty when no metadata field is present");
    }

    @Test
    void validate_flatMissingFields_shouldApplyDefaults() {
        // "ok", "code", "message", "anchorId" are missing
        String rawJson = """
            {
              "stage": "custom_stage"
            }
            """;

        Console.log("TEST: validate_flatMissingFields_shouldApplyDefaults - RAW_JSON", rawJson);

        GsonPhaseResultValidator validator = new GsonPhaseResultValidator();

        ValidationResult result = validator.validate(rawJson);

        Console.log("TEST: validate_flatMissingFields_shouldApplyDefaults - RESULT", result);

        assertNotNull(result);

        // Missing ok → defaults to false
        assertFalse(result.isOk());

        // Missing code → because ok=false → "response.contract.error"
        assertEquals("response.contract.error", result.getCode());

        // Missing message → defaults to empty string
        assertEquals("", result.getMessage());

        // Stage exists → must be preserved
        assertEquals("custom_stage", result.getStage());

        // AnchorId missing → null
        assertNull(result.getAnchorId());

        // Metadata missing → empty map
        Map<String, Object> metadata = result.getMetadata();
        assertNotNull(metadata);
        assertTrue(metadata.isEmpty());
    }

    @Test
    void validate_metadataEmptyObject_shouldReturnEmptyMetadata() {
        String rawJson = """
            {
              "ok": true,
              "code": "response.contract.ok",
              "message": "All good, empty metadata",
              "stage": "llm_response_validation",
              "anchorId": "Fact:789",
              "metadata": { }
            }
            """;

        Console.log("TEST: validate_metadataEmptyObject_shouldReturnEmptyMetadata - RAW_JSON", rawJson);

        GsonPhaseResultValidator validator = new GsonPhaseResultValidator();

        ValidationResult result = validator.validate(rawJson);

        Console.log("TEST: validate_metadataEmptyObject_shouldReturnEmptyMetadata - RESULT", result);

        assertNotNull(result);
        assertTrue(result.isOk());
        assertEquals("response.contract.ok", result.getCode());
        assertEquals("All good, empty metadata", result.getMessage());
        assertEquals("llm_response_validation", result.getStage());
        assertEquals("Fact:789", result.getAnchorId());

        Map<String, Object> metadata = result.getMetadata();
        assertNotNull(metadata, "metadata must never be null");
        assertTrue(metadata.isEmpty(), "metadata should be empty when JSON metadata object is empty");
    }

    @Test
    void validate_metadataNotObject_shouldReturnEmptyMetadata() {
        String rawJson = """
            {
              "ok": true,
              "code": "response.contract.ok",
              "message": "All good, bad metadata type",
              "stage": "llm_response_validation",
              "anchorId": "Fact:999",
              "metadata": 123
            }
            """;

        Console.log("TEST: validate_metadataNotObject_shouldReturnEmptyMetadata - RAW_JSON", rawJson);

        GsonPhaseResultValidator validator = new GsonPhaseResultValidator();

        ValidationResult result = validator.validate(rawJson);

        Console.log("TEST: validate_metadataNotObject_shouldReturnEmptyMetadata - RESULT", result);

        assertNotNull(result);
        assertTrue(result.isOk(), "ok should be true from JSON");
        assertEquals("response.contract.ok", result.getCode());
        assertEquals("All good, bad metadata type", result.getMessage());
        assertEquals("llm_response_validation", result.getStage());
        assertEquals("Fact:999", result.getAnchorId());

        Map<String, Object> metadata = result.getMetadata();
        assertNotNull(metadata, "metadata must never be null");
        assertTrue(metadata.isEmpty(), "metadata should be empty when JSON metadata is not an object");
    }

    @Test
    void validate_minimalFlatOkResult_shouldUseDefaultsAndReturnOk() {
        String rawJson = """
            {
              "ok": true,
              "code": "response.contract.ok",
              "message": "Minimal OK"
            }
            """;

        Console.log("TEST: validate_minimalFlatOkResult_shouldUseDefaultsAndReturnOk - RAW_JSON", rawJson);

        GsonPhaseResultValidator validator = new GsonPhaseResultValidator();

        ValidationResult result = validator.validate(rawJson);

        Console.log("TEST: validate_minimalFlatOkResult_shouldUseDefaultsAndReturnOk - RESULT", result);

        assertNotNull(result, "ValidationResult must not be null");
        assertTrue(result.isOk(), "ok should be true from JSON");

        assertEquals("response.contract.ok", result.getCode(), "code should come from JSON");
        assertEquals("Minimal OK", result.getMessage(), "message should come from JSON");

        // Stage missing -> default
        assertEquals("llm_response_validation", result.getStage());

        // AnchorId missing -> null
        assertNull(result.getAnchorId());

        // Metadata missing -> empty
        Map<String, Object> metadata = result.getMetadata();
        assertNotNull(metadata);
        assertTrue(metadata.isEmpty());
    }
}
