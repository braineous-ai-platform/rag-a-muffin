package ai.braineous.rag.prompt.cgo.query;

import ai.braineous.rag.prompt.cgo.api.*;
import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

public class QueryExecutionTest {

    @Test
    void queryExecution_json_roundtrip_preserves_request_rawResponse_and_validations() {

        // Arrange
        Meta meta = new Meta("v1", "validate_fact", "exec json roundtrip");
        GraphContext ctx = new GraphContext(java.util.Map.of());
        ValidateTask task = new ValidateTask("validate this fact", "Flight:F100");
        QueryRequest<ValidateTask> req = new QueryRequest<>(meta, ctx, task);

        java.util.Map<String, Object> md = new java.util.HashMap<>();
        md.put("missingField", "result.items[0].id");
        md.put("count", Integer.valueOf(2));
        md.put("flag", Boolean.TRUE);

        ValidationResult promptValidation = ValidationResult.ok("PROMPT_CONTRACT");
        ValidationResult llmValidation = ValidationResult.error(
                "CONTRACT_VIOLATION",
                "llm contract violation",
                "LLM_RESPONSE_VALIDATION",
                "result.items[0].id",
                md
        );
        ValidationResult domainValidation = ValidationResult.ok("DOMAIN");

        QueryExecution<ValidateTask> exec = new QueryExecution<>(
                req,
                "raw-llm-response",
                promptValidation,
                llmValidation,
                domainValidation
        );

        // Act
        JsonObject json = exec.toJson();
        Console.log("UT:QueryExecution.json:original", json.toString());

        QueryExecution<?> rt = QueryExecution.fromJson(json);
        JsonObject rtJson = rt.toJson();
        Console.log("UT:QueryExecution.json:rehydrated", rtJson.toString());

        // Assert: request exists and task type preserved
        org.junit.jupiter.api.Assertions.assertNotNull(rt.getRequest());
        org.junit.jupiter.api.Assertions.assertNotNull(rt.getRequest().getMeta());
        org.junit.jupiter.api.Assertions.assertEquals("v1", rt.getRequest().getMeta().getVersion());
        org.junit.jupiter.api.Assertions.assertEquals("validate_fact", rt.getRequest().getMeta().getQueryKind());

        org.junit.jupiter.api.Assertions.assertTrue(rt.getRequest().getTask() instanceof ValidateTask);
        ValidateTask rtTask = (ValidateTask) rt.getRequest().getTask();
        org.junit.jupiter.api.Assertions.assertEquals("Flight:F100", rtTask.getFactId());

        // Assert: rawResponse preserved
        org.junit.jupiter.api.Assertions.assertEquals("raw-llm-response", rt.getRawResponse());

        // Assert: validations preserved
        org.junit.jupiter.api.Assertions.assertNotNull(rt.getPromptValidation());
        org.junit.jupiter.api.Assertions.assertTrue(rt.getPromptValidation().isOk());

        org.junit.jupiter.api.Assertions.assertNotNull(rt.getLlmResponseValidation());
        org.junit.jupiter.api.Assertions.assertFalse(rt.getLlmResponseValidation().isOk());
        org.junit.jupiter.api.Assertions.assertEquals("CONTRACT_VIOLATION", rt.getLlmResponseValidation().getCode());
        org.junit.jupiter.api.Assertions.assertEquals("LLM_RESPONSE_VALIDATION", rt.getLlmResponseValidation().getStage());
        org.junit.jupiter.api.Assertions.assertEquals("result.items[0].id", rt.getLlmResponseValidation().getAnchorId());

        org.junit.jupiter.api.Assertions.assertNotNull(rt.getDomainValidation());
        org.junit.jupiter.api.Assertions.assertTrue(rt.getDomainValidation().isOk());

        // Assert: derived surfaces remain consistent after rehydrate
        org.junit.jupiter.api.Assertions.assertFalse(rt.isOk());
        org.junit.jupiter.api.Assertions.assertEquals("llm_response", rt.getStage());
        org.junit.jupiter.api.Assertions.assertEquals("ERROR", rt.getStatus());
        org.junit.jupiter.api.Assertions.assertNotNull(rt.getPrimaryValidation());
        org.junit.jupiter.api.Assertions.assertEquals("CONTRACT_VIOLATION", rt.getPrimaryValidation().getCode());

        // Small observability snapshot
        ValidationResult primary = rt.getPrimaryValidation();
        boolean ok = rt.isOk();
        String stage = rt.getStage();
        String status = rt.getStatus();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("ok", ok);
        jsonObject.addProperty("stage", stage);
        jsonObject.addProperty("status", status);
        jsonObject.addProperty("primaryCode", (primary == null) ? "null" : primary.getCode());
        Console.log("UT:QueryExecution.json:surfaces", jsonObject.toString());
    }

    @Test
    void queryExecution_fromJson_null_throws() {

        try {
            QueryExecution.fromJson(null);
            org.junit.jupiter.api.Assertions.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException iae) {

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("exception", iae.getClass().getSimpleName());
            jsonObject.addProperty("message", iae.getMessage());
            Console.log("UT:QueryExecution.fromJson:null_throws", jsonObject.toString());

            org.junit.jupiter.api.Assertions.assertTrue(iae.getMessage().contains("QueryExecution JSON cannot be null"));
        }
    }

    @Test
    void queryExecution_toJson_keeps_shape_with_all_optional_nulls() {

        // Arrange
        Meta meta = new Meta("v1", "validate_fact", "exec null shape test");
        GraphContext ctx = new GraphContext(java.util.Map.of());
        ValidateTask task = new ValidateTask("validate this fact", "Flight:F100");
        QueryRequest<ValidateTask> req = new QueryRequest<>(meta, ctx, task);

        QueryExecution<ValidateTask> exec = new QueryExecution<>(
                req,
                null,
                null,
                null,
                null
        );

        // Act
        JsonObject json = exec.toJson();
        Console.log("UT:QueryExecution.toJson:null_shape", json.toString());

        // Assert: keys exist
        org.junit.jupiter.api.Assertions.assertTrue(json.has("request"));
        org.junit.jupiter.api.Assertions.assertTrue(json.has("rawResponse"));
        org.junit.jupiter.api.Assertions.assertTrue(json.has("promptValidation"));
        org.junit.jupiter.api.Assertions.assertTrue(json.has("llmResponseValidation"));
        org.junit.jupiter.api.Assertions.assertTrue(json.has("domainValidation"));

        // Assert: nulls are explicit
        org.junit.jupiter.api.Assertions.assertFalse(json.get("request").isJsonNull());
        org.junit.jupiter.api.Assertions.assertTrue(json.get("rawResponse").isJsonNull());
        org.junit.jupiter.api.Assertions.assertTrue(json.get("promptValidation").isJsonNull());
        org.junit.jupiter.api.Assertions.assertTrue(json.get("llmResponseValidation").isJsonNull());
        org.junit.jupiter.api.Assertions.assertTrue(json.get("domainValidation").isJsonNull());

        // Assert: derived fields still present + correct
        org.junit.jupiter.api.Assertions.assertTrue(json.has("status"));
        org.junit.jupiter.api.Assertions.assertTrue(json.has("stage"));
        org.junit.jupiter.api.Assertions.assertTrue(json.has("ok"));

        org.junit.jupiter.api.Assertions.assertEquals("OK", json.get("status").getAsString());
        org.junit.jupiter.api.Assertions.assertEquals("ok", json.get("stage").getAsString());
        org.junit.jupiter.api.Assertions.assertTrue(json.get("ok").getAsBoolean());

        // Surface snapshot (pattern)
        boolean ok = exec.isOk();
        String stage = exec.getStage();
        String status = exec.getStatus();
        ValidationResult primary = exec.getPrimaryValidation();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("ok", ok);
        jsonObject.addProperty("stage", stage);
        jsonObject.addProperty("status", status);
        jsonObject.addProperty("primaryCode", (primary == null) ? "null" : primary.getCode());
        Console.log("UT:QueryExecution.toJson:null_shape:surfaces", jsonObject.toString());
    }


    @Test
    void queryExecution_fromJson_ignores_derived_fields_and_recomputes_surfaces() {

        // Arrange
        Meta meta = new Meta("v1", "validate_fact", "derived fields ignored");
        GraphContext ctx = new GraphContext(java.util.Map.of());
        ValidateTask task = new ValidateTask("validate this fact", "Flight:F100");
        QueryRequest<ValidateTask> req = new QueryRequest<>(meta, ctx, task);

        ValidationResult promptOk = ValidationResult.ok("PROMPT_CONTRACT");
        ValidationResult llmFail = ValidationResult.error(
                "PARSING_FAILED",
                "llm bad",
                "LLM_RESPONSE",
                "raw"
        );

        QueryExecution<ValidateTask> exec = new QueryExecution<>(
                req,
                "raw-llm-response",
                promptOk,
                llmFail,
                null
        );

        JsonObject json = exec.toJson();

        // Tamper derived fields (should be ignored)
        json.addProperty("ok", true);
        json.addProperty("status", "OK");
        json.addProperty("stage", "ok");
        Console.log("UT:QueryExecution.derived_tampered:input", json.toString());

        // Act
        QueryExecution<?> rt = QueryExecution.fromJson(json);

        boolean ok = rt.isOk();
        String stage = rt.getStage();
        String status = rt.getStatus();
        ValidationResult primary = rt.getPrimaryValidation();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("ok", ok);
        jsonObject.addProperty("stage", stage);
        jsonObject.addProperty("status", status);
        jsonObject.addProperty("primaryCode", (primary == null) ? "null" : primary.getCode());
        Console.log("UT:QueryExecution.derived_recomputed", jsonObject.toString());

        // Assert: recomputed from validations, not tampered values
        org.junit.jupiter.api.Assertions.assertFalse(ok);
        org.junit.jupiter.api.Assertions.assertEquals("llm_response", stage);
        org.junit.jupiter.api.Assertions.assertEquals("ERROR", status);
        org.junit.jupiter.api.Assertions.assertNotNull(primary);
        org.junit.jupiter.api.Assertions.assertEquals("PARSING_FAILED", primary.getCode());
    }

    @Test
    void queryExecution_fromJson_missing_optional_keys_defaults_to_nulls_and_ok() {

        // Arrange: build minimal JSON with only request
        Meta meta = new Meta("v1", "validate_fact", "minimal exec json");
        GraphContext ctx = new GraphContext(java.util.Map.of());
        ValidateTask task = new ValidateTask("validate this fact", "Flight:F100");
        QueryRequest<ValidateTask> req = new QueryRequest<>(meta, ctx, task);

        JsonObject json = new JsonObject();
        json.add("request", req.toJson());

        // Intentionally omit: rawResponse, promptValidation, llmResponseValidation, domainValidation, ok/status/stage
        Console.log("UT:QueryExecution.fromJson:minimal_input", json.toString());

        // Act
        QueryExecution<?> rt = QueryExecution.fromJson(json);

        boolean ok = rt.isOk();
        String stage = rt.getStage();
        String status = rt.getStatus();
        ValidationResult primary = rt.getPrimaryValidation();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("ok", ok);
        jsonObject.addProperty("stage", stage);
        jsonObject.addProperty("status", status);
        jsonObject.addProperty("primaryCode", (primary == null) ? "null" : primary.getCode());
        Console.log("UT:QueryExecution.fromJson:minimal_surfaces", jsonObject.toString());

        // Assert
        org.junit.jupiter.api.Assertions.assertNotNull(rt.getRequest());
        org.junit.jupiter.api.Assertions.assertNull(rt.getRawResponse());
        org.junit.jupiter.api.Assertions.assertNull(rt.getPromptValidation());
        org.junit.jupiter.api.Assertions.assertNull(rt.getLlmResponseValidation());
        org.junit.jupiter.api.Assertions.assertNull(rt.getDomainValidation());

        org.junit.jupiter.api.Assertions.assertTrue(ok);
        org.junit.jupiter.api.Assertions.assertEquals("ok", stage);
        org.junit.jupiter.api.Assertions.assertEquals("OK", status);
        org.junit.jupiter.api.Assertions.assertNull(primary);
    }

    @Test
    void queryExecution_with_rawResponse_and_no_validations_is_ok() {

        // Arrange
        Meta meta = new Meta("v1", "validate_fact", "raw only exec");
        GraphContext ctx = new GraphContext(java.util.Map.of());
        ValidateTask task = new ValidateTask("validate this fact", "Flight:F100");
        QueryRequest<ValidateTask> req = new QueryRequest<>(meta, ctx, task);

        QueryExecution<ValidateTask> exec = new QueryExecution<>(
                req,
                "some-raw-llm-response",
                null,
                null,
                null
        );

        // Act
        boolean ok = exec.isOk();
        String stage = exec.getStage();
        String status = exec.getStatus();
        ValidationResult primary = exec.getPrimaryValidation();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("ok", ok);
        jsonObject.addProperty("stage", stage);
        jsonObject.addProperty("status", status);
        jsonObject.addProperty("primaryCode", (primary == null) ? "null" : primary.getCode());
        Console.log("UT:QueryExecution.raw_only", jsonObject.toString());

        // Assert
        org.junit.jupiter.api.Assertions.assertTrue(ok);
        org.junit.jupiter.api.Assertions.assertEquals("ok", stage);
        org.junit.jupiter.api.Assertions.assertEquals("OK", status);
        org.junit.jupiter.api.Assertions.assertNull(primary);
    }

    @Test
    void queryExecution_with_prompt_validation_ok_only_is_ok() {

        // Arrange
        Meta meta = new Meta("v1", "validate_fact", "prompt ok only");
        GraphContext ctx = new GraphContext(java.util.Map.of());
        ValidateTask task = new ValidateTask("validate this fact", "Flight:F100");
        QueryRequest<ValidateTask> req = new QueryRequest<>(meta, ctx, task);

        ValidationResult promptOk = ValidationResult.ok("PROMPT_CONTRACT");

        QueryExecution<ValidateTask> exec = new QueryExecution<>(
                req,
                null,
                promptOk,
                null,
                null
        );

        // Act
        boolean ok = exec.isOk();
        String stage = exec.getStage();
        String status = exec.getStatus();
        ValidationResult primary = exec.getPrimaryValidation();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("ok", ok);
        jsonObject.addProperty("stage", stage);
        jsonObject.addProperty("status", status);
        jsonObject.addProperty("primaryCode", (primary == null) ? "null" : primary.getCode());
        Console.log("UT:QueryExecution.prompt_ok_only", jsonObject.toString());

        // Assert
        org.junit.jupiter.api.Assertions.assertTrue(ok);
        org.junit.jupiter.api.Assertions.assertEquals("ok", stage);
        org.junit.jupiter.api.Assertions.assertEquals("OK", status);
        org.junit.jupiter.api.Assertions.assertNull(primary);
    }



}
