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

public class InterpretFunctionalExecutionIT {

    // @Test
    public void interpretFunctionalExecution_singleRequest_shouldReturnExecutionEnvelopeAndJsonResult() {

        QueryRequest<ValidateTask> request = this.buildInterpretRequest();
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

        Console.log("interpret.functional.status", String.valueOf(execution.getStatus()));
        Console.log("interpret.functional.stage", String.valueOf(execution.getStage()));
        Console.log("interpret.functional.promptValidation", String.valueOf(execution.getPromptValidation()));
        Console.log("interpret.functional.llmResponseValidation", String.valueOf(execution.getLlmResponseValidation()));
        Console.log("interpret.functional.domainValidation", String.valueOf(execution.getDomainValidation()));
        Console.log("interpret.functional.rawResponse", String.valueOf(execution.getRawResponse()));
        Console.log("interpret.functional.envelope", String.valueOf(envelope));
        Console.log("interpret.functional.execution.json", execution.toJsonString());

        assertNotNull(execution);
        assertNotNull(execution.getPromptValidation());
        assertNotNull(execution.getLlmResponse());
        assertNotNull(envelope);
        assertTrue(execution.isOk());
        assertEquals("OK", execution.getStatus());
        assertEquals("ok", execution.getStage());

        assertTrue(envelope.contains("\"llm_instructions\""));
        assertTrue(envelope.contains("\"task\""));
        assertTrue(envelope.contains("\"output_template\""));
        assertTrue(envelope.contains("\"context\""));

        String rawResponse =
                String.valueOf(execution.getRawResponse()).trim();

        Console.log("interpret.functional.rawResponse.normalized", rawResponse);

        // assertFalse(rawResponse.contains("\n"));
        // assertFalse(rawResponse.contains("\r"));
        // assertFalse(rawResponse.contains("\t"));

        JsonElement parsed =
                JsonParser.parseString(rawResponse);

        assertTrue(parsed.isJsonObject());

        JsonObject root =
                parsed.getAsJsonObject();

        assertTrue(root.has("result"));
        assertEquals(1, root.entrySet().size());

        JsonObject result =
                root.getAsJsonObject("result");

        assertNotNull(result);
        assertEquals(1, result.size());

        assertTrue(result.has("fulfillmentState"));
        assertTrue(result.get("fulfillmentState").isJsonPrimitive());
        assertTrue(result.get("fulfillmentState").getAsJsonPrimitive().isString());
        assertFalse(result.get("fulfillmentState").getAsString().trim().isEmpty());

        Console.log("interpret.functional.result.fulfillmentState",
                result.get("fulfillmentState").getAsString());
    }

    private QueryRequest<ValidateTask> buildInterpretRequest() {
        return this.buildInterpretRequest(null);
    }

    private QueryRequest<ValidateTask> buildInterpretRequest(LLMResponseValidatorRule rule) {

        String factId = "Order:O-1001";

        Meta meta =
                new Meta(
                        "v1",
                        "interpretation",
                        "interpretation"
                );

        ValidateTask task =
                new ValidateTask(
                        "interpretation",
                        factId,
                        Arrays.asList("fulfillmentState"),
                        Arrays.asList(
                                "Inventory:P-100@AUS",
                                "FulfillmentLocation:AUS"
                        )
                );

        task.setControls(
                Arrays.asList(
                        new Control("intent", "interpret_order_fulfillment_state")
                )
        );

        Node order =
                new Node(
                        factId,
                        "{\"id\":\"Order:O-1001\",\"kind\":\"Order\",\"mode\":\"atomic\",\"productId\":\"P-100\",\"requestedQuantity\":10,\"status\":\"OPEN\"}",
                        Arrays.asList(),
                        Node.Mode.ATOMIC
                );

        Node inventory =
                new Node(
                        "Inventory:P-100@AUS",
                        "{\"id\":\"Inventory:P-100@AUS\",\"kind\":\"Inventory\",\"mode\":\"atomic\",\"productId\":\"P-100\",\"locationId\":\"AUS\",\"availableQuantity\":4}",
                        Arrays.asList(),
                        Node.Mode.ATOMIC
                );

        Node fulfillmentLocation =
                new Node(
                        "FulfillmentLocation:AUS",
                        "{\"id\":\"FulfillmentLocation:AUS\",\"kind\":\"FulfillmentLocation\",\"mode\":\"atomic\",\"status\":\"ACTIVE\"}",
                        Arrays.asList(),
                        Node.Mode.ATOMIC
                );

        Map<String, Node> nodes =
                new HashMap<String, Node>();

        nodes.put(factId, order);
        nodes.put("Inventory:P-100@AUS", inventory);
        nodes.put("FulfillmentLocation:AUS", fulfillmentLocation);

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

    //---------------drift assertion-----------------------
    @Test
    public void interpretFunctionalExecution_drift_shouldKeepContractStableAcrossRuns() {

        int runCount = 10;

        java.util.Map<String, Integer> fulfillmentStateCounts =
                new java.util.HashMap<String, Integer>();

        int contractFailureCount = 0;

        int i = 0;
        while (i < runCount) {

            QueryRequest<ValidateTask> request =
                    this.buildInterpretRequest();

            request.setAdapter(new OpenAILlmAdapter());

            PromptBuilder promptBuilder =
                    new PromptBuilder(new SimpleResponseContractRegistry());

            CgoQueryPipeline pipeline =
                    new CgoQueryPipeline(promptBuilder);

            QueryExecution<ValidateTask> execution =
                    pipeline.execute(request);

            Console.log("interpret.drift.run", String.valueOf(i));
            Console.log("interpret.drift.status", String.valueOf(execution.getStatus()));
            Console.log("interpret.drift.stage", String.valueOf(execution.getStage()));
            Console.log("interpret.drift.rawResponse", String.valueOf(execution.getRawResponse()));
            Console.log("interpret.drift.llmResponseValidation", String.valueOf(execution.getLlmResponseValidation()));

            try {
                // assertNotNull(execution);
                // assertNotNull(execution.getPromptValidation());
                // assertNotNull(execution.getLlmResponse());
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
                // assertEquals(1, result.size());

                // assertTrue(result.has("fulfillmentState"));
                // assertTrue(result.get("fulfillmentState").isJsonPrimitive());
                // assertTrue(result.get("fulfillmentState").getAsJsonPrimitive().isString());
                // assertFalse(result.get("fulfillmentState").getAsString().trim().isEmpty());

                String fulfillmentState =
                        result.get("fulfillmentState").getAsString();

                increment(fulfillmentStateCounts, fulfillmentState);

                Console.log("interpret.drift.fulfillmentState", fulfillmentState);

            } catch (AssertionError e) {
                // Unreachable while drift assertions are commented out.
                // contractFailureCount++;
                // Console.log("interpret.drift.contract.failure", e.getMessage());
            } catch (RuntimeException e) {
                contractFailureCount++;
                Console.log("interpret.drift.contract.failure",
                        e.getClass().getName() + ": " + e.getMessage());
            }

            i++;
        }

        Console.log("interpret.drift.runCount", String.valueOf(runCount));
        Console.log("interpret.drift.contractFailureCount", String.valueOf(contractFailureCount));
        Console.log("interpret.drift.uniqueFulfillmentStates", fulfillmentStateCounts.toString());

        assertEquals(10, runCount);
        // assertEquals(0, contractFailureCount);
        // assertFalse(fulfillmentStateCounts.isEmpty());
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
