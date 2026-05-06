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
import ai.braineous.rag.prompt.cgo.querygen.OutputTemplateFieldGenerator;
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
    private final FieldGenerator outputTemplateFieldGenerator;
    private final FieldGenerator responseContractFieldGenerator;
    private final FieldGenerator llmInstructionsFieldGenerator;
    private final FieldGenerator llmTraceFieldGenerator;
    private final FieldGenerator llmTraceInstructionsFieldGenerator;

    public QueryGenService() {
        this.validator = new QueryGenValidator();
        this.metaFieldGenerator = new MetaFieldGenerator();
        this.contextFieldGenerator = new ContextFieldGenerator();
        this.taskFieldGenerator = new TaskFieldGenerator();
        this.outputTemplateFieldGenerator = new OutputTemplateFieldGenerator();
        this.responseContractFieldGenerator = new ResponseContractFieldGenerator();
        this.llmInstructionsFieldGenerator = new LlmInstructionsFieldGenerator();
        this.llmTraceFieldGenerator = new LlmTraceFieldGenerator();
        this.llmTraceInstructionsFieldGenerator = new LlmTraceInstructionsFieldGenerator();
    }

    public QueryGenOutput generateQuery(QueryRequest queryRequest) {
        JsonObject llmQuery = new JsonObject();

        addField(llmQuery, "llm_instructions", this.llmInstructionsFieldGenerator, queryRequest); // MUST be first
        addField(llmQuery, "meta", this.metaFieldGenerator, queryRequest);
        addField(llmQuery, "task", this.taskFieldGenerator, queryRequest);
        addField(llmQuery, "output_template", this.outputTemplateFieldGenerator, queryRequest);
        addField(llmQuery, "response_contract", this.responseContractFieldGenerator, queryRequest);
        addField(llmQuery, "context", this.contextFieldGenerator, queryRequest); // MUST be late
        addField(llmQuery, "llm_trace", this.llmTraceFieldGenerator, queryRequest);
        addField(llmQuery, "llm_trace_instructions", this.llmTraceInstructionsFieldGenerator, queryRequest);

        ValidationResult validationResult = validateQueryOutput(llmQuery);

        return new QueryGenOutput(llmQuery, validationResult);
    }

    private void addField(
            JsonObject llmQuery,
            String fieldName,
            FieldGenerator fieldGenerator,
            QueryRequest queryRequest
    ) {
        FieldGenerationResult result =
                fieldGenerator.generate(
                        new FieldDefinition(fieldName),
                        queryRequest
                );

        llmQuery.add(fieldName, result.getFieldValue());
    }

    private ValidationResult validateQueryOutput(JsonObject llmQuery) {
        if (this.validator != null) {
            return this.validator.validate(llmQuery.toString());
        }

        return null;
    }
}