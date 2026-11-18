package ai.braineous.rag.prompt.cgo.query;

import ai.braineous.arc.IntelligenceBridge;
import ai.braineous.rag.prompt.cgo.api.Control;
import ai.braineous.rag.prompt.cgo.api.Edge;
import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.cgo.api.GraphContext;
import ai.braineous.rag.prompt.cgo.api.LLMRequest;
import ai.braineous.rag.prompt.cgo.api.LLMResponse;
import ai.braineous.rag.prompt.cgo.api.LLMResponseValidatorRule;
import ai.braineous.rag.prompt.cgo.api.Meta;
import ai.braineous.rag.prompt.cgo.api.QueryExecution;
import ai.braineous.rag.prompt.cgo.api.ValidateTask;
import ai.braineous.rag.prompt.cgo.api.ValidationResult;
import ai.braineous.rag.prompt.cgo.prompt.PromptBuilder;
import ai.braineous.rag.prompt.cgo.querygen.LlmInstructionsFieldGenerator;
import ai.braineous.rag.prompt.cgo.querygen.services.QueryResultValidator;
import ai.braineous.rag.prompt.models.cgo.graph.BindResult;
import ai.braineous.rag.prompt.models.cgo.graph.GraphBuilder;
import ai.braineous.rag.prompt.models.cgo.graph.Input;
import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class CgoQueryPipelineTests {

    private static final String NEXUS_EXPRESSION =
            "(Inventory.quantityOnHand - Inventory.quantityReserved) / Sales.quantitySold";
    private static final String NEXUS_DEFINITION = "inventory availability.";
    private static final String NEXUS_RULE_SECTION =
            "rule\n"
                    + "if the result is greater than or equal to 1,\n"
                    + "the inventory is available.\n"
                    + "otherwise, the inventory is unavailable.";
    private static final String NEXUS_DECISION_SECTION =
            "decision\n"
                    + "state the inventory status as either available or unavailable.";

    @Test
    public void test_1() {
        QueryRequest<ValidateTask> request =
                buildRequest();

        request.getTask().setControls(
                Arrays.asList(
                        new Control(
                                "spec",
                                "When the expression is true, the runtime result is success. When the expression is false, the runtime result is fail."
                        )
                )
        );

        CountingIntelligenceBridge intelligenceBridge =
                new CountingIntelligenceBridge(
                        "success"
                );

        CgoQueryPipeline pipeline =
                new CgoQueryPipeline(new PromptBuilder(), intelligenceBridge);

        QueryExecution<ValidateTask> execution =
                this.executeNexus(pipeline, request);

        Console.log("pipeline.ok.raw", execution.getRawResponse());
        Console.log("pipeline.ok.promptValidation", String.valueOf(execution.getPromptValidation()));
        Console.log("pipeline.ok.llmValidation", String.valueOf(execution.getLlmResponseValidation()));
        Console.log("pipeline.ok.domainValidation", String.valueOf(execution.getDomainValidation()));
        Console.log("pipeline.ok.execution", execution.toJsonString());

        Assertions.assertEquals(1, intelligenceBridge.callCount);
        Assertions.assertNotNull(intelligenceBridge.lastPrompt);

        String promptText =
                intelligenceBridge.lastPrompt.get("prompt").getAsString();

        JsonObject llmQuery =
                execution.getLlmResponse()
                        .getLlmRequest()
                        .getLlmQuery();

        Assertions.assertEquals(promptText, llmQuery.get("prompt").getAsString());

        Assertions.assertNotNull(execution);
        Assertions.assertSame(request, execution.getRequest());
        Assertions.assertTrue(execution.isOk());
        Assertions.assertEquals("OK", execution.getStatus());
        Assertions.assertEquals("ok", execution.getStage());

        Assertions.assertEquals(
                "success",
                execution.getRawResponse()
        );

        Assertions.assertNotNull(execution.getPromptValidation());
        Assertions.assertTrue(execution.getPromptValidation().isOk());
        Assertions.assertEquals(
                "prompt.execution.ok",
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

        request.getTask().setControls(
                Arrays.asList(
                        new Control(
                                "spec",
                                "When the expression is true, the runtime result is success. When the expression is false, the runtime result is fail."
                        )
                )
        );

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

        CountingIntelligenceBridge intelligenceBridge =
                new CountingIntelligenceBridge(
                        "{\"result\":{\"decision\":\"CAPTURE\",\"reason\":\"RISK_LEVEL_LOW\",\"code\":\"00\"}}"
                );

        CgoQueryPipeline pipeline =
                new CgoQueryPipeline(
                        new PromptBuilder(),
                        intelligenceBridge,
                        validator
                );

        QueryExecution<ValidateTask> execution =
                this.executeNexus(pipeline, request);

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

        request.getTask().setControls(
                Arrays.asList(
                        new Control(
                                "spec",
                                "When the expression is true, the runtime result is success. When the expression is false, the runtime result is fail."
                        )
                )
        );

        CountingIntelligenceBridge intelligenceBridge =
                new CountingIntelligenceBridge(
                        "success"
                );

        CgoQueryPipeline pipeline =
                new CgoQueryPipeline(new PromptBuilder(), intelligenceBridge);

        QueryExecution<ValidateTask> execution =
                this.executeNexus(pipeline, request);

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

        request.getTask().setControls(
                Arrays.asList(
                        new Control(
                                "spec",
                                "When the expression is true, the runtime result is success. When the expression is false, the runtime result is fail."
                        )
                )
        );

        CountingIntelligenceBridge intelligenceBridge =
                new CountingIntelligenceBridge(
                        "success"
                );

        CgoQueryPipeline pipeline =
                new CgoQueryPipeline(new PromptBuilder(), intelligenceBridge);

        QueryExecution<ValidateTask> execution =
                this.executeNexus(pipeline, request);

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

        request.getTask().setControls(
                Arrays.asList(
                        new Control(
                                "spec",
                                "When the expression is true, the runtime result is success. When the expression is false, the runtime result is fail."
                        )
                )
        );

        CountingIntelligenceBridge intelligenceBridge =
                new CountingIntelligenceBridge("{bad-json");

        CgoQueryPipeline pipeline =
                new CgoQueryPipeline(new PromptBuilder(), intelligenceBridge);

        QueryExecution<ValidateTask> execution =
                this.executeNexus(pipeline, request);

        Console.log("pipeline.malformed.json", execution.toJsonString());

        Assertions.assertFalse(execution.isOk());
        Assertions.assertEquals("ERROR", execution.getStatus());
        Assertions.assertEquals("llm_response", execution.getStage());

        Assertions.assertNotNull(execution.getLlmResponseValidation());
        Assertions.assertFalse(execution.getLlmResponseValidation().isOk());
        Assertions.assertEquals(
                "queryresult.contract.noise",
                execution.getLlmResponseValidation().getCode()
        );

        Assertions.assertNotNull(execution.getLlmResponse());
        Assertions.assertEquals("{bad-json", execution.getLlmResponse().getRawResponse());
    }

    @Test
    public void test_7() {
        QueryRequest<ValidateTask> request =
                buildRequest();

        request.getTask().setControls(
                Arrays.asList(
                        new Control(
                                "spec",
                                "When the expression is true, the runtime result is success. When the expression is false, the runtime result is fail."
                        )
                )
        );

        CountingIntelligenceBridge intelligenceBridge =
                new CountingIntelligenceBridge("{\"unused\":true}");

        CgoQueryPipeline pipeline =
                new CgoQueryPipeline(new PromptBuilder(), intelligenceBridge);

        QueryExecution<ValidateTask> execution =
                this.executeNexus(pipeline, request);

        Console.log("pipeline.result.missing", execution.toJsonString());

        Assertions.assertEquals(1, intelligenceBridge.callCount);

        Assertions.assertFalse(execution.isOk());
        Assertions.assertEquals("ERROR", execution.getStatus());
        Assertions.assertEquals("llm_response", execution.getStage());

        Assertions.assertNotNull(execution.getPromptValidation());
        Assertions.assertTrue(execution.getPromptValidation().isOk());
        Assertions.assertEquals(
                "prompt.execution.ok",
                execution.getPromptValidation().getCode()
        );

        Assertions.assertNotNull(execution.getLlmResponseValidation());
        Assertions.assertFalse(execution.getLlmResponseValidation().isOk());
        Assertions.assertEquals(
                "queryresult.contract.noise",
                execution.getLlmResponseValidation().getCode()
        );

        Assertions.assertNull(execution.getDomainValidation());
        Assertions.assertNotNull(execution.getLlmResponse());
    }

    @Test
    public void test_8() {
        QueryRequest<ValidateTask> request =
                buildRequest();

        request.getTask().setControls(
                Arrays.asList(
                        new Control(
                                "spec",
                                "When the expression is true, the runtime result is success. When the expression is false, the runtime result is fail."
                        )
                )
        );

        CountingIntelligenceBridge intelligenceBridge =
                new CountingIntelligenceBridge(
                        "success"
                );

        CgoQueryPipeline pipeline =
                new CgoQueryPipeline(new PromptBuilder(), intelligenceBridge);

        pipeline.setInMemoryMode(true);

        QueryExecution<ValidateTask> execution =
                this.executeNexus(pipeline, request);

        Console.log("pipeline.in.memory", execution.toJsonString());

        Assertions.assertTrue(execution.isOk());
        Assertions.assertTrue(execution.isInMemoryMode());
    }

    @Test
    public void test_9() {
        QueryRequest<ValidateTask> request =
                buildRequest();

        request.getTask().setControls(
                Arrays.asList(
                        new Control(
                                "spec",
                                "When the expression is true, the runtime result is success. When the expression is false, the runtime result is fail."
                        )
                )
        );

        String arcResponse =
                "result=success";

        String expectedJson =
                "success";

        AtomicInteger validationCount =
                new AtomicInteger(0);

        AtomicReference<String> validatorRaw =
                new AtomicReference<String>();

        final QueryResultValidator queryResultValidator =
                new QueryResultValidator();

        PhaseResultValidator validator =
                new PhaseResultValidator() {
                    @Override
                    public ValidationResult validate(String raw) {
                        validationCount.incrementAndGet();
                        validatorRaw.set(raw);
                        return queryResultValidator.validate(raw);
                    }
                };

        CountingIntelligenceBridge intelligenceBridge =
                new CountingIntelligenceBridge(arcResponse);

        CgoQueryPipeline pipeline =
                new CgoQueryPipeline(
                        new PromptBuilder(),
                        intelligenceBridge,
                        validator
                );

        QueryExecution<ValidateTask> execution =
                this.executeNexus(pipeline, request);

        Console.log("pipeline.map.arc", arcResponse);
        Console.log("pipeline.map.constructed", String.valueOf(validatorRaw.get()));
        Console.log("pipeline.map.validation", String.valueOf(execution.getLlmResponseValidation()));
        Console.log("pipeline.map.status", String.valueOf(execution.getStatus()));
        Console.log("pipeline.map.stage", String.valueOf(execution.getStage()));
        Console.log("pipeline.map.execution.rawResponse", String.valueOf(execution.getRawResponse()));

        Assertions.assertEquals(1, intelligenceBridge.callCount);
        Assertions.assertEquals(1, validationCount.get());
        Assertions.assertEquals(expectedJson, validatorRaw.get());

        Assertions.assertTrue(execution.isOk());
        Assertions.assertEquals("OK", execution.getStatus());
        Assertions.assertEquals("ok", execution.getStage());

        Assertions.assertNotNull(execution.getLlmResponseValidation());
        Assertions.assertTrue(execution.getLlmResponseValidation().isOk());
        Assertions.assertEquals(
                "queryresult.contract.ok",
                execution.getLlmResponseValidation().getCode()
        );

        Assertions.assertEquals(expectedJson, execution.getRawResponse());
        Assertions.assertNotNull(execution.getLlmResponse());
        Assertions.assertEquals(expectedJson, execution.getLlmResponse().getRawResponse());
        Assertions.assertEquals(
                execution.getRawResponse(),
                execution.getLlmResponse().getRawResponse()
        );
        Assertions.assertEquals(
                execution.getRawResponse(),
                validatorRaw.get()
        );
    }

    @Test
    public void test_10() {
        Fact inventory = this.hydrateInventoryUnavailable();
        QueryRequest<ValidateTask> request =
                buildRequest();

        String instructions =
                new LlmInstructionsFieldGenerator().generateNexusInstructions();
        String expectedNexus =
                "inventory availability.\n\ncontext\nThe result is 0.";
        String expectedPrompt =
                instructions
                        + "\n\n"
                        + expectedNexus
                        + "\n\n"
                        + NEXUS_RULE_SECTION
                        + "\n\n"
                        + NEXUS_DECISION_SECTION;

        String arcResponse =
                "result=success";
        String expectedJson =
                "success";

        AtomicInteger validationCount =
                new AtomicInteger(0);
        AtomicReference<String> validatorRaw =
                new AtomicReference<String>();
        final QueryResultValidator queryResultValidator =
                new QueryResultValidator();
        PhaseResultValidator validator =
                new PhaseResultValidator() {
                    @Override
                    public ValidationResult validate(String raw) {
                        validationCount.incrementAndGet();
                        validatorRaw.set(raw);
                        return queryResultValidator.validate(raw);
                    }
                };

        CountingIntelligenceBridge intelligenceBridge =
                new CountingIntelligenceBridge(arcResponse);

        CgoQueryPipeline pipeline =
                new CgoQueryPipeline(
                        new PromptBuilder(),
                        intelligenceBridge,
                        validator
                );

        QueryExecution<ValidateTask> execution =
                pipeline.execute(
                        request,
                        inventory,
                        List.of("Sales"),
                        NEXUS_EXPRESSION,
                        NEXUS_DEFINITION,
                        NEXUS_RULE_SECTION,
                        NEXUS_DECISION_SECTION);

        String assembledPrompt =
                intelligenceBridge.lastPrompt.get("prompt").getAsString();

        Console.log("pipeline.nexus.prompt", String.valueOf(intelligenceBridge.lastPrompt));
        Console.log("pipeline.nexus.raw", execution.getRawResponse());
        Console.log("pipeline.nexus.promptValidation", String.valueOf(execution.getPromptValidation()));
        Console.log("pipeline.nexus.llmValidation", String.valueOf(execution.getLlmResponseValidation()));

        Assertions.assertEquals(1, intelligenceBridge.callCount);
        Assertions.assertNotNull(intelligenceBridge.lastPrompt);
        Assertions.assertEquals(2, intelligenceBridge.lastPrompt.size());
        Assertions.assertTrue(intelligenceBridge.lastPrompt.has("prompt"));
        Assertions.assertTrue(intelligenceBridge.lastPrompt.has("stream"));
        Assertions.assertEquals(expectedPrompt, assembledPrompt);
        Assertions.assertTrue(assembledPrompt.startsWith(instructions));
        Assertions.assertEquals(
                assembledPrompt.indexOf(expectedNexus),
                instructions.length() + 2);
        Assertions.assertEquals(
                assembledPrompt.indexOf("\n\nrule\n"),
                instructions.length() + 2 + expectedNexus.length());
        Assertions.assertEquals(
                assembledPrompt.indexOf("\n\ndecision\n"),
                instructions.length() + 2 + expectedNexus.length() + 2 + NEXUS_RULE_SECTION.length());
        Assertions.assertFalse(
                intelligenceBridge.lastPrompt.get("stream").getAsBoolean());
        Assertions.assertFalse(assembledPrompt.contains("llm_instructions"));
        Assertions.assertFalse(assembledPrompt.contains("task.controls"));
        Assertions.assertFalse(intelligenceBridge.lastPrompt.has("llm_instructions"));
        Assertions.assertFalse(intelligenceBridge.lastPrompt.has("task"));
        Assertions.assertFalse(intelligenceBridge.lastPrompt.has("context"));

        Assertions.assertEquals(1, validationCount.get());
        Assertions.assertEquals(expectedJson, validatorRaw.get());
        Assertions.assertEquals(expectedJson, execution.getRawResponse());

        Assertions.assertNotNull(execution.getPromptValidation());
        Assertions.assertTrue(execution.getPromptValidation().isOk());
        Assertions.assertEquals(
                "prompt.execution.ok",
                execution.getPromptValidation().getCode());
        Assertions.assertNotEquals(
                "querygen.contract.ok",
                execution.getPromptValidation().getCode());

        Assertions.assertNotNull(execution.getLlmResponseValidation());
        Assertions.assertTrue(execution.getLlmResponseValidation().isOk());
        Assertions.assertEquals(
                "queryresult.contract.ok",
                execution.getLlmResponseValidation().getCode());

        Assertions.assertNull(execution.getDomainValidation());
        Assertions.assertTrue(execution.isOk());

        JsonObject llmQuery =
                execution.getLlmResponse()
                        .getLlmRequest()
                        .getLlmQuery();
        Assertions.assertNotNull(llmQuery);
        Assertions.assertEquals(expectedPrompt, llmQuery.get("prompt").getAsString());
        Assertions.assertFalse(llmQuery.has("llm_instructions"));
        Assertions.assertFalse(llmQuery.has("task"));
        Assertions.assertFalse(llmQuery.has("context"));
        Assertions.assertSame(
                request,
                execution.getLlmResponse().getLlmRequest().getQueryRequest());
    }

    @Test
    public void test_11() {
        QueryExecution<ValidateTask> execution =
                new QueryExecution<ValidateTask>(null);

        Console.log("pipeline.developer.null.llmResponse", execution.toJsonString());

        Assertions.assertNull(execution.getLlmQuery());
        JsonObject json = execution.toJson();
        Assertions.assertNotNull(json);
        Assertions.assertEquals(0, json.size());
        Assertions.assertFalse(json.has("request"));
        Assertions.assertFalse(json.has("rawResponse"));
        Assertions.assertFalse(json.has("promptValidation"));
        Assertions.assertFalse(json.has("llmResponseValidation"));
        Assertions.assertFalse(json.has("domainValidation"));
        Assertions.assertFalse(json.has("status"));
        Assertions.assertFalse(json.has("stage"));
        Assertions.assertFalse(json.has("ok"));
        Assertions.assertFalse(json.has("llmResponse"));
    }

    @Test
    public void test_12() {
        QueryExecution<ValidateTask> execution =
                new QueryExecution<ValidateTask>(null);
        LLMResponse llmResponse = new LLMResponse();
        execution.setLlmResponse(llmResponse);

        Console.log("pipeline.developer.null.llmRequest", execution.toJsonString());

        Assertions.assertNull(execution.getLlmQuery());
        JsonObject json = execution.toJson();
        Assertions.assertNotNull(json);
        Assertions.assertEquals(0, json.size());
        Assertions.assertFalse(json.has("request"));
        Assertions.assertFalse(json.has("rawResponse"));
        Assertions.assertFalse(json.has("promptValidation"));
        Assertions.assertFalse(json.has("llmResponseValidation"));
        Assertions.assertFalse(json.has("domainValidation"));
        Assertions.assertFalse(json.has("status"));
        Assertions.assertFalse(json.has("stage"));
        Assertions.assertFalse(json.has("ok"));
        Assertions.assertFalse(json.has("llmResponse"));

        LLMRequest llmRequest = new LLMRequest();
        llmResponse.setLlmRequest(llmRequest);

        Console.log("pipeline.developer.null.llmQuery", execution.toJsonString());

        Assertions.assertNull(execution.getLlmQuery());
        JsonObject empty = execution.toJson();
        Assertions.assertNotNull(empty);
        Assertions.assertEquals(0, empty.size());
    }

    @Test
    public void test_13() {
        QueryRequest<ValidateTask> request =
                buildRequest();

        CountingIntelligenceBridge intelligenceBridge =
                new CountingIntelligenceBridge("success");

        CgoQueryPipeline pipeline =
                new CgoQueryPipeline(new PromptBuilder(), intelligenceBridge);

        QueryExecution<ValidateTask> execution =
                this.executeNexus(pipeline, request);

        JsonObject stored =
                execution.getLlmResponse()
                        .getLlmRequest()
                        .getLlmQuery();
        JsonObject returned = execution.getLlmQuery();
        JsonObject json = execution.toJson();

        Console.log("pipeline.developer.llmQuery", json.toString());

        Assertions.assertNotNull(stored);
        Assertions.assertSame(stored, returned);
        Assertions.assertSame(returned, json);

        String promptText =
                intelligenceBridge.lastPrompt.get("prompt").getAsString();
        Assertions.assertEquals(promptText, json.get("prompt").getAsString());

        Assertions.assertFalse(json.has("request"));
        Assertions.assertFalse(json.has("rawResponse"));
        Assertions.assertFalse(json.has("promptValidation"));
        Assertions.assertFalse(json.has("llmResponseValidation"));
        Assertions.assertFalse(json.has("domainValidation"));
        Assertions.assertFalse(json.has("status"));
        Assertions.assertFalse(json.has("stage"));
        Assertions.assertFalse(json.has("ok"));
        Assertions.assertFalse(json.has("llmResponse"));
        Assertions.assertFalse(json.has("queryRequest"));
        Assertions.assertFalse(json.has("context"));
        Assertions.assertFalse(json.has("task"));
        Assertions.assertFalse(json.has("nodes"));
        Assertions.assertFalse(json.has("factId"));
        Assertions.assertFalse(json.has("llmRequest"));
    }

    private QueryExecution<ValidateTask> executeNexus(
            CgoQueryPipeline pipeline,
            QueryRequest<ValidateTask> request) {
        Fact inventory = this.hydrateInventoryUnavailable();
        return pipeline.execute(
                request,
                inventory,
                List.of("Sales"),
                NEXUS_EXPRESSION,
                NEXUS_DEFINITION,
                NEXUS_RULE_SECTION,
                NEXUS_DECISION_SECTION);
    }

    private Fact hydrateInventoryUnavailable() {
        GraphBuilder graphBuilder = GraphBuilder.getInstance();
        Fact product = this.fact(
                "Product:PROD-SHAMPOO-250",
                "{\"id\":\"Product:PROD-SHAMPOO-250\",\"kind\":\"Product\","
                        + "\"mode\":\"atomic\",\"productId\":\"PROD-SHAMPOO-250\","
                        + "\"sku\":\"SHAMPOO-250\",\"name\":\"Shampoo 250 mL\","
                        + "\"unitOfMeasure\":\"bottle\"}");
        Fact inventory = this.fact(
                "Inventory:INV-001",
                "{\"id\":\"Inventory:INV-001\",\"kind\":\"Inventory\","
                        + "\"mode\":\"atomic\",\"inventoryId\":\"INV-001\","
                        + "\"quantityOnHand\":0,\"quantityReserved\":0,"
                        + "\"product\":\"Product:PROD-SHAMPOO-250\"}");
        Fact sales = this.fact(
                "Sales:SALE-001",
                "{\"id\":\"Sales:SALE-001\",\"kind\":\"Sales\","
                        + "\"mode\":\"atomic\",\"saleId\":\"SALE-001\","
                        + "\"quantitySold\":25,"
                        + "\"product\":\"Product:PROD-SHAMPOO-250\"}");
        graphBuilder.addNode(product);
        graphBuilder.addNode(inventory);
        graphBuilder.addNode(sales);
        BindResult inventoryProduct = graphBuilder.bind(this.edgeInput(inventory, product), null);
        BindResult salesProduct = graphBuilder.bind(this.edgeInput(sales, product), null);
        Assertions.assertTrue(inventoryProduct.isOk());
        Assertions.assertTrue(salesProduct.isOk());
        return inventory;
    }

    private Fact fact(String id, String text) {
        Fact fact = new Fact(id, text);
        fact.setMode("atomic");
        return fact;
    }

    private Input edgeInput(Fact from, Fact to) {
        Edge edge = new Edge();
        edge.setId("Edge:" + from.getId() + "->" + to.getId());
        edge.setFromFactId(from.getId());
        edge.setToFactId(to.getId());
        edge.setMode("relational");
        return new Input(from, to, edge);
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

    private static class CountingIntelligenceBridge extends IntelligenceBridge {

        private final String rawResponse;
        private int callCount;
        private JsonObject lastPrompt;

        private CountingIntelligenceBridge(String rawResponse) {
            this.rawResponse = rawResponse;
        }

        @Override
        public String invoke(String requestJson, String environmentJson) {
            this.callCount++;
            this.lastPrompt = JsonParser.parseString(requestJson).getAsJsonObject();
            return this.rawResponse;
        }
    }
}
