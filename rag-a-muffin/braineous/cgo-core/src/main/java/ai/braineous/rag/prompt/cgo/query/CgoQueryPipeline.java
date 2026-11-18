package ai.braineous.rag.prompt.cgo.query;

import ai.braineous.arc.IntelligenceBridge;
import ai.braineous.rag.prompt.cgo.api.*;
import ai.braineous.rag.prompt.cgo.nexus.orchestrator.NexusOrchestrator;
import ai.braineous.rag.prompt.cgo.prompt.PromptBuilder;
import ai.braineous.rag.prompt.cgo.prompt.PromptRequestOutput;
import ai.braineous.rag.prompt.cgo.querygen.LlmInstructionsFieldGenerator;
import ai.braineous.rag.prompt.cgo.querygen.services.QueryResultValidator;
import ai.braineous.rag.prompt.utils.Resources;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class CgoQueryPipeline implements QueryPipeline {

    private final PromptBuilder promptBuilder;

    private final LlmInstructionsFieldGenerator llmInstructionsFieldGenerator;
    private final NexusOrchestrator nexusOrchestrator;
    private final IntelligenceBridge intelligenceBridge;

    private volatile ScorerClient scorerClient;

    private final PhaseResultValidator llmResponseValidator;
    private final MapToJsonResponseConverter mapToJsonResponseConverter;

    private boolean inMemoryMode = false;

    public CgoQueryPipeline(PromptBuilder promptBuilder) {
        this.promptBuilder = Objects.requireNonNull(promptBuilder, "promptBuilder must not be null");
        this.llmInstructionsFieldGenerator = new LlmInstructionsFieldGenerator();
        this.nexusOrchestrator = new NexusOrchestrator();
        this.intelligenceBridge = new IntelligenceBridge();
        this.llmResponseValidator = new QueryResultValidator();
        this.mapToJsonResponseConverter = new MapToJsonResponseConverter();
    }

    public CgoQueryPipeline(PromptBuilder promptBuilder, IntelligenceBridge intelligenceBridge) {
        this.promptBuilder = Objects.requireNonNull(promptBuilder, "promptBuilder must not be null");
        this.llmInstructionsFieldGenerator = new LlmInstructionsFieldGenerator();
        this.nexusOrchestrator = new NexusOrchestrator();
        this.intelligenceBridge = Objects.requireNonNull(intelligenceBridge, "intelligenceBridge must not be null");
        this.llmResponseValidator = new QueryResultValidator();
        this.mapToJsonResponseConverter = new MapToJsonResponseConverter();
    }

    CgoQueryPipeline(PromptBuilder promptBuilder,
                     IntelligenceBridge intelligenceBridge,
                     PhaseResultValidator llmResponseValidator) {
        this.promptBuilder = Objects.requireNonNull(promptBuilder, "promptBuilder must not be null");
        this.intelligenceBridge = Objects.requireNonNull(intelligenceBridge, "intelligenceBridge must not be null");
        this.llmResponseValidator = llmResponseValidator;
        this.llmInstructionsFieldGenerator = new LlmInstructionsFieldGenerator();
        this.nexusOrchestrator = new NexusOrchestrator();
        this.mapToJsonResponseConverter = new MapToJsonResponseConverter();
    }

    public ScorerClient getScorerClient() {
        return scorerClient;
    }

    public void setInMemoryMode(boolean inMemoryMode) {
        this.inMemoryMode = inMemoryMode;
    }

    @Override
    public <T extends QueryTask> QueryExecution<T> execute(
            QueryRequest<T> request,
            Fact axis,
            List<String> relatedFacts,
            String expression,
            String definition,
            String ruleSection,
            String decisionSection) {
        Objects.requireNonNull(request, "request must not be null");
        Objects.requireNonNull(axis, "axis must not be null");

        String nexusTitleAndContext =
                this.nexusOrchestrator.execute(axis, relatedFacts, expression, definition);

        String instructions =
                this.llmInstructionsFieldGenerator.generateNexusInstructions();

        PromptRequestOutput executionPrompt =
                promptBuilder.generateExecutionPrompt(
                        instructions,
                        nexusTitleAndContext,
                        ruleSection,
                        decisionSection);

        ValidationResult promptValidation =
                executionPrompt.getValidationResult();
        if (promptValidation != null && !promptValidation.isOk()) {
            return new QueryExecution<T>(request, null, promptValidation, null, null);
        }

        JsonObject llmQuery = executionPrompt.getRequestOutput();
        String executionPromptText =
                llmQuery.get("prompt")
                        .getAsString()
                        .toLowerCase(Locale.ROOT);
        llmQuery.addProperty("prompt", executionPromptText);

        JsonObject llmPayload = new JsonObject();
        llmPayload.addProperty("prompt", executionPromptText);
        llmPayload.addProperty("stream", false);

        request.generateRequestId();
        System.out.print("=== CGO OUTBOUND PROMPT BEGIN ===\n");
        System.out.print(llmPayload.toString());
        System.out.print("\n=== CGO OUTBOUND PROMPT END ===\n");
        String rawResponse =
                this.intelligenceBridge.invoke(llmPayload.toString(), "{}");
        rawResponse = this.mapToJsonResponseConverter.convert(rawResponse);

        LLMResponse llmResponse =
                this.createLlmResponse(request, llmQuery, rawResponse);

        ValidationResult responseValidation = null;

        if (this.llmResponseValidator != null) {
            responseValidation =
                    llmResponseValidator.validate(rawResponse);

            if (responseValidation != null && !responseValidation.isOk()) {
                QueryExecution<T> failedExecution =
                        new QueryExecution<T>(
                                request,
                                rawResponse,
                                promptValidation,
                                responseValidation,
                                null
                        );

                failedExecution.setLlmResponse(llmResponse);
                failedExecution.setInMemoryMode(this.inMemoryMode);
                this.score(failedExecution);

                return failedExecution;
            }
        }

        LLMResponseValidatorRule rule = request.getRule();
        ValidationResult domainValidation = null;

        if (rule != null) {
            domainValidation =
                    rule.validate(rawResponse);

            if (domainValidation != null && !domainValidation.isOk()) {
                QueryExecution<T> failedExecution =
                        new QueryExecution<T>(
                                request,
                                rawResponse,
                                promptValidation,
                                responseValidation,
                                domainValidation
                        );

                failedExecution.setLlmResponse(llmResponse);
                failedExecution.setInMemoryMode(this.inMemoryMode);
                this.score(failedExecution);

                return failedExecution;
            }
        }

        this.parseLlmResponseIfJsonObject(rawResponse);

        QueryExecution<T> execution =
                new QueryExecution<T>(
                        request,
                        rawResponse,
                        promptValidation,
                        responseValidation,
                        domainValidation
                );

        execution.setLlmResponse(llmResponse);
        execution.setInMemoryMode(this.inMemoryMode);

        this.score(execution);

        return execution;
    }

    private ScorerClient findScorerClient() {
        try {
            if (this.scorerClient != null) {
                return this.scorerClient;
            }

            synchronized (this) {
                if (this.scorerClient != null) {
                    return this.scorerClient;
                }

                String pipelineStr =
                        Resources.getResource("pipeline.json");

                JsonObject pipeLineJson =
                        JsonParser.parseString(pipelineStr).getAsJsonObject();

                String scorerStr =
                        pipeLineJson.get("scorer").getAsString();

                ScorerClient scorer =
                        (ScorerClient) Thread.currentThread()
                                .getContextClassLoader()
                                .loadClass(scorerStr)
                                .getDeclaredConstructor()
                                .newInstance();

                this.scorerClient = scorer;

                return this.scorerClient;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("Failed to resolve ScorerClient from pipeline.json", e);
        }
    }

    private void score(QueryExecution execution) {
        ScorerClient scorer = this.findScorerClient();
        scorer.orchestrate(execution);
    }

    private <T extends QueryTask> LLMResponse createLlmResponse(
            QueryRequest<T> request,
            JsonObject llmQuery,
            String rawResponse
    ) {

        LLMRequest llmRequest = new LLMRequest();
        llmRequest.setQueryRequest(request);
        llmRequest.setLlmQuery(llmQuery);

        LLMResponse llmResponse = new LLMResponse();
        llmResponse.setLlmRequest(llmRequest);
        llmResponse.setRawResponse(rawResponse);
        llmResponse.setSuccess(true);

        return llmResponse;
    }

    private JsonObject parseLlmResponseIfJsonObject(String rawResponse) {
        if (QueryResultValidator.isAcceptedScalar(rawResponse == null ? null : rawResponse.trim())) {
            return null;
        }
        if (rawResponse == null) {
            return null;
        }
        try {
            JsonElement parsed = JsonParser.parseString(rawResponse);
            if (parsed == null || !parsed.isJsonObject()) {
                return null;
            }
        } catch (JsonParseException e) {
            return null;
        }
        return this.parseLlmResponse(rawResponse);
    }

    private JsonObject parseLlmResponse(String rawResponse) {
        LLMResponseParser parser = new LLMResponseParser();
        return parser.parse(rawResponse);
    }
}
