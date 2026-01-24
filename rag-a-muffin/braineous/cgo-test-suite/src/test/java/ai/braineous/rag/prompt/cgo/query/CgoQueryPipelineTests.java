package ai.braineous.rag.prompt.cgo.query;

import ai.braineous.cgo.history.HistoryStore;
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
        request.setAdapter(new FakeLlmAdapter());

        // PromptBuilder with NO prompt validator
        PromptBuilder promptBuilder = new PromptBuilder();

        // LlmClient stub: no validators configured anywhere
        String raw = """
        {
          "result": {
            "ok": true,
            "code": "response.contract.ok",
            "message": "VALID",
            "stage": "llm_response_validation",
            "anchorId": null,
            "metadata": { "adapter": "fake" }
          }
        }
        """;
        FakeLlmClient llmClient = new FakeLlmClient(raw);

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
        assertEquals(raw, execution.getRawResponse());
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
        request.setAdapter(new FakeLlmAdapter());

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
        request.setAdapter(new FakeLlmAdapter());

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
        request.setAdapter(new FakeLlmAdapter());

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
        request.setAdapter(new FakeLlmAdapter());

        // no prompt validator
        PromptBuilder promptBuilder = new PromptBuilder(new SimpleResponseContractRegistry());

        // LLM returns some JSON
        String raw = """
        {
          "result": {
            "ok": true,
            "code": "response.contract.ok",
            "message": "VALID",
            "stage": "llm_response_validation",
            "anchorId": null,
            "metadata": { "adapter": "fake" }
          }
        }
        """;
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
        request.setAdapter(new FakeLlmAdapter());

        // no prompt validator
        PromptBuilder promptBuilder = new PromptBuilder(new SimpleResponseContractRegistry());

        // LLM returns some JSON
        String raw = """
        {
          "result": {
            "ok": true,
            "code": "response.contract.ok",
            "message": "VALID",
            "stage": "llm_response_validation",
            "anchorId": null,
            "metadata": { "adapter": "fake" }
          }
        }
        """;

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
        request.setAdapter(new FakeLlmAdapter());

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


    @Test
    void pipeline_execute_missingAdapter_shouldFailFast_andNotAppendHistory() {
        Console.log("test_start", "pipeline_execute_missingAdapter_shouldFailFast_andNotAppendHistory");

        // arrange
        HistoryStore store = HistoryStore.getInstance();
        store.clear();

        int before = store.getAll().size();
        Console.log("history_before", before);

        String factId = "Flight:F100";
        Meta meta = new Meta("v1", "validate_flight_airports", "missing adapter guard");
        ValidateTask task = new ValidateTask("validate flight airports", factId);

        Node node = new Node(
                factId,
                "{\"id\":\"F100\",\"kind\":\"Flight\",\"from\":\"AUS\",\"to\":\"DFW\"}",
                List.of(),
                Node.Mode.RELATIONAL
        );

        GraphContext context = new GraphContext(Map.of(factId, node));

        QueryRequest<ValidateTask> request = QueryRequests.validateTask(meta, task, context, factId);

        // IMPORTANT: DO NOT set adapter. This should trigger fail-fast.
        // request.setAdapter(...);

        PromptBuilder promptBuilder = new PromptBuilder(new SimpleResponseContractRegistry());
        CgoQueryPipeline pipeline = new CgoQueryPipeline(promptBuilder, (LlmClient) null);

        // act
        try {
            pipeline.execute(request);
            fail("Expected pipeline to fail fast when adapter is missing");
        } catch (Exception e) {
            Console.log("caught_exception_class", e.getClass().getName());
            Console.log("caught_exception_message", e.getMessage());
            // optional: assert message contains your exact guardrail text
            assertTrue(
                    e.getMessage() != null && e.getMessage().toLowerCase().contains("missing llmadapter"),
                    "Exception message should mention missing LlmAdapter"
            );
        }

        // assert: scorer side-effect did NOT happen
        int after = store.getAll().size();
        Console.log("history_after", after);

        assertEquals(before, after, "History should not change when adapter is missing");
    }

    @Test
    void pipeline_doubleExecute_shouldAppendTwoHistoryRecords() {
        Console.log("test_start", "pipeline_doubleExecute_shouldAppendTwoHistoryRecords");

        // arrange
        HistoryStore store = HistoryStore.getInstance();
        store.clear();

        int before = store.getAll().size();
        Console.log("history_before", before);

        String factId = "Flight:F100";
        Meta meta = new Meta("v1", "validate_flight_airports", "double execute");
        ValidateTask task = new ValidateTask("validate flight airports", factId);

        Node node = new Node(
                factId,
                "{\"id\":\"F100\",\"kind\":\"Flight\",\"from\":\"AUS\",\"to\":\"DFW\"}",
                List.of(),
                Node.Mode.RELATIONAL
        );

        GraphContext context = new GraphContext(Map.of(factId, node));
        QueryRequest<ValidateTask> request = QueryRequests.validateTask(meta, task, context, factId);

        request.setAdapter(new LlmAdapter() {
            @Override
            public String invokeLlm(JsonObject prompt) {
                Console.log("fake_adapter_invoked", "ok");
                return """
                {
                  "result": {
                    "ok": true,
                    "code": "response.contract.ok",
                    "message": "VALID",
                    "stage": "llm_response_validation",
                    "anchorId": null,
                    "metadata": { "adapter": "fake" }
                  }
                }
                """;

            }
        });

        PromptBuilder promptBuilder = new PromptBuilder(new SimpleResponseContractRegistry());
        CgoQueryPipeline pipeline = new CgoQueryPipeline(promptBuilder);

        // act
        QueryExecution<ValidateTask> e1 = pipeline.execute(request);
        QueryExecution<ValidateTask> e2 = pipeline.execute(request);

        // assert
        assertNotNull(e1);
        assertNotNull(e2);

        int after = store.getAll().size();
        Console.log("history_after", after);

        assertEquals(before + 2, after, "Pipeline should append two history records for two executes");
    }

    @Test
    void execute_withCoreValidator_error_shouldNotInvokeDomainRule() {

        // arrange
        String factId = "Flight:F100";

        Meta meta = new Meta("v1", "validate_flight_airports", "core fail skips domain rule");
        ValidateTask task = new ValidateTask("validate flight airports", factId);

        Node node = new Node(
                factId,
                "{\"id\":\"F100\",\"kind\":\"Flight\",\"mode\":\"relational\",\"from\":\"AUS\",\"to\":\"DFW\"}",
                java.util.List.of(),
                Node.Mode.RELATIONAL
        );

        GraphContext context = new GraphContext(java.util.Map.of(factId, node));

        // Domain rule with call counter (must NOT run)
        final java.util.concurrent.atomic.AtomicInteger ruleCalls = new java.util.concurrent.atomic.AtomicInteger(0);
        LLMResponseValidatorRule rule = raw -> {
            ruleCalls.incrementAndGet();
            return ValidationResult.ok("domain_rule_validation");
        };

        QueryRequest<ValidateTask> request =
                QueryRequests.validateTask(meta, task, context, factId, rule);
        request.setAdapter(new FakeLlmAdapter());

        PromptBuilder promptBuilder = new PromptBuilder(new SimpleResponseContractRegistry());

        String raw = "malformed-or-contract-violating-response";
        FakeLlmClient llmClient = new FakeLlmClient(raw);

        ValidationResult coreError = ValidationResult.error(
                "CONTRACT_VIOLATION",
                "core validator failed",
                "LLM_RESPONSE_VALIDATION",
                null
        );
        FakePhaseResultValidator coreValidator = new FakePhaseResultValidator(coreError);

        CgoQueryPipeline pipeline = new CgoQueryPipeline(promptBuilder, llmClient, coreValidator);

        // act
        QueryExecution<ValidateTask> execution = pipeline.execute(request);

        // observe
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("ruleCalls", ruleCalls.get());
        jsonObject.addProperty("coreValidationOk", execution.getLlmResponseValidation() != null && execution.getLlmResponseValidation().isOk());
        jsonObject.addProperty("domainValidationPresent", execution.getDomainValidation() != null);
        Console.log("UT:CgoQueryPipeline.core_fail_skips_domain", jsonObject.toString());

        // assert
        org.junit.jupiter.api.Assertions.assertEquals(0, ruleCalls.get(), "Domain rule must not run when core validation fails");
        org.junit.jupiter.api.Assertions.assertNotNull(execution.getLlmResponseValidation());
        org.junit.jupiter.api.Assertions.assertFalse(execution.getLlmResponseValidation().isOk());
        org.junit.jupiter.api.Assertions.assertNull(execution.getDomainValidation(), "domainValidation must be null when rule never ran");
    }

    @Test
    void execute_withDomainRule_error_shouldNotInvokeScorer() throws Exception {

        // arrange
        String factId = "Flight:F100";

        Meta meta = new Meta("v1", "validate_flight_airports", "domain fail skips scorer");
        ValidateTask task = new ValidateTask("validate flight airports", factId);

        Node node = new Node(
                factId,
                "{\"id\":\"F100\",\"kind\":\"Flight\",\"mode\":\"relational\",\"from\":\"AUS\",\"to\":\"DFW\"}",
                java.util.List.of(),
                Node.Mode.RELATIONAL
        );

        GraphContext context = new GraphContext(java.util.Map.of(factId, node));

        // Domain rule that FAILS
        LLMResponseValidatorRule rule = new LLMResponseValidatorRule() {
            @Override
            public ValidationResult validate(String raw) {
                return ValidationResult.error("DOMAIN_RULE_ERROR", "domain rule failed", "domain_rule_validation", null);
            }
        };

        QueryRequest<ValidateTask> request =
                QueryRequests.validateTask(meta, task, context, factId, rule);
        request.setAdapter(new FakeLlmAdapter());

        PromptBuilder promptBuilder = new PromptBuilder(new SimpleResponseContractRegistry());

        String raw = "{\"some\":\"response\"}";
        FakeLlmClient llmClient = new FakeLlmClient(raw);

        // Core validator OK (so domain rule runs and fails)
        ValidationResult okCoreValidation = ValidationResult.ok("LLM_RESPONSE_VALIDATION");
        FakePhaseResultValidator coreValidator = new FakePhaseResultValidator(okCoreValidation);

        CgoQueryPipeline pipeline = new CgoQueryPipeline(promptBuilder, llmClient, coreValidator);

        // scorer stub with call counter
        final java.util.concurrent.atomic.AtomicInteger scorerCalls = new java.util.concurrent.atomic.AtomicInteger(0);
        ScorerClient scorer = new ScorerClient() {
            @Override
            public void orchestrate(QueryExecution execution) {
                scorerCalls.incrementAndGet();
                Console.log("UT:ScorerClient.orchestrate", "called");
            }
        };

        // inject scorerClient via reflection (core untouched)
        java.lang.reflect.Field f = CgoQueryPipeline.class.getDeclaredField("scorerClient");
        f.setAccessible(true);
        f.set(pipeline, scorer);

        // act
        QueryExecution<ValidateTask> execution = pipeline.execute(request);

        // observe
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("scorerCalls", scorerCalls.get());
        jsonObject.addProperty("domainOk", execution.getDomainValidation() != null && execution.getDomainValidation().isOk());
        Console.log("UT:CgoQueryPipeline.domain_fail_skips_scorer", jsonObject.toString());

        // assert
        org.junit.jupiter.api.Assertions.assertNotNull(execution.getDomainValidation());
        org.junit.jupiter.api.Assertions.assertFalse(execution.getDomainValidation().isOk());
        org.junit.jupiter.api.Assertions.assertEquals(0, scorerCalls.get(), "Scorer must not run when domain validation fails");
    }

    @Test
    void execute_withCoreValidator_error_shouldNotInvokeScorer() throws Exception {

        // arrange
        String factId = "Flight:F100";

        Meta meta = new Meta("v1", "validate_flight_airports", "core fail skips scorer");
        ValidateTask task = new ValidateTask("validate flight airports", factId);

        Node node = new Node(
                factId,
                "{\"id\":\"F100\",\"kind\":\"Flight\",\"mode\":\"relational\",\"from\":\"AUS\",\"to\":\"DFW\"}",
                java.util.List.of(),
                Node.Mode.RELATIONAL
        );

        GraphContext context = new GraphContext(java.util.Map.of(factId, node));

        // rule exists but must NOT run due to core failure
        LLMResponseValidatorRule rule = new LLMResponseValidatorRule() {
            @Override
            public ValidationResult validate(String raw) {
                return ValidationResult.ok("domain_rule_validation");
            }
        };

        QueryRequest<ValidateTask> request =
                QueryRequests.validateTask(meta, task, context, factId, rule);
        request.setAdapter(new FakeLlmAdapter());

        PromptBuilder promptBuilder = new PromptBuilder(new SimpleResponseContractRegistry());

        String raw = "malformed-or-contract-violating-response";
        FakeLlmClient llmClient = new FakeLlmClient(raw);

        // Core validator FAILS
        ValidationResult coreError = ValidationResult.error(
                "CONTRACT_VIOLATION",
                "core validator failed",
                "LLM_RESPONSE_VALIDATION",
                null
        );
        FakePhaseResultValidator coreValidator = new FakePhaseResultValidator(coreError);

        CgoQueryPipeline pipeline = new CgoQueryPipeline(promptBuilder, llmClient, coreValidator);

        // scorer stub with call counter
        final java.util.concurrent.atomic.AtomicInteger scorerCalls = new java.util.concurrent.atomic.AtomicInteger(0);
        ScorerClient scorer = new ScorerClient() {
            @Override
            public void orchestrate(QueryExecution execution) {
                scorerCalls.incrementAndGet();
                Console.log("UT:ScorerClient.orchestrate", "called");
            }
        };

        // inject scorerClient via reflection
        java.lang.reflect.Field f = CgoQueryPipeline.class.getDeclaredField("scorerClient");
        f.setAccessible(true);
        f.set(pipeline, scorer);

        // act
        QueryExecution<ValidateTask> execution = pipeline.execute(request);

        // observe
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("scorerCalls", scorerCalls.get());
        jsonObject.addProperty("coreOk", execution.getLlmResponseValidation() != null && execution.getLlmResponseValidation().isOk());
        Console.log("UT:CgoQueryPipeline.core_fail_skips_scorer", jsonObject.toString());

        // assert
        org.junit.jupiter.api.Assertions.assertNotNull(execution.getLlmResponseValidation());
        org.junit.jupiter.api.Assertions.assertFalse(execution.getLlmResponseValidation().isOk());
        org.junit.jupiter.api.Assertions.assertEquals(0, scorerCalls.get(), "Scorer must not run when core validation fails");
    }

    @Test
    void execute_allOk_shouldInvokeScorer_once_with_same_execution_instance() throws Exception {

        // arrange
        String factId = "Flight:F100";

        Meta meta = new Meta("v1", "validate_flight_airports", "all ok scorer call");
        ValidateTask task = new ValidateTask("validate flight airports", factId);

        Node node = new Node(
                factId,
                "{\"id\":\"F100\",\"kind\":\"Flight\",\"mode\":\"relational\",\"from\":\"AUS\",\"to\":\"DFW\"}",
                java.util.List.of(),
                Node.Mode.RELATIONAL
        );

        GraphContext context = new GraphContext(java.util.Map.of(factId, node));

        // domain rule OK
        LLMResponseValidatorRule rule = new LLMResponseValidatorRule() {
            @Override
            public ValidationResult validate(String raw) {
                return ValidationResult.ok("domain_rule_validation");
            }
        };

        QueryRequest<ValidateTask> request =
                QueryRequests.validateTask(meta, task, context, factId, rule);
        request.setAdapter(new FakeLlmAdapter());

        // prompt builder with prompt validator OK (optional, but keeps phase present)
        PhaseResultValidator okPromptValidator = new PhaseResultValidator() {
            @Override
            public ValidationResult validate(String raw) {
                return ValidationResult.ok("prompt_contract_validation");
            }
        };

        PromptBuilder promptBuilder = new PromptBuilder(
                new SimpleResponseContractRegistry(),
                okPromptValidator
        );

        String rawResponse = "{\"result\":{\"status\":\"VALID\"}}";
        FakeLlmClient llmClient = new FakeLlmClient(rawResponse);

        // core validator OK
        ValidationResult okCoreValidation = ValidationResult.ok("LLM_RESPONSE_VALIDATION");
        FakePhaseResultValidator coreValidator = new FakePhaseResultValidator(okCoreValidation);

        CgoQueryPipeline pipeline = new CgoQueryPipeline(promptBuilder, llmClient, coreValidator);

        // scorer stub captures execution
        final java.util.concurrent.atomic.AtomicInteger scorerCalls = new java.util.concurrent.atomic.AtomicInteger(0);
        final java.util.concurrent.atomic.AtomicReference<QueryExecution> seen = new java.util.concurrent.atomic.AtomicReference<>(null);

        ScorerClient scorer = new ScorerClient() {
            @Override
            public void orchestrate(QueryExecution execution) {
                scorerCalls.incrementAndGet();
                seen.set(execution);
                Console.log("UT:ScorerClient.orchestrate", "called");
            }
        };

        // inject scorerClient via reflection
        java.lang.reflect.Field f = CgoQueryPipeline.class.getDeclaredField("scorerClient");
        f.setAccessible(true);
        f.set(pipeline, scorer);

        // act
        QueryExecution<ValidateTask> execution = pipeline.execute(request);

        // observe
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("scorerCalls", scorerCalls.get());
        jsonObject.addProperty("sameInstance", seen.get() == execution);
        Console.log("UT:CgoQueryPipeline.all_ok_scorer_called", jsonObject.toString());

        // assert
        org.junit.jupiter.api.Assertions.assertEquals(1, scorerCalls.get(), "Scorer must be invoked exactly once on happy path");
        org.junit.jupiter.api.Assertions.assertSame(execution, seen.get(), "Scorer must see the same QueryExecution instance returned by pipeline");
        org.junit.jupiter.api.Assertions.assertTrue(execution.isOk());
        org.junit.jupiter.api.Assertions.assertEquals("ok", execution.getStage());
        org.junit.jupiter.api.Assertions.assertEquals("OK", execution.getStatus());
    }

    @Test
    void execute_allOk_shouldInvokeScorer_andAppendHistoryRecord() {

        Console.log("test_start", "execute_allOk_shouldInvokeScorer_andAppendHistoryRecord");

        // arrange
        ai.braineous.cgo.history.HistoryStore store = ai.braineous.cgo.history.HistoryStore.getInstance();
        store.clear();

        int before = store.getAll().size();
        Console.log("history_before", before);

        String factId = "Flight:F100";

        Meta meta = new Meta("v1", "validate_flight_airports", "scorer mandatory");
        ValidateTask task = new ValidateTask("validate flight airports", factId);

        Node node = new Node(
                factId,
                "{\"id\":\"F100\",\"kind\":\"Flight\",\"mode\":\"relational\",\"from\":\"AUS\",\"to\":\"DFW\"}",
                java.util.List.of(),
                Node.Mode.RELATIONAL
        );

        GraphContext context = new GraphContext(java.util.Map.of(factId, node));

        // domain rule OK
        LLMResponseValidatorRule rule = new LLMResponseValidatorRule() {
            @Override
            public ValidationResult validate(String raw) {
                return ValidationResult.ok("domain_rule_validation");
            }
        };

        QueryRequest<ValidateTask> request =
                QueryRequests.validateTask(meta, task, context, factId, rule);
        request.setAdapter(new FakeLlmAdapter());

        PromptBuilder promptBuilder = new PromptBuilder(new SimpleResponseContractRegistry());

        String rawResponse = "{\"result\":{\"status\":\"VALID\"}}";
        FakeLlmClient llmClient = new FakeLlmClient(rawResponse);

        // core validator OK
        ValidationResult okCoreValidation = ValidationResult.ok("LLM_RESPONSE_VALIDATION");
        FakePhaseResultValidator coreValidator = new FakePhaseResultValidator(okCoreValidation);

        CgoQueryPipeline pipeline = new CgoQueryPipeline(promptBuilder, llmClient, coreValidator);

        // act
        QueryExecution<ValidateTask> execution = pipeline.execute(request);

        int after = store.getAll().size();
        Console.log("history_after", after);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("historyDelta", after - before);
        jsonObject.addProperty("executionOk", execution != null && execution.isOk());
        Console.log("UT:CgoQueryPipeline.scorer_mandatory_history", jsonObject.toString());

        // assert
        org.junit.jupiter.api.Assertions.assertNotNull(execution);
        org.junit.jupiter.api.Assertions.assertTrue(execution.isOk());
        org.junit.jupiter.api.Assertions.assertEquals(before + 1, after,
                "Scorer is mandatory and must append exactly one History record for a single execute()");
    }

    @Test
    void execute_domainRuleFails_shouldNotAppendHistoryRecord() {

        Console.log("test_start", "execute_domainRuleFails_shouldNotAppendHistoryRecord");

        // arrange
        ai.braineous.cgo.history.HistoryStore store = ai.braineous.cgo.history.HistoryStore.getInstance();
        store.clear();

        int before = store.getAll().size();
        Console.log("history_before", before);

        String factId = "Flight:F100";

        Meta meta = new Meta("v1", "validate_flight_airports", "domain fail skips scorer");
        ValidateTask task = new ValidateTask("validate flight airports", factId);

        Node node = new Node(
                factId,
                "{\"id\":\"F100\",\"kind\":\"Flight\",\"mode\":\"relational\",\"from\":\"AUS\",\"to\":\"DFW\"}",
                java.util.List.of(),
                Node.Mode.RELATIONAL
        );

        GraphContext context = new GraphContext(java.util.Map.of(factId, node));

        // domain rule FAIL
        LLMResponseValidatorRule rule = new LLMResponseValidatorRule() {
            @Override
            public ValidationResult validate(String raw) {
                return ValidationResult.error("DOMAIN_RULE_ERROR", "domain rule failed", "domain_rule_validation", null);
            }
        };

        QueryRequest<ValidateTask> request =
                QueryRequests.validateTask(meta, task, context, factId, rule);
        request.setAdapter(new FakeLlmAdapter());

        PromptBuilder promptBuilder = new PromptBuilder(new SimpleResponseContractRegistry());

        String rawResponse = "{\"result\":{\"status\":\"VALID\"}}";
        FakeLlmClient llmClient = new FakeLlmClient(rawResponse);

        // core validator OK so rule runs and fails
        ValidationResult okCoreValidation = ValidationResult.ok("LLM_RESPONSE_VALIDATION");
        FakePhaseResultValidator coreValidator = new FakePhaseResultValidator(okCoreValidation);

        CgoQueryPipeline pipeline = new CgoQueryPipeline(promptBuilder, llmClient, coreValidator);

        // act
        QueryExecution<ValidateTask> execution = pipeline.execute(request);

        int after = store.getAll().size();
        Console.log("history_after", after);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("historyDelta", after - before);
        jsonObject.addProperty("domainOk", execution.getDomainValidation() != null && execution.getDomainValidation().isOk());
        Console.log("UT:CgoQueryPipeline.domain_fail_history_unchanged", jsonObject.toString());

        // assert
        org.junit.jupiter.api.Assertions.assertNotNull(execution);
        org.junit.jupiter.api.Assertions.assertNotNull(execution.getDomainValidation());
        org.junit.jupiter.api.Assertions.assertFalse(execution.getDomainValidation().isOk());
        org.junit.jupiter.api.Assertions.assertEquals(before, after,
                "History must NOT change when domain rule fails (scorer should not run)");
    }






    ////--------------------------------------------------------------------------
    private static final class CountingLlmClient implements LlmClient {
        int callCount = 0;
        JsonObject lastPrompt;

        @Override
        public String executePrompt(LlmAdapter adapter,JsonObject prompt) {
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

    private static class FakeLlmAdapter extends LlmAdapter{

        @Override
        public String invokeLlm(JsonObject prompt) {
            return "STUBBED";
        }
    }
}

