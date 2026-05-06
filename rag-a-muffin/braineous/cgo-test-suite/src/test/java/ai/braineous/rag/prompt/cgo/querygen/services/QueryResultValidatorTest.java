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
                        "{\"result\":{\"decision\":\"CAPTURE\",\"reason\":\"RISK_LEVEL_LOW\",\"code\":\"00\"}}"
                );

        Console.log("queryresult.valid", String.valueOf(result));

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isOk());
        Assertions.assertEquals("queryresult.contract.ok", result.getCode());
        Assertions.assertEquals("Query result contract is valid", result.getMessage());
        Assertions.assertEquals("llm_response_validation", result.getStage());
        Assertions.assertNull(result.getAnchorId());
        Assertions.assertNotNull(result.getMetadata());
        Assertions.assertEquals("[decision, reason, code]", result.getMetadata().get("resultFields"));
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
        Assertions.assertEquals("Missing 'result' object", result.getMessage());
    }

    @Test
    public void test_6() {
        QueryResultValidator validator =
                new QueryResultValidator();

        ValidationResult result =
                validator.validate("{\"result\":\"CAPTURE\"}");

        Console.log("queryresult.result.invalid", String.valueOf(result));

        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isOk());
        Assertions.assertEquals("queryresult.contract.result_invalid", result.getCode());
        Assertions.assertEquals("'result' must be a JSON object", result.getMessage());
    }

    @Test
    public void test_7() {
        QueryResultValidator validator =
                new QueryResultValidator();

        ValidationResult result =
                validator.validate("{\"result\":{}}");

        Console.log("queryresult.result.empty", String.valueOf(result));

        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isOk());
        Assertions.assertEquals("queryresult.contract.result_empty", result.getCode());
        Assertions.assertEquals("'result' object is empty", result.getMessage());
    }

    @Test
    public void test_8() {
        QueryResultValidator validator =
                new QueryResultValidator();

        ValidationResult result =
                validator.validate(
                        "{\"result\":{\"decision\":\"CAPTURE\",\"reason\":null,\"code\":\"00\"}}"
                );

        Console.log("queryresult.field.null", String.valueOf(result));

        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isOk());
        Assertions.assertEquals("queryresult.contract.result.field_invalid", result.getCode());
        Assertions.assertEquals("Result field must be a string: reason", result.getMessage());
    }

    @Test
    public void test_9() {
        QueryResultValidator validator =
                new QueryResultValidator();

        ValidationResult result =
                validator.validate(
                        "{\"result\":{\"decision\":\"CAPTURE\",\"reason\":{\"value\":\"LOW\"},\"code\":\"00\"}}"
                );

        Console.log("queryresult.field.object", String.valueOf(result));

        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isOk());
        Assertions.assertEquals("queryresult.contract.result.field_invalid", result.getCode());
        Assertions.assertEquals("Result field must be a string: reason", result.getMessage());
    }

    @Test
    public void test_10() {
        QueryResultValidator validator =
                new QueryResultValidator();

        ValidationResult result =
                validator.validate(
                        "{\"result\":{\"decision\":\"CAPTURE\",\"reason\":true,\"code\":\"00\"}}"
                );

        Console.log("queryresult.field.boolean", String.valueOf(result));

        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isOk());
        Assertions.assertEquals("queryresult.contract.result.field_invalid", result.getCode());
        Assertions.assertEquals("Result field must be a string: reason", result.getMessage());
    }

    @Test
    public void test_11() {
        QueryResultValidator validator =
                new QueryResultValidator();

        ValidationResult result =
                validator.validate(
                        "{\"result\":{\"decision\":\"\",\"reason\":\"\",\"code\":\"\"}}"
                );

        Console.log("queryresult.empty.strings.valid", String.valueOf(result));

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isOk());
        Assertions.assertEquals("queryresult.contract.ok", result.getCode());
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