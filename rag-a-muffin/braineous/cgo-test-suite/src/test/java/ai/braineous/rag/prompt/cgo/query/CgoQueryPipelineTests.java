package ai.braineous.rag.prompt.cgo.query;

import ai.braineous.rag.prompt.cgo.api.GraphContext;
import ai.braineous.rag.prompt.cgo.api.LLMResponseValidatorRule;
import ai.braineous.rag.prompt.cgo.api.LlmAdapter;
import ai.braineous.rag.prompt.cgo.api.Meta;
import ai.braineous.rag.prompt.cgo.api.QueryExecution;
import ai.braineous.rag.prompt.cgo.api.ValidateTask;
import ai.braineous.rag.prompt.cgo.api.ValidationResult;
import ai.braineous.rag.prompt.cgo.prompt.LlmClient;
import ai.braineous.rag.prompt.cgo.prompt.PromptBuilder;
import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class CgoQueryPipelineTests {

    @Test
    public void test_1() {
        QueryRequest<ValidateTask> request =
                buildRequest();

        request.setAdapter(new FakeLlmAdapter());

        CountingLlmClient llmClient =
                new CountingLlmClient(
                        "{\"result\":{\"decision\":\"CAPTURE\",\"reason\":\"RISK_LEVEL_LOW\",\"code\":\"00\"}}"
                );

        CgoQueryPipeline pipeline =
                new CgoQueryPipeline(new PromptBuilder(), llmClient);

        QueryExecution<ValidateTask> execution =
                pipeline.execute(request);

        Console.log("pipeline.ok.raw", execution.getRawResponse());
        Console.log("pipeline.ok.promptValidation", String.valueOf(execution.getPromptValidation()));
        Console.log("pipeline.ok.llmValidation", String.valueOf(execution.getLlmResponseValidation()));
        Console.log("pipeline.ok.domainValidation", String.valueOf(execution.getDomainValidation()));
        Console.log("pipeline.ok.execution", execution.toJsonString());

        Assertions.assertEquals(1, llmClient.callCount);
        Assertions.assertNotNull(llmClient.lastPrompt);

        String promptText =
                llmClient.lastPrompt.get("prompt").getAsString();

        Assertions.assertTrue(promptText.contains("You are an execution engine"));
        Assertions.assertTrue(promptText.contains("INPUT:"));
        Assertions.assertFalse(promptText.startsWith("{\"llm_instructions\""));

        Assertions.assertNotNull(execution);
        Assertions.assertSame(request, execution.getRequest());
        Assertions.assertTrue(execution.isOk());
        Assertions.assertEquals("OK", execution.getStatus());
        Assertions.assertEquals("ok", execution.getStage());

        Assertions.assertEquals(
                "{\"result\":{\"decision\":\"CAPTURE\",\"reason\":\"RISK_LEVEL_LOW\",\"code\":\"00\"}}",
                execution.getRawResponse()
        );

        Assertions.assertNotNull(execution.getPromptValidation());
        Assertions.assertTrue(execution.getPromptValidation().isOk());
        Assertions.assertEquals(
                "querygen.contract.ok",
                execution.getPromptValidation().getCode()
        );

        Assertions.assertNotNull(execution.getLlmResponseValidation());
        Assertions.assertTrue(execution.getLlmResponseValidation().isOk());
        Assertions.assertEquals(
                "queryresult.contract.ok",
                execution.getLlmResponseValidation().getCode()
        );

        Assertions.assertNull(execution.getDomainValidation());

        Assertions.assertNotNull(execution.getLlmResponse());
        Assertions.assertEquals(
                execution.getRawResponse(),
                execution.getLlmResponse().getRawResponse()
        );
        Assertions.assertTrue(execution.getLlmResponse().isSuccess());
        Assertions.assertNotNull(execution.getLlmResponse().getLlmRequest());
        Assertions.assertSame(
                request,
                execution.getLlmResponse().getLlmRequest().getQueryRequest()
        );
        Assertions.assertNotNull(
                execution.getLlmResponse().getLlmRequest().getLlmQuery()
        );
    }

    @Test
    public void test_2() {
        QueryRequest<ValidateTask> request =
                buildRequest();

        request.setAdapter(new FakeLlmAdapter());

        AtomicInteger validationCount =
                new AtomicInteger(0);

        AtomicReference<String> lastRaw =
                new AtomicReference<String>();

        PhaseResultValidator validator =
                new PhaseResultValidator() {
                    @Override
                    public ValidationResult validate(String raw) {
                        validationCount.incrementAndGet();
                        lastRaw.set(raw);

                        return ValidationResult.error(
                                "queryresult.contract.custom_error",
                                "custom validation failed",
                                "llm_response_validation",
                                null
                        );
                    }
                };

        CountingLlmClient llmClient =
                new CountingLlmClient(
                        "{\"result\":{\"decision\":\"CAPTURE\",\"reason\":\"RISK_LEVEL_LOW\",\"code\":\"00\"}}"
                );

        CgoQueryPipeline pipeline =
                new CgoQueryPipeline(
                        new PromptBuilder(),
                        llmClient,
                        validator
                );

        QueryExecution<ValidateTask> execution =
                pipeline.execute(request);

        Console.log("pipeline.llm.validation.fail", execution.toJsonString());

        Assertions.assertEquals(1, validationCount.get());
        Assertions.assertEquals(execution.getRawResponse(), lastRaw.get());

        Assertions.assertFalse(execution.isOk());
        Assertions.assertEquals("ERROR", execution.getStatus());
        Assertions.assertEquals("llm_response", execution.getStage());

        Assertions.assertNotNull(execution.getLlmResponseValidation());
        Assertions.assertFalse(execution.getLlmResponseValidation().isOk());
        Assertions.assertEquals(
                "queryresult.contract.custom_error",
                execution.getLlmResponseValidation().getCode()
        );

        Assertions.assertNull(execution.getDomainValidation());
        Assertions.assertNotNull(execution.getLlmResponse());
    }



    @Test
    public void test_3() {
        AtomicInteger domainCount =
                new AtomicInteger(0);

        AtomicReference<String> domainRaw =
                new AtomicReference<String>();

        LLMResponseValidatorRule rule =
                new LLMResponseValidatorRule() {
                    @Override
                    public ValidationResult validate(String raw) {
                        domainCount.incrementAndGet();
                        domainRaw.set(raw);

                        return ValidationResult.ok("DOMAIN_OK");
                    }
                };

        QueryRequest<ValidateTask> request =
                buildRequest(rule);

        request.setAdapter(new FakeLlmAdapter());

        CountingLlmClient llmClient =
                new CountingLlmClient(
                        "{\"result\":{\"decision\":\"CAPTURE\",\"reason\":\"RISK_LEVEL_LOW\",\"code\":\"00\"}}"
                );

        CgoQueryPipeline pipeline =
                new CgoQueryPipeline(new PromptBuilder(), llmClient);

        QueryExecution<ValidateTask> execution =
                pipeline.execute(request);

        Console.log("pipeline.domain.ok", execution.toJsonString());

        Assertions.assertEquals(1, domainCount.get());
        Assertions.assertEquals(execution.getRawResponse(), domainRaw.get());

        Assertions.assertTrue(execution.isOk());

        Assertions.assertNotNull(execution.getDomainValidation());
        Assertions.assertTrue(execution.getDomainValidation().isOk());

        Assertions.assertEquals(
                "OK",
                execution.getDomainValidation().getCode()
        );

        Assertions.assertEquals(
                "DOMAIN_OK",
                execution.getDomainValidation().getStage()
        );
    }


    @Test
    public void test_4() {
        AtomicInteger domainCount =
                new AtomicInteger(0);

        AtomicReference<String> domainRaw =
                new AtomicReference<String>();

        LLMResponseValidatorRule rule =
                new LLMResponseValidatorRule() {
                    @Override
                    public ValidationResult validate(String raw) {
                        domainCount.incrementAndGet();
                        domainRaw.set(raw);

                        return ValidationResult.error(
                                "DOMAIN_ERROR",
                                "domain failed",
                                "domain_validation",
                                null
                        );
                    }
                };

        QueryRequest<ValidateTask> request =
                buildRequest(rule);

        request.setAdapter(new FakeLlmAdapter());

        CountingLlmClient llmClient =
                new CountingLlmClient(
                        "{\"result\":{\"decision\":\"CAPTURE\",\"reason\":\"RISK_LEVEL_LOW\",\"code\":\"00\"}}"
                );

        CgoQueryPipeline pipeline =
                new CgoQueryPipeline(new PromptBuilder(), llmClient);

        QueryExecution<ValidateTask> execution =
                pipeline.execute(request);

        Console.log("pipeline.domain.fail", execution.toJsonString());

        Assertions.assertEquals(1, domainCount.get());
        Assertions.assertEquals(execution.getRawResponse(), domainRaw.get());

        Assertions.assertFalse(execution.isOk());

        Assertions.assertEquals("ERROR", execution.getStatus());
        Assertions.assertEquals("domain", execution.getStage());

        Assertions.assertNotNull(execution.getDomainValidation());
        Assertions.assertFalse(execution.getDomainValidation().isOk());

        Assertions.assertEquals(
                "DOMAIN_ERROR",
                execution.getDomainValidation().getCode()
        );

        Assertions.assertEquals(
                "domain_validation",
                execution.getDomainValidation().getStage()
        );

        Assertions.assertNotNull(execution.getLlmResponse());
    }

    @Test
    public void test_5() {
        QueryRequest<ValidateTask> request =
                buildRequest();

        request.setAdapter(new FakeLlmAdapter());

        CountingLlmClient llmClient =
                new CountingLlmClient("{bad-json");

        CgoQueryPipeline pipeline =
                new CgoQueryPipeline(new PromptBuilder(), llmClient);

        QueryExecution<ValidateTask> execution =
                pipeline.execute(request);

        Console.log("pipeline.malformed.json", execution.toJsonString());

        Assertions.assertFalse(execution.isOk());
        Assertions.assertEquals("ERROR", execution.getStatus());
        Assertions.assertEquals("llm_response", execution.getStage());

        Assertions.assertNotNull(execution.getLlmResponseValidation());
        Assertions.assertFalse(execution.getLlmResponseValidation().isOk());
        Assertions.assertEquals(
                "queryresult.contract.invalid_json",
                execution.getLlmResponseValidation().getCode()
        );

        Assertions.assertNotNull(execution.getLlmResponse());
        Assertions.assertEquals("{bad-json", execution.getLlmResponse().getRawResponse());
    }

    @Test
    public void test_6() {
        QueryRequest<ValidateTask> request =
                buildRequest();

        CgoQueryPipeline pipeline =
                new CgoQueryPipeline(
                        new PromptBuilder(),
                        new CountingLlmClient(
                                "{\"result\":{\"decision\":\"CAPTURE\",\"reason\":\"RISK_LEVEL_LOW\",\"code\":\"00\"}}"
                        )
                );

        NullPointerException exception =
                Assertions.assertThrows(
                        NullPointerException.class,
                        () -> pipeline.execute(request)
                );

        Console.log("pipeline.missing.adapter", exception.getMessage());

        Assertions.assertTrue(
                exception.getMessage().contains("Missing LlmAdapter")
        );
    }

    @Test
    public void test_7() {
        QueryRequest<ValidateTask> request =
                buildRequest();

        request.setAdapter(new FakeLlmAdapter());

        CountingLlmClient llmClient =
                new CountingLlmClient("{\"unused\":true}");

        CgoQueryPipeline pipeline =
                new CgoQueryPipeline(new PromptBuilder(), llmClient);

        QueryExecution<ValidateTask> execution =
                pipeline.execute(request);

        Console.log("pipeline.result.missing", execution.toJsonString());

        Assertions.assertEquals(1, llmClient.callCount);

        Assertions.assertFalse(execution.isOk());
        Assertions.assertEquals("ERROR", execution.getStatus());
        Assertions.assertEquals("llm_response", execution.getStage());

        Assertions.assertNotNull(execution.getPromptValidation());
        Assertions.assertTrue(execution.getPromptValidation().isOk());
        Assertions.assertEquals(
                "querygen.contract.ok",
                execution.getPromptValidation().getCode()
        );

        Assertions.assertNotNull(execution.getLlmResponseValidation());
        Assertions.assertFalse(execution.getLlmResponseValidation().isOk());
        Assertions.assertEquals(
                "queryresult.contract.result_missing",
                execution.getLlmResponseValidation().getCode()
        );

        Assertions.assertNull(execution.getDomainValidation());
        Assertions.assertNotNull(execution.getLlmResponse());
    }

    @Test
    public void test_8() {
        QueryRequest<ValidateTask> request =
                buildRequest();

        request.setAdapter(new FakeLlmAdapter());

        CountingLlmClient llmClient =
                new CountingLlmClient(
                        "{\"result\":{\"decision\":\"CAPTURE\",\"reason\":\"RISK_LEVEL_LOW\",\"code\":\"00\"}}"
                );

        CgoQueryPipeline pipeline =
                new CgoQueryPipeline(new PromptBuilder(), llmClient);

        pipeline.setInMemoryMode(true);

        QueryExecution<ValidateTask> execution =
                pipeline.execute(request);

        Console.log("pipeline.in.memory", execution.toJsonString());

        Assertions.assertTrue(execution.isOk());
        Assertions.assertTrue(execution.isInMemoryMode());
    }

    private QueryRequest<ValidateTask> buildRequest() {
        return buildRequest(null);
    }

    private QueryRequest<ValidateTask> buildRequest(LLMResponseValidatorRule rule) {
        Meta meta =
                new Meta(
                        "v1",
                        "decision",
                        "pay decision"
                );

        ValidateTask task =
                new ValidateTask(
                        "decision",
                        "PaymentRequest:PAY-1001",
                        Arrays.asList("decision", "reason", "code"),
                        Arrays.asList(
                                "CustomerAccount:CUST-2001",
                                "RiskProfile:RISK-4001"
                        )
                );

        Node payment =
                new Node(
                        "PaymentRequest:PAY-1001",
                        "{\"id\":\"PaymentRequest:PAY-1001\",\"amount\":\"125.00\",\"currency\":\"USD\"}",
                        Arrays.asList(),
                        Node.Mode.ATOMIC
                );

        Node customer =
                new Node(
                        "CustomerAccount:CUST-2001",
                        "{\"id\":\"CustomerAccount:CUST-2001\",\"status\":\"ACTIVE\"}",
                        Arrays.asList(),
                        Node.Mode.ATOMIC
                );

        Node risk =
                new Node(
                        "RiskProfile:RISK-4001",
                        "{\"id\":\"RiskProfile:RISK-4001\",\"level\":\"LOW\"}",
                        Arrays.asList(),
                        Node.Mode.ATOMIC
                );

        Map<String, Node> nodes =
                new LinkedHashMap<String, Node>();

        nodes.put(payment.getId(), payment);
        nodes.put(customer.getId(), customer);
        nodes.put(risk.getId(), risk);

        GraphContext context =
                new GraphContext(nodes);

        return new QueryRequest<ValidateTask>(
                meta,
                context,
                task,
                "PaymentRequest:PAY-1001",
                rule
        );
    }

    private static class CountingLlmClient implements LlmClient {

        private final String rawResponse;
        private int callCount;
        private JsonObject lastPrompt;

        private CountingLlmClient(String rawResponse) {
            this.rawResponse = rawResponse;
        }

        @Override
        public String executePrompt(LlmAdapter adapter,
                                    QueryRequest queryRequest,
                                    JsonObject prompt) {
            this.callCount++;
            this.lastPrompt = prompt;

            return this.rawResponse;
        }
    }
}