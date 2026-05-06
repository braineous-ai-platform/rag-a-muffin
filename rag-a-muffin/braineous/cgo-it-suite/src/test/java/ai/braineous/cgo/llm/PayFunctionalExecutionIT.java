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

        assertFalse(rawResponse.contains("\n"));
        assertFalse(rawResponse.contains("\r"));
        assertFalse(rawResponse.contains("\t"));

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
            llmPayload.addProperty("model", "llama3");
            llmPayload.addProperty("prompt", prompt.toString());
            llmPayload.addProperty("stream", false);

            return ((OpenAILlmAdapter) adapter).invokeLlm(queryRequest, llmPayload);
        }
    }
}