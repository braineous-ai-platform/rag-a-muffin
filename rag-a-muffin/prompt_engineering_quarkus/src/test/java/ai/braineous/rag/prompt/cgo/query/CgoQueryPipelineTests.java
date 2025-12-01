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
    void execute_withoutValidators_shouldReturnRawResponse_andNoValidations() {
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

        // LlmClient stub: no validators configured anywhere
        FakeLlmClient llmClient = new FakeLlmClient("{\"result\":{\"status\":\"VALID\"}}");

        CgoQueryPipeline pipeline = new CgoQueryPipeline(promptBuilder, llmClient);

        // act
        QueryExecution<ValidateTask> execution = pipeline.execute(request);

        // console inspect
        Console.log("happy.prompt", llmClient.getLastPrompt());
        Console.log("happy.rawResponse", execution.getRawResponse());
        Console.log("happy.promptValidation", execution.getPromptValidation());
        Console.log("happy.llmResponseValidation", execution.getLlmResponseValidation());
        Console.log("happy.domainValidation", execution.getDomainValidation());

        // assert
        assertNotNull(execution, "QueryExecution should not be null");
        assertSame(request, execution.getRequest(), "Execution should wrap the same QueryRequest instance");

        // rawResponse should be whatever FakeLlmClient returned
        assertEquals("{\"result\":{\"status\":\"VALID\"}}", execution.getRawResponse());

        // with no validators configured:
        assertNull(execution.getPromptValidation(), "promptValidation should be null when no prompt validator is configured");
        assertNull(execution.getLlmResponseValidation(), "llmResponseValidation should be null when no PhaseResultValidator is configured");
        assertNull(execution.getDomainValidation(), "domainValidation should be null when no per-request rule is configured");
    }

    @Test
    void execute_withPromptValidationError_shouldFailFast_beforeCallingLlm() {
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

        // Prompt validator that ALWAYS returns an error
        PhaseResultValidator failingPromptValidator = raw ->
                ValidationResult.error(
                        "PROMPT_ERROR",
                        "Prompt contract invalid",
                        "prompt_contract_validation",
                        null
                );

        // PromptBuilder wired with failing prompt validator
        PromptBuilder promptBuilder = new PromptBuilder(
                new SimpleResponseContractRegistry(),
                failingPromptValidator
        );

        // LLM client that we expect NOT to be called
        CountingLlmClient llmClient = new CountingLlmClient();

        CgoQueryPipeline pipeline = new CgoQueryPipeline(promptBuilder, llmClient);

        // act
        QueryExecution<ValidateTask> execution = pipeline.execute(request);

        // observe
        Console.log("promptFail.rawResponse", execution.getRawResponse());
        Console.log("promptFail.promptValidation", execution.getPromptValidation());
        Console.log("promptFail.llmResponseValidation", execution.getLlmResponseValidation());
        Console.log("promptFail.domainValidation", execution.getDomainValidation());
        Console.log("promptFail.llmCallCount", llmClient.callCount);

        // assert

        // 1) LLM must NOT be called
        assertEquals(0, llmClient.callCount, "LLM should not be called when prompt validation fails");

        // 2) rawResponse must be null
        assertNull(execution.getRawResponse(), "rawResponse should be null on prompt fail-fast");

        // 3) promptValidation must be set and failing
        assertNotNull(execution.getPromptValidation(), "promptValidation must be set");
        assertFalse(execution.getPromptValidation().isOk(), "promptValidation must indicate failure");
        assertEquals("PROMPT_ERROR", execution.getPromptValidation().getCode());
        assertEquals("prompt_contract_validation", execution.getPromptValidation().getStage());

        // 4) later-phase validations must be null
        assertNull(execution.getLlmResponseValidation(), "llmResponseValidation must be null after prompt fail-fast");
        assertNull(execution.getDomainValidation(), "domainValidation must be null when domain rule never ran");
    }

    @Test
    void execute_withCoreValidator_ok_shouldAttachLlmResponseValidation_andLeaveDomainNull() {
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

        // PromptBuilder with NO prompt-validation in this scenario
        PromptBuilder promptBuilder = new PromptBuilder(new SimpleResponseContractRegistry());

        // LLM returns some JSON
        String raw = "{\"some\":\"response\"}";
        FakeLlmClient llmClient = new FakeLlmClient(raw);

        // Core validator returns OK
        ValidationResult okCoreValidation = ValidationResult.ok("LLM_RESPONSE_VALIDATION");
        FakePhaseResultValidator coreValidator = new FakePhaseResultValidator(okCoreValidation);

        CgoQueryPipeline pipeline = new CgoQueryPipeline(promptBuilder, llmClient, coreValidator);

        // act
        QueryExecution<ValidateTask> execution = pipeline.execute(request);

        // observe
        Console.log("coreOk.rawResponse", execution.getRawResponse());
        Console.log("coreOk.promptValidation", execution.getPromptValidation());
        Console.log("coreOk.llmResponseValidation", execution.getLlmResponseValidation());
        Console.log("coreOk.domainValidation", execution.getDomainValidation());
        Console.log("coreOk.validator.lastRawResponse", coreValidator.getLastRawResponse());

        // assert
        // 1) validator must have been called with the raw LLM response
        assertTrue(coreValidator.wasCalled(), "Core validator should be called");
        assertEquals(raw, coreValidator.getLastRawResponse(), "Validator should see the same raw response as in QueryExecution");

        // 2) promptValidation should be null (no prompt validation wired)
        assertNull(execution.getPromptValidation(), "promptValidation should be null when no prompt validator is configured");

        // 3) llmResponseValidation should be present and OK
        assertNotNull(execution.getLlmResponseValidation(), "llmResponseValidation should be attached");
        assertTrue(execution.getLlmResponseValidation().isOk(), "llmResponseValidation should be ok");
        // optional: same instance
        assertSame(okCoreValidation, execution.getLlmResponseValidation(),
                "llmResponseValidation should be the same instance returned by the core validator");

        // 4) domainValidation should still be null (no rule configured)
        assertNull(execution.getDomainValidation(), "domainValidation should be null when no per-request rule is configured");
    }

    @Test
    void execute_withCoreValidator_error_shouldExposeFailureOnLlmResponseValidation() {
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

        // PromptBuilder with NO prompt-validation in this scenario
        PromptBuilder promptBuilder = new PromptBuilder(new SimpleResponseContractRegistry());

        // LLM returns malformed / contract-violating response
        String raw = "malformed-or-contract-violating-response";
        FakeLlmClient llmClient = new FakeLlmClient(raw);

        // Core validator returns an error
        ValidationResult errorResult = ValidationResult.error(
                "CONTRACT_VIOLATION",
                "Response did not match expected schema",
                "LLM_RESPONSE_VALIDATION",
                null
        );

        FakePhaseResultValidator coreValidator = new FakePhaseResultValidator(errorResult);

        CgoQueryPipeline pipeline = new CgoQueryPipeline(promptBuilder, llmClient, coreValidator);

        // act
        QueryExecution<ValidateTask> execution = pipeline.execute(request);

        // observe
        Console.log("coreErr.rawResponse", execution.getRawResponse());
        Console.log("coreErr.promptValidation", execution.getPromptValidation());
        Console.log("coreErr.llmResponseValidation", execution.getLlmResponseValidation());
        Console.log("coreErr.domainValidation", execution.getDomainValidation());
        Console.log("coreErr.validator.lastRawResponse", coreValidator.getLastRawResponse());

        // assert

        // 1) validator must have been called with the raw LLM response
        assertTrue(coreValidator.wasCalled(), "Core validator should be called");
        assertEquals(raw, coreValidator.getLastRawResponse(),
                "Validator should see the same raw response as in QueryExecution");

        // 2) promptValidation should be null (no prompt validation wired)
        assertNull(execution.getPromptValidation(), "promptValidation should be null when no prompt validator is configured");

        // 3) llmResponseValidation should be present and failing
        assertNotNull(execution.getLlmResponseValidation(), "llmResponseValidation should be attached");
        assertFalse(execution.getLlmResponseValidation().isOk(), "llmResponseValidation should indicate failure");
        assertEquals("CONTRACT_VIOLATION", execution.getLlmResponseValidation().getCode());
        assertEquals("LLM_RESPONSE_VALIDATION", execution.getLlmResponseValidation().getStage());

        // 4) domainValidation should still be null (no rule configured)
        assertNull(execution.getDomainValidation(), "domainValidation should be null when no per-request rule is configured");
    }

    @Test
    void execute_withDomainRule_ok_shouldAttachDomainValidation() {
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

        // rule = domain validation for this query
        // it always returns OK in this scenario
        LLMResponseValidatorRule rule = raw ->
                ValidationResult.ok("domain_rule_validation");

        QueryRequest<ValidateTask> request =
                QueryRequests.validateTask(meta, task, context, factId, rule);

        // no prompt validator
        PromptBuilder promptBuilder = new PromptBuilder(new SimpleResponseContractRegistry());

        // LLM returns some JSON
        String raw = "{\"result\":\"anything\"}";
        FakeLlmClient llmClient = new FakeLlmClient(raw);

        // no core PhaseResultValidator
        CgoQueryPipeline pipeline = new CgoQueryPipeline(promptBuilder, llmClient);

        // act
        QueryExecution<ValidateTask> execution = pipeline.execute(request);

        // observe
        Console.log("domainOk.rawResponse", execution.getRawResponse());
        Console.log("domainOk.promptValidation", execution.getPromptValidation());
        Console.log("domainOk.llmResponseValidation", execution.getLlmResponseValidation());
        Console.log("domainOk.domainValidation", execution.getDomainValidation());

        // assert

        // 1) basic wiring
        assertNotNull(execution, "QueryExecution should not be null");
        assertSame(request, execution.getRequest(), "Execution should wrap the same QueryRequest instance");
        assertEquals(raw, execution.getRawResponse(), "rawResponse should be whatever LLM returned");

        // 2) no prompt/core validations configured in this scenario
        assertNull(execution.getPromptValidation(), "promptValidation should be null when no prompt validator is configured");
        assertNull(execution.getLlmResponseValidation(), "llmResponseValidation should be null when no PhaseResultValidator is configured");

        // 3) domainValidation should be present and OK
        assertNotNull(execution.getDomainValidation(), "domainValidation should be attached when a rule is configured");
        assertTrue(execution.getDomainValidation().isOk(), "domainValidation should be ok for passing rule");
        assertEquals("domain_rule_validation", execution.getDomainValidation().getStage(),
                "domainValidation.stage should match the rule's stage");
    }

    @Test
    void execute_withDomainRule_error_shouldAttachDomainValidationError() {
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

        // Domain rule that always FAILS
        ValidationResult domainError = ValidationResult.error(
                "DOMAIN_RULE_ERROR",
                "Domain rule failed",
                "domain_rule_validation",
                null
        );

        LLMResponseValidatorRule rule = raw -> domainError;

        QueryRequest<ValidateTask> request =
                QueryRequests.validateTask(meta, task, context, factId, rule);

        // no prompt validator
        PromptBuilder promptBuilder = new PromptBuilder(new SimpleResponseContractRegistry());

        // LLM returns some JSON
        String raw = "{\"result\":\"anything\"}";
        FakeLlmClient llmClient = new FakeLlmClient(raw);

        // no core PhaseResultValidator
        CgoQueryPipeline pipeline = new CgoQueryPipeline(promptBuilder, llmClient);

        // act
        QueryExecution<ValidateTask> execution = pipeline.execute(request);

        // observe
        Console.log("domainErr.rawResponse", execution.getRawResponse());
        Console.log("domainErr.promptValidation", execution.getPromptValidation());
        Console.log("domainErr.llmResponseValidation", execution.getLlmResponseValidation());
        Console.log("domainErr.domainValidation", execution.getDomainValidation());

        // assert

        // 1) rawResponse should still be the LLM output
        assertEquals(raw, execution.getRawResponse(), "rawResponse should be whatever LLM returned");

        // 2) earlier phases have no validators in this scenario
        assertNull(execution.getPromptValidation(), "promptValidation should be null when no prompt validator is configured");
        assertNull(execution.getLlmResponseValidation(), "llmResponseValidation should be null when no PhaseResultValidator is configured");

        // 3) domainValidation should be present and failing
        assertNotNull(execution.getDomainValidation(), "domainValidation should be attached when rule is configured");
        assertFalse(execution.getDomainValidation().isOk(), "domainValidation should indicate failure");
        assertEquals("DOMAIN_RULE_ERROR", execution.getDomainValidation().getCode());
        assertEquals("domain_rule_validation", execution.getDomainValidation().getStage());
    }

    @Test
    void execute_withPromptCoreAndDomainAllOk_shouldAttachAllValidationPhases() {
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

        // Domain rule (RuleValidation) – domainValidation slot
        LLMResponseValidatorRule rule = raw ->
                ValidationResult.ok("domain_rule_validation");

        QueryRequest<ValidateTask> request =
                QueryRequests.validateTask(meta, task, context, factId, rule);

        // Prompt validator – promptValidation slot
        PhaseResultValidator okPromptValidator = raw ->
                ValidationResult.ok("prompt_contract_validation");

        PromptBuilder promptBuilder = new PromptBuilder(
                new SimpleResponseContractRegistry(),
                okPromptValidator
        );

        // LLM returns some JSON
        String rawResponse = "{\"result\":{\"status\":\"VALID\"}}";
        FakeLlmClient llmClient = new FakeLlmClient(rawResponse);

        // Core LLM validator – llmResponseValidation slot
        ValidationResult okCoreValidation = ValidationResult.ok("LLM_RESPONSE_VALIDATION");
        FakePhaseResultValidator coreValidator = new FakePhaseResultValidator(okCoreValidation);

        CgoQueryPipeline pipeline = new CgoQueryPipeline(promptBuilder, llmClient, coreValidator);

        // act
        QueryExecution<ValidateTask> execution = pipeline.execute(request);

        // observe
        Console.log("allOk.rawResponse", execution.getRawResponse());
        Console.log("allOk.promptValidation", execution.getPromptValidation());
        Console.log("allOk.llmResponseValidation", execution.getLlmResponseValidation());
        Console.log("allOk.domainValidation", execution.getDomainValidation());

        // assert: rawResponse
        assertEquals(rawResponse, execution.getRawResponse(), "rawResponse should be whatever LLM returned");

        // promptValidation – present & OK
        assertNotNull(execution.getPromptValidation(), "promptValidation should be attached when prompt validator is configured");
        assertTrue(execution.getPromptValidation().isOk(), "promptValidation should be ok");
        assertEquals("prompt_contract_validation", execution.getPromptValidation().getStage());

        // llmResponseValidation – present & OK
        assertNotNull(execution.getLlmResponseValidation(), "llmResponseValidation should be attached when core validator is configured");
        assertTrue(execution.getLlmResponseValidation().isOk(), "llmResponseValidation should be ok");
        assertEquals("LLM_RESPONSE_VALIDATION", execution.getLlmResponseValidation().getStage());

        // domainValidation – present & OK
        assertNotNull(execution.getDomainValidation(), "domainValidation should be attached when rule is configured");
        assertTrue(execution.getDomainValidation().isOk(), "domainValidation should be ok");
        assertEquals("domain_rule_validation", execution.getDomainValidation().getStage());
    }

    @Test
    void execute_withNullRequest_shouldThrowNullPointerException() {
        // arrange
        PromptBuilder promptBuilder = new PromptBuilder(new SimpleResponseContractRegistry());
        FakeLlmClient llmClient = new FakeLlmClient("{\"ignored\":true}");
        CgoQueryPipeline pipeline = new CgoQueryPipeline(promptBuilder, llmClient);

        // act + assert
        assertThrows(NullPointerException.class,
                () -> pipeline.execute(null),
                "execute(null) should fail fast with NullPointerException due to Objects.requireNonNull");
    }
    ////--------------------------------------------------------------------------
    private static final class CountingLlmClient implements LlmClient {
        int callCount = 0;
        JsonObject lastPrompt;

        @Override
        public String executePrompt(JsonObject prompt) {
            callCount++;
            lastPrompt = prompt;
            Console.log("LLM Prompt (CountingLlmClient)", prompt);
            return "{\"ignored\":true}";
        }
    }

    private static final class FakePhaseResultValidator implements PhaseResultValidator {

        private final ValidationResult toReturn;
        private boolean called = false;
        private String lastRawResponse;

        private FakePhaseResultValidator(ValidationResult toReturn) {
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

