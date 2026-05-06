package ai.braineous.rag.prompt.cgo.querygen;

import ai.braineous.rag.prompt.cgo.api.GraphContext;
import ai.braineous.rag.prompt.cgo.api.Meta;
import ai.braineous.rag.prompt.cgo.api.ValidateTask;
import ai.braineous.rag.prompt.cgo.api.ValidationResult;
import ai.braineous.rag.prompt.cgo.query.Node;
import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import ai.braineous.rag.prompt.cgo.querygen.model.FieldDefinition;
import ai.braineous.rag.prompt.cgo.querygen.model.FieldGenerationResult;
import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class ContextFieldGeneratorTest {

    @Test
    public void test_1() {
        ContextFieldGenerator generator = new ContextFieldGenerator();

        boolean supported = generator.supports(new FieldDefinition("context"));

        Console.log("context.supported", String.valueOf(supported));

        Assertions.assertTrue(supported);
    }

    @Test
    public void test_2() {
        ContextFieldGenerator generator = new ContextFieldGenerator();

        boolean supported = generator.supports(null);

        Console.log("context.supported.null", String.valueOf(supported));

        Assertions.assertFalse(supported);
    }

    @Test
    public void test_3() {
        ContextFieldGenerator generator = new ContextFieldGenerator();

        boolean supported = generator.supports(new FieldDefinition(null));

        Console.log("context.supported.null.name", String.valueOf(supported));

        Assertions.assertFalse(supported);
    }

    @Test
    public void test_4() {
        ContextFieldGenerator generator = new ContextFieldGenerator();

        boolean supported = generator.supports(new FieldDefinition("task"));

        Console.log("context.supported.other", String.valueOf(supported));

        Assertions.assertFalse(supported);
    }

    @Test
    public void test_5() {
        ContextFieldGenerator generator = new ContextFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("context");

        QueryRequest request = buildRequest();

        FieldGenerationResult result =
                generator.generate(fieldDefinition, request);

        JsonObject expected = buildExpectedContextJson();

        Console.log("context.generate.actual", result.getFieldValue().toString());
        Console.log("context.generate.expected", expected.toString());
        Console.log("context.generate.validation", String.valueOf(result.getValidationResult()));

        Assertions.assertNotNull(result);
        Assertions.assertSame(fieldDefinition, result.getFieldDefinition());

        assertExpectedNodes(expected, result.getFieldValue());

        ValidationResult validationResult = result.getValidationResult();

        Assertions.assertNotNull(validationResult);
        Assertions.assertTrue(validationResult.isOk());
        Assertions.assertEquals("field.context.ok", validationResult.getCode());
        Assertions.assertEquals("VALID", validationResult.getMessage());
        Assertions.assertEquals("field_generation", validationResult.getStage());
        Assertions.assertEquals("context", validationResult.getAnchorId());
        Assertions.assertNotNull(validationResult.getMetadata());
        Assertions.assertTrue(validationResult.getMetadata().isEmpty());
    }

    @Test
    public void test_6() {
        ContextFieldGenerator generator = new ContextFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("context");

        QueryRequest request = buildRequest();

        FieldGenerationResult result =
                generator.generate(fieldDefinition, request);

        JsonObject nodes =
                result.getFieldValue().getAsJsonObject("nodes");

        JsonObject paymentNode =
                nodes.getAsJsonObject("PaymentRequest:PAY-1001");

        JsonObject customerNode =
                nodes.getAsJsonObject("CustomerAccount:CUST-2001");

        Console.log("context.nodes.size", String.valueOf(nodes.entrySet().size()));
        Console.log("context.payment.node", paymentNode.toString());
        Console.log("context.customer.node", customerNode.toString());

        Assertions.assertEquals(2, nodes.entrySet().size());

        Assertions.assertNotNull(paymentNode);
        Assertions.assertEquals(
                "PaymentRequest:PAY-1001",
                paymentNode.get("id").getAsString()
        );

        Assertions.assertEquals(
                "{\"paymentId\":\"PAY-1001\",\"amount\":\"125.00\",\"currency\":\"USD\",\"risk\":\"LOW\"}",
                paymentNode.get("text").getAsString()
        );

        Assertions.assertEquals(
                "RELATIONAL",
                paymentNode.get("mode").getAsString()
        );

        Assertions.assertNotNull(customerNode);

        Assertions.assertEquals(
                "CustomerAccount:CUST-2001",
                customerNode.get("id").getAsString()
        );

        Assertions.assertEquals(
                "{\"customerId\":\"CUST-2001\",\"status\":\"ACTIVE\",\"segment\":\"TRUSTED\"}",
                customerNode.get("text").getAsString()
        );

        Assertions.assertEquals(
                "RELATIONAL",
                customerNode.get("mode").getAsString()
        );
    }

    @Test
    public void test_7() {
        ContextFieldGenerator generator = new ContextFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("context");

        FieldGenerationResult result =
                generator.generate(fieldDefinition, null);

        JsonObject context = result.getFieldValue();

        Console.log("context.null.request", context.toString());

        Assertions.assertNotNull(context);
        Assertions.assertEquals(0, context.size());

        Assertions.assertTrue(result.getValidationResult().isOk());
    }

    @Test
    public void test_8() {
        ContextFieldGenerator generator = new ContextFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("context");

        Meta meta =
                new Meta(
                        "v1",
                        "decision",
                        "pay decision"
                );

        ValidateTask task =
                new ValidateTask(
                        "decision",
                        "PaymentRequest:PAY-1001",
                        Arrays.asList("decision", "reason", "code"),
                        Arrays.asList("CustomerAccount:CUST-2001")
                );

        QueryRequest request =
                new QueryRequest(meta, null, task, null, null);

        FieldGenerationResult result =
                generator.generate(fieldDefinition, request);

        JsonObject context = result.getFieldValue();

        Console.log("context.null.graph", context.toString());

        Assertions.assertNotNull(context);
        Assertions.assertEquals(0, context.size());
        Assertions.assertTrue(result.getValidationResult().isOk());
    }

    private void assertExpectedNodes(JsonObject expected, JsonObject actual) {
        JsonObject expectedNodes =
                expected.getAsJsonObject("nodes");

        JsonObject actualNodes =
                actual.getAsJsonObject("nodes");

        Assertions.assertNotNull(actualNodes);
        Assertions.assertEquals(expectedNodes.size(), actualNodes.size());

        Assertions.assertTrue(actualNodes.has("PaymentRequest:PAY-1001"));
        Assertions.assertTrue(actualNodes.has("CustomerAccount:CUST-2001"));

        Assertions.assertEquals(
                expectedNodes.getAsJsonObject("PaymentRequest:PAY-1001"),
                actualNodes.getAsJsonObject("PaymentRequest:PAY-1001")
        );

        Assertions.assertEquals(
                expectedNodes.getAsJsonObject("CustomerAccount:CUST-2001"),
                actualNodes.getAsJsonObject("CustomerAccount:CUST-2001")
        );
    }

    private QueryRequest buildRequest() {
        Meta meta =
                new Meta(
                        "v1",
                        "decision",
                        "pay decision"
                );

        Map<String, Node> nodes =
                new LinkedHashMap<String, Node>();

        nodes.put(
                "PaymentRequest:PAY-1001",
                new Node(
                        "PaymentRequest:PAY-1001",
                        "{\"paymentId\":\"PAY-1001\",\"amount\":\"125.00\",\"currency\":\"USD\",\"risk\":\"LOW\"}",
                        Arrays.asList(),
                        Node.Mode.RELATIONAL
                )
        );

        nodes.put(
                "CustomerAccount:CUST-2001",
                new Node(
                        "CustomerAccount:CUST-2001",
                        "{\"customerId\":\"CUST-2001\",\"status\":\"ACTIVE\",\"segment\":\"TRUSTED\"}",
                        Arrays.asList(),
                        Node.Mode.RELATIONAL
                )
        );

        GraphContext context =
                new GraphContext(nodes);

        ValidateTask task =
                new ValidateTask(
                        "decision",
                        "PaymentRequest:PAY-1001",
                        Arrays.asList("decision", "reason", "code"),
                        Arrays.asList(
                                "CustomerAccount:CUST-2001"
                        )
                );

        return new QueryRequest(meta, context, task);
    }

    private JsonObject buildExpectedContextJson() {
        JsonObject expected = new JsonObject();

        JsonObject nodes = new JsonObject();

        JsonObject paymentNode = new JsonObject();
        paymentNode.addProperty("id", "PaymentRequest:PAY-1001");
        paymentNode.addProperty(
                "text",
                "{\"paymentId\":\"PAY-1001\",\"amount\":\"125.00\",\"currency\":\"USD\",\"risk\":\"LOW\"}"
        );
        paymentNode.add("attributes", new JsonArray());
        paymentNode.addProperty("mode", "RELATIONAL");

        JsonObject customerNode = new JsonObject();
        customerNode.addProperty("id", "CustomerAccount:CUST-2001");
        customerNode.addProperty(
                "text",
                "{\"customerId\":\"CUST-2001\",\"status\":\"ACTIVE\",\"segment\":\"TRUSTED\"}"
        );
        customerNode.add("attributes", new JsonArray());
        customerNode.addProperty("mode", "RELATIONAL");

        nodes.add("PaymentRequest:PAY-1001", paymentNode);
        nodes.add("CustomerAccount:CUST-2001", customerNode);

        expected.add("nodes", nodes);

        return expected;
    }
}