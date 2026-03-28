package ai.braineous.rag.prompt.cgo.querygen.model;

import ai.braineous.rag.prompt.cgo.api.ValidationResult;
import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class QueryGenModelTest {

    @Test
    public void fieldDefinition_shouldExposeName() {
        FieldDefinition fieldDefinition = new FieldDefinition("response_contract");

        Console.log("____fieldDefinition.name____", fieldDefinition.getName());

        assertEquals("response_contract", fieldDefinition.getName());
    }

    @Test
    public void fieldValue_shouldExposeFieldNameAndValue() {
        FieldValue fieldValue = new FieldValue("llm_instructions", "Return exactly one JSON object.");

        Console.log("____fieldValue.fieldName____", fieldValue.getFieldName());
        Console.log("____fieldValue.value____", fieldValue.getValue());

        assertEquals("llm_instructions", fieldValue.getFieldName());
        assertEquals("Return exactly one JSON object.", fieldValue.getValue());
    }

    @Test
    public void fieldGenerationResult_shouldExposeDefinitionValueAndValidationResult() {
        FieldDefinition fieldDefinition = new FieldDefinition("llm_trace");
        FieldValue fieldValue = new FieldValue("llm_trace", "{\"requestId\":\"REQ-1\"}");
        ValidationResult validationResult = new ValidationResult(
                true,
                "field.contract.ok",
                "VALID",
                "field_generation",
                "llm_trace",
                null
        );

        FieldGenerationResult result =
                new FieldGenerationResult(fieldDefinition, fieldValue, validationResult);

        Console.log("____fieldGenerationResult.fieldDefinition.name____",
                result.getFieldDefinition().getName());
        Console.log("____fieldGenerationResult.fieldValue.fieldName____",
                result.getFieldValue().getFieldName());
        Console.log("____fieldGenerationResult.fieldValue.value____",
                result.getFieldValue().getValue());
        Console.log("____fieldGenerationResult.validationResult____",
                String.valueOf(result.getValidationResult()));

        assertSame(fieldDefinition, result.getFieldDefinition());
        assertSame(fieldValue, result.getFieldValue());
        assertSame(validationResult, result.getValidationResult());
    }

    @Test
    public void assemblyResult_shouldExposeResultJson() {
        JsonObject resultJson = new JsonObject();
        resultJson.addProperty("ok", "true");
        resultJson.addProperty("code", "validation.ok");

        AssemblyResult assemblyResult = new AssemblyResult(resultJson);

        Console.log("____assemblyResult.result____", assemblyResult.getResult().toString());

        assertSame(resultJson, assemblyResult.getResult());
        assertEquals("true", assemblyResult.getResult().get("ok").getAsString());
        assertEquals("validation.ok", assemblyResult.getResult().get("code").getAsString());
    }

    @Test
    public void queryGenOutput_shouldExposePayloadAndValidationResult() {
        JsonObject payload = new JsonObject();

        JsonObject result = new JsonObject();
        result.addProperty("ok", "true");
        result.addProperty("message", "VALID");
        payload.add("result", result);

        ValidationResult validationResult = new ValidationResult(
                true,
                "querygen.output.ok",
                "VALID",
                "query_generation",
                null,
                null
        );

        QueryGenOutput output = new QueryGenOutput(payload, validationResult);

        Console.log("____queryGenOutput.payload____", output.getPayload().toString());
        Console.log("____queryGenOutput.validationResult____", String.valueOf(output.getValidationResult()));

        assertSame(payload, output.getPayload());
        assertSame(validationResult, output.getValidationResult());
        assertTrue(output.getPayload().has("result"));
        assertEquals("true", output.getPayload().getAsJsonObject("result").get("ok").getAsString());
    }

    @Test
    public void fieldSet_shouldExposeFields() {
        FieldDefinition field1 = new FieldDefinition("response_contract");
        FieldDefinition field2 = new FieldDefinition("llm_instructions");
        FieldDefinition field3 = new FieldDefinition("llm_trace");

        List<FieldDefinition> fields = Arrays.asList(field1, field2, field3);

        FieldSet fieldSet = new FieldSet(fields);

        Console.log("____fieldSet.size____", String.valueOf(fieldSet.getFields().size()));
        Console.log("____fieldSet.field0____", fieldSet.getFields().get(0).getName());
        Console.log("____fieldSet.field1____", fieldSet.getFields().get(1).getName());
        Console.log("____fieldSet.field2____", fieldSet.getFields().get(2).getName());

        assertSame(fields, fieldSet.getFields());
        assertEquals(3, fieldSet.getFields().size());
        assertEquals("response_contract", fieldSet.getFields().get(0).getName());
        assertEquals("llm_instructions", fieldSet.getFields().get(1).getName());
        assertEquals("llm_trace", fieldSet.getFields().get(2).getName());
    }
}
