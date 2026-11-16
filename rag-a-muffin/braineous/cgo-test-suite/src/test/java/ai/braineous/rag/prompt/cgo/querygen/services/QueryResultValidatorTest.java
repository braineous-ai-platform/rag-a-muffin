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
                validator.validate("success");

        Console.log("queryresult.valid.success", String.valueOf(result));

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isOk());
        Assertions.assertEquals("queryresult.contract.ok", result.getCode());
        Assertions.assertEquals("Query result contract is valid", result.getMessage());
        Assertions.assertEquals("llm_response_validation", result.getStage());
        Assertions.assertNull(result.getAnchorId());
        Assertions.assertNotNull(result.getMetadata());
        Assertions.assertEquals("success", result.getMetadata().get("result"));
    }

    @Test
    public void test_2() {
        QueryResultValidator validator =
                new QueryResultValidator();

        ValidationResult result =
                validator.validate("fail");

        Console.log("queryresult.valid.fail", String.valueOf(result));

        Assertions.assertTrue(result.isOk());
        Assertions.assertEquals("fail", result.getMetadata().get("result"));
    }

    @Test
    public void test_3() {
        QueryResultValidator validator =
                new QueryResultValidator();

        ValidationResult result =
                validator.validate(null);

        Console.log("queryresult.null", String.valueOf(result));

        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isOk());
        Assertions.assertEquals("queryresult.contract.empty", result.getCode());
        Assertions.assertEquals("LLM response is empty", result.getMessage());
        Assertions.assertEquals("llm_response_validation", result.getStage());
    }

    @Test
    public void test_4() {
        QueryResultValidator validator =
                new QueryResultValidator();

        ValidationResult result =
                validator.validate("");

        Console.log("queryresult.blank", String.valueOf(result));

        Assertions.assertFalse(result.isOk());
        Assertions.assertEquals("queryresult.contract.empty", result.getCode());
    }

    @Test
    public void test_5() {
        QueryResultValidator validator =
                new QueryResultValidator();

        ValidationResult result =
                validator.validate("{\"result\":\"success\"}");

        Console.log("queryresult.json.object", String.valueOf(result));

        Assertions.assertFalse(result.isOk());
        Assertions.assertEquals("queryresult.contract.noise", result.getCode());
        Assertions.assertEquals("Result must be exactly success or fail", result.getMessage());
    }

    @Test
    public void test_6() {
        QueryResultValidator validator =
                new QueryResultValidator();

        ValidationResult result =
                validator.validate("ELIGIBLE");

        Console.log("queryresult.noise", String.valueOf(result));

        Assertions.assertFalse(result.isOk());
        Assertions.assertEquals("queryresult.contract.noise", result.getCode());
    }

    @Test
    public void test_7() {
        QueryResultValidator validator =
                new QueryResultValidator();

        ValidationResult result =
                validator.validate(" success ");

        Console.log("queryresult.trim", String.valueOf(result));

        Assertions.assertTrue(result.isOk());
        Assertions.assertEquals("success", result.getMetadata().get("result"));
    }
}
