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
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
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

        Assertions.assertTrue(payload.has("llm_instructions"));
        Assertions.assertTrue(payload.has("meta"));
        Assertions.assertTrue(payload.has("task"));
        Assertions.assertTrue(payload.has("context"));
        Assertions.assertTrue(payload.has("llm_trace"));
        Assertions.assertTrue(payload.has("llm_trace_instructions"));
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

        Assertions.assertTrue(serialized.indexOf("\"llm_instructions\"") < serialized.indexOf("\"meta\""));
        Assertions.assertTrue(serialized.indexOf("\"meta\"") < serialized.indexOf("\"task\""));
        Assertions.assertTrue(serialized.indexOf("\"task\"") < serialized.indexOf("\"context\""));
        Assertions.assertTrue(serialized.indexOf("\"context\"") < serialized.indexOf("\"llm_trace\""));
        Assertions.assertTrue(serialized.indexOf("\"llm_trace\"") < serialized.indexOf("\"llm_trace_instructions\""));
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

        Assertions.assertEquals("decision", task.getAsJsonObject("intent").get("goal").getAsString());
        Assertions.assertEquals("PaymentRequest:PAY-1001", task.get("factId").getAsString());

        JsonArray select = task.getAsJsonArray("select");
        Assertions.assertEquals("decision", select.get(0).getAsString());
        Assertions.assertEquals("reason", select.get(1).getAsString());
        Assertions.assertEquals("code", select.get(2).getAsString());

        JsonObject controls = task.getAsJsonObject("controls");
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
        assertInstructionExists(instructions, "Execute task.controls.method.");
        assertInstructionExists(instructions, "Resolve task.controls.parameters from context.nodes.");
        assertInstructionExists(instructions, "Use task.controls.expression with the resolved parameters.");
        assertInstructionExists(instructions, "Use task.controls.spec as the definition of the expression.");
        assertInstructionExists(instructions, "Place the computed value in the field named by task.controls.result.");
        assertInstructionExists(instructions, "Return exactly one field named result.");
        assertInstructionExists(instructions, "Return a field called result at any cost.");
        assertInstructionExists(instructions, "Use result=value.");
        assertInstructionExists(instructions, "Return the value as a string.");
        assertInstructionExists(instructions, "No other fields or text.");

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
}