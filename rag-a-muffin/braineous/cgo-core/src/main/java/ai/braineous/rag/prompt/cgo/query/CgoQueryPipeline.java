package ai.braineous.rag.prompt.cgo.query;

import ai.braineous.arc.IntelligenceBridge;
import ai.braineous.rag.prompt.cgo.api.*;
import ai.braineous.rag.prompt.cgo.prompt.PromptBuilder;
import ai.braineous.rag.prompt.cgo.prompt.PromptRequestOutput;
import ai.braineous.rag.prompt.cgo.querygen.model.QueryGenOutput;
import ai.braineous.rag.prompt.cgo.querygen.services.QueryGenService;
import ai.braineous.rag.prompt.cgo.querygen.services.QueryResultValidator;
import ai.braineous.rag.prompt.utils.Resources;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Objects;

public final class CgoQueryPipeline implements QueryPipeline {

    private final PromptBuilder promptBuilder;

    private final QueryGenService queryGenService;
    private final IntelligenceBridge intelligenceBridge;

    private volatile ScorerClient scorerClient;

    private final PhaseResultValidator llmResponseValidator;

    private boolean inMemoryMode = false;

    public CgoQueryPipeline(PromptBuilder promptBuilder) {
        this.promptBuilder = Objects.requireNonNull(promptBuilder, "promptBuilder must not be null");
        this.queryGenService = new QueryGenService();
        this.intelligenceBridge = new IntelligenceBridge();
        this.llmResponseValidator = new QueryResultValidator();
    }

    public CgoQueryPipeline(PromptBuilder promptBuilder, IntelligenceBridge intelligenceBridge) {
        this.promptBuilder = Objects.requireNonNull(promptBuilder, "promptBuilder must not be null");
        this.queryGenService = new QueryGenService();
        this.intelligenceBridge = Objects.requireNonNull(intelligenceBridge, "intelligenceBridge must not be null");
        this.llmResponseValidator = new QueryResultValidator();
    }

    CgoQueryPipeline(PromptBuilder promptBuilder,
                     IntelligenceBridge intelligenceBridge,
                     PhaseResultValidator llmResponseValidator) {
        this.promptBuilder = Objects.requireNonNull(promptBuilder, "promptBuilder must not be null");
        this.intelligenceBridge = Objects.requireNonNull(intelligenceBridge, "intelligenceBridge must not be null");
        this.llmResponseValidator = llmResponseValidator;
        this.queryGenService = new QueryGenService();
    }

    public ScorerClient getScorerClient() {
        return scorerClient;
    }

    public void setInMemoryMode(boolean inMemoryMode) {
        this.inMemoryMode = inMemoryMode;
    }

    @Override
    public <T extends QueryTask> QueryExecution<T> execute(QueryRequest<T> request) {
        Objects.requireNonNull(request, "request must not be null");

        QueryGenOutput requestOutput = queryGenService.generateQuery(request);
        JsonObject llmQuery = requestOutput.getPayload();

        ValidationResult promptValidation = requestOutput.getValidationResult();
        if (promptValidation != null && !promptValidation.isOk()) {
            return new QueryExecution<T>(request, null, promptValidation, null, null);
        }

        PromptRequestOutput executionPrompt =
                promptBuilder.generateExecutionPrompt(llmQuery);

        String executionPromptText =
                executionPrompt.getRequestOutput()
                        .get("prompt")
                        .getAsString();

        JsonObject llmPayload = new JsonObject();
        llmPayload.addProperty("prompt", executionPromptText);
        llmPayload.addProperty("stream", false);

        request.generateRequestId();
        String rawResponse =
                this.intelligenceBridge.invoke(llmPayload.toString(), "{}");

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

        JsonObject parsedResponse =
                this.parseLlmResponse(rawResponse);

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

    private JsonObject parseLlmResponse(String rawResponse) {
        LLMResponseParser parser = new LLMResponseParser();
        return parser.parse(rawResponse);
    }
}
