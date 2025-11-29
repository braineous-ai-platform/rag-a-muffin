package ai.braineous.rag.prompt.cgo.query;

import ai.braineous.rag.prompt.cgo.api.*;
import ai.braineous.rag.prompt.cgo.prompt.FakeLlmClient;
import ai.braineous.rag.prompt.cgo.prompt.PromptBuilder;
import ai.braineous.rag.prompt.cgo.prompt.SimpleResponseContractRegistry;
import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CgoQueryPipelineTests {

    @Test
    void execute_shouldBuildPromptCallLlmAndReturnExecutionWithOriginalRequest() {
        // arrange
        String factId = "Flight:F100";

        Meta meta = new Meta(
                "v1",
                "validate_flight_airports",
                "Validate that the selected flight fact has valid departure and arrival airport codes using graph context."
        );

        String taskDescription =
                "Validate that the selected flight has valid departure and arrival airport codes based on the airport nodes in the graph. " +
                        "A valid flight must have: (1) 'from' matching one Airport:* code, (2) 'to' matching one Airport:* code, (3) 'from' != 'to'.";

        ValidateTask task = new ValidateTask(taskDescription, factId);

        Node node = new Node(
                factId,
                "{\"id\":\"F100\",\"kind\":\"Flight\",\"mode\":\"relational\",\"from\":\"AUS\",\"to\":\"DFW\"}",
                List.of(),
                Node.Mode.RELATIONAL
        );

        GraphContext context = new GraphContext(Map.of(factId, node));

        QueryRequest<ValidateTask> request =
                QueryRequests.validateTask(meta, task, context, factId);

        PromptBuilder promptBuilder = new PromptBuilder(new SimpleResponseContractRegistry());
        FakeLlmClient llmClient = new FakeLlmClient("{\"result\":{\"status\":\"VALID\"}}");

        CgoQueryPipeline pipeline = new CgoQueryPipeline(promptBuilder, llmClient);

        // act
        QueryExecution<ValidateTask> execution = pipeline.execute(request);

        // assert: execution basics
        assertNotNull(execution, "QueryExecution should not be null");
        assertNotNull(execution.getRequest(), "Execution should contain the original request");
        assertSame(request, execution.getRequest(), "Execution should wrap the exact same QueryRequest instance");

        // assert: LLM was called with a prompt
        JsonObject lastPrompt = llmClient.getLastPrompt();
        assertNotNull(lastPrompt, "LlmClient should have received a prompt");

        // Console inspect the prompt used by the pipeline
        Console.log("Pipeline LLM Prompt", lastPrompt);

        // lightweight structure checks (PromptBuilderTests already test full shape)
        assertTrue(lastPrompt.has("meta"), "Prompt should contain 'meta'");
        assertTrue(lastPrompt.has("context"), "Prompt should contain 'context'");
        assertTrue(lastPrompt.has("task"), "Prompt should contain 'task'");
        assertTrue(lastPrompt.has("response_contract"), "Prompt should contain 'response_contract'");
        assertTrue(lastPrompt.has("instructions"), "Prompt should contain 'instructions'");
        assertTrue(lastPrompt.has("llm_instructions"), "Prompt should contain 'llm_instructions'");
    }

    @Test
    void execute_withValidator_shouldAttachOkValidationResult() {
        // arrange
        String factId = "Flight:F100";

        Meta meta = new Meta(
                "v1",
                "validate_flight_airports",
                "Validate that the selected flight fact has valid departure and arrival airport codes using graph context."
        );

        String taskDescription =
                "Validate that the selected flight has valid departure and arrival airport codes based on the airport nodes in the graph. " +
                        "A valid flight must have: (1) 'from' matching one Airport:* code, (2) 'to' matching one Airport:* code, (3) 'from' != 'to'.";

        ValidateTask task = new ValidateTask(taskDescription, factId);

        Node node = new Node(
                factId,
                "{\"id\":\"F100\",\"kind\":\"Flight\",\"mode\":\"relational\",\"from\":\"AUS\",\"to\":\"DFW\"}",
                List.of(),
                Node.Mode.RELATIONAL
        );

        GraphContext context = new GraphContext(Map.of(factId, node));

        QueryRequest<ValidateTask> request =
                QueryRequests.validateTask(meta, task, context, factId);

        PromptBuilder promptBuilder = new PromptBuilder(new SimpleResponseContractRegistry());/* real or fake, as long as JSON comes out */;
        FakeLlmClient llmClient = new FakeLlmClient("{\"some\":\"response\"}");

        FakeValidationResultValidator validator =
                new FakeValidationResultValidator(ValidationResult.ok("LLM_RESPONSE_VALIDATION"));

        CgoQueryPipeline pipeline = new CgoQueryPipeline(promptBuilder, llmClient, validator);

        // act
        QueryExecution<ValidateTask> execution = pipeline.execute(request);

        // assert
        assertTrue(validator.wasCalled());
        assertEquals("{\"some\":\"response\"}", validator.getLastRawResponse());

        ValidationResult vr = execution.getValidationResult();
        assertNotNull(vr);
        assertTrue(vr.isOk());
        assertEquals("LLM_RESPONSE_VALIDATION", vr.getStage());
    }

    @Test
    void execute_withValidator_errorShouldBeExposedOnExecution() {
        // arrange
        String factId = "Flight:F100";

        Meta meta = new Meta(
                "v1",
                "validate_flight_airports",
                "Validate that the selected flight fact has valid departure and arrival airport codes using graph context."
        );

        String taskDescription =
                "Validate that the selected flight has valid departure and arrival airport codes based on the airport nodes in the graph. " +
                        "A valid flight must have: (1) 'from' matching one Airport:* code, (2) 'to' matching one Airport:* code, (3) 'from' != 'to'.";

        ValidateTask task = new ValidateTask(taskDescription, factId);

        Node node = new Node(
                factId,
                "{\"id\":\"F100\",\"kind\":\"Flight\",\"mode\":\"relational\",\"from\":\"AUS\",\"to\":\"DFW\"}",
                List.of(),
                Node.Mode.RELATIONAL
        );

        GraphContext context = new GraphContext(Map.of(factId, node));

        QueryRequest<ValidateTask> request =
                QueryRequests.validateTask(meta, task, context, factId);

        PromptBuilder promptBuilder = new PromptBuilder(new SimpleResponseContractRegistry());
        FakeLlmClient llmClient = new FakeLlmClient("malformed-or-contract-violating-response");

        ValidationResult errorResult = ValidationResult.error(
                "CONTRACT_VIOLATION",
                "Response did not match ValidationResult schema",
                "LLM_RESPONSE_VALIDATION",
                null
        );

        FakeValidationResultValidator validator =
                new FakeValidationResultValidator(errorResult);

        CgoQueryPipeline pipeline = new CgoQueryPipeline(promptBuilder, llmClient, validator);

        // act
        QueryExecution<ValidateTask> execution = pipeline.execute(request);

        // assert
        assertTrue(validator.wasCalled(), "Validator should be called when configured");
        assertEquals("malformed-or-contract-violating-response", validator.getLastRawResponse());

        ValidationResult vr = execution.getValidationResult();
        assertNotNull(vr, "ValidationResult should be attached to execution");
        assertFalse(vr.isOk(), "ValidationResult should indicate failure");
        assertEquals("CONTRACT_VIOLATION", vr.getCode());
        assertEquals("LLM_RESPONSE_VALIDATION", vr.getStage());
    }

    @Test
    void execute_withoutValidator_shouldNotAttachValidationResult() {
        // arrange
        String factId = "Flight:F100";

        Meta meta = new Meta(
                "v1",
                "validate_flight_airports",
                "Validate that the selected flight fact has valid departure and arrival airport codes using graph context."
        );

        String taskDescription =
                "Validate that the selected flight has valid departure and arrival airport codes based on the airport nodes in the graph. " +
                        "A valid flight must have: (1) 'from' matching one Airport:* code, (2) 'to' matching one Airport:* code, (3) 'from' != 'to'.";

        ValidateTask task = new ValidateTask(taskDescription, factId);

        Node node = new Node(
                factId,
                "{\"id\":\"F100\",\"kind\":\"Flight\",\"mode\":\"relational\",\"from\":\"AUS\",\"to\":\"DFW\"}",
                List.of(),
                Node.Mode.RELATIONAL
        );

        GraphContext context = new GraphContext(Map.of(factId, node));

        QueryRequest<ValidateTask> request =
                QueryRequests.validateTask(meta, task, context, factId);

        PromptBuilder promptBuilder = new PromptBuilder(new SimpleResponseContractRegistry());
        FakeLlmClient llmClient = new FakeLlmClient("{\"some\":\"response\"}");

        // use the constructor without a ValidationResultValidator
        CgoQueryPipeline pipeline = new CgoQueryPipeline(promptBuilder, llmClient);

        // act
        QueryExecution<ValidateTask> execution = pipeline.execute(request);

        // assert
        assertEquals("{\"some\":\"response\"}", execution.getRawResponse());
        assertNull(execution.getValidationResult(), "ValidationResult should be null when no validator is configured");
    }

    class FakeValidationResultValidator implements ValidationResultValidator {

        private final ValidationResult toReturn;
        private boolean called = false;
        private String lastRawResponse;

        FakeValidationResultValidator(ValidationResult toReturn) {
            this.toReturn = toReturn;
        }

        @Override
        public ValidationResult validate(String rawResponse) {
            this.called = true;
            this.lastRawResponse = rawResponse;
            return toReturn;
        }

        boolean wasCalled() {
            return called;
        }

        String getLastRawResponse() {
            return lastRawResponse;
        }
    }

}

