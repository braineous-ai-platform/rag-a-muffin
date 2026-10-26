package ai.braineous.rag.prompt.cgo.querygen.services;

import ai.braineous.rag.prompt.cgo.api.ValidationResult;
import ai.braineous.rag.prompt.observe.Console;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class QueryResultValidatorTest {

    @Test
    public void test_1() {
        QueryResultValidator validator =
                new QueryResultValidator();

        ValidationResult result =
                validator.validate(
                        "{\"result\":\"ELIGIBLE\"}"
                );

        Console.log("queryresult.valid", String.valueOf(result));

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isOk());
        Assertions.assertEquals("queryresult.contract.ok", result.getCode());
        Assertions.assertEquals("Query result contract is valid", result.getMessage());
        Assertions.assertEquals("llm_response_validation", result.getStage());
        Assertions.assertNull(result.getAnchorId());
        Assertions.assertNotNull(result.getMetadata());
        Assertions.assertEquals("ELIGIBLE", result.getMetadata().get("result"));
    }

    @Test
    public void test_2() {
        QueryResultValidator validator =
                new QueryResultValidator();

        ValidationResult result =
                validator.validate(null);

        Console.log("queryresult.null", String.valueOf(result));

        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isOk());
        Assertions.assertEquals("queryresult.contract.empty", result.getCode());
        Assertions.assertEquals("LLM response JSON is empty", result.getMessage());
        Assertions.assertEquals("llm_response_validation", result.getStage());
    }

    @Test
    public void test_3() {
        QueryResultValidator validator =
                new QueryResultValidator();

        ValidationResult result =
                validator.validate("");

        Console.log("queryresult.blank", String.valueOf(result));

        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isOk());
        Assertions.assertEquals("queryresult.contract.empty", result.getCode());
        Assertions.assertEquals("LLM response JSON is empty", result.getMessage());
    }

    @Test
    public void test_4() {
        QueryResultValidator validator =
                new QueryResultValidator();

        ValidationResult result =
                validator.validate("[]");

        Console.log("queryresult.root.array", String.valueOf(result));

        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isOk());
        Assertions.assertEquals("queryresult.contract.root_not_object", result.getCode());
        Assertions.assertEquals("Expected JSON object at root", result.getMessage());
    }

    @Test
    public void test_5() {
        QueryResultValidator validator =
                new QueryResultValidator();

        ValidationResult result =
                validator.validate("{\"decision\":\"CAPTURE\"}");

        Console.log("queryresult.result.missing", String.valueOf(result));

        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isOk());
        Assertions.assertEquals("queryresult.contract.result_missing", result.getCode());
        Assertions.assertEquals("Missing 'result'", result.getMessage());
    }

    @Test
    public void test_6() {
        QueryResultValidator validator =
                new QueryResultValidator();

        ValidationResult result =
                validator.validate("{\"result\":\"ELIGIBLE\",\"other\":\"x\"}");

        Console.log("queryresult.unexpected.fields", String.valueOf(result));

        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isOk());
        Assertions.assertEquals("queryresult.contract.unexpected_fields", result.getCode());
        Assertions.assertEquals(
                "Root must contain exactly one field named 'result'",
                result.getMessage()
        );
    }

    @Test
    public void test_7() {
        QueryResultValidator validator =
                new QueryResultValidator();

        ValidationResult result =
                validator.validate("{\"result\":\"\"}");

        Console.log("queryresult.result.empty", String.valueOf(result));

        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isOk());
        Assertions.assertEquals("queryresult.contract.result_empty", result.getCode());
        Assertions.assertEquals("'result' is empty", result.getMessage());
    }

    @Test
    public void test_8() {
        QueryResultValidator validator =
                new QueryResultValidator();

        ValidationResult result =
                validator.validate("{\"result\":null}");

        Console.log("queryresult.result.null", String.valueOf(result));

        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isOk());
        Assertions.assertEquals("queryresult.contract.result_invalid", result.getCode());
        Assertions.assertEquals("'result' must be a JSON string", result.getMessage());
    }

    @Test
    public void test_9() {
        QueryResultValidator validator =
                new QueryResultValidator();

        ValidationResult result =
                validator.validate(
                        "{\"result\":{\"decision\":\"CAPTURE\"}}"
                );

        Console.log("queryresult.result.object", String.valueOf(result));

        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isOk());
        Assertions.assertEquals("queryresult.contract.result_invalid", result.getCode());
        Assertions.assertEquals("'result' must be a JSON string", result.getMessage());
    }

    @Test
    public void test_10() {
        QueryResultValidator validator =
                new QueryResultValidator();

        ValidationResult result =
                validator.validate("{\"result\":true}");

        Console.log("queryresult.result.boolean", String.valueOf(result));

        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isOk());
        Assertions.assertEquals("queryresult.contract.result_invalid", result.getCode());
        Assertions.assertEquals("'result' must be a JSON string", result.getMessage());
    }

    @Test
    public void test_11() {
        QueryResultValidator validator =
                new QueryResultValidator();

        ValidationResult result =
                validator.validate("{\"result\":1}");

        Console.log("queryresult.result.number", String.valueOf(result));

        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isOk());
        Assertions.assertEquals("queryresult.contract.result_invalid", result.getCode());
        Assertions.assertEquals("'result' must be a JSON string", result.getMessage());
    }

    @Test
    public void test_12() {
        QueryResultValidator validator =
                new QueryResultValidator();

        ValidationResult result =
                validator.validate("{bad-json");

        Console.log("queryresult.invalid.json", String.valueOf(result));

        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isOk());
        Assertions.assertEquals("queryresult.contract.invalid_json", result.getCode());
        Assertions.assertTrue(result.getMessage().startsWith("Failed to parse LLM response as JSON:"));
    }
}
