package ai.braineous.rag.prompt.cgo.querygen;

import ai.braineous.rag.prompt.cgo.api.ValidationResult;
import ai.braineous.rag.prompt.cgo.prompt.ResponseContractRegistry;
import ai.braineous.rag.prompt.cgo.prompt.SimpleResponseContractRegistry;
import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import ai.braineous.rag.prompt.cgo.querygen.model.FieldDefinition;
import ai.braineous.rag.prompt.cgo.querygen.model.FieldGenerationResult;
import ai.braineous.rag.prompt.cgo.querygen.model.FieldValue;
import com.google.gson.JsonObject;

public class ResponseContractFieldGenerator implements FieldGenerator {

    private final ResponseContractRegistry registry;

    public ResponseContractFieldGenerator() {
        this.registry = new SimpleResponseContractRegistry();
    }

    public ResponseContractFieldGenerator(ResponseContractRegistry registry) {
        this.registry = registry;
    }

    @Override
    public boolean supports(FieldDefinition fieldDefinition) {
        if (fieldDefinition == null) {
            return false;
        }
        if (fieldDefinition.getName() == null) {
            return false;
        }
        return "response_contract".equals(fieldDefinition.getName());
    }

    @Override
    public FieldGenerationResult generate(FieldDefinition fieldDefinition, QueryRequest<?> request) {
        JsonObject responseContract =
                registry.responseContractFor(request.getMeta().getQueryKind());

        FieldValue fieldValue =
                new FieldValue(fieldDefinition.getName(), responseContract.toString());

        ValidationResult validationResult =
                new ValidationResult(
                        true,
                        "field.response_contract.ok",
                        "VALID",
                        "field_generation",
                        fieldDefinition.getName(),
                        null
                );

        return new FieldGenerationResult(fieldDefinition, fieldValue, validationResult);
    }
}