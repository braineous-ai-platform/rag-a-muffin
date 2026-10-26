package ai.braineous.rag.prompt.cgo.querygen.services;

import ai.braineous.rag.prompt.cgo.api.Control;
import ai.braineous.rag.prompt.cgo.api.GraphContext;
import ai.braineous.rag.prompt.cgo.api.Meta;
import ai.braineous.rag.prompt.cgo.api.ValidateTask;
import ai.braineous.rag.prompt.cgo.query.Node;
import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import ai.braineous.rag.prompt.cgo.querygen.model.QueryGenOutput;
import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class QueryGenServiceTest {

    @Test
    public void test_1() {
        QueryGenService service = new QueryGenService();
        QueryRequest request = buildRequest();

        QueryGenOutput output = service.generateQuery(request);

        Console.log("querygen.output.payload", output.getPayload().toString());
        Console.log("querygen.output.validation", String.valueOf(output.getValidationResult()));

        Assertions.assertNotNull(output);
        Assertions.assertNotNull(output.getPayload());
        Assertions.assertNotNull(output.getValidationResult());
        Assertions.assertTrue(output.getValidationResult().isOk());
    }

    @Test
    public void test_2() {
        QueryGenService service = new QueryGenService();
        QueryRequest request = buildRequest();

        QueryGenOutput output = service.generateQuery(request);
        JsonObject payload = output.getPayload();

        Console.log("querygen.payload", payload.toString());
        Console.log("querygen.payload.keys", payload.keySet().toString());
        Console.log("querygen.payload.size", String.valueOf(payload.size()));

        Assertions.assertEquals(3, payload.size());
        Assertions.assertTrue(payload.has("llm_instructions"));
        Assertions.assertTrue(payload.has("task"));
        Assertions.assertTrue(payload.has("context"));
        Assertions.assertFalse(payload.has("llm_trace"));
        Assertions.assertFalse(payload.has("llm_trace_instructions"));
        Assertions.assertFalse(payload.has("meta"));
        Assertions.assertFalse(payload.has("output_template"));
        Assertions.assertFalse(payload.has("response_contract"));
    }

    @Test
    public void test_3() {
        QueryGenService service = new QueryGenService();
        QueryRequest request = buildRequest();

        QueryGenOutput output = service.generateQuery(request);
        JsonObject payload = output.getPayload();

        String serialized = payload.toString();

        Console.log("querygen.payload.order", serialized);

        Assertions.assertFalse(payload.has("meta"));
        Assertions.assertFalse(payload.has("llm_trace"));
        Assertions.assertFalse(payload.has("llm_trace_instructions"));
        Assertions.assertTrue(serialized.indexOf("\"llm_instructions\"") < serialized.indexOf("\"task\""));
        Assertions.assertTrue(serialized.indexOf("\"task\"") < serialized.indexOf("\"context\""));
    }

    @Test
    public void test_4() {
        QueryGenService service = new QueryGenService();
        QueryRequest request = buildRequest();

        QueryGenOutput output = service.generateQuery(request);
        JsonObject payload = output.getPayload();

        JsonObject task = payload.getAsJsonObject("task");
        JsonObject context = payload.getAsJsonObject("context");
        JsonObject llmInstructions = payload.getAsJsonObject("llm_instructions");

        Console.log("querygen.task", task.toString());
        Console.log("querygen.output.template.absent", String.valueOf(payload.has("output_template")));
        Console.log("querygen.response.contract.absent", String.valueOf(payload.has("response_contract")));
        Console.log("querygen.context", context.toString());
        Console.log("querygen.instructions", llmInstructions.toString());

        Assertions.assertFalse(payload.has("output_template"));
        Assertions.assertFalse(payload.has("response_contract"));

        Assertions.assertTrue(task.isJsonObject());
        Assertions.assertEquals(1, task.size());
        Assertions.assertTrue(task.has("controls"));
        Assertions.assertFalse(task.has("intent"));
        Assertions.assertFalse(task.has("factId"));
        Assertions.assertFalse(task.has("relatedFactIds"));
        Assertions.assertFalse(task.has("select"));

        JsonObject controls = task.getAsJsonObject("controls");
        Assertions.assertTrue(controls.isJsonObject());
        Assertions.assertEquals("decide_payment_capture", controls.get("intent").getAsString());
        Assertions.assertEquals("true", controls.get("include_reason").getAsString());
        Assertions.assertEquals("true", controls.get("include_code").getAsString());

        JsonObject nodes = context.getAsJsonObject("nodes");
        Assertions.assertTrue(nodes.has("PaymentRequest:PAY-1001"));
        Assertions.assertTrue(nodes.has("CustomerAccount:CUST-2001"));

        JsonArray instructions = llmInstructions.getAsJsonArray("instructions");
        Console.log("querygen.instructions.verbatim", instructions.toString());
        int instructionIndex = 0;
        while (instructionIndex < instructions.size()) {
            Console.log(
                    "querygen.instruction." + instructionIndex,
                    instructions.get(instructionIndex).getAsString()
            );
            instructionIndex = instructionIndex + 1;
        }

        JsonArray expectedInstructions =
                loadV14LlmInstructions();

        Console.log("querygen.instructions.expected", expectedInstructions.toString());

        Assertions.assertEquals(12, instructions.size());
        Assertions.assertEquals(expectedInstructions, instructions);

        assertInstructionDoesNotExist(instructions, "Use the requested SELECT fields as keys.");
        assertInstructionDoesNotExist(instructions, "Set result.decision as a string value derived from llm_query execution.");
        assertInstructionDoesNotExist(instructions, "Set result.reason as a string value derived from llm_query execution.");
        assertInstructionDoesNotExist(instructions, "Set result.code as a string value derived from llm_query execution.");
    }

    @Test
    public void test_5() {
        QueryGenService service = new QueryGenService();
        QueryRequest request = buildRequest();

        QueryGenOutput output = service.generateQuery(request);
        JsonObject payload = output.getPayload();

        Console.log("querygen.no.constraints", payload.toString());

        Assertions.assertFalse(payload.getAsJsonObject("task").has("constraints"));
    }

    @Test
    public void test_6() {
        QueryGenService service = new QueryGenService();

        JsonObject reference =
                loadV14Query();

        QueryRequest request =
                buildPayV14Request(reference);

        QueryGenOutput output =
                service.generateQuery(request);

        JsonObject generated =
                output.getPayload();

        Console.log("querygen.v14.generated", generated.toString());
        Console.log("querygen.v14.reference", reference.toString());

        Assertions.assertNotNull(generated);
        Assertions.assertEquals(3, generated.size());
        Assertions.assertTrue(generated.has("llm_instructions"));
        Assertions.assertTrue(generated.has("task"));
        Assertions.assertTrue(generated.has("context"));

        JsonObject generatedControls =
                generated.getAsJsonObject("task")
                        .getAsJsonObject("controls");

        JsonObject referenceControls =
                reference.getAsJsonObject("task")
                        .getAsJsonObject("controls");

        Assertions.assertEquals(referenceControls, generatedControls);

        JsonObject generatedNodes =
                generated.getAsJsonObject("context")
                        .getAsJsonObject("nodes");

        JsonObject referenceNodes =
                reference.getAsJsonObject("context")
                        .getAsJsonObject("nodes");

        Assertions.assertEquals(referenceNodes, generatedNodes);
    }

    private void assertInstructionExists(JsonArray instructions, String expected) {
        boolean found = false;

        int i = 0;
        while (i < instructions.size()) {
            String instruction = instructions.get(i).getAsString();

            if (expected.equals(instruction)) {
                found = true;
            }

            i++;
        }

        Assertions.assertTrue(found, expected);
    }

    private void assertInstructionDoesNotExist(JsonArray instructions, String unexpected) {
        boolean found = false;

        int i = 0;
        while (i < instructions.size()) {
            String instruction = instructions.get(i).getAsString();

            if (unexpected.equals(instruction)) {
                found = true;
            }

            i++;
        }

        Assertions.assertFalse(found, unexpected);
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
                                "CustomerAccount:CUST-2001"
                        )
                );

        task.setControls(controls);

        return new QueryRequest(meta, context, task);
    }

    private QueryRequest buildPayV14Request(JsonObject reference) {
        Meta meta =
                new Meta(
                        "v1",
                        "decision",
                        "pay decision"
                );

        JsonObject controlsJson =
                reference.getAsJsonObject("task")
                        .getAsJsonObject("controls");

        List<Control> controls =
                new ArrayList<Control>();

        for (Map.Entry<String, JsonElement> entry : controlsJson.entrySet()) {
            String value = null;

            if (entry.getValue() != null && !entry.getValue().isJsonNull()) {
                value = entry.getValue().getAsString();
            }

            controls.add(new Control(entry.getKey(), value));
        }

        JsonObject nodesJson =
                reference.getAsJsonObject("context")
                        .getAsJsonObject("nodes");

        Map<String, Node> nodes =
                new HashMap<String, Node>();

        for (Map.Entry<String, JsonElement> entry : nodesJson.entrySet()) {
            Node node =
                    Node.fromJson(entry.getValue().getAsJsonObject());

            nodes.put(entry.getKey(), node);
        }

        ValidateTask task =
                new ValidateTask(
                        "decision",
                        "PaymentRequest:PAY-1001",
                        Arrays.asList("decision", "reason", "code"),
                        Arrays.asList(
                                "CustomerAccount:CUST-2001",
                                "PaymentMethod:PM-3001",
                                "RiskProfile:RISK-4001",
                                "MerchantPolicy:POL-5001"
                        )
                );

        task.setControls(controls);

        GraphContext context =
                new GraphContext(nodes);

        return new QueryRequest(meta, context, task);
    }

    private JsonArray loadV14LlmInstructions() {
        InputStream stream =
                QueryGenServiceTest.class.getResourceAsStream(
                        "/ai/braineous/rag/prompt/cgo/querygen/v14-llm-instructions.json"
                );

        Assertions.assertNotNull(stream);

        JsonObject root =
                JsonParser.parseReader(
                        new InputStreamReader(stream, StandardCharsets.UTF_8)
                ).getAsJsonObject();

        JsonArray expected =
                root.getAsJsonArray("instructions");

        Assertions.assertNotNull(expected);

        return expected;
    }

    private JsonObject loadV14Query() {
        InputStream stream =
                QueryGenServiceTest.class.getResourceAsStream(
                        "/ai/braineous/rag/prompt/cgo/prompt/v14-query.json"
                );

        Assertions.assertNotNull(stream);

        return JsonParser.parseReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8)
        ).getAsJsonObject();
    }
}
