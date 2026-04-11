package ai.braineous.rag.prompt.cgo.querygen.services;

import ai.braineous.rag.prompt.cgo.api.ValidationResult;
import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QueryGenValidatorTest {

    @Test
    public void validate_shouldReturnOk_forValidQueryGenPayload() {
        QueryGenValidator validator = new QueryGenValidator();
        JsonObject payload = buildValidPayload();

        ValidationResult result = validator.validate(payload.toString());

        Console.log("____queryGenValidator.valid.payload____", payload.toString());
        Console.log("____queryGenValidator.valid.result____", String.valueOf(result));

        assertNotNull(result);
        assertTrue(result.isOk());
        assertEquals("querygen.contract.ok", result.getCode());
        assertEquals("QueryGen contract is valid", result.getMessage());
        assertEquals("querygen_contract_validation", result.getStage());
        assertNull(result.getAnchorId());
        assertNotNull(result.getMetadata());
        assertEquals("v1", result.getMetadata().get("version"));
        assertEquals("validate_flight_airports", result.getMetadata().get("queryKind"));
    }

    @Test
    public void validate_shouldReturnError_whenRawQueryIsNull() {
        QueryGenValidator validator = new QueryGenValidator();

        ValidationResult result = validator.validate(null);

        Console.log("____queryGenValidator.null.result____", String.valueOf(result));

        assertNotNull(result);
        assertFalse(result.isOk());
        assertEquals("querygen.contract.empty", result.getCode());
        assertEquals("QueryGen JSON is empty", result.getMessage());
        assertEquals("querygen_contract_validation", result.getStage());
    }

    @Test
    public void validate_shouldReturnError_whenRootIsNotObject() {
        QueryGenValidator validator = new QueryGenValidator();

        ValidationResult result = validator.validate("[1,2,3]");

        Console.log("____queryGenValidator.rootNotObject.result____", String.valueOf(result));

        assertNotNull(result);
        assertFalse(result.isOk());
        assertEquals("querygen.contract.root_not_object", result.getCode());
        assertEquals("Expected JSON object at root", result.getMessage());
        assertEquals("querygen_contract_validation", result.getStage());
    }

    @Test
    public void validate_shouldReturnError_whenMetaIsMissing() {
        QueryGenValidator validator = new QueryGenValidator();
        JsonObject payload = buildValidPayload();
        payload.remove("meta");

        ValidationResult result = validator.validate(payload.toString());

        Console.log("____queryGenValidator.metaMissing.payload____", payload.toString());
        Console.log("____queryGenValidator.metaMissing.result____", String.valueOf(result));

        assertNotNull(result);
        assertFalse(result.isOk());
        assertEquals("querygen.contract.meta_missing_or_invalid", result.getCode());
        assertEquals("Missing or invalid 'meta' object", result.getMessage());
        assertEquals("querygen_contract_validation", result.getStage());
    }

    @Test
    public void validate_shouldReturnError_whenMetaQueryKindIsMissing() {
        QueryGenValidator validator = new QueryGenValidator();
        JsonObject payload = buildValidPayload();
        payload.getAsJsonObject("meta").remove("queryKind");

        ValidationResult result = validator.validate(payload.toString());

        Console.log("____queryGenValidator.metaQueryKindMissing.payload____", payload.toString());
        Console.log("____queryGenValidator.metaQueryKindMissing.result____", String.valueOf(result));

        assertNotNull(result);
        assertFalse(result.isOk());
        assertEquals("querygen.contract.meta.queryKind_missing", result.getCode());
        assertEquals("Missing or invalid 'meta.queryKind'", result.getMessage());
        assertEquals("querygen_contract_validation", result.getStage());
    }

    @Test
    public void validate_shouldReturnError_whenContextNodesIsMissing() {
        QueryGenValidator validator = new QueryGenValidator();
        JsonObject payload = buildValidPayload();
        payload.getAsJsonObject("context").remove("nodes");

        ValidationResult result = validator.validate(payload.toString());

        Console.log("____queryGenValidator.contextNodesMissing.payload____", payload.toString());
        Console.log("____queryGenValidator.contextNodesMissing.result____", String.valueOf(result));

        assertNotNull(result);
        assertFalse(result.isOk());
        assertEquals("querygen.contract.context.nodes_missing_or_invalid", result.getCode());
        assertEquals("Missing or invalid 'context.nodes' object", result.getMessage());
        assertEquals("querygen_contract_validation", result.getStage());
    }

    @Test
    public void validate_shouldReturnError_whenTaskIntentIsMissing() {
        QueryGenValidator validator = new QueryGenValidator();
        JsonObject payload = buildValidPayload();
        payload.getAsJsonObject("task").remove("intent");

        ValidationResult result = validator.validate(payload.toString());

        Console.log("____queryGenValidator.taskIntentMissing.payload____", payload.toString());
        Console.log("____queryGenValidator.taskIntentMissing.result____", String.valueOf(result));

        assertNotNull(result);
        assertFalse(result.isOk());
        assertEquals("querygen.contract.task.intent_missing_or_invalid", result.getCode());
        assertEquals("Missing or invalid 'task.intent' object", result.getMessage());
        assertEquals("querygen_contract_validation", result.getStage());
    }

    @Test
    public void validate_shouldReturnError_whenTaskIntentGoalIsMissing() {
        QueryGenValidator validator = new QueryGenValidator();
        JsonObject payload = buildValidPayload();
        payload.getAsJsonObject("task").getAsJsonObject("intent").remove("goal");

        ValidationResult result = validator.validate(payload.toString());

        Console.log("____queryGenValidator.taskIntentGoalMissing.payload____", payload.toString());
        Console.log("____queryGenValidator.taskIntentGoalMissing.result____", String.valueOf(result));

        assertNotNull(result);
        assertFalse(result.isOk());
        assertEquals("querygen.contract.task.intent.goal_missing", result.getCode());
        assertEquals("Missing or invalid 'task.intent.goal'", result.getMessage());
        assertEquals("querygen_contract_validation", result.getStage());
    }

    @Test
    public void validate_shouldReturnError_whenSelectContainsNonString() {
        QueryGenValidator validator = new QueryGenValidator();
        JsonObject payload = buildValidPayload();
        JsonArray select = payload.getAsJsonObject("task").getAsJsonArray("select");
        select.add(123);

        ValidationResult result = validator.validate(payload.toString());

        Console.log("____queryGenValidator.selectInvalid.payload____", payload.toString());
        Console.log("____queryGenValidator.selectInvalid.result____", String.valueOf(result));

        assertNotNull(result);
        assertFalse(result.isOk());
        assertEquals("querygen.contract.task.select_not_all_strings", result.getCode());
        assertEquals("'task.select' must contain only strings or nulls", result.getMessage());
        assertEquals("querygen_contract_validation", result.getStage());
    }

    @Test
    public void validate_shouldReturnError_whenOutputTemplateIsMissing() {
        QueryGenValidator validator = new QueryGenValidator();
        JsonObject payload = buildValidPayload();
        payload.remove("output_template");

        ValidationResult result = validator.validate(payload.toString());

        Console.log("____queryGenValidator.outputTemplateMissing.payload____", payload.toString());
        Console.log("____queryGenValidator.outputTemplateMissing.result____", String.valueOf(result));

        assertNotNull(result);
        assertFalse(result.isOk());
        assertEquals("querygen.contract.output_template_missing_or_invalid", result.getCode());
        assertEquals("Missing or invalid 'output_template' object", result.getMessage());
        assertEquals("querygen_contract_validation", result.getStage());
    }

    @Test
    public void validate_shouldReturnError_whenResponseContractIsMissing() {
        QueryGenValidator validator = new QueryGenValidator();
        JsonObject payload = buildValidPayload();
        payload.remove("response_contract");

        ValidationResult result = validator.validate(payload.toString());

        Console.log("____queryGenValidator.responseContractMissing.payload____", payload.toString());
        Console.log("____queryGenValidator.responseContractMissing.result____", String.valueOf(result));

        assertNotNull(result);
        assertFalse(result.isOk());
        assertEquals("querygen.contract.response_contract_missing_or_invalid", result.getCode());
        assertEquals("Missing or invalid 'response_contract' object", result.getMessage());
        assertEquals("querygen_contract_validation", result.getStage());
    }

    @Test
    public void validate_shouldReturnError_whenLlmInstructionsIsMissing() {
        QueryGenValidator validator = new QueryGenValidator();
        JsonObject payload = buildValidPayload();
        payload.remove("llm_instructions");

        ValidationResult result = validator.validate(payload.toString());

        Console.log("____queryGenValidator.llmInstructionsMissing.payload____", payload.toString());
        Console.log("____queryGenValidator.llmInstructionsMissing.result____", String.valueOf(result));

        assertNotNull(result);
        assertFalse(result.isOk());
        assertEquals("querygen.contract.llm_instructions_missing_or_invalid", result.getCode());
        assertEquals("Missing or invalid 'llm_instructions' object", result.getMessage());
        assertEquals("querygen_contract_validation", result.getStage());
    }

    @Test
    public void validate_shouldReturnError_whenLlmInstructionsArrayContainsNonString() {
        QueryGenValidator validator = new QueryGenValidator();
        JsonObject payload = buildValidPayload();
        JsonArray instructions = payload.getAsJsonObject("llm_instructions").getAsJsonArray("instructions");
        instructions.add(true);

        ValidationResult result = validator.validate(payload.toString());

        Console.log("____queryGenValidator.llmInstructionsInvalid.payload____", payload.toString());
        Console.log("____queryGenValidator.llmInstructionsInvalid.result____", String.valueOf(result));

        assertNotNull(result);
        assertFalse(result.isOk());
        assertEquals("querygen.contract.llm_instructions.instructions_not_all_strings", result.getCode());
        assertEquals("'llm_instructions.instructions' must be an array of strings", result.getMessage());
        assertEquals("querygen_contract_validation", result.getStage());
    }

    @Test
    public void validate_shouldReturnOk_whenOptionalTraceFieldsAreAbsent() {
        QueryGenValidator validator = new QueryGenValidator();
        JsonObject payload = buildValidPayload();
        payload.remove("llm_trace");
        payload.remove("llm_trace_instructions");

        ValidationResult result = validator.validate(payload.toString());

        Console.log("____queryGenValidator.optionalTraceAbsent.payload____", payload.toString());
        Console.log("____queryGenValidator.optionalTraceAbsent.result____", String.valueOf(result));

        assertNotNull(result);
        assertTrue(result.isOk());
        assertEquals("querygen.contract.ok", result.getCode());
        assertEquals("querygen_contract_validation", result.getStage());
    }

    @Test
    public void validate_shouldReturnError_whenJsonIsMalformed() {
        QueryGenValidator validator = new QueryGenValidator();

        ValidationResult result = validator.validate("{\"meta\": ");

        Console.log("____queryGenValidator.invalidJson.result____", String.valueOf(result));

        assertNotNull(result);
        assertFalse(result.isOk());
        assertEquals("querygen.contract.invalid_json", result.getCode());
        assertTrue(result.getMessage().startsWith("Failed to parse query as JSON:"));
        assertEquals("querygen_contract_validation", result.getStage());
    }

    private JsonObject buildValidPayload() {
        JsonObject root = new JsonObject();

        JsonObject meta = new JsonObject();
        meta.addProperty("version", "v1");
        meta.addProperty("queryKind", "validate_flight_airports");
        meta.addProperty("description", "Validate departure and arrival airport codes");
        root.add("meta", meta);

        JsonObject context = new JsonObject();
        JsonObject nodes = new JsonObject();
        JsonObject flightNode = new JsonObject();
        flightNode.addProperty("id", "Flight:F100");
        flightNode.addProperty("text", "{\"id\":\"F100\",\"kind\":\"Flight\",\"mode\":\"relational\",\"from\":\"AUS\",\"to\":\"DFW\"}");
        flightNode.add("attributes", new JsonArray());
        flightNode.addProperty("mode", "RELATIONAL");
        nodes.add("Flight:F100", flightNode);
        context.add("nodes", nodes);
        root.add("context", context);

        JsonObject task = new JsonObject();
        JsonObject intent = new JsonObject();
        intent.addProperty("goal", "Validate departure and arrival airport codes");
        task.add("intent", intent);
        task.addProperty("factId", "Flight:F100");

        JsonArray select = new JsonArray();
        select.add("ok");
        select.add("code");
        select.add("message");
        select.add("anchorId");
        task.add("select", select);

        JsonArray relatedFactIds = new JsonArray();
        relatedFactIds.add("Airport:AUS");
        relatedFactIds.add("Airport:DFW");
        task.add("relatedFactIds", relatedFactIds);

        root.add("task", task);

        JsonObject outputTemplate = new JsonObject();
        JsonObject outputTemplateResult = new JsonObject();
        outputTemplateResult.addProperty("ok", "");
        outputTemplateResult.addProperty("code", "");
        outputTemplateResult.addProperty("message", "");
        outputTemplateResult.addProperty("anchorId", "");
        outputTemplate.add("result", outputTemplateResult);
        root.add("output_template", outputTemplate);

        JsonObject responseContract = new JsonObject();
        responseContract.addProperty("type", "validation_result");
        responseContract.addProperty("description", "Deterministic response contract derived from selected fields.");

        JsonObject schema = new JsonObject();
        JsonObject result = new JsonObject();
        JsonObject fields = new JsonObject();
        fields.addProperty("ok", "string");
        fields.addProperty("code", "string");
        fields.addProperty("message", "string");
        fields.addProperty("anchorId", "string");
        result.add("fields", fields);
        schema.add("result", result);
        responseContract.add("schema", schema);
        root.add("response_contract", responseContract);

        JsonObject llmInstructions = new JsonObject();
        JsonArray instructions = new JsonArray();
        instructions.add("Return ONLY the output_template with values filled.");
        instructions.add("Use runtime_result as truth.");
        instructions.add("Do NOT recompute validation from context.");
        instructions.add("Do not evaluate constraints from scratch.");
        instructions.add("Return compact JSON on a single line.");
        instructions.add("Do not include spaces, tabs, or newlines outside JSON syntax.");
        instructions.add("Set every value as a string.");
        instructions.add("Return exactly the output_template shape.");
        instructions.add("Do not add, remove, or rename any fields.");
        instructions.add("Return exactly one JSON object.");
        instructions.add("Do not wrap the JSON in markdown fences.");
        instructions.add("Do not include explanation before or after the JSON.");
        instructions.add("Set result.ok from runtime_result.ok.");
        instructions.add("Set result.code from runtime_result.code.");
        instructions.add("Set result.message from runtime_result.message.");
        instructions.add("Set result.anchorId from runtime_result.anchorId.");
        llmInstructions.add("instructions", instructions);
        root.add("llm_instructions", llmInstructions);

        JsonObject llmTrace = new JsonObject();
        llmTrace.addProperty("enabled", "false");
        root.add("llm_trace", llmTrace);

        JsonObject llmTraceInstructions = new JsonObject();
        JsonArray traceInstructions = new JsonArray();
        traceInstructions.add("Do not include chain of thought.");
        llmTraceInstructions.add("instructions", traceInstructions);
        root.add("llm_trace_instructions", llmTraceInstructions);

        return root;
    }
}