package ai.braineous.rag.prompt.cgo.querygen;

import ai.braineous.rag.prompt.cgo.api.GraphContext;
import ai.braineous.rag.prompt.cgo.api.Meta;
import ai.braineous.rag.prompt.cgo.api.ValidateTask;
import ai.braineous.rag.prompt.cgo.api.ValidationResult;
import ai.braineous.rag.prompt.cgo.prompt.ResponseContractRegistry;
import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import ai.braineous.rag.prompt.cgo.querygen.model.FieldDefinition;
import ai.braineous.rag.prompt.cgo.querygen.model.FieldGenerationResult;
import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ResponseContractFieldGeneratorTest {

    @Test
    public void supports_shouldReturnTrue_forResponseContractField() {
        ResponseContractFieldGenerator generator = new ResponseContractFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("response_contract");

        boolean supported = generator.supports(fieldDefinition);

        Console.log("____responseContract.supported____", String.valueOf(supported));

        assertTrue(supported);
    }

    @Test
    public void supports_shouldReturnFalse_forNonResponseContractField() {
        ResponseContractFieldGenerator generator = new ResponseContractFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("llm_instructions");

        boolean supported = generator.supports(fieldDefinition);

        Console.log("____responseContract.notSupported____", String.valueOf(supported));

        assertFalse(supported);
    }

    @Test
    public void generate_shouldReturnFieldGenerationResult_withSerializedResponseContract() {
        ResponseContractRegistry registry = new ResponseContractRegistry() {
            @Override
            public JsonObject responseContractFor(String queryKind) {
                JsonObject root = new JsonObject();
                root.addProperty("type", "validation_result");
                return root;
            }

            @Override
            public java.util.List<String> llmInstructionsFor(String queryKind) {
                return java.util.Collections.emptyList();
            }
        };

        ResponseContractFieldGenerator generator = new ResponseContractFieldGenerator(registry);
        FieldDefinition fieldDefinition = new FieldDefinition("response_contract");
        QueryRequest request = buildRequest("validate_flight_airports");

        FieldGenerationResult result = generator.generate(fieldDefinition, request);

        Console.log("____responseContract.fieldName____", result.getFieldValue().getFieldName());
        Console.log("____responseContract.value____", result.getFieldValue().getValue());
        Console.log("____responseContract.validation____", String.valueOf(result.getValidationResult()));

        assertNotNull(result);
        assertEquals("response_contract", result.getFieldDefinition().getName());
        assertEquals("response_contract", result.getFieldValue().getFieldName());
        assertEquals("{\"type\":\"validation_result\"}", result.getFieldValue().getValue());

        ValidationResult validationResult = result.getValidationResult();
        assertNotNull(validationResult);
        assertTrue(validationResult.isOk());
        assertEquals("field.response_contract.ok", validationResult.getCode());
        assertEquals("field_generation", validationResult.getStage());
        assertEquals("response_contract", validationResult.getAnchorId());
    }

    private QueryRequest buildRequest(String queryKind) {
        Meta meta = new Meta("v1", queryKind, "test description");
        GraphContext context = new GraphContext();
        ValidateTask task = new ValidateTask("validate", "Flight:F100");
        return new QueryRequest(meta, context, task);
    }
}
