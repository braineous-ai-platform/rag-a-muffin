package ai.braineous.rag.prompt.cgo.query;

import ai.braineous.rag.prompt.cgo.api.*;
import ai.braineous.rag.prompt.cgo.prompt.FakeLlmClient;
import ai.braineous.rag.prompt.cgo.prompt.LlmClient;
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

        FakePhaseResultValidator validator =
                new FakePhaseResultValidator(ValidationResult.ok("LLM_RESPONSE_VALIDATION"));

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

        FakePhaseResultValidator validator =
                new FakePhaseResultValidator(errorResult);

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

    class FakePhaseResultValidator implements PhaseResultValidator {

        private final ValidationResult toReturn;
        private boolean called = false;
        private String lastRawResponse;

        FakePhaseResultValidator(ValidationResult toReturn) {
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

    @Test
    void execute_withGsonValidator_andValidJson_shouldAttachOkValidationResult() {
        // arrange
        String factId = "Flight:F100";

        Meta meta = new Meta(
                "v1",
                "validate_flight_airports",
                "Validate that the selected flight fact has valid departure and arrival airport codes using graph context."
        );

        String taskDescription =
                "Validate minimal shape for LLM validation via pipeline integration.";

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

        // Fake but valid ValidationResult JSON
        String llmJson = """
            {
              "result": {
                "ok": true,
                "code": "response.contract.ok",
                "message": "All good",
                "stage": "llm_response_validation",
                "anchorId": "Fact:123",
                "metadata": {
                  "raw_length": 100
                }
              }
            }
            """;

        PromptBuilder promptBuilder = new PromptBuilder(new SimpleResponseContractRegistry());
        FakeLlmClient llmClient = new FakeLlmClient(llmJson);

        GsonPhaseResultValidator validator = new GsonPhaseResultValidator();

        CgoQueryPipeline pipeline = new CgoQueryPipeline(promptBuilder, llmClient, validator);

        // act
        QueryExecution<ValidateTask> execution = pipeline.execute(request);

        Console.log("Pipeline LLM Prompt", llmClient.getLastPrompt());
        Console.log("Pipeline ValidationResult", execution.getValidationResult());

        // assert: ValidationResult comes through pipeline
        ValidationResult vr = execution.getValidationResult();
        assertNotNull(vr, "ValidationResult should be attached to QueryExecution");

        assertTrue(vr.isOk(), "ValidationResult should be ok=true");
        assertEquals("response.contract.ok", vr.getCode());
        assertEquals("All good", vr.getMessage());
        assertEquals("llm_response_validation", vr.getStage());
        assertEquals("Fact:123", vr.getAnchorId());

        Map<String, Object> metadata = vr.getMetadata();
        assertNotNull(metadata);
        assertEquals(1, metadata.size());
        assertEquals(100.0, metadata.get("raw_length"));  // Gson → Double
    }

    @Test
    void execute_withoutValidators_shouldReturnRawResponseAndNoValidationResult() {
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

        // PromptBuilder with NO prompt validator
        PromptBuilder promptBuilder = new PromptBuilder(new SimpleResponseContractRegistry());

        // LlmClient stub
        LlmClient llmClient = prompt -> {
            Console.log("LLM Prompt (no validation)", prompt);
            return "{\"ok\":true,\"code\":\"dummy\",\"message\":\"hello\"}";
        };

        CgoQueryPipeline pipeline = new CgoQueryPipeline(promptBuilder, llmClient);

        // act
        QueryExecution<ValidateTask> execution = pipeline.execute(request);

        // console inspect
        Console.log("QueryExecution.rawResponse", execution.getRawResponse());
        Console.log("QueryExecution.validationResult", execution.getValidationResult());

        // assert
        assertNotNull(execution, "QueryExecution should not be null");
        assertEquals(request, execution.getRequest());
        assertEquals("{\"ok\":true,\"code\":\"dummy\",\"message\":\"hello\"}", execution.getRawResponse());
        assertFalse(execution.hasValidationResult(), "Expected no ValidationResult in no-validation mode");
        assertNull(execution.getValidationResult(), "ValidationResult should be null in no-validation mode");
    }

    @Test
    void execute_withPromptValidationError_shouldNotCallLlmAndShouldReturnPromptValidation() {
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

        // Fake prompt validator that ALWAYS returns an error
        PhaseResultValidator failingPromptValidator = raw ->
                ValidationResult.createInternal(
                        false,
                        "prompt.contract.error",
                        "Prompt contract invalid (fake error)",
                        "prompt_contract_validation",
                        null,
                        Map.of("phase", "prompt_builder_prompt_validation")
                );

        // PromptBuilder in validation mode
        PromptBuilder promptBuilder = new PromptBuilder(new SimpleResponseContractRegistry(), failingPromptValidator);

        // LlmClient that we expect NOT to be called
        class CountingLlmClient implements LlmClient {
            int callCount = 0;
            JsonObject lastPrompt;

            @Override
            public String executePrompt(JsonObject prompt) {
                callCount++;
                lastPrompt = prompt;
                Console.log("LLM Prompt (should NOT be called)", prompt);
                return "{\"ok\":true}";
            }
        }

        CountingLlmClient llmClient = new CountingLlmClient();

        // No response validator – we are testing prompt validation gate
        CgoQueryPipeline pipeline = new CgoQueryPipeline(promptBuilder, llmClient);

        // act
        QueryExecution<ValidateTask> execution = pipeline.execute(request);

        // console inspect
        Console.log("QueryExecution.rawResponse (prompt error)", execution.getRawResponse());
        Console.log("QueryExecution.validationResult (prompt error)", execution.getValidationResult());
        Console.log("LLM callCount", llmClient.callCount);

        // assert
        assertNotNull(execution, "QueryExecution should not be null");
        assertEquals(request, execution.getRequest());

        // LLM should NOT have been called
        assertEquals(0, llmClient.callCount, "LLM should not be called when prompt validation fails");
        assertNull(execution.getRawResponse(), "Raw response should be null when prompt validation fails");

        // ValidationResult should be the prompt validation error
        assertFalse(execution.hasValidationResult(), "fail-fast if prompt validation fails");
    }
}

