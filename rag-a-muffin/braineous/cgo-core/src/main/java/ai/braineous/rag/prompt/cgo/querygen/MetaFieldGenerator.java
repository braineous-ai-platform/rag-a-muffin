package ai.braineous.rag.prompt.cgo.querygen;

import ai.braineous.rag.prompt.cgo.api.Meta;
import ai.braineous.rag.prompt.cgo.api.ValidationResult;
import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import ai.braineous.rag.prompt.cgo.querygen.model.FieldDefinition;
import ai.braineous.rag.prompt.cgo.querygen.model.FieldGenerationResult;
import com.google.gson.JsonObject;

public class MetaFieldGenerator implements FieldGenerator {

    public MetaFieldGenerator() {
    }

    @Override
    public boolean supports(FieldDefinition fieldDefinition) {
        if (fieldDefinition == null) {
            return false;
        }
        if (fieldDefinition.getName() == null) {
            return false;
        }
        return "meta".equals(fieldDefinition.getName());
    }

    @Override
    public FieldGenerationResult generate(FieldDefinition fieldDefinition, QueryRequest request) {

        Meta meta = request.getMeta();

        JsonObject fieldValue = new JsonObject();
        fieldValue.addProperty("version", meta.getVersion());
        fieldValue.addProperty("queryKind", meta.getQueryKind());
        fieldValue.addProperty("description", meta.getDescription());

        ValidationResult validationResult =
                new ValidationResult(
                        true,
                        "field.meta.ok",
                        "VALID",
                        "field_generation",
                        fieldDefinition.getName(),
                        null
                );

        return new FieldGenerationResult(fieldDefinition, fieldValue, validationResult);
    }
}
