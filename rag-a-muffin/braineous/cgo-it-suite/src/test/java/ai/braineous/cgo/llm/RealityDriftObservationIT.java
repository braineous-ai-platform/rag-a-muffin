package ai.braineous.cgo.llm;

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
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RealityDriftObservationIT {

    //@Test
// @Disabled("Reality drift observation — nondeterministic by design")
    void realityDriftObservation_sameRequest_multipleExecutions_shouldLogAndCompareEnvelopeStability() {

        QueryRequest<ValidateTask> request = this.buildValidateTaskRequest();
        request.setAdapter(new OpenAILlmAdapter());

        PromptBuilder promptBuilder = new PromptBuilder(new SimpleResponseContractRegistry());

        LlmClient bridgingClient = new OpenAIAdapterBridgeClient();

        // IMPORTANT:
        // - keep pipeline as experiment surface
        // - disable response validator for this observation IT
        CgoQueryPipeline pipeline = new CgoQueryPipeline(promptBuilder);

        int runs = 5;
        String firstEnvelope = null;

        for (int i = 0; i < runs; i++) {

            QueryExecution<ValidateTask> execution = pipeline.execute(request);

            String envelope = null;
            if (execution.getLlmResponse() != null
                    && execution.getLlmResponse().getLlmRequest() != null
                    && execution.getLlmResponse().getLlmRequest().getLlmQuery() != null) {
                envelope = execution.getLlmResponse()
                        .getLlmRequest()
                        .getLlmQuery()
                        .toString();
            }

            Console.log("drift.run", String.valueOf(i));
            Console.log("drift.status", String.valueOf(execution.getStatus()));
            Console.log("drift.stage", String.valueOf(execution.getStage()));

            Console.log("drift.promptValidation", String.valueOf(execution.getPromptValidation()));
            Console.log("drift.llmResponseValidation", String.valueOf(execution.getLlmResponseValidation()));
            Console.log("drift.domainValidation", String.valueOf(execution.getDomainValidation()));

            Console.log("drift.rawResponse", String.valueOf(execution.getRawResponse()));
            Console.log("drift.envelope", String.valueOf(envelope));
            Console.log("drift.execution.json", execution.toJsonString());

            assertNotNull(execution);
            assertNotNull(execution.getPromptValidation());
            assertNotNull(execution.getLlmResponse());
            assertNotNull(envelope);

            if (i == 0) {
                firstEnvelope = envelope;
            } else {
                assertEquals(firstEnvelope, envelope);
            }
        }
    }

    private QueryRequest<ValidateTask> buildValidateTaskRequest() {
        return this.buildValidateTaskRequest(null);
    }

    private QueryRequest<ValidateTask> buildValidateTaskRequest(LLMResponseValidatorRule rule) {
        String factId = "Flight:F100";

        Meta meta = new Meta(
                "v1",
                "validate_flight_airports",
                "Validate that the selected flight fact has valid departure and arrival airport codes using graph context."
        );

        String taskDescription =
                "Validate that the selected flight has valid departure and arrival airport codes based on the airport nodes in the graph. " +
                        "A valid flight must have: (1) 'from' matching one Airport:* code, (2) 'to' matching one Airport:* code, (3) 'from' != 'to'.";

        ValidateTask task = new ValidateTask(taskDescription, factId,
                Arrays.asList("ok", "code", "message", "anchorId"),
                Arrays.asList("Airport:AUS", "Airport:DFW")
                );

        Node flight = new Node(
                factId,
                "{\"id\":\"F100\",\"kind\":\"Flight\",\"mode\":\"relational\",\"from\":\"AUS\",\"to\":\"DFW\"}",
                List.of(),
                Node.Mode.RELATIONAL
        );

        Node aus = new Node(
                "Airport:AUS",
                "{\"id\":\"AUS\",\"kind\":\"Airport\",\"code\":\"AUS\"}",
                List.of(),
                Node.Mode.ATOMIC
        );

        Node dfw = new Node(
                "Airport:DFW",
                "{\"id\":\"DFW\",\"kind\":\"Airport\",\"code\":\"DFW\"}",
                List.of(),
                Node.Mode.ATOMIC
        );

        GraphContext context = new GraphContext(
                Map.of(
                        factId, flight,
                        "Airport:AUS", aus,
                        "Airport:DFW", dfw
                )
        );

        return new QueryRequest<ValidateTask>(meta, context, task, factId, rule);
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