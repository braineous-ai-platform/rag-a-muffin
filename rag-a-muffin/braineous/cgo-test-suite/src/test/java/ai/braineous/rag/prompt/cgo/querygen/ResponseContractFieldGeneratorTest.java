package ai.braineous.rag.prompt.cgo.querygen;

import ai.braineous.rag.prompt.cgo.api.GraphContext;
import ai.braineous.rag.prompt.cgo.api.Meta;
import ai.braineous.rag.prompt.cgo.api.ValidateTask;
import ai.braineous.rag.prompt.cgo.api.ValidationResult;
import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import ai.braineous.rag.prompt.cgo.query.QueryTask;
import ai.braineous.rag.prompt.cgo.querygen.model.FieldDefinition;
import ai.braineous.rag.prompt.cgo.querygen.model.FieldGenerationResult;
import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

public class ResponseContractFieldGeneratorTest {

    @Test
    public void test_1() {
        ResponseContractFieldGenerator generator =
                new ResponseContractFieldGenerator();

        boolean supported =
                generator.supports(new FieldDefinition("response_contract"));

        Console.log("response.contract.supported", String.valueOf(supported));

        Assertions.assertTrue(supported);
    }

    @Test
    public void test_2() {
        ResponseContractFieldGenerator generator =
                new ResponseContractFieldGenerator();

        boolean supported =
                generator.supports(null);

        Console.log("response.contract.supported.null", String.valueOf(supported));

        Assertions.assertFalse(supported);
    }

    @Test
    public void test_3() {
        ResponseContractFieldGenerator generator =
                new ResponseContractFieldGenerator();

        boolean supported =
                generator.supports(new FieldDefinition(null));

        Console.log("response.contract.supported.null.name", String.valueOf(supported));

        Assertions.assertFalse(supported);
    }

    @Test
    public void test_4() {
        ResponseContractFieldGenerator generator =
                new ResponseContractFieldGenerator();

        boolean supported =
                generator.supports(new FieldDefinition("llm_instructions"));

        Console.log("response.contract.supported.other", String.valueOf(supported));

        Assertions.assertFalse(supported);
    }

    @Test
    public void test_5() {
        ResponseContractFieldGenerator generator =
                new ResponseContractFieldGenerator();

        FieldDefinition fieldDefinition =
                new FieldDefinition("response_contract");

        QueryRequest request =
                buildRequest(
                        Arrays.asList("decision", "reason", "code")
                );

        FieldGenerationResult result =
                generator.generate(fieldDefinition, request);

        JsonObject expected =
                buildExpected(
                        Arrays.asList("decision", "reason", "code")
                );

        Console.log("response.contract.actual", result.getFieldValue().toString());
        Console.log("response.contract.expected", expected.toString());
        Console.log("response.contract.validation", String.valueOf(result.getValidationResult()));

        Assertions.assertNotNull(result);
        Assertions.assertSame(fieldDefinition, result.getFieldDefinition());
        Assertions.assertEquals(expected, result.getFieldValue());

        ValidationResult validationResult =
                result.getValidationResult();

        Assertions.assertNotNull(validationResult);
        Assertions.assertTrue(validationResult.isOk());
        Assertions.assertEquals("field.response_contract.ok", validationResult.getCode());
        Assertions.assertEquals("VALID", validationResult.getMessage());
        Assertions.assertEquals("field_generation", validationResult.getStage());
        Assertions.assertEquals("response_contract", validationResult.getAnchorId());
        Assertions.assertNotNull(validationResult.getMetadata());
        Assertions.assertTrue(validationResult.getMetadata().isEmpty());
    }

    @Test
    public void test_6() {
        ResponseContractFieldGenerator generator =
                new ResponseContractFieldGenerator();

        FieldDefinition fieldDefinition =
                new FieldDefinition("response_contract");

        QueryRequest request =
                buildRequest(Collections.<String>emptyList());

        FieldGenerationResult result =
                generator.generate(fieldDefinition, request);

        JsonObject expected =
                buildExpected(Collections.<String>emptyList());

        Console.log("response.contract.empty.actual", result.getFieldValue().toString());
        Console.log("response.contract.empty.expected", expected.toString());

        Assertions.assertEquals(expected, result.getFieldValue());
        Assertions.assertTrue(result.getValidationResult().isOk());
    }

    @Test
    public void test_7() {
        ResponseContractFieldGenerator generator =
                new ResponseContractFieldGenerator();

        FieldDefinition fieldDefinition =
                new FieldDefinition("response_contract");

        QueryRequest request =
                buildRequest(
                        Arrays.asList("decision", null, "code")
                );

        FieldGenerationResult result =
                generator.generate(fieldDefinition, request);

        JsonObject fields =
                result.getFieldValue()
                        .getAsJsonObject("schema")
                        .getAsJsonObject("result")
                        .getAsJsonObject("fields");

        Console.log("response.contract.skip.null.actual", result.getFieldValue().toString());

        Assertions.assertEquals(2, fields.entrySet().size());
        Assertions.assertTrue(fields.has("decision"));
        Assertions.assertTrue(fields.has("code"));
        Assertions.assertFalse(fields.has("null"));
        Assertions.assertEquals("string", fields.get("decision").getAsString());
        Assertions.assertEquals("string", fields.get("code").getAsString());
    }

    @Test
    public void test_8() {
        ResponseContractFieldGenerator generator =
                new ResponseContractFieldGenerator();

        FieldDefinition fieldDefinition =
                new FieldDefinition("response_contract");

        FieldGenerationResult result =
                generator.generate(fieldDefinition, null);

        JsonObject fields =
                result.getFieldValue()
                        .getAsJsonObject("schema")
                        .getAsJsonObject("result")
                        .getAsJsonObject("fields");

        Console.log("response.contract.null.request", result.getFieldValue().toString());

        Assertions.assertEquals("execution_result", result.getFieldValue().get("type").getAsString());
        Assertions.assertEquals(0, fields.entrySet().size());
        Assertions.assertTrue(result.getValidationResult().isOk());
    }

    @Test
    public void test_9() {
        ResponseContractFieldGenerator generator =
                new ResponseContractFieldGenerator();

        FieldDefinition fieldDefinition =
                new FieldDefinition("response_contract");

        QueryRequest request =
                new QueryRequest(
                        new Meta(
                                "v1",
                                "non_validate",
                                "non validate task"
                        ),
                        new GraphContext(),
                        new FakeQueryTask()
                );

        FieldGenerationResult result =
                generator.generate(fieldDefinition, request);

        JsonObject fields =
                result.getFieldValue()
                        .getAsJsonObject("schema")
                        .getAsJsonObject("result")
                        .getAsJsonObject("fields");

        Console.log("response.contract.non.validate.task", result.getFieldValue().toString());

        Assertions.assertEquals("execution_result", result.getFieldValue().get("type").getAsString());
        Assertions.assertEquals(0, fields.entrySet().size());
        Assertions.assertTrue(result.getValidationResult().isOk());
    }

    @Test
    public void test_10() {
        ResponseContractFieldGenerator generator =
                new ResponseContractFieldGenerator();

        FieldDefinition fieldDefinition =
                new FieldDefinition("response_contract");

        QueryRequest request =
                buildRequest(
                        Arrays.asList("decision", "reason", "code")
                );

        FieldGenerationResult result =
                generator.generate(fieldDefinition, request);

        JsonObject responseContract =
                result.getFieldValue();

        Console.log("response.contract.execution.semantic", responseContract.toString());

        Assertions.assertEquals(
                "execution_result",
                responseContract.get("type").getAsString()
        );

        Assertions.assertEquals(
                "Deterministic execution response contract derived from selected fields.",
                responseContract.get("description").getAsString()
        );

        Assertions.assertFalse(responseContract.toString().contains("validation_result"));
    }

    private QueryRequest buildRequest(java.util.List<String> requestedFields) {
        Meta meta =
                new Meta(
                        "v1",
                        "decision",
                        "pay decision"
                );

        GraphContext context =
                new GraphContext();

        ValidateTask task =
                new ValidateTask(
                        "decision",
                        "PaymentRequest:PAY-1001",
                        requestedFields,
                        Arrays.asList(
                                "CustomerAccount:CUST-2001",
                                "RiskProfile:RISK-4001"
                        )
                );

        return new QueryRequest(meta, context, task);
    }

    private JsonObject buildExpected(java.util.List<String> requestedFields) {
        JsonObject expected =
                new JsonObject();

        expected.addProperty("type", "execution_result");
        expected.addProperty(
                "description",
                "Deterministic execution response contract derived from selected fields."
        );

        JsonObject schema =
                new JsonObject();

        JsonObject result =
                new JsonObject();

        JsonObject fields =
                new JsonObject();

        int i = 0;
        while (i < requestedFields.size()) {
            String field =
                    requestedFields.get(i);

            if (field != null) {
                fields.addProperty(field, "string");
            }

            i++;
        }

        result.add("fields", fields);
        schema.add("result", result);
        expected.add("schema", schema);

        return expected;
    }

    private static class FakeQueryTask implements QueryTask {

        @Override
        public String getDescription() {
            return "fake task";
        }

        @Override
        public JsonObject toJson() {
            JsonObject json =
                    new JsonObject();

            json.addProperty("description", "fake task");

            return json;
        }
    }
}