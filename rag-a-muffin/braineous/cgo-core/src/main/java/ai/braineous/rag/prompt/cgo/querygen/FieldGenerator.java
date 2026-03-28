package ai.braineous.rag.prompt.cgo.querygen;

import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import ai.braineous.rag.prompt.cgo.querygen.model.FieldDefinition;
import ai.braineous.rag.prompt.cgo.querygen.model.FieldGenerationResult;

public interface FieldGenerator {

    boolean supports(FieldDefinition fieldDefinition);

    FieldGenerationResult generate(FieldDefinition fieldDefinition, QueryRequest request);
}