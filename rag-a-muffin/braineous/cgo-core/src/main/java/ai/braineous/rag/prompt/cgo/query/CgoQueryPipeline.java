package ai.braineous.rag.prompt.cgo.query;

import ai.braineous.rag.prompt.cgo.api.*;
import ai.braineous.rag.prompt.cgo.prompt.LlmClient;
import ai.braineous.rag.prompt.cgo.prompt.PromptBuilder;
import ai.braineous.rag.prompt.cgo.prompt.PromptRequestOutput;
import ai.braineous.rag.prompt.cgo.querygen.model.QueryGenOutput;
import ai.braineous.rag.prompt.cgo.querygen.services.QueryGenService;
import ai.braineous.rag.prompt.cgo.querygen.services.QueryGenValidator;
import ai.braineous.rag.prompt.utils.Resources;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Objects;

/**
 * Core, domain-agnostic implementation of the QueryPipeline.
 *
 * Responsibilities:
 *  - Take a QueryRequest<T extends QueryTask>
 *  - Use PromptBuilder to construct the LLM prompt JSON
 *  - Call LlmClient
 *  - Wrap request + raw response in QueryExecution<T>
 *
 * This class MUST remain domain-agnostic:
 *  - No references to specific tasks like "ValidateTask"
 *  - No references to domain result DTOs (e.g., ValidationResult for flights)
 */
public final class CgoQueryPipeline implements QueryPipeline {

    private final PromptBuilder promptBuilder;

    private final QueryGenService queryGenService;
    private volatile LlmClient llmClient;

    private volatile ScorerClient scorerClient;

    private final PhaseResultValidator llmResponseValidator;

    private boolean inMemoryMode = false; //not-in-memory by default



    public CgoQueryPipeline(PromptBuilder promptBuilder) {
        this.promptBuilder = Objects.requireNonNull(promptBuilder, "promptBuilder must not be null");
        this.queryGenService = new QueryGenService();
        this.llmClient = null;
        //this.llmResponseValidator = new GsonPhaseResultValidator();
        this.llmResponseValidator = new QueryGenValidator();
    }

    public CgoQueryPipeline(PromptBuilder promptBuilder, LlmClient llmClient) {
        this.promptBuilder = promptBuilder;
        this.llmClient = llmClient;
        //this.llmResponseValidator = new GsonPhaseResultValidator();
        this.llmResponseValidator = new QueryGenValidator();
        this.queryGenService = new QueryGenService();
    }

    CgoQueryPipeline(PromptBuilder promptBuilder, LlmClient llmClient,
                     PhaseResultValidator llmResponseValidator) {
        this.promptBuilder = Objects.requireNonNull(promptBuilder, "promptBuilder must not be null");
        this.llmClient = llmClient;
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

        LlmAdapter adapter = request.getAdapter();
        Objects.requireNonNull(adapter,
                "Missing LlmAdapter on QueryRequest. Adapter must be explicit (cost guard).");

        /*PromptRequestOutput requestOutput = promptBuilder.generateRequestPrompt(request);
        JsonObject prompt = requestOutput.getRequestOutput();

        ValidationResult promptValidation = requestOutput.getValidationResult();
        if (promptValidation != null && !promptValidation.isOk()) {
            return new QueryExecution<T>(request, null, promptValidation, null, null);
        }*/
        QueryGenOutput requestOutput = queryGenService.generateQuery(request);
        JsonObject prompt = requestOutput.getPayload();

        ValidationResult promptValidation = requestOutput.getValidationResult();
        if (promptValidation != null && !promptValidation.isOk()) {
            return new QueryExecution<T>(request, null, promptValidation, null, null);
        }


        LlmClient client = this.findLlmClient();
        JsonObject llmPayload = new JsonObject();
        llmPayload.addProperty("model", "llama3");
        llmPayload.addProperty("prompt", prompt.toString());
        llmPayload.addProperty("stream", false);

        String rawResponse = client.executePrompt(adapter, request, llmPayload);

        LLMResponse llmResponse = this.createLlmResponse(request, prompt, rawResponse);

        ValidationResult responseValidation = null;
        if (this.llmResponseValidator != null) {
            responseValidation = llmResponseValidator.validate(rawResponse);
            if (responseValidation != null && !responseValidation.isOk()) {
                QueryExecution<T> failedExecution =
                        new QueryExecution<T>(request, rawResponse, promptValidation, responseValidation, null);
                failedExecution.setLlmResponse(llmResponse);
                failedExecution.setInMemoryMode(this.inMemoryMode);
                this.score(failedExecution);
                return failedExecution;
            }
        }

        LLMResponseValidatorRule rule = request.getRule();
        ValidationResult domainValidation = null;
        if (rule != null) {
            domainValidation = rule.validate(rawResponse);
            if (domainValidation != null && !domainValidation.isOk()) {
                QueryExecution<T> failedExecution =
                        new QueryExecution<T>(request, rawResponse, promptValidation, responseValidation, domainValidation);
                failedExecution.setLlmResponse(llmResponse);
                failedExecution.setInMemoryMode(this.inMemoryMode);
                this.score(failedExecution);
                return failedExecution;
            }
        }

        JsonObject parsedResponse = this.parseLlmResponse(rawResponse);

        QueryExecution<T> execution =
                new QueryExecution<T>(request, rawResponse, promptValidation, responseValidation, domainValidation);

        execution.setLlmResponse(llmResponse);
        execution.setInMemoryMode(this.inMemoryMode);

        this.score(execution);

        return execution;
    }
    //--------------------------------------------------------------------------------------
    private LlmClient findLlmClient(){
        try {
            if (this.llmClient != null) {
                return this.llmClient;
            }

            synchronized (this) {
                if (this.llmClient != null) {   // <-- add this
                    return this.llmClient;
                }

                //otherwise use the core-cgo-llm-orchestrator
                String pipelineStr = Resources.getResource("pipeline.json");
                JsonObject pipeLineJson = JsonParser.parseString(pipelineStr).getAsJsonObject();

                String llmOrchestratorClass = pipeLineJson.get("llm_client").getAsString();
                LlmClient cgoLlmClient = (LlmClient) Thread.currentThread().getContextClassLoader().
                        loadClass(llmOrchestratorClass).getDeclaredConstructor().newInstance();
                this.llmClient = cgoLlmClient;

                return this.llmClient;
            }
        }catch (Exception e){
            throw new IllegalStateException("Failed to resolve LlmClient from pipeline.json", e);
        }
    }

    private ScorerClient findScorerClient(){
        try {
            if (this.scorerClient != null) {
                return this.scorerClient;
            }

            synchronized (this) {
                if (this.scorerClient != null) {   // <-- add this
                    return this.scorerClient;
                }

                //otherwise use the core-cgo-llm-orchestrator
                String pipelineStr = Resources.getResource("pipeline.json");
                JsonObject pipeLineJson = JsonParser.parseString(pipelineStr).getAsJsonObject();

                String scorerStr = pipeLineJson.get("scorer").getAsString();
                ScorerClient scorer = (ScorerClient) Thread.currentThread().getContextClassLoader().
                        loadClass(scorerStr).getDeclaredConstructor().newInstance();
                this.scorerClient = scorer;

                return this.scorerClient;
            }
        }catch (Exception e){
            e.printStackTrace();
            throw new IllegalStateException("Failed to resolve ScorerClient from pipeline.json", e);
        }
    }

    private void score(QueryExecution execution){
        ScorerClient scorer = this.findScorerClient();
        scorer.orchestrate(execution);
    }

    private <T extends QueryTask> LLMResponse createLlmResponse(
            QueryRequest<T> request,
            JsonObject prompt,
            String rawResponse) {

        LLMRequest llmRequest = new LLMRequest();
        llmRequest.setQueryRequest(request);
        llmRequest.setLlmQuery(prompt);

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



