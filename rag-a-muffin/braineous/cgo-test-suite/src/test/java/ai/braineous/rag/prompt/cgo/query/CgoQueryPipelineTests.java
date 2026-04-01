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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class CgoQueryPipelineTests {

    @Test
    void execute_withCoreValidatorOk_shouldAttachValidation_andLlmResponse() {
        QueryRequest<ValidateTask> request = this.buildValidateTaskRequest();
        request.setAdapter(new FakeLlmAdapter());

        PromptBuilder promptBuilder = new PromptBuilder(new SimpleResponseContractRegistry());

        String raw = "{\"some\":\"response\"}";
        FakeLlmClient llmClient = new FakeLlmClient(raw);

        AtomicInteger callCount = new AtomicInteger(0);
        AtomicReference<String> lastRaw = new AtomicReference<String>();

        PhaseResultValidator coreValidator = new PhaseResultValidator() {
            @Override
            public ValidationResult validate(String response) {
                callCount.incrementAndGet();
                lastRaw.set(response);
                return ValidationResult.ok("LLM_RESPONSE_VALIDATION");
            }
        };

        CgoQueryPipeline pipeline = new CgoQueryPipeline(promptBuilder, llmClient, coreValidator);

        QueryExecution<ValidateTask> execution = pipeline.execute(request);

        Console.log("coreOk.rawResponse", execution.getRawResponse());
        Console.log("coreOk.llmResponseValidation", execution.getLlmResponseValidation());
        Console.log("coreOk.llmResponse", execution.getLlmResponse());

        assertEquals(1, callCount.get());
        assertEquals(raw, lastRaw.get());

        assertNotNull(execution);
        assertEquals(raw, execution.getRawResponse());

        assertNotNull(execution.getPromptValidation());
        assertTrue(execution.getPromptValidation().isOk());

        assertNotNull(execution.getLlmResponseValidation());
        assertTrue(execution.getLlmResponseValidation().isOk());
        assertNull(execution.getDomainValidation());

        assertNotNull(execution.getLlmResponse());
        assertEquals(raw, execution.getLlmResponse().getRawResponse());
        assertTrue(execution.getLlmResponse().isSuccess());
        assertNotNull(execution.getLlmResponse().getLlmRequest());
        assertSame(request, execution.getLlmResponse().getLlmRequest().getQueryRequest());
        assertNotNull(execution.getLlmResponse().getLlmRequest().getLlmQuery());
    }

    @Test
    void execute_withCoreValidatorError_shouldReturnFailureExecution_andPreserveLlmResponse() {
        QueryRequest<ValidateTask> request = this.buildValidateTaskRequest();
        request.setAdapter(new FakeLlmAdapter());

        PromptBuilder promptBuilder = new PromptBuilder(new SimpleResponseContractRegistry());

        String raw = "{\"some\":\"response\"}";
        FakeLlmClient llmClient = new FakeLlmClient(raw);

        AtomicInteger callCount = new AtomicInteger(0);
        AtomicReference<String> lastRaw = new AtomicReference<String>();

        PhaseResultValidator coreValidator = new PhaseResultValidator() {
            @Override
            public ValidationResult validate(String response) {
                callCount.incrementAndGet();
                lastRaw.set(response);
                return ValidationResult.error(
                        "LLM_RESPONSE_ERROR",
                        "LLM response contract invalid",
                        "llm_response_validation",
                        null
                );
            }
        };

        CgoQueryPipeline pipeline = new CgoQueryPipeline(promptBuilder, llmClient, coreValidator);

        QueryExecution<ValidateTask> execution = pipeline.execute(request);

        Console.log("coreFail.rawResponse", execution.getRawResponse());
        Console.log("coreFail.llmResponseValidation", execution.getLlmResponseValidation());
        Console.log("coreFail.llmResponse", execution.getLlmResponse());

        assertEquals(1, callCount.get());
        assertEquals(raw, lastRaw.get());

        assertNotNull(execution);
        assertEquals(raw, execution.getRawResponse());

        assertNotNull(execution.getPromptValidation());
        assertTrue(execution.getPromptValidation().isOk());

        assertNotNull(execution.getLlmResponseValidation());
        assertFalse(execution.getLlmResponseValidation().isOk());
        assertEquals("LLM_RESPONSE_ERROR", execution.getLlmResponseValidation().getCode());
        assertEquals("llm_response_validation", execution.getLlmResponseValidation().getStage());

        assertNull(execution.getDomainValidation());

        assertNotNull(execution.getLlmResponse());
        assertEquals(raw, execution.getLlmResponse().getRawResponse());
        assertTrue(execution.getLlmResponse().isSuccess());
    }

    @Test
    void execute_withDomainRuleOk_shouldAttachDomainValidation_andLlmResponse() {
        AtomicInteger domainCallCount = new AtomicInteger(0);
        AtomicReference<String> domainLastRaw = new AtomicReference<String>();

        LLMResponseValidatorRule rule = new LLMResponseValidatorRule() {
            @Override
            public ValidationResult validate(String response) {
                domainCallCount.incrementAndGet();
                domainLastRaw.set(response);
                return ValidationResult.ok("DOMAIN_VALIDATION");
            }
        };

        QueryRequest<ValidateTask> request = this.buildValidateTaskRequest(rule);
        request.setAdapter(new FakeLlmAdapter());

        PromptBuilder promptBuilder = new PromptBuilder(new SimpleResponseContractRegistry());

        String raw = "{\"decision\":\"GO\"}";
        FakeLlmClient llmClient = new FakeLlmClient(raw);

        PhaseResultValidator coreValidator = new PhaseResultValidator() {
            @Override
            public ValidationResult validate(String response) {
                return ValidationResult.ok("LLM_RESPONSE_VALIDATION");
            }
        };

        CgoQueryPipeline pipeline = new CgoQueryPipeline(promptBuilder, llmClient, coreValidator);

        QueryExecution<ValidateTask> execution = pipeline.execute(request);

        Console.log("domainOk.rawResponse", execution.getRawResponse());
        Console.log("domainOk.domainValidation", execution.getDomainValidation());
        Console.log("domainOk.llmResponse", execution.getLlmResponse());

        assertEquals(1, domainCallCount.get());
        assertEquals(raw, domainLastRaw.get());

        assertNotNull(execution);
        assertEquals(raw, execution.getRawResponse());

        assertNotNull(execution.getPromptValidation());
        assertTrue(execution.getPromptValidation().isOk());

        assertNotNull(execution.getLlmResponseValidation());
        assertTrue(execution.getLlmResponseValidation().isOk());

        assertNotNull(execution.getDomainValidation());
        assertTrue(execution.getDomainValidation().isOk());

        assertNotNull(execution.getLlmResponse());
        assertEquals(raw, execution.getLlmResponse().getRawResponse());
        assertTrue(execution.getLlmResponse().isSuccess());
    }

    @Test
    void execute_withDomainRuleError_shouldReturnFailureExecution_andPreserveLlmResponse() {
        AtomicInteger domainCallCount = new AtomicInteger(0);
        AtomicReference<String> domainLastRaw = new AtomicReference<String>();

        LLMResponseValidatorRule rule = new LLMResponseValidatorRule() {
            @Override
            public ValidationResult validate(String response) {
                domainCallCount.incrementAndGet();
                domainLastRaw.set(response);
                return ValidationResult.error(
                        "DOMAIN_ERROR",
                        "Domain validation failed",
                        "domain_validation",
                        null
                );
            }
        };

        QueryRequest<ValidateTask> request = this.buildValidateTaskRequest(rule);
        request.setAdapter(new FakeLlmAdapter());

        PromptBuilder promptBuilder = new PromptBuilder(new SimpleResponseContractRegistry());

        String raw = "{\"decision\":\"GO\"}";
        FakeLlmClient llmClient = new FakeLlmClient(raw);

        PhaseResultValidator coreValidator = new PhaseResultValidator() {
            @Override
            public ValidationResult validate(String response) {
                return ValidationResult.ok("LLM_RESPONSE_VALIDATION");
            }
        };

        CgoQueryPipeline pipeline = new CgoQueryPipeline(promptBuilder, llmClient, coreValidator);

        QueryExecution<ValidateTask> execution = pipeline.execute(request);

        Console.log("domainFail.rawResponse", execution.getRawResponse());
        Console.log("domainFail.domainValidation", execution.getDomainValidation());
        Console.log("domainFail.llmResponse", execution.getLlmResponse());

        assertEquals(1, domainCallCount.get());
        assertEquals(raw, domainLastRaw.get());

        assertNotNull(execution);
        assertEquals(raw, execution.getRawResponse());

        assertNotNull(execution.getPromptValidation());
        assertTrue(execution.getPromptValidation().isOk());

        assertNotNull(execution.getLlmResponseValidation());
        assertTrue(execution.getLlmResponseValidation().isOk());

        assertNotNull(execution.getDomainValidation());
        assertFalse(execution.getDomainValidation().isOk());
        assertEquals("DOMAIN_ERROR", execution.getDomainValidation().getCode());
        assertEquals("domain_validation", execution.getDomainValidation().getStage());

        assertNotNull(execution.getLlmResponse());
        assertEquals(raw, execution.getLlmResponse().getRawResponse());
        assertTrue(execution.getLlmResponse().isSuccess());
    }

    @Test
    void execute_withMalformedJsonResponse_shouldReturnExecution_currentBehavior() {
        QueryRequest<ValidateTask> request = this.buildValidateTaskRequest();
        request.setAdapter(new FakeLlmAdapter());

        PromptBuilder promptBuilder = new PromptBuilder(new SimpleResponseContractRegistry());

        String malformed = "{\"answer\":\"ok\"";
        FakeLlmClient llmClient = new FakeLlmClient(malformed);

        CgoQueryPipeline pipeline = new CgoQueryPipeline(promptBuilder, llmClient);

        Console.log("parserFail.rawResponse", malformed);

        QueryExecution<ValidateTask> execution = pipeline.execute(request);

        assertNotNull(execution);
        assertEquals(malformed, execution.getRawResponse());
        assertNotNull(execution.getLlmResponse());
    }

    @Test
    void execute_withoutAdapter_shouldThrowFailFast() {
        QueryRequest<ValidateTask> request = this.buildValidateTaskRequest();

        PromptBuilder promptBuilder = new PromptBuilder();
        FakeLlmClient llmClient = new FakeLlmClient("{\"ok\":true}");

        CgoQueryPipeline pipeline = new CgoQueryPipeline(promptBuilder, llmClient);

        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> pipeline.execute(request)
        );

        Console.log("missingAdapter.exception", ex.getMessage());

        assertNotNull(ex.getMessage());
        assertTrue(ex.getMessage().contains("LlmAdapter"));
    }

    @Test
    void execute_withPromptValidationError_shouldUseQueryGenPath_andCallLlm() {
        QueryRequest<ValidateTask> request = this.buildValidateTaskRequest();
        request.setAdapter(new FakeLlmAdapter());

        PhaseResultValidator failingPromptValidator = new PhaseResultValidator() {
            @Override
            public ValidationResult validate(String raw) {
                return ValidationResult.error(
                        "PROMPT_ERROR",
                        "Prompt contract invalid",
                        "prompt_contract_validation",
                        null
                );
            }
        };

        PromptBuilder promptBuilder = new PromptBuilder(
                new SimpleResponseContractRegistry(),
                failingPromptValidator
        );

        CountingLlmClient llmClient = new CountingLlmClient("{\"unused\":true}");
        CgoQueryPipeline pipeline = new CgoQueryPipeline(promptBuilder, llmClient);

        QueryExecution<ValidateTask> execution = pipeline.execute(request);

        Console.log("promptPath.rawResponse", String.valueOf(execution.getRawResponse()));
        Console.log("promptPath.promptValidation", String.valueOf(execution.getPromptValidation()));
        Console.log("promptPath.llmCallCount", String.valueOf(llmClient.callCount));
        Console.log("promptPath.llmResponse", String.valueOf(execution.getLlmResponse()));

        assertEquals(1, llmClient.callCount);

        assertNotNull(execution);
        assertSame(request, execution.getRequest());
        assertEquals("{\"unused\":true}", execution.getRawResponse());

        assertNotNull(execution.getPromptValidation());
        assertTrue(execution.getPromptValidation().isOk());
        assertEquals("querygen.contract.ok", execution.getPromptValidation().getCode());
        assertEquals("querygen_contract_validation", execution.getPromptValidation().getStage());

        assertNotNull(execution.getLlmResponse());
        assertEquals("{\"unused\":true}", execution.getLlmResponse().getRawResponse());
        assertTrue(execution.getLlmResponse().isSuccess());

        assertNotNull(execution.getLlmResponseValidation());
        assertFalse(execution.getLlmResponseValidation().isOk());
        assertEquals("querygen.contract.meta_missing_or_invalid", execution.getLlmResponseValidation().getCode());
        assertEquals("querygen_contract_validation", execution.getLlmResponseValidation().getStage());

        assertNull(execution.getDomainValidation());
    }

    @Test
    void execute_withoutValidators_shouldReturnFailureExecution_fromDefaultQueryGenValidator_andAttachLlmResponse() {
        QueryRequest<ValidateTask> request = this.buildValidateTaskRequest();
        request.setAdapter(new FakeLlmAdapter());

        PromptBuilder promptBuilder = new PromptBuilder();

        String raw = "{\n" +
                "  \"result\": {\n" +
                "    \"ok\": true,\n" +
                "    \"code\": \"response.contract.ok\",\n" +
                "    \"message\": \"VALID\",\n" +
                "    \"stage\": \"llm_response_validation\",\n" +
                "    \"anchorId\": null,\n" +
                "    \"metadata\": { \"adapter\": \"fake\" }\n" +
                "  }\n" +
                "}";

        FakeLlmClient llmClient = new FakeLlmClient(raw);
        CgoQueryPipeline pipeline = new CgoQueryPipeline(promptBuilder, llmClient);

        QueryExecution<ValidateTask> execution = pipeline.execute(request);

        Console.log("noValidators.rawResponse", String.valueOf(execution.getRawResponse()));
        Console.log("noValidators.promptValidation", String.valueOf(execution.getPromptValidation()));
        Console.log("noValidators.llmResponseValidation", String.valueOf(execution.getLlmResponseValidation()));
        Console.log("noValidators.domainValidation", String.valueOf(execution.getDomainValidation()));
        Console.log("noValidators.llmResponse", String.valueOf(execution.getLlmResponse()));

        assertNotNull(execution);
        assertSame(request, execution.getRequest());
        assertEquals(raw, execution.getRawResponse());

        assertNotNull(execution.getPromptValidation());
        assertTrue(execution.getPromptValidation().isOk());
        assertEquals("querygen.contract.ok", execution.getPromptValidation().getCode());
        assertEquals("querygen_contract_validation", execution.getPromptValidation().getStage());

        assertNotNull(execution.getLlmResponseValidation());
        assertFalse(execution.getLlmResponseValidation().isOk());
        assertEquals("querygen.contract.meta_missing_or_invalid", execution.getLlmResponseValidation().getCode());
        assertEquals("querygen_contract_validation", execution.getLlmResponseValidation().getStage());

        assertNull(execution.getDomainValidation());

        assertNotNull(execution.getLlmResponse());
        assertEquals(raw, execution.getLlmResponse().getRawResponse());
        assertTrue(execution.getLlmResponse().isSuccess());

        assertNotNull(execution.getLlmResponse().getLlmRequest());
        assertSame(request, execution.getLlmResponse().getLlmRequest().getQueryRequest());
        assertNotNull(execution.getLlmResponse().getLlmRequest().getLlmQuery());
    }
    //---------------------------------------------------------------------------------
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

        ValidateTask task = new ValidateTask(taskDescription, factId);

        Node node = new Node(
                factId,
                "{\"id\":\"F100\",\"kind\":\"Flight\",\"mode\":\"relational\",\"from\":\"AUS\",\"to\":\"DFW\"}",
                List.of(),
                Node.Mode.RELATIONAL
        );

        GraphContext context = new GraphContext(Map.of(factId, node));

        return new QueryRequest<ValidateTask>(meta, context, task, factId, rule);
    }

    private static class CountingLlmClient implements LlmClient {

        private final String rawResponse;
        private int callCount;

        private CountingLlmClient(String rawResponse) {
            this.rawResponse = rawResponse;
            this.callCount = 0;
        }

        @Override
        public String executePrompt(LlmAdapter adapter, QueryRequest queryRequest, JsonObject prompt) {
            this.callCount++;
            return rawResponse;
        }
    }
}

