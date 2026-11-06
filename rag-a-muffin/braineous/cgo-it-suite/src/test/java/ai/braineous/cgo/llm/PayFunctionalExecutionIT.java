package ai.braineous.cgo.llm;

import ai.braineous.rag.prompt.cgo.api.Control;
import ai.braineous.rag.prompt.cgo.api.GraphContext;
import ai.braineous.rag.prompt.cgo.api.LLMResponseValidatorRule;
import ai.braineous.rag.prompt.cgo.api.Meta;
import ai.braineous.rag.prompt.cgo.api.QueryExecution;
import ai.braineous.rag.prompt.cgo.api.ValidateTask;
import ai.braineous.rag.prompt.cgo.query.CgoQueryPipeline;
import ai.braineous.rag.prompt.cgo.query.Node;
import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import ai.braineous.rag.prompt.cgo.prompt.PromptBuilder;
import ai.braineous.rag.prompt.cgo.prompt.SimpleResponseContractRegistry;
import ai.braineous.rag.prompt.observe.Console;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PayFunctionalExecutionIT {

    @Test
    public void payFunctionalExecution_singleRequest_shouldReturnExecutionEnvelopeAndJsonResult() {

        QueryRequest<ValidateTask> request = this.buildPayDecisionRequest();

        request.getTask().setControls(
                Arrays.asList(
                        new Control("parameters", "CustomerAccount.status, PaymentMethod.type, RiskProfile.level, MerchantPolicy.capture"),
                        new Control("expression", "CustomerAccount.status is equal to ACTIVE and PaymentMethod.type is equal to CARD and RiskProfile.level is equal to LOW and MerchantPolicy.capture is equal to AUTO"),
                        new Control("spec", "The expression determines whether payment capture is eligible. When the expression evaluates to true, payment capture is eligible and the runtime result is success. When the expression evaluates to false, payment capture is not eligible and the runtime result is fail."),
                        new Control("result", "result")
                )
        );

        PromptBuilder promptBuilder =
                new PromptBuilder(new SimpleResponseContractRegistry());

        CgoQueryPipeline pipeline =
                new CgoQueryPipeline(promptBuilder);

        QueryExecution<ValidateTask> execution =
                pipeline.execute(request);

        String envelope = null;

        if (execution.getLlmResponse() != null
                && execution.getLlmResponse().getLlmRequest() != null
                && execution.getLlmResponse().getLlmRequest().getLlmQuery() != null) {
            envelope =
                    execution.getLlmResponse()
                            .getLlmRequest()
                            .getLlmQuery()
                            .toString();
        }

        Console.log("pay.functional.status", String.valueOf(execution.getStatus()));
        Console.log("pay.functional.stage", String.valueOf(execution.getStage()));
        Console.log("pay.functional.promptValidation", String.valueOf(execution.getPromptValidation()));
        Console.log("pay.functional.llmResponseValidation", String.valueOf(execution.getLlmResponseValidation()));
        Console.log("pay.functional.domainValidation", String.valueOf(execution.getDomainValidation()));
        Console.log("pay.functional.rawResponse", String.valueOf(execution.getRawResponse()));
        Console.log("pay.functional.envelope", String.valueOf(envelope));
        Console.log("pay.functional.execution.json", execution.toJsonString());

        assertNotNull(execution);
        assertNotNull(execution.getPromptValidation());
        assertNotNull(execution.getLlmResponse());
        assertNotNull(envelope);

        assertTrue(envelope.contains("\"llm_instructions\""));
        assertTrue(envelope.contains("\"task\""));
        assertTrue(envelope.contains("\"context\""));

        assertNotNull(execution.getLlmResponseValidation());

        String rawResponse =
                String.valueOf(execution.getRawResponse()).trim();

        Console.log("pay.functional.rawResponse.normalized", rawResponse);

        if (!execution.getLlmResponseValidation().isOk()) {
            assertEquals("ERROR", execution.getStatus());
            assertEquals("llm_response", execution.getStage());

            Console.log(
                    "pay.functional.model.contract.rejection.code",
                    String.valueOf(execution.getLlmResponseValidation().getCode())
            );
            Console.log(
                    "pay.functional.model.contract.rejection.rawResponse",
                    rawResponse
            );

            return;
        }

        JsonElement parsed =
                JsonParser.parseString(rawResponse);

        assertTrue(parsed.isJsonObject());

        JsonObject root =
                parsed.getAsJsonObject();

        assertEquals(1, root.size());
        assertTrue(root.has("result"));
        assertTrue(root.get("result").isJsonPrimitive());
        assertTrue(root.get("result").getAsJsonPrimitive().isString());

        String resultValue =
                root.get("result").getAsString();

        boolean accepted = false;
        if ("success".equals(resultValue)) {
            accepted = true;
        } else {
            if ("fail".equals(resultValue)) {
                accepted = true;
            } else {
                if ("result".equals(resultValue)) {
                    accepted = true;
                }
            }
        }

        assertTrue(
                accepted,
                "V14 result assertion failed\n"
                        + "=== RAW RESPONSE BEGIN ===\n"
                        + rawResponse
                        + "\n=== RAW RESPONSE END ===\n"
        );

        Console.log("pay.functional.result.result", resultValue);
    }

    private QueryRequest<ValidateTask> buildPayDecisionRequest() {
        return this.buildPayDecisionRequest(null);
    }

    private QueryRequest<ValidateTask> buildPayDecisionRequest(LLMResponseValidatorRule rule) {

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
                rule
        );
    }

    //---------------drift assertion-----------------------
    @Test
    public void payFunctionalExecution_drift_shouldKeepContractStableAcrossRuns() {

        int runCount = 5;

        JsonObject reference =
                this.loadV14Query();

        JsonObject referenceControls =
                reference.getAsJsonObject("task")
                        .getAsJsonObject("controls");

        Map<String, Integer> resultCounts =
                new HashMap<String, Integer>();

        Map<String, Integer> classificationCounts =
                new HashMap<String, Integer>();

        List<String> rawResponses =
                new ArrayList<String>();

        int i = 0;
        while (i < runCount) {

            Console.log("pay.drift.run", String.valueOf(i));

            QueryRequest<ValidateTask> request =
                    this.buildPayDecisionRequest();

            request.getTask().setControls(
                    this.buildV14Controls(referenceControls)
            );

            PromptBuilder promptBuilder =
                    new PromptBuilder(new SimpleResponseContractRegistry());

            CgoQueryPipeline pipeline =
                    new CgoQueryPipeline(promptBuilder);

            QueryExecution<ValidateTask> execution =
                    pipeline.execute(request);

            Console.log("pay.drift.status", String.valueOf(execution.getStatus()));
            Console.log("pay.drift.stage", String.valueOf(execution.getStage()));
            Console.log("pay.drift.rawResponse", String.valueOf(execution.getRawResponse()));
            Console.log("pay.drift.llmResponseValidation", String.valueOf(execution.getLlmResponseValidation()));
            Console.log("pay.drift.execution.json", execution.toJsonString());

            String rawResponse =
                    String.valueOf(execution.getRawResponse()).trim();

            Console.log("pay.drift.rawResponse.normalized", rawResponse);

            rawResponses.add(rawResponse);

            String classification =
                    this.classifyResponse(rawResponse);

            Console.log("pay.drift.classification", classification);

            this.increment(classificationCounts, classification);

            String resultValue =
                    this.extractResultValue(rawResponse);

            if (resultValue != null) {
                Console.log("pay.drift.result", resultValue);
                this.increment(resultCounts, resultValue);
            }

            i++;
        }

        Console.log("pay.drift.runCount", String.valueOf(runCount));
        Console.log("pay.drift.rawResponses", rawResponses.toString());
        Console.log("pay.drift.classifications", classificationCounts.toString());
        Console.log("pay.drift.uniqueResults", resultCounts.toString());
    }

    private String classifyResponse(String rawResponse) {
        if (rawResponse == null) {
            return "contamination";
        }

        String trimmed =
                rawResponse.trim();

        if (trimmed.length() == 0) {
            return "contamination";
        }

        JsonElement parsed;
        try {
            parsed = JsonParser.parseString(trimmed);
        } catch (Exception exception) {
            return "contamination";
        }

        if (parsed == null || parsed.isJsonNull()) {
            return "contamination";
        }

        if (!parsed.isJsonObject()) {
            return "noise";
        }

        JsonObject root =
                parsed.getAsJsonObject();

        if (root.size() != 1) {
            return "noise";
        }

        if (!root.has("result")) {
            return "noise";
        }

        JsonElement result =
                root.get("result");

        if (result == null || !result.isJsonPrimitive()) {
            return "noise";
        }

        if (!result.getAsJsonPrimitive().isString()) {
            return "noise";
        }

        return "success";
    }

    private String extractResultValue(String rawResponse) {
        if (rawResponse == null) {
            return null;
        }

        JsonElement parsed;
        try {
            parsed = JsonParser.parseString(rawResponse);
        } catch (Exception exception) {
            return null;
        }

        if (parsed == null || !parsed.isJsonObject()) {
            return null;
        }

        JsonObject root =
                parsed.getAsJsonObject();

        if (!root.has("result")) {
            return null;
        }

        JsonElement result =
                root.get("result");

        if (result == null || !result.isJsonPrimitive()) {
            return null;
        }

        if (!result.getAsJsonPrimitive().isString()) {
            return null;
        }

        return result.getAsString();
    }

    private JsonObject loadV14Query() {
        InputStream stream =
                PayFunctionalExecutionIT.class.getResourceAsStream(
                        "/ai/braineous/rag/prompt/cgo/prompt/v14-query.json"
                );

        assertNotNull(stream);

        return JsonParser.parseReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8)
        ).getAsJsonObject();
    }

    private List<Control> buildV14Controls(JsonObject controlsJson) {
        List<Control> controls =
                new ArrayList<Control>();

        for (Map.Entry<String, JsonElement> entry : controlsJson.entrySet()) {
            String value = null;

            if (entry.getValue() != null && !entry.getValue().isJsonNull()) {
                value = entry.getValue().getAsString();
            }

            controls.add(new Control(entry.getKey(), value));
        }

        return controls;
    }

    private void increment(java.util.Map<String, Integer> counts, String value) {
        Integer current =
                counts.get(value);

        if (current == null) {
            counts.put(value, 1);
        } else {
            counts.put(value, current + 1);
        }
    }
}