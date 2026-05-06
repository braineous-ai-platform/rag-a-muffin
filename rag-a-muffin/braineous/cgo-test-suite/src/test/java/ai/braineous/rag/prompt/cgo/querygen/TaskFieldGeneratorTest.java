package ai.braineous.rag.prompt.cgo.querygen;

import ai.braineous.rag.prompt.cgo.api.Control;
import ai.braineous.rag.prompt.cgo.api.GraphContext;
import ai.braineous.rag.prompt.cgo.api.Meta;
import ai.braineous.rag.prompt.cgo.api.ValidateTask;
import ai.braineous.rag.prompt.cgo.api.ValidationResult;
import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import ai.braineous.rag.prompt.cgo.querygen.model.FieldDefinition;
import ai.braineous.rag.prompt.cgo.querygen.model.FieldGenerationResult;
import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class TaskFieldGeneratorTest {

    @Test
    public void test_1() {
        TaskFieldGenerator generator = new TaskFieldGenerator();

        boolean supported = generator.supports(new FieldDefinition("task"));

        Console.log("task.supported", String.valueOf(supported));

        Assertions.assertTrue(supported);
    }

    @Test
    public void test_2() {
        TaskFieldGenerator generator = new TaskFieldGenerator();

        boolean supported = generator.supports(null);

        Console.log("task.supported.null", String.valueOf(supported));

        Assertions.assertFalse(supported);
    }

    @Test
    public void test_3() {
        TaskFieldGenerator generator = new TaskFieldGenerator();

        boolean supported = generator.supports(new FieldDefinition(null));

        Console.log("task.supported.null.name", String.valueOf(supported));

        Assertions.assertFalse(supported);
    }

    @Test
    public void test_4() {
        TaskFieldGenerator generator = new TaskFieldGenerator();

        boolean supported = generator.supports(new FieldDefinition("output_template"));

        Console.log("task.supported.other", String.valueOf(supported));

        Assertions.assertFalse(supported);
    }

    @Test
    public void test_5() {
        TaskFieldGenerator generator = new TaskFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("task");

        QueryRequest request = buildRequestWithControls();

        FieldGenerationResult result = generator.generate(fieldDefinition, request);

        JsonObject expected = buildExpectedTaskWithControls();

        Console.log("task.generate.actual", result.getFieldValue().toString());
        Console.log("task.generate.expected", expected.toString());
        Console.log("task.generate.validation", String.valueOf(result.getValidationResult()));

        Assertions.assertNotNull(result);
        Assertions.assertSame(fieldDefinition, result.getFieldDefinition());
        Assertions.assertEquals(expected, result.getFieldValue());

        ValidationResult validationResult = result.getValidationResult();

        Assertions.assertNotNull(validationResult);
        Assertions.assertTrue(validationResult.isOk());
        Assertions.assertEquals("field.task.ok", validationResult.getCode());
        Assertions.assertEquals("VALID", validationResult.getMessage());
        Assertions.assertEquals("field_generation", validationResult.getStage());
        Assertions.assertEquals("task", validationResult.getAnchorId());
        Assertions.assertNotNull(validationResult.getMetadata());
        Assertions.assertTrue(validationResult.getMetadata().isEmpty());
    }

    @Test
    public void test_6() {
        TaskFieldGenerator generator = new TaskFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("task");

        QueryRequest request = buildRequestWithControls();

        FieldGenerationResult result = generator.generate(fieldDefinition, request);

        JsonObject task = result.getFieldValue();
        JsonObject intent = task.getAsJsonObject("intent");
        JsonArray relatedFactIds = task.getAsJsonArray("relatedFactIds");
        JsonArray select = task.getAsJsonArray("select");
        JsonObject controls = task.getAsJsonObject("controls");

        Console.log("task.intent.goal", intent.get("goal").getAsString());
        Console.log("task.factId", task.get("factId").getAsString());
        Console.log("task.relatedFactIds", relatedFactIds.toString());
        Console.log("task.select", select.toString());
        Console.log("task.controls", controls.toString());

        Assertions.assertEquals("decision", intent.get("goal").getAsString());
        Assertions.assertEquals("PaymentRequest:PAY-1001", task.get("factId").getAsString());

        Assertions.assertEquals(2, relatedFactIds.size());
        Assertions.assertEquals("CustomerAccount:CUST-2001", relatedFactIds.get(0).getAsString());
        Assertions.assertEquals("PaymentMethod:PM-3001", relatedFactIds.get(1).getAsString());

        Assertions.assertEquals(3, select.size());
        Assertions.assertEquals("decision", select.get(0).getAsString());
        Assertions.assertEquals("reason", select.get(1).getAsString());
        Assertions.assertEquals("code", select.get(2).getAsString());

        Assertions.assertEquals("decide_payment_capture", controls.get("intent").getAsString());
        Assertions.assertEquals("true", controls.get("include_reason").getAsString());
        Assertions.assertEquals("true", controls.get("include_code").getAsString());
    }

    @Test
    public void test_7() {
        TaskFieldGenerator generator = new TaskFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("task");

        QueryRequest request = buildRequestWithControls();

        FieldGenerationResult result = generator.generate(fieldDefinition, request);

        JsonObject task = result.getFieldValue();

        Console.log("task.no.constraints", task.toString());

        Assertions.assertFalse(task.has("constraints"));
    }

    @Test
    public void test_8() {
        TaskFieldGenerator generator = new TaskFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("task");

        QueryRequest request = buildRequestWithNullControls();

        FieldGenerationResult result = generator.generate(fieldDefinition, request);

        JsonObject task = result.getFieldValue();
        JsonObject controls = task.getAsJsonObject("controls");

        Console.log("task.controls.null", task.toString());
        Console.log("task.controls.null.size", String.valueOf(controls.size()));

        Assertions.assertNotNull(controls);
        Assertions.assertEquals(0, controls.size());
        Assertions.assertTrue(task.has("intent"));
        Assertions.assertTrue(task.has("factId"));
        Assertions.assertTrue(task.has("relatedFactIds"));
        Assertions.assertTrue(task.has("select"));
    }

    @Test
    public void test_9() {
        TaskFieldGenerator generator = new TaskFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("task");

        QueryRequest request = buildRequestWithControls();

        FieldGenerationResult result = generator.generate(fieldDefinition, request);

        JsonObject task = result.getFieldValue();
        String actual = task.toString();

        Console.log("task.controls.order", actual);

        Assertions.assertTrue(actual.indexOf("\"intent\"") < actual.indexOf("\"include_reason\""));
        Assertions.assertTrue(actual.indexOf("\"include_reason\"") < actual.indexOf("\"include_code\""));
    }

    @Test
    public void test_10() {
        TaskFieldGenerator generator = new TaskFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("task");

        QueryRequest request = buildRequestWithNullKeyAndNullValueControls();

        FieldGenerationResult result = generator.generate(fieldDefinition, request);

        JsonObject task = result.getFieldValue();
        JsonObject controls = task.getAsJsonObject("controls");

        Console.log("task.controls.null.key.value", task.toString());
        Console.log("task.controls.has.include_reason", String.valueOf(controls.has("include_reason")));
        Console.log("task.controls.include_reason.isJsonNull", String.valueOf(controls.get("include_reason").isJsonNull()));

        Assertions.assertFalse(controls.has("ignored_null_key"));
        Assertions.assertEquals("decide_payment_capture", controls.get("intent").getAsString());
        Assertions.assertTrue(controls.has("include_reason"));
        Assertions.assertTrue(controls.get("include_reason").isJsonNull());
    }

    @Test
    public void test_11() {
        TaskFieldGenerator generator = new TaskFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("task");

        FieldGenerationResult result = generator.generate(fieldDefinition, null);

        JsonObject task = result.getFieldValue();

        Console.log("task.null.request", task.toString());

        Assertions.assertNotNull(task);
        Assertions.assertEquals(0, task.size());
        Assertions.assertTrue(result.getValidationResult().isOk());
    }

    private QueryRequest buildRequestWithControls() {
        Meta meta =
                new Meta(
                        "v1",
                        "decision",
                        "pay decision"
                );

        GraphContext context =
                new GraphContext();

        List<Control> controls =
                Arrays.asList(
                        new Control("intent", "decide_payment_capture"),
                        new Control("include_reason", "true"),
                        new Control("include_code", "true")
                );

        ValidateTask task =
                new ValidateTask(
                        "decision",
                        "PaymentRequest:PAY-1001",
                        Arrays.asList("decision", "reason", "code"),
                        Arrays.asList(
                                "CustomerAccount:CUST-2001",
                                "PaymentMethod:PM-3001"
                        )
                );

        task.setControls(controls);

        return new QueryRequest(meta, context, task);
    }

    private QueryRequest buildRequestWithNullControls() {
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
                        Arrays.asList("decision", "reason", "code"),
                        Arrays.asList(
                                "CustomerAccount:CUST-2001",
                                "PaymentMethod:PM-3001"
                        )
                );

        task.setControls(null);

        return new QueryRequest(meta, context, task);
    }

    private QueryRequest buildRequestWithNullKeyAndNullValueControls() {
        Meta meta =
                new Meta(
                        "v1",
                        "decision",
                        "pay decision"
                );

        GraphContext context =
                new GraphContext();

        List<Control> controls =
                Arrays.asList(
                        new Control("intent", "decide_payment_capture"),
                        new Control(null, "ignored_value"),
                        new Control("include_reason", null)
                );

        ValidateTask task =
                new ValidateTask(
                        "decision",
                        "PaymentRequest:PAY-1001",
                        Arrays.asList("decision", "reason", "code"),
                        Arrays.asList(
                                "CustomerAccount:CUST-2001",
                                "PaymentMethod:PM-3001"
                        )
                );

        task.setControls(controls);

        return new QueryRequest(meta, context, task);
    }

    private JsonObject buildExpectedTaskWithControls() {
        JsonObject expected = new JsonObject();

        JsonObject intent = new JsonObject();
        intent.addProperty("goal", "decision");
        expected.add("intent", intent);

        expected.addProperty("factId", "PaymentRequest:PAY-1001");

        JsonArray relatedFactIds = new JsonArray();
        relatedFactIds.add("CustomerAccount:CUST-2001");
        relatedFactIds.add("PaymentMethod:PM-3001");
        expected.add("relatedFactIds", relatedFactIds);

        JsonArray select = new JsonArray();
        select.add("decision");
        select.add("reason");
        select.add("code");
        expected.add("select", select);

        JsonObject controls = new JsonObject();
        controls.addProperty("intent", "decide_payment_capture");
        controls.addProperty("include_reason", "true");
        controls.addProperty("include_code", "true");
        expected.add("controls", controls);

        return expected;
    }
}