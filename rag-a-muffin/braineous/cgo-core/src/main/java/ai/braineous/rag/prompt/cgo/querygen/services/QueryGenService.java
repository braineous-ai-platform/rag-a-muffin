package ai.braineous.rag.prompt.cgo.querygen.services;

import ai.braineous.rag.prompt.cgo.api.ValidationResult;
import ai.braineous.rag.prompt.cgo.query.PhaseResultValidator;
import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import ai.braineous.rag.prompt.cgo.querygen.ContextFieldGenerator;
import ai.braineous.rag.prompt.cgo.querygen.FieldGenerator;
import ai.braineous.rag.prompt.cgo.querygen.LlmInstructionsFieldGenerator;
import ai.braineous.rag.prompt.cgo.querygen.LlmTraceFieldGenerator;
import ai.braineous.rag.prompt.cgo.querygen.LlmTraceInstructionsFieldGenerator;
import ai.braineous.rag.prompt.cgo.querygen.MetaFieldGenerator;
import ai.braineous.rag.prompt.cgo.querygen.ResponseContractFieldGenerator;
import ai.braineous.rag.prompt.cgo.querygen.TaskFieldGenerator;
import ai.braineous.rag.prompt.cgo.querygen.model.FieldDefinition;
import ai.braineous.rag.prompt.cgo.querygen.model.FieldGenerationResult;
import ai.braineous.rag.prompt.cgo.querygen.model.QueryGenOutput;
import com.google.gson.JsonObject;

public class QueryGenService {

    private final PhaseResultValidator validator;

    private final FieldGenerator metaFieldGenerator;
    private final FieldGenerator contextFieldGenerator;
    private final FieldGenerator taskFieldGenerator;
    private final FieldGenerator responseContractFieldGenerator;
    private final FieldGenerator llmInstructionsFieldGenerator;
    private final FieldGenerator llmTraceFieldGenerator;
    private final FieldGenerator llmTraceInstructionsFieldGenerator;

    public QueryGenService() {
        this.validator = new QueryGenValidator();
        this.metaFieldGenerator = new MetaFieldGenerator();
        this.contextFieldGenerator = new ContextFieldGenerator();
        this.taskFieldGenerator = new TaskFieldGenerator();
        this.responseContractFieldGenerator = new ResponseContractFieldGenerator();
        this.llmInstructionsFieldGenerator = new LlmInstructionsFieldGenerator();
        this.llmTraceFieldGenerator = new LlmTraceFieldGenerator();
        this.llmTraceInstructionsFieldGenerator = new LlmTraceInstructionsFieldGenerator();
    }

    public QueryGenOutput generateQuery(QueryRequest queryRequest) {
        JsonObject llmQuery = new JsonObject();

        addMeta(llmQuery, queryRequest);
        addContext(llmQuery, queryRequest);
        addTask(llmQuery, queryRequest);
        addResponseContract(llmQuery, queryRequest);
        addLlmInstructions(llmQuery, queryRequest);
        addLlmTrace(llmQuery, queryRequest);
        addLlmTraceInstructions(llmQuery, queryRequest);

        ValidationResult validationResult = validateQueryOutput(llmQuery);

        return new QueryGenOutput(llmQuery, validationResult);
    }

    private void addMeta(JsonObject llmQuery, QueryRequest queryRequest) {
        FieldGenerationResult result =
                this.metaFieldGenerator.generate(new FieldDefinition("meta"), queryRequest);

        llmQuery.add("meta", result.getFieldValue());
    }

    private void addContext(JsonObject llmQuery, QueryRequest queryRequest) {
        FieldGenerationResult result =
                this.contextFieldGenerator.generate(new FieldDefinition("context"), queryRequest);

        llmQuery.add("context", result.getFieldValue());
    }

    private void addTask(JsonObject llmQuery, QueryRequest queryRequest) {
        FieldGenerationResult result =
                this.taskFieldGenerator.generate(new FieldDefinition("task"), queryRequest);

        llmQuery.add("task", result.getFieldValue());
    }

    private void addResponseContract(JsonObject llmQuery, QueryRequest queryRequest) {
        FieldGenerationResult result =
                this.responseContractFieldGenerator.generate(
                        new FieldDefinition("response_contract"),
                        queryRequest
                );

        llmQuery.add("response_contract", result.getFieldValue());
    }

    private void addLlmInstructions(JsonObject llmQuery, QueryRequest queryRequest) {
        FieldGenerationResult result =
                this.llmInstructionsFieldGenerator.generate(
                        new FieldDefinition("llm_instructions"),
                        queryRequest
                );

        llmQuery.add("llm_instructions", result.getFieldValue());
    }

    private void addLlmTrace(JsonObject llmQuery, QueryRequest queryRequest) {
        FieldGenerationResult result =
                this.llmTraceFieldGenerator.generate(
                        new FieldDefinition("llm_trace"),
                        queryRequest
                );

        llmQuery.add("llm_trace", result.getFieldValue());
    }

    private void addLlmTraceInstructions(JsonObject llmQuery, QueryRequest queryRequest) {
        FieldGenerationResult result =
                this.llmTraceInstructionsFieldGenerator.generate(
                        new FieldDefinition("llm_trace_instructions"),
                        queryRequest
                );

        llmQuery.add("llm_trace_instructions", result.getFieldValue());
    }

    private ValidationResult validateQueryOutput(JsonObject llmQuery) {
        if (this.validator != null) {
            return this.validator.validate(llmQuery.toString());
        }
        return null;
    }
}