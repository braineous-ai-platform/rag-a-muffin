package ai.braineous.cgo.llm;

import ai.braineous.rag.prompt.cgo.api.Control;
import ai.braineous.rag.prompt.cgo.api.GraphContext;
import ai.braineous.rag.prompt.cgo.api.Meta;
import ai.braineous.rag.prompt.cgo.api.ValidateTask;
import ai.braineous.rag.prompt.cgo.prompt.PromptBuilder;
import ai.braineous.rag.prompt.cgo.prompt.PromptRequestOutput;
import ai.braineous.rag.prompt.cgo.prompt.SimpleResponseContractRegistry;
import ai.braineous.rag.prompt.cgo.query.Node;
import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import ai.braineous.rag.prompt.cgo.querygen.model.QueryGenOutput;
import ai.braineous.rag.prompt.cgo.querygen.services.QueryGenService;
import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SpecIT {

    @Test
    public void test_2() throws Exception {

        JsonObject reference =
                this.loadV14Query();

        QueryRequest<ValidateTask> request =
                this.buildPayV14Request(reference);

        QueryGenService queryGenService =
                new QueryGenService();

        QueryGenOutput queryGenOutput =
                queryGenService.generateQuery(request);

        JsonObject llmQuery =
                queryGenOutput.getPayload();

        PromptBuilder promptBuilder =
                new PromptBuilder(new SimpleResponseContractRegistry());

        PromptRequestOutput executionPrompt =
                promptBuilder.generateExecutionPrompt(llmQuery);

        String actual =
                executionPrompt.getRequestOutput()
                        .get("prompt")
                        .getAsString();

        JsonObject parsedPrompt =
                JsonParser.parseString(actual).getAsJsonObject();

        Console.log("spec.v14.actual.prompt", actual);
        Console.log("spec.v14.reference", reference.toString());

        assertEquals(3, parsedPrompt.size());
        assertTrue(parsedPrompt.has("llm_instructions"));
        assertTrue(parsedPrompt.has("task"));
        assertTrue(parsedPrompt.has("context"));

        JsonArray actualInstructions =
                parsedPrompt.getAsJsonObject("llm_instructions")
                        .getAsJsonArray("instructions");

        JsonArray expectedInstructions =
                reference.getAsJsonObject("llm_instructions")
                        .getAsJsonArray("instructions");

        assertEquals(12, actualInstructions.size());
        assertEquals(expectedInstructions, actualInstructions);

        assertEquals(
                reference.getAsJsonObject("task").getAsJsonObject("controls"),
                parsedPrompt.getAsJsonObject("task").getAsJsonObject("controls")
        );

        assertEquals(
                reference.getAsJsonObject("context").getAsJsonObject("nodes"),
                parsedPrompt.getAsJsonObject("context").getAsJsonObject("nodes")
        );

        assertEquals(reference, parsedPrompt);
    }

    private QueryRequest<ValidateTask> buildPayDecisionRequest() {

        String factId = "PaymentRequest:PAY-1001";

        Meta meta =
                new Meta(
                        "v1",
                        "decision",
                        "pay decision"
                );

        ValidateTask task =
                new ValidateTask(
                        "decision",
                        factId,
                        Arrays.asList("decision", "reason", "code"),
                        Arrays.asList(
                                "CustomerAccount:CUST-2001",
                                "PaymentMethod:PM-3001",
                                "RiskProfile:RISK-4001",
                                "MerchantPolicy:POL-5001"
                        )
                );

        task.setControls(
                Arrays.asList(
                        new Control("intent", "decide_payment_capture")
                )
        );

        Node paymentRequest =
                new Node(
                        factId,
                        "{\"id\":\"PaymentRequest:PAY-1001\",\"kind\":\"PaymentRequest\",\"mode\":\"atomic\",\"amount\":\"125.00\",\"currency\":\"USD\"}",
                        Arrays.asList(),
                        Node.Mode.ATOMIC
                );

        Node customerAccount =
                new Node(
                        "CustomerAccount:CUST-2001",
                        "{\"id\":\"CustomerAccount:CUST-2001\",\"kind\":\"CustomerAccount\",\"mode\":\"atomic\",\"status\":\"ACTIVE\"}",
                        Arrays.asList(),
                        Node.Mode.ATOMIC
                );

        Node paymentMethod =
                new Node(
                        "PaymentMethod:PM-3001",
                        "{\"id\":\"PaymentMethod:PM-3001\",\"kind\":\"PaymentMethod\",\"mode\":\"atomic\",\"type\":\"CARD\"}",
                        Arrays.asList(),
                        Node.Mode.ATOMIC
                );

        Node riskProfile =
                new Node(
                        "RiskProfile:RISK-4001",
                        "{\"id\":\"RiskProfile:RISK-4001\",\"kind\":\"RiskProfile\",\"mode\":\"atomic\",\"level\":\"LOW\"}",
                        Arrays.asList(),
                        Node.Mode.ATOMIC
                );

        Node merchantPolicy =
                new Node(
                        "MerchantPolicy:POL-5001",
                        "{\"id\":\"MerchantPolicy:POL-5001\",\"kind\":\"MerchantPolicy\",\"mode\":\"atomic\",\"capture\":\"AUTO\"}",
                        Arrays.asList(),
                        Node.Mode.ATOMIC
                );

        Map<String, Node> nodes =
                new HashMap<String, Node>();

        nodes.put(factId, paymentRequest);
        nodes.put("CustomerAccount:CUST-2001", customerAccount);
        nodes.put("PaymentMethod:PM-3001", paymentMethod);
        nodes.put("RiskProfile:RISK-4001", riskProfile);
        nodes.put("MerchantPolicy:POL-5001", merchantPolicy);

        GraphContext context =
                new GraphContext(nodes);

        return new QueryRequest<ValidateTask>(
                meta,
                context,
                task,
                factId,
                null
        );
    }

    private JsonObject loadV14Query() throws Exception {
        String raw =
                this.readClasspathResource(
                        "ai/braineous/rag/prompt/cgo/prompt/v14-query.json"
                );

        return JsonParser.parseString(raw).getAsJsonObject();
    }

    private QueryRequest<ValidateTask> buildPayV14Request(JsonObject reference) {
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

        List<String> relatedFactIds =
                new ArrayList<String>();

        String factId = null;

        for (Map.Entry<String, JsonElement> entry : nodesJson.entrySet()) {
            Node node =
                    Node.fromJson(entry.getValue().getAsJsonObject());

            nodes.put(entry.getKey(), node);

            if (entry.getKey() != null
                    && entry.getKey().indexOf("PaymentRequest:") == 0) {
                factId = entry.getKey();
            } else {
                relatedFactIds.add(entry.getKey());
            }
        }

        ValidateTask task =
                new ValidateTask(
                        "decision",
                        factId,
                        Arrays.asList(),
                        relatedFactIds
                );

        task.setControls(controls);

        GraphContext context =
                new GraphContext(nodes);

        return new QueryRequest<ValidateTask>(
                meta,
                context,
                task,
                factId,
                null
        );
    }

    private String readClasspathResource(String name) throws Exception {
        InputStream in =
                SpecIT.class.getClassLoader().getResourceAsStream(name);

        if (in == null) {
            throw new IllegalStateException("missing resource: " + name);
        }

        InputStreamReader reader = null;
        try {
            reader =
                    new InputStreamReader(in, StandardCharsets.UTF_8);

            StringBuilder sb =
                    new StringBuilder();

            char[] buf =
                    new char[1024];

            int n;
            while ((n = reader.read(buf)) != -1) {
                sb.append(buf, 0, n);
            }

            return sb.toString();
        } finally {
            if (reader != null) {
                reader.close();
            } else {
                in.close();
            }
        }
    }

    private String extractInstructionsBeforeInput(String prompt) {
        String boundary = "\nINPUT:\n";

        int index = prompt.indexOf(boundary);

        if (index < 0) {
            throw new IllegalStateException("missing prompt boundary: \\nINPUT:\\n");
        }

        return prompt.substring(0, index);
    }
}
