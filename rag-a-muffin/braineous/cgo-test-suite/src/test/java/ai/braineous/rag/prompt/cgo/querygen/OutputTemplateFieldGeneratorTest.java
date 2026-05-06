package ai.braineous.rag.prompt.cgo.querygen;

import ai.braineous.rag.prompt.cgo.api.GraphContext;
import ai.braineous.rag.prompt.cgo.api.Meta;
import ai.braineous.rag.prompt.cgo.api.ValidateTask;
import ai.braineous.rag.prompt.cgo.api.ValidationResult;
import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import ai.braineous.rag.prompt.cgo.querygen.model.FieldDefinition;
import ai.braineous.rag.prompt.cgo.querygen.model.FieldGenerationResult;
import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

public class OutputTemplateFieldGeneratorTest {

    @Test
    public void test_1() {
        OutputTemplateFieldGenerator generator = new OutputTemplateFieldGenerator();

        boolean supported = generator.supports(new FieldDefinition("output_template"));

        Console.log("output.template.supported", String.valueOf(supported));

        Assertions.assertTrue(supported);
    }

    @Test
    public void test_2() {
        OutputTemplateFieldGenerator generator = new OutputTemplateFieldGenerator();

        boolean supported = generator.supports(null);

        Console.log("output.template.supported.null", String.valueOf(supported));

        Assertions.assertFalse(supported);
    }

    @Test
    public void test_3() {
        OutputTemplateFieldGenerator generator = new OutputTemplateFieldGenerator();

        boolean supported = generator.supports(new FieldDefinition(null));

        Console.log("output.template.supported.null.name", String.valueOf(supported));

        Assertions.assertFalse(supported);
    }

    @Test
    public void test_4() {
        OutputTemplateFieldGenerator generator = new OutputTemplateFieldGenerator();

        boolean supported = generator.supports(new FieldDefinition("llm_instructions"));

        Console.log("output.template.supported.other", String.valueOf(supported));

        Assertions.assertFalse(supported);
    }

    @Test
    public void test_5() {
        OutputTemplateFieldGenerator generator = new OutputTemplateFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("output_template");

        QueryRequest request =
                buildRequest(Arrays.asList("decision", "reason", "code"));

        FieldGenerationResult result =
                generator.generate(fieldDefinition, request);

        JsonObject expected = new JsonObject();
        JsonObject expectedResult = new JsonObject();
        expectedResult.addProperty("decision", "");
        expectedResult.addProperty("reason", "");
        expectedResult.addProperty("code", "");
        expected.add("result", expectedResult);

        Console.log("output.template.actual", result.getFieldValue().toString());
        Console.log("output.template.expected", expected.toString());
        Console.log("output.template.validation", String.valueOf(result.getValidationResult()));

        Assertions.assertNotNull(result);
        Assertions.assertSame(fieldDefinition, result.getFieldDefinition());
        Assertions.assertEquals(expected, result.getFieldValue());

        ValidationResult validationResult = result.getValidationResult();

        Assertions.assertNotNull(validationResult);
        Assertions.assertTrue(validationResult.isOk());
        Assertions.assertEquals("field.output_template.ok", validationResult.getCode());
        Assertions.assertEquals("VALID", validationResult.getMessage());
        Assertions.assertEquals("field_generation", validationResult.getStage());
        Assertions.assertEquals("output_template", validationResult.getAnchorId());
        Assertions.assertNotNull(validationResult.getMetadata());
        Assertions.assertTrue(validationResult.getMetadata().isEmpty());
    }

    @Test
    public void test_6() {
        OutputTemplateFieldGenerator generator = new OutputTemplateFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("output_template");

        QueryRequest request =
                buildRequest(Arrays.asList("decision", "reason", "code"));

        FieldGenerationResult result =
                generator.generate(fieldDefinition, request);

        JsonObject outputTemplate = result.getFieldValue();
        JsonObject resultObject = outputTemplate.getAsJsonObject("result");

        Console.log("output.template.result", resultObject.toString());

        Assertions.assertEquals(3, resultObject.size());
        Assertions.assertTrue(resultObject.has("decision"));
        Assertions.assertTrue(resultObject.has("reason"));
        Assertions.assertTrue(resultObject.has("code"));

        Assertions.assertEquals("", resultObject.get("decision").getAsString());
        Assertions.assertEquals("", resultObject.get("reason").getAsString());
        Assertions.assertEquals("", resultObject.get("code").getAsString());
    }

    @Test
    public void test_7() {
        OutputTemplateFieldGenerator generator = new OutputTemplateFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("output_template");

        QueryRequest request =
                buildRequest(Arrays.asList("code", "decision", "reason"));

        FieldGenerationResult result =
                generator.generate(fieldDefinition, request);

        JsonObject expected = new JsonObject();
        JsonObject expectedResult = new JsonObject();
        expectedResult.addProperty("code", "");
        expectedResult.addProperty("decision", "");
        expectedResult.addProperty("reason", "");
        expected.add("result", expectedResult);

        Console.log("output.template.order.actual", result.getFieldValue().toString());
        Console.log("output.template.order.expected", expected.toString());

        Assertions.assertEquals(expected, result.getFieldValue());
    }

    @Test
    public void test_8() {
        OutputTemplateFieldGenerator generator = new OutputTemplateFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("output_template");

        QueryRequest request = buildRequest(null);

        FieldGenerationResult result =
                generator.generate(fieldDefinition, request);

        JsonObject outputTemplate = result.getFieldValue();
        JsonObject resultObject = outputTemplate.getAsJsonObject("result");

        Console.log("output.template.null.fields", outputTemplate.toString());

        Assertions.assertNotNull(resultObject);
        Assertions.assertEquals(0, resultObject.size());
    }

    @Test
    public void test_9() {
        OutputTemplateFieldGenerator generator = new OutputTemplateFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("output_template");

        QueryRequest request =
                buildRequest(Collections.<String>emptyList());

        FieldGenerationResult result =
                generator.generate(fieldDefinition, request);

        JsonObject outputTemplate = result.getFieldValue();
        JsonObject resultObject = outputTemplate.getAsJsonObject("result");

        Console.log("output.template.empty.fields", outputTemplate.toString());

        Assertions.assertNotNull(resultObject);
        Assertions.assertEquals(0, resultObject.size());
    }

    @Test
    public void test_10() {
        OutputTemplateFieldGenerator generator = new OutputTemplateFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("output_template");

        QueryRequest request =
                buildRequest(Arrays.asList("decision", null, "code"));

        FieldGenerationResult result =
                generator.generate(fieldDefinition, request);

        JsonObject outputTemplate = result.getFieldValue();
        JsonObject resultObject = outputTemplate.getAsJsonObject("result");

        Console.log("output.template.null.item", outputTemplate.toString());

        Assertions.assertEquals(2, resultObject.size());
        Assertions.assertTrue(resultObject.has("decision"));
        Assertions.assertTrue(resultObject.has("code"));
        Assertions.assertFalse(resultObject.has("null"));

        Assertions.assertEquals("", resultObject.get("decision").getAsString());
        Assertions.assertEquals("", resultObject.get("code").getAsString());
    }

    @Test
    public void test_11() {
        OutputTemplateFieldGenerator generator = new OutputTemplateFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("output_template");

        FieldGenerationResult result =
                generator.generate(fieldDefinition, null);

        JsonObject outputTemplate = result.getFieldValue();
        JsonObject resultObject = outputTemplate.getAsJsonObject("result");

        Console.log("output.template.null.request", outputTemplate.toString());

        Assertions.assertNotNull(resultObject);
        Assertions.assertEquals(0, resultObject.size());
        Assertions.assertTrue(result.getValidationResult().isOk());
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
                                "PaymentMethod:PM-3001"
                        )
                );

        return new QueryRequest(meta, context, task);
    }
}