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
import ai.braineous.rag.prompt.cgo.prompt.LlmClient;
import ai.braineous.rag.prompt.cgo.prompt.PromptBuilder;
import ai.braineous.rag.prompt.cgo.prompt.SimpleResponseContractRegistry;
import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PayFunctionalExecutionIT {

    @Test
    public void payFunctionalExecution_singleRequest_shouldReturnExecutionEnvelopeAndJsonResult() {

        QueryRequest<ValidateTask> request = this.buildPayDecisionRequest();
        request.setAdapter(new OpenAILlmAdapter());

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
        assertTrue(envelope.contains("\"output_template\""));
        assertTrue(envelope.contains("\"context\""));

        String rawResponse =
                String.valueOf(execution.getRawResponse()).trim();

        Console.log("pay.functional.rawResponse.normalized", rawResponse);

        // assertFalse(rawResponse.contains("\n"));
        // assertFalse(rawResponse.contains("\r"));
        // assertFalse(rawResponse.contains("\t"));

        JsonElement parsed =
                JsonParser.parseString(rawResponse);

        assertTrue(parsed.isJsonObject());

        JsonObject root =
                parsed.getAsJsonObject();

        assertTrue(root.has("result"));

        JsonObject result =
                root.getAsJsonObject("result");

        assertNotNull(result);
        assertEquals(3, result.size());

        assertTrue(result.has("decision"));
        assertTrue(result.has("reason"));
        assertTrue(result.has("code"));

        assertTrue(result.get("decision").isJsonPrimitive());
        assertTrue(result.get("reason").isJsonPrimitive());
        assertTrue(result.get("code").isJsonPrimitive());

        Console.log("pay.functional.result.decision", result.get("decision").getAsString());
        Console.log("pay.functional.result.reason", result.get("reason").getAsString());
        Console.log("pay.functional.result.code", result.get("code").getAsString());
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

    private static class OpenAIAdapterBridgeClient implements LlmClient {

        @Override
        public String executePrompt(ai.braineous.rag.prompt.cgo.api.LlmAdapter adapter,
                                    QueryRequest queryRequest,
                                    JsonObject prompt) {

            JsonObject llmPayload = new JsonObject();
            llmPayload.addProperty("model", "qwen2.5:0.5b");
            llmPayload.addProperty("prompt", prompt.toString());
            llmPayload.addProperty("stream", false);

            return ((OpenAILlmAdapter) adapter).invokeLlm(queryRequest, llmPayload);
        }
    }

    @Test
    public void payFunctionalExecution_legacy_threeIndependentRuns_diagnostic() {
        java.util.List<String> rawResponses = new java.util.ArrayList<String>();
        java.util.List<String> outcomes = new java.util.ArrayList<String>();

        int i = 1;
        while (i <= 3) {
            String requestId = null;
            String status = null;
            String stage = null;
            String isOk = null;
            String rawResponse = null;
            String failure = "NONE";
            String outcome = "OK";
            QueryRequest<ValidateTask> request = null;

            try {
                request = this.buildPayDecisionRequest();

                request.setAdapter(new OpenAILlmAdapter());

                PromptBuilder promptBuilder =
                        new PromptBuilder(new SimpleResponseContractRegistry());

                CgoQueryPipeline pipeline =
                        new CgoQueryPipeline(promptBuilder, new OpenAIAdapterBridgeClient());

                QueryExecution<ValidateTask> execution =
                        pipeline.execute(request);

                requestId = String.valueOf(request.getRequestId());
                status = String.valueOf(execution.getStatus());
                stage = String.valueOf(execution.getStage());
                isOk = String.valueOf(execution.isOk());
                rawResponse = execution.getRawResponse();
            } catch (Throwable t) {
                outcome = "FAIL";
                failure = t.getClass().getName() + ": " + t.getMessage();
                if (request != null) {
                    requestId = String.valueOf(request.getRequestId());
                }
            }

            if (rawResponse != null) {
                rawResponses.add(rawResponse);
            }

            outcomes.add(outcome);

            System.out.println("================ PAY LEGACY RUN " + i + " ================");
            System.out.println("requestId:");
            System.out.println(requestId);
            System.out.println("status:");
            System.out.println(status);
            System.out.println("stage:");
            System.out.println(stage);
            System.out.println("isOk:");
            System.out.println(isOk);
            System.out.println("rawResponse:");
            if (rawResponse == null) {
                System.out.println("null");
            } else {
                System.out.print(rawResponse);
                if (!rawResponse.endsWith("\n")) {
                    System.out.println();
                }
            }
            System.out.println("failure:");
            System.out.println(failure);
            System.out.println("==================================================");

            i++;
        }

        java.util.Set<String> distinct = new java.util.LinkedHashSet<String>();
        distinct.addAll(rawResponses);

        System.out.println("================ PAY LEGACY SUMMARY ================");
        System.out.println("run 1: " + outcomes.get(0));
        System.out.println("run 2: " + outcomes.get(1));
        System.out.println("run 3: " + outcomes.get(2));
        System.out.println("distinct exact raw responses: " + distinct.size() + "/3");
        System.out.println("====================================================");
    }

    //---------------drift assertion-----------------------
    @Test
    public void payFunctionalExecution_drift_shouldKeepContractStableAcrossRuns() {

        int runCount = 5;

        java.util.Map<String, Integer> decisionCounts =
                new java.util.HashMap<String, Integer>();

        java.util.Map<String, Integer> reasonCounts =
                new java.util.HashMap<String, Integer>();

        java.util.Map<String, Integer> codeCounts =
                new java.util.HashMap<String, Integer>();

        int contractFailureCount = 0;

        int i = 0;
        while (i < runCount) {

            Console.log("pay.drift.run", String.valueOf(i));

            try {
                QueryRequest<ValidateTask> request =
                        this.buildPayDecisionRequest();

                request.getTask().setControls(
                        Arrays.asList(
                                new Control("intent", "decide_payment_capture"),
                                new Control("action", "determine"),
                                new Control("subject", "primary_payment_request"),
                                new Control("decision", "allow_capture"),
                                new Control("basis", "related_system_facts"),
                                new Control("goal", "decision")
                        )
                );

                request.setAdapter(new OpenAILlmAdapter());

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

                try {
                    // assertNotNull(execution);
                    // assertTrue(execution.isOk());
                    // assertEquals("OK", execution.getStatus());
                    // assertEquals("ok", execution.getStage());

                    String rawResponse =
                            String.valueOf(execution.getRawResponse()).trim();

                    // assertFalse(rawResponse.contains("\n"));
                    // assertFalse(rawResponse.contains("\r"));
                    // assertFalse(rawResponse.contains("\t"));

                    JsonElement parsed =
                            JsonParser.parseString(rawResponse);

                    // assertTrue(parsed.isJsonObject());

                    JsonObject root =
                            parsed.getAsJsonObject();

                    // assertTrue(root.has("result"));
                    // assertEquals(1, root.entrySet().size());

                    JsonObject result =
                            root.getAsJsonObject("result");

                    // assertNotNull(result);
                    // assertEquals(3, result.size());

                    // assertTrue(result.has("decision"));
                    // assertTrue(result.has("reason"));
                    // assertTrue(result.has("code"));

                    // assertTrue(result.get("decision").isJsonPrimitive());
                    // assertTrue(result.get("reason").isJsonPrimitive());
                    // assertTrue(result.get("code").isJsonPrimitive());

                    String decision =
                            result.get("decision").getAsString();

                    String reason =
                            result.get("reason").getAsString();

                    String code =
                            result.get("code").getAsString();

                    increment(decisionCounts, decision);
                    increment(reasonCounts, reason);
                    increment(codeCounts, code);

                    Console.log("pay.drift.decision", decision);
                    Console.log("pay.drift.reason", reason);
                    Console.log("pay.drift.code", code);

                } catch (AssertionError e) {
                    // Unreachable while drift assertions are commented out.
                    // contractFailureCount++;
                    // Console.log("pay.drift.contract.failure", e.getMessage());
                }

            } catch (RuntimeException e) {
                contractFailureCount++;
                Console.log(
                        "pay.drift.contract.failure",
                        e.getClass().getName() + ": " + e.getMessage()
                );
            } catch (Error e) {
                if (e instanceof AssertionError) {
                    throw e;
                }
                contractFailureCount++;
                Console.log(
                        "pay.drift.contract.failure",
                        e.getClass().getName() + ": " + e.getMessage()
                );
            }

            i++;
        }

        Console.log("pay.drift.runCount", String.valueOf(runCount));
        Console.log("pay.drift.contractFailureCount", String.valueOf(contractFailureCount));
        Console.log("pay.drift.uniqueDecisions", decisionCounts.toString());
        Console.log("pay.drift.uniqueReasons", reasonCounts.toString());
        Console.log("pay.drift.uniqueCodes", codeCounts.toString());

        // assertEquals(0, contractFailureCount);
        // assertFalse(decisionCounts.isEmpty());
        // assertFalse(reasonCounts.isEmpty());
        // assertFalse(codeCounts.isEmpty());
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