package ai.braineous.rag.prompt.cgo.query;

import ai.braineous.rag.prompt.cgo.api.*;
import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class QueryRequestToJsonTest {

    @Test
    void toJson_emits_canonical_shape_and_taskType() {

        // Arrange
        Meta meta = new Meta("v1", "validate_fact", "basic validate task");
        GraphContext ctx = new GraphContext(Map.of());
        ValidateTask task = new ValidateTask("validate this fact", "Flight:F100");

        QueryRequest<ValidateTask> req = new QueryRequest<>(meta, ctx, task);

        // Act
        JsonObject json = req.toJson();
        Console.log("UT:QueryRequest.toJson", json);

        // Assert (top-level keys exist)
        assertNotNull(json);
        assertTrue(json.has("meta"));
        assertTrue(json.has("context"));
        assertTrue(json.has("task"));
        assertTrue(json.has("taskType"));
        assertTrue(json.has("rule"));
        assertTrue(json.has("adapter"));

        // Assert taskType is exact class name
        assertEquals(ValidateTask.class.getName(), json.get("taskType").getAsString());

        // Assert rule/adapter are explicitly null (shape stability)
        assertTrue(json.get("rule").isJsonNull());
        assertTrue(json.get("adapter").isJsonNull());

        // Assert nested meta fields
        JsonObject metaJson = json.getAsJsonObject("meta");
        assertEquals("v1", metaJson.get("version").getAsString());
        assertEquals("validate_fact", metaJson.get("queryKind").getAsString());
        assertEquals("basic validate task", metaJson.get("description").getAsString());

        // Assert nested task fields
        JsonObject taskJson = json.getAsJsonObject("task");
        assertEquals("validate this fact", taskJson.get("description").getAsString());
        assertEquals("Flight:F100", taskJson.get("factId").getAsString());

        // Assert context is present (nodes object exists)
        JsonObject ctxJson = json.getAsJsonObject("context");
        assertTrue(ctxJson.has("nodes"));
        assertTrue(ctxJson.get("nodes").isJsonObject());
    }

    @Test
    void fromJson_rehydrates_task_meta_context_and_leaves_rule_adapter_null() {

        // Arrange
        Meta meta = new Meta("v1", "validate_fact", "rehydrate test");
        GraphContext ctx = new GraphContext(java.util.Map.of());
        ValidateTask task = new ValidateTask("validate this fact", "Flight:F100");

        QueryRequest<ValidateTask> original = new QueryRequest<>(meta, ctx, task);
        JsonObject json = original.toJson();
        Console.log("UT:QueryRequest.fromJson:input", json);

        // Act
        QueryRequest<?> rehydrated = QueryRequest.fromJson(json);
        Console.log("UT:QueryRequest.fromJson:rehydrated", rehydrated.toJson());

        // Assert
        org.junit.jupiter.api.Assertions.assertNotNull(rehydrated);

        // meta
        org.junit.jupiter.api.Assertions.assertEquals("v1", rehydrated.getMeta().getVersion());
        org.junit.jupiter.api.Assertions.assertEquals("validate_fact", rehydrated.getMeta().getQueryKind());
        org.junit.jupiter.api.Assertions.assertEquals("rehydrate test", rehydrated.getMeta().getDescription());

        // context
        org.junit.jupiter.api.Assertions.assertEquals(0, rehydrated.getContext().getNodes().size());

        // task
        org.junit.jupiter.api.Assertions.assertTrue(rehydrated.getTask() instanceof ValidateTask);
        ValidateTask rt = (ValidateTask) rehydrated.getTask();
        org.junit.jupiter.api.Assertions.assertEquals("validate this fact", rt.getDescription());
        org.junit.jupiter.api.Assertions.assertEquals("Flight:F100", rt.getFactId());

        // identity-only fields not rehydrated
        org.junit.jupiter.api.Assertions.assertNull(rehydrated.getRule());
        org.junit.jupiter.api.Assertions.assertNull(rehydrated.getAdapter());
    }

    @Test
    void fromJson_throws_when_taskType_is_missing() {

        // Arrange
        Meta meta = new Meta("v1", "validate_fact", "missing taskType");
        GraphContext ctx = new GraphContext(java.util.Map.of());
        ValidateTask task = new ValidateTask("validate this fact", "Flight:F100");

        QueryRequest<ValidateTask> original = new QueryRequest<>(meta, ctx, task);
        JsonObject json = original.toJson();

        // remove taskType to simulate bad payload
        json.remove("taskType");
        Console.log("UT:QueryRequest.fromJson:missing_taskType", json);

        // Act + Assert
        org.junit.jupiter.api.Assertions.assertThrows(
                Exception.class,
                () -> QueryRequest.fromJson(json)
        );
    }

    @Test
    void fromJson_throws_when_taskType_is_invalid_class() {

        // Arrange
        Meta meta = new Meta("v1", "validate_fact", "invalid taskType");
        GraphContext ctx = new GraphContext(java.util.Map.of());
        ValidateTask task = new ValidateTask("validate this fact", "Flight:F100");

        QueryRequest<ValidateTask> original = new QueryRequest<>(meta, ctx, task);
        JsonObject json = original.toJson();

        // corrupt taskType to a class that won't exist
        json.addProperty("taskType", "com.fake.DoesNotExistTask");
        Console.log("UT:QueryRequest.fromJson:invalid_taskType", json);

        // Act + Assert
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalStateException.class,
                () -> QueryRequest.fromJson(json)
        );
    }

    @Test
    void roundtrip_preserves_context_nodes_with_one_node() {

        // Arrange: one node in context
        Node node = new Node(
                "Flight:F100",
                "{\"flight\":\"F100\",\"from\":\"AUS\"}",
                java.util.List.of("flight", "airport"),
                Node.Mode.ATOMIC
        );

        GraphContext ctx = new GraphContext(java.util.Map.of("Flight:F100", node));
        Meta meta = new Meta("v1", "validate_fact", "ctx node roundtrip");
        ValidateTask task = new ValidateTask("validate this fact", "Flight:F100");

        QueryRequest<ValidateTask> original = new QueryRequest<>(meta, ctx, task);

        // Act
        JsonObject json = original.toJson();
        Console.log("UT:QueryRequest.roundtrip:json", json);

        QueryRequest<?> rehydrated = QueryRequest.fromJson(json);
        Console.log("UT:QueryRequest.roundtrip:rehydrated", rehydrated.toJson());

        // Assert: node preserved
        org.junit.jupiter.api.Assertions.assertNotNull(rehydrated.getContext());
        org.junit.jupiter.api.Assertions.assertEquals(1, rehydrated.getContext().getNodes().size());
        org.junit.jupiter.api.Assertions.assertTrue(rehydrated.getContext().getNodes().containsKey("Flight:F100"));

        Node rt = rehydrated.getContext().getNodes().get("Flight:F100");
        org.junit.jupiter.api.Assertions.assertNotNull(rt);
        org.junit.jupiter.api.Assertions.assertEquals("Flight:F100", rt.getId());
        org.junit.jupiter.api.Assertions.assertEquals("{\"flight\":\"F100\",\"from\":\"AUS\"}", rt.getText());
        org.junit.jupiter.api.Assertions.assertEquals(2, rt.getAttributes().size());
        org.junit.jupiter.api.Assertions.assertEquals(Node.Mode.ATOMIC, rt.getMode());
    }

    @Test
    void graphContext_fromJson_null_returns_empty_context() {

        // Act
        GraphContext ctx = GraphContext.fromJson(null);
        Console.log("UT:GraphContext.fromJson:null", (ctx == null) ? "null" : ctx.toJson());

        // Assert
        org.junit.jupiter.api.Assertions.assertNotNull(ctx);
        org.junit.jupiter.api.Assertions.assertNotNull(ctx.getNodes());
        org.junit.jupiter.api.Assertions.assertEquals(0, ctx.getNodes().size());
    }

    @Test
    void node_fromJson_unknown_mode_defaults_to_relational() {

        // Arrange
        JsonObject json = new JsonObject();
        json.addProperty("id", "Flight:F100");
        json.addProperty("text", "{\"flight\":\"F100\"}");
        json.addProperty("mode", "ALIEN_MODE"); // invalid
        Console.log("UT:Node.fromJson:unknown_mode", json);

        // Act
        Node node = Node.fromJson(json);

        // Assert
        org.junit.jupiter.api.Assertions.assertNotNull(node);
        org.junit.jupiter.api.Assertions.assertEquals("Flight:F100", node.getId());
        org.junit.jupiter.api.Assertions.assertEquals("{\"flight\":\"F100\"}", node.getText());
        org.junit.jupiter.api.Assertions.assertEquals(Node.Mode.RELATIONAL, node.getMode());
    }

    @Test
    void node_toJson_emits_attributes_array_when_empty() {

        // Arrange
        Node node = new Node(
                "Flight:F100",
                "{\"flight\":\"F100\"}",
                java.util.List.of(),          // empty attributes
                Node.Mode.RELATIONAL
        );

        // Act
        JsonObject json = node.toJson();
        Console.log("UT:Node.toJson:empty_attributes", json);

        // Assert
        org.junit.jupiter.api.Assertions.assertTrue(json.has("attributes"));
        org.junit.jupiter.api.Assertions.assertTrue(json.get("attributes").isJsonArray());
        org.junit.jupiter.api.Assertions.assertEquals(0, json.getAsJsonArray("attributes").size());
    }

    @Test
    void validationResult_roundtrip_preserves_fields_and_metadata_primitives() {

        // Arrange
        java.util.Map<String, Object> md = new java.util.HashMap<>();
        md.put("missingField", "items[0].id");
        md.put("count", Integer.valueOf(2));
        md.put("flag", Boolean.TRUE);
        md.put("paths", java.util.List.of("a.b", "c.d"));

        ValidationResult vr = ValidationResult.error(
                "CONTRACT_VIOLATION",
                "missing required fields",
                "LLM_RESPONSE_VALIDATION",
                "result.items[0].id",
                md
        );

        // Act
        JsonObject json = vr.toJson();
        Console.log("UT:ValidationResult.roundtrip:json", json);

        ValidationResult rt = ValidationResult.fromJson(json);
        Console.log("UT:ValidationResult.roundtrip:rt", rt.toJson());

        // Assert core fields
        org.junit.jupiter.api.Assertions.assertFalse(rt.isOk());
        org.junit.jupiter.api.Assertions.assertEquals("CONTRACT_VIOLATION", rt.getCode());
        org.junit.jupiter.api.Assertions.assertEquals("missing required fields", rt.getMessage());
        org.junit.jupiter.api.Assertions.assertEquals("LLM_RESPONSE_VALIDATION", rt.getStage());
        org.junit.jupiter.api.Assertions.assertEquals("result.items[0].id", rt.getAnchorId());

        // Assert metadata exists and has expected keys
        org.junit.jupiter.api.Assertions.assertNotNull(rt.getMetadata());
        org.junit.jupiter.api.Assertions.assertEquals("items[0].id", rt.getMetadata().get("missingField"));

        // numbers come back as Number (implementation keeps getAsNumber())
        Object count = rt.getMetadata().get("count");
        org.junit.jupiter.api.Assertions.assertTrue(count instanceof Number);
        org.junit.jupiter.api.Assertions.assertEquals(2, ((Number) count).intValue());

        org.junit.jupiter.api.Assertions.assertEquals(Boolean.TRUE, rt.getMetadata().get("flag"));

        Object paths = rt.getMetadata().get("paths");
        org.junit.jupiter.api.Assertions.assertTrue(paths instanceof java.util.List);
        java.util.List list = (java.util.List) paths;
        org.junit.jupiter.api.Assertions.assertEquals(2, list.size());
        org.junit.jupiter.api.Assertions.assertEquals("a.b", list.get(0));
        org.junit.jupiter.api.Assertions.assertEquals("c.d", list.get(1));
    }

    @Test
    void queryExecution_computed_surfaces_pick_first_failure_in_order() {

        // Arrange
        Meta meta = new Meta("v1", "validate_fact", "exec surface test");
        GraphContext ctx = new GraphContext(java.util.Map.of());
        ValidateTask task = new ValidateTask("validate this fact", "Flight:F100");
        QueryRequest<ValidateTask> req = new QueryRequest<>(meta, ctx, task);

        ValidationResult promptFail = ValidationResult.error("CONTRACT_VIOLATION", "prompt bad", "PROMPT_CONTRACT", "prompt");
        ValidationResult llmFail = ValidationResult.error("PARSING_FAILED", "llm bad", "LLM_RESPONSE", "raw");
        ValidationResult domainFail = ValidationResult.error("DOMAIN_INVALID", "domain bad", "DOMAIN", "factId");

        QueryExecution<ValidateTask> exec = new QueryExecution<>(
                req,
                null,
                promptFail,
                llmFail,
                domainFail
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
        Console.log("UT:QueryExecution.surfaces", jsonObject.toString());

        // Assert
        org.junit.jupiter.api.Assertions.assertFalse(ok);
        org.junit.jupiter.api.Assertions.assertEquals("prompt_contract", stage);
        org.junit.jupiter.api.Assertions.assertEquals("ERROR", status);
        org.junit.jupiter.api.Assertions.assertNotNull(primary);
        org.junit.jupiter.api.Assertions.assertEquals("CONTRACT_VIOLATION", primary.getCode());
    }

    @Test
    void queryExecution_stage_llm_response_when_prompt_ok_and_llm_fails() {

        // Arrange
        Meta meta = new Meta("v1", "validate_fact", "exec llm stage test");
        GraphContext ctx = new GraphContext(java.util.Map.of());
        ValidateTask task = new ValidateTask("validate this fact", "Flight:F100");
        QueryRequest<ValidateTask> req = new QueryRequest<>(meta, ctx, task);

        ValidationResult promptOk = ValidationResult.ok("PROMPT_CONTRACT");
        ValidationResult llmFail = ValidationResult.error("PARSING_FAILED", "llm bad", "LLM_RESPONSE", "raw");
        ValidationResult domainOk = ValidationResult.ok("DOMAIN");

        QueryExecution<ValidateTask> exec = new QueryExecution<>(
                req,
                "raw-llm-response",
                promptOk,
                llmFail,
                domainOk
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
        Console.log("UT:QueryExecution.stage_llm_response", jsonObject.toString());

        // Assert
        org.junit.jupiter.api.Assertions.assertFalse(ok);
        org.junit.jupiter.api.Assertions.assertEquals("llm_response", stage);
        org.junit.jupiter.api.Assertions.assertEquals("ERROR", status);
        org.junit.jupiter.api.Assertions.assertNotNull(primary);
        org.junit.jupiter.api.Assertions.assertEquals("PARSING_FAILED", primary.getCode());
    }

    @Test
    void queryExecution_stage_domain_when_prompt_and_llm_ok_but_domain_fails() {

        // Arrange
        Meta meta = new Meta("v1", "validate_fact", "exec domain stage test");
        GraphContext ctx = new GraphContext(java.util.Map.of());
        ValidateTask task = new ValidateTask("validate this fact", "Flight:F100");
        QueryRequest<ValidateTask> req = new QueryRequest<>(meta, ctx, task);

        ValidationResult promptOk = ValidationResult.ok("PROMPT_CONTRACT");
        ValidationResult llmOk = ValidationResult.ok("LLM_RESPONSE");
        ValidationResult domainFail = ValidationResult.error(
                "DOMAIN_INVALID",
                "domain bad",
                "DOMAIN",
                "factId"
        );

        QueryExecution<ValidateTask> exec = new QueryExecution<>(
                req,
                "raw-llm-response",
                promptOk,
                llmOk,
                domainFail
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
        Console.log("UT:QueryExecution.stage_domain", jsonObject.toString());

        // Assert
        org.junit.jupiter.api.Assertions.assertFalse(ok);
        org.junit.jupiter.api.Assertions.assertEquals("domain", stage);
        org.junit.jupiter.api.Assertions.assertEquals("ERROR", status);
        org.junit.jupiter.api.Assertions.assertNotNull(primary);
        org.junit.jupiter.api.Assertions.assertEquals("DOMAIN_INVALID", primary.getCode());
    }

    @Test
    void queryExecution_stage_ok_when_all_validations_ok() {

        // Arrange
        Meta meta = new Meta("v1", "validate_fact", "exec all ok test");
        GraphContext ctx = new GraphContext(java.util.Map.of());
        ValidateTask task = new ValidateTask("validate this fact", "Flight:F100");
        QueryRequest<ValidateTask> req = new QueryRequest<>(meta, ctx, task);

        ValidationResult promptOk = ValidationResult.ok("PROMPT_CONTRACT");
        ValidationResult llmOk = ValidationResult.ok("LLM_RESPONSE");
        ValidationResult domainOk = ValidationResult.ok("DOMAIN");

        QueryExecution<ValidateTask> exec = new QueryExecution<>(
                req,
                "raw-llm-response",
                promptOk,
                llmOk,
                domainOk
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
        Console.log("UT:QueryExecution.stage_ok", jsonObject.toString());

        // Assert
        org.junit.jupiter.api.Assertions.assertTrue(ok);
        org.junit.jupiter.api.Assertions.assertEquals("ok", stage);
        org.junit.jupiter.api.Assertions.assertEquals("OK", status);
        org.junit.jupiter.api.Assertions.assertNull(primary);
    }


}

