package ai.braineous.rag.prompt.cgo.api;

import ai.braineous.rag.prompt.observe.Console;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ValidationResultTests {

    @Test
    void ok_simpleFactory_shouldSetOkTrueAndPreserveCodeAndMessage() {
        Console.log("TEST: ok_simpleFactory_shouldSetOkTrueAndPreserveCodeAndMessage", null);

        String code = "response.contract.ok";
        String message = "All good";

        ValidationResult result = ValidationResult.ok(code, message);

        Console.log("Result", result);

        assertNotNull(result);
        assertTrue(result.isOk());
        assertEquals(code, result.getCode());
        assertEquals(message, result.getMessage());
        assertNotNull(result.getMetadata());
    }

    @Test
    void error_simpleFactory_shouldSetOkFalseAndPreserveCodeAndMessage() {
        Console.log("TEST: error_simpleFactory_shouldSetOkFalseAndPreserveCodeAndMessage", null);

        String code = "response.contract.error";
        String message = "Something went wrong";

        ValidationResult result = ValidationResult.error(code, message);

        Console.log("Result", result);

        assertNotNull(result);
        assertFalse(result.isOk());
        assertEquals(code, result.getCode());
        assertEquals(message, result.getMessage());
        assertNotNull(result.getMetadata());
    }

    @Test
    void ok_fullFactory_shouldUseFixedCodeAndMessageAndWrapMetadata() {
        Console.log("TEST: ok_fullFactory_shouldUseFixedCodeAndMessageAndWrapMetadata", null);

        String code = "response.contract.ok";
        String message = "All good";
        String stage = "llm_response_validation";
        String anchorId = "Fact:123";

        Map<String, Object> inputMeta = new HashMap<>();
        inputMeta.put("raw_length", 123);
        inputMeta.put("model", "gpt-x");

        ValidationResult result = ValidationResult.ok(
                code, message, stage, anchorId, inputMeta
        );

        Console.log("Result", result);
        Console.log("Metadata (wrapped)", result.getMetadata());

        assertNotNull(result);
        assertTrue(result.isOk());

        assertEquals("OK", result.getCode());
        assertEquals("OK", result.getMessage());

        assertEquals(stage, result.getStage());
        assertEquals(anchorId, result.getAnchorId());

        Map<String, Object> metadata = result.getMetadata();
        assertNotNull(metadata);
        assertEquals(2, metadata.size());
        assertEquals(123, metadata.get("raw_length"));
        assertEquals("gpt-x", metadata.get("model"));

        assertThrows(UnsupportedOperationException.class,
                () -> metadata.put("late_mutation", true));
    }

    @Test
    void error_fullFactory_shouldPreserveFieldsAndWrapMetadata() {
        Console.log("TEST: error_fullFactory_shouldPreserveFieldsAndWrapMetadata", null);

        String code = "response.contract.invalid_json";
        String message = "Failed to parse JSON";
        String stage = "llm_response_validation";
        String anchorId = "Fact:456";

        Map<String, Object> meta = new HashMap<>();
        meta.put("raw_snippet", "{not-json");
        meta.put("exception_type", "JsonParseException");

        ValidationResult result = ValidationResult.error(
                code, message, stage, anchorId, meta
        );

        Console.log("Result", result);
        Console.log("Metadata (wrapped)", result.getMetadata());

        assertNotNull(result);
        assertFalse(result.isOk());
        assertEquals(code, result.getCode());
        assertEquals(message, result.getMessage());
        assertEquals(stage, result.getStage());
        assertEquals(anchorId, result.getAnchorId());

        Map<String, Object> metadata = result.getMetadata();
        assertNotNull(metadata);
        assertEquals(2, metadata.size());
        assertEquals("{not-json", metadata.get("raw_snippet"));
        assertEquals("JsonParseException", metadata.get("exception_type"));

        assertThrows(UnsupportedOperationException.class,
                () -> metadata.put("late_mutation", true));
    }

    @Test
    void toString_shouldBeNonNullAndNonEmpty() {
        Console.log("TEST: toString_shouldBeNonNullAndNonEmpty", null);

        Map<String, Object> meta = new HashMap<>();
        meta.put("k", "v");

        ValidationResult result = ValidationResult.ok(
                "response.contract.ok",
                "All good",
                "llm_response_validation",
                "Fact:123",
                meta
        );

        Console.log("Result.toString()", result.toString());

        String text = result.toString();

        assertNotNull(text);
        assertFalse(text.isBlank());
    }
}
