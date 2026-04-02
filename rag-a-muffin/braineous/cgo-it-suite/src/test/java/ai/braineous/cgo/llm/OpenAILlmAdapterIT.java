package ai.braineous.cgo.llm;

import ai.braineous.rag.prompt.cgo.api.Meta;
import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class OpenAILlmAdapterIT {

    @Test
    void invokeLlm_returns_body_on_2xx_response() {
        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setMeta(new Meta("v1", "reroute_passengers", "test"));

        String expectedRequestId = queryRequest.generateRequestId();

        JsonObject prompt = new JsonObject();
        prompt.addProperty("model", "llama3");
        prompt.addProperty("prompt", "Why is the sky blue?");
        prompt.addProperty("stream", false);

        OpenAILlmAdapter adapter = new OpenAILlmAdapter();
        String response = adapter.invokeLlm(queryRequest, prompt);

        JsonObject root = JsonParser.parseString(response).getAsJsonObject();

        Assertions.assertTrue(root.has("llmRequest"));
        Assertions.assertTrue(root.has("rawResponse"));
        Assertions.assertTrue(root.has("success"));

        JsonObject llmRequest = root.getAsJsonObject("llmRequest");

        Assertions.assertEquals(expectedRequestId, llmRequest.get("requestId").getAsString());
        Assertions.assertEquals("reroute_passengers", llmRequest.get("queryKind").getAsString());
        Assertions.assertTrue(root.get("success").getAsBoolean());
        Assertions.assertFalse(root.get("rawResponse").isJsonNull());
    }

    @Test
    void invokeLlm_deterministic_payload_executor_mode_should_return_json_only() {

        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setMeta(new Meta("v1", "validate_flight_airports", "test"));

        String expectedRequestId = queryRequest.generateRequestId();

        String prompt =
                "You are a task execution engine.\n\n" +
                        "Return only JSON.\n" +
                        "Do not explain anything.\n" +
                        "Do not describe the input.\n" +
                        "Do not summarize the task.\n" +
                        "Use the provided output_template as the final answer format.\n" +
                        "Replace only the empty string values in output_template.\n" +
                        "Do not return task, context, or instructions.\n" +
                        "Return exactly one JSON object.\n" +
                        "No text before or after JSON.\n\n" +
                        "INPUT:\n" +
                        "{\"task\":{\"description\":\"Validate that the selected flight has valid departure and arrival airport codes based on the airport nodes in the graph. A valid flight must have: (1) 'from' matching one Airport:* code, (2) 'to' matching one Airport:* code, (3) 'from' != 'to'.\",\"factId\":\"Flight:F100\",\"relatedFactIds\":[\"Airport:AUS\",\"Airport:DFW\"]},\"context\":{\"nodes\":{\"Flight:F100\":{\"id\":\"Flight:F100\",\"text\":\"{\\\"id\\\":\\\"F100\\\",\\\"kind\\\":\\\"Flight\\\",\\\"mode\\\":\\\"relational\\\",\\\"from\\\":\\\"AUS\\\",\\\"to\\\":\\\"DFW\\\"}\",\"attributes\":[],\"mode\":\"RELATIONAL\"},\"Airport:AUS\":{\"id\":\"Airport:AUS\",\"text\":\"{\\\"id\\\":\\\"AUS\\\",\\\"kind\\\":\\\"Airport\\\",\\\"code\\\":\\\"AUS\\\"}\",\"attributes\":[],\"mode\":\"ATOMIC\"},\"Airport:DFW\":{\"id\":\"Airport:DFW\",\"text\":\"{\\\"id\\\":\\\"DFW\\\",\\\"kind\\\":\\\"Airport\\\",\\\"code\\\":\\\"DFW\\\"}\",\"attributes\":[],\"mode\":\"ATOMIC\"}}},\"output_template\":{\"result\":{\"ok\":\"\",\"code\":\"\",\"message\":\"\",\"anchorId\":\"\"}},\"instructions\":[\"The output_template below is the final answer format.\",\"Return ONLY the output_template with values filled.\",\"Do NOT return task, context, or instructions.\",\"Do NOT describe or explain anything.\",\"Replace empty string values in output_template.\",\"Return exactly one JSON object.\",\"No text before or after JSON.\"]}";

        JsonObject llmPayload = new JsonObject();
        llmPayload.addProperty("model", "llama3");
        llmPayload.addProperty("prompt", prompt);
        llmPayload.addProperty("stream", false);

        OpenAILlmAdapter adapter = new OpenAILlmAdapter();
        String response = adapter.invokeLlm(queryRequest, llmPayload);

        JsonObject root = JsonParser.parseString(response).getAsJsonObject();

        Assertions.assertTrue(root.has("llmRequest"));
        Assertions.assertTrue(root.has("rawResponse"));
        Assertions.assertTrue(root.has("success"));

        JsonObject llmRequest = root.getAsJsonObject("llmRequest");

        Assertions.assertEquals(expectedRequestId, llmRequest.get("requestId").getAsString());
        Assertions.assertEquals("validate_flight_airports", llmRequest.get("queryKind").getAsString());
        Assertions.assertTrue(root.get("success").getAsBoolean());
        Assertions.assertFalse(root.get("rawResponse").isJsonNull());

        // 🔥 Core assertion: executor mode → JSON only
        String modelOutput = root.getAsJsonObject("rawResponse").get("response").getAsString().trim();

        Assertions.assertTrue(modelOutput.startsWith("{"), "Expected JSON output, got: " + modelOutput);
    }
    //@Test
    void invokeLlm_executor_mode_should_return_filled_result_object() {

        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setMeta(new Meta("v1", "validate_flight_airports", "test"));

        String expectedRequestId = queryRequest.generateRequestId();

        String prompt =
                "You are a task execution engine.\n\n" +
                        "Return only JSON.\n" +
                        "Do not explain anything.\n" +
                        "Do not describe the input.\n" +
                        "Do not summarize the task.\n" +
                        "Use the provided output_template as the final answer format.\n" +
                        "Replace every empty value in output_template if the value can be determined from the input.\n" +
                        "Return the completed result object.\n" +
                        "Do not return task, context, or instructions.\n" +
                        "Return exactly one JSON object.\n" +
                        "No text before or after JSON.\n\n" +
                        "INPUT:\n" +
                        "{\"task\":{\"description\":\"Validate that the selected flight has valid departure and arrival airport codes based on the airport nodes in the graph. A valid flight must have: (1) from matching one Airport:* code, (2) to matching one Airport:* code, (3) from != to.\",\"factId\":\"Flight:F100\",\"relatedFactIds\":[\"Airport:AUS\",\"Airport:DFW\"]},\"context\":{\"nodes\":{\"Flight:F100\":{\"id\":\"Flight:F100\",\"text\":\"{\\\"id\\\":\\\"F100\\\",\\\"kind\\\":\\\"Flight\\\",\\\"mode\\\":\\\"relational\\\",\\\"from\\\":\\\"AUS\\\",\\\"to\\\":\\\"DFW\\\"}\",\"attributes\":[],\"mode\":\"RELATIONAL\"},\"Airport:AUS\":{\"id\":\"Airport:AUS\",\"text\":\"{\\\"id\\\":\\\"AUS\\\",\\\"kind\\\":\\\"Airport\\\",\\\"code\\\":\\\"AUS\\\"}\",\"attributes\":[],\"mode\":\"ATOMIC\"},\"Airport:DFW\":{\"id\":\"Airport:DFW\",\"text\":\"{\\\"id\\\":\\\"DFW\\\",\\\"kind\\\":\\\"Airport\\\",\\\"code\\\":\\\"DFW\\\"}\",\"attributes\":[],\"mode\":\"ATOMIC\"}}},\"output_template\":{\"result\":{\"ok\":\"\",\"code\":\"\",\"message\":\"\",\"anchorId\":\"\"}},\"instructions\":[\"Return ONLY the output_template with values filled.\",\"Do NOT return task, context, or instructions.\",\"Do NOT describe or explain anything.\",\"Replace all empty values in output_template.\",\"Return exactly one JSON object.\",\"No text before or after JSON.\"]}";

        JsonObject llmPayload = new JsonObject();
        llmPayload.addProperty("model", "llama3");
        llmPayload.addProperty("prompt", prompt);
        llmPayload.addProperty("stream", false);

        OpenAILlmAdapter adapter = new OpenAILlmAdapter();
        String response = adapter.invokeLlm(queryRequest, llmPayload);

        JsonObject root = JsonParser.parseString(response).getAsJsonObject();

        Assertions.assertTrue(root.has("llmRequest"));
        Assertions.assertTrue(root.has("rawResponse"));
        Assertions.assertTrue(root.has("success"));

        JsonObject llmRequest = root.getAsJsonObject("llmRequest");

        Assertions.assertEquals(expectedRequestId, llmRequest.get("requestId").getAsString());
        Assertions.assertEquals("validate_flight_airports", llmRequest.get("queryKind").getAsString());
        Assertions.assertTrue(root.get("success").getAsBoolean());
        Assertions.assertFalse(root.get("rawResponse").isJsonNull());

        String modelOutput = root.getAsJsonObject("rawResponse").get("response").getAsString().trim();

        Assertions.assertTrue(modelOutput.startsWith("{"), "Expected JSON output, got: " + modelOutput);

        JsonObject output = JsonParser.parseString(modelOutput).getAsJsonObject();
        Assertions.assertTrue(output.has("result"));

        JsonObject result = output.getAsJsonObject("result");

        Assertions.assertTrue(result.has("ok"));
        Assertions.assertTrue(result.has("code"));
        Assertions.assertTrue(result.has("message"));
        Assertions.assertTrue(result.has("anchorId"));

        Assertions.assertFalse(result.get("code").getAsString().trim().isEmpty());
        Assertions.assertFalse(result.get("message").getAsString().trim().isEmpty());
        Assertions.assertEquals("Flight:F100", result.get("anchorId").getAsString());
    }

    //----------baseline-----------------------------------------------
    @Test
    void invokeLlm_executor_mode_with_response_contract_should_synthesize_result_values() {

        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setMeta(new Meta("v1", "validate_flight_airports", "test"));

        String expectedRequestId = queryRequest.generateRequestId();

        String prompt =
                "You are a task execution engine.\n\n" +
                        "Return only JSON.\n" +
                        "Do not explain anything.\n" +
                        "Do not describe the input.\n" +
                        "Do not summarize the task.\n" +
                        "Use the provided output_template as the final answer format.\n" +
                        "Replace every empty value in output_template if the value can be determined from the input.\n" +
                        "Synthesize result values from the task and context.\n" +
                        "Do not copy the task description into result.message.\n" +
                        "Set result.message to one short outcome sentence.\n" +
                        "Set result.code to one short outcome code.\n" +
                        "Set result.anchorId to task.factId.\n" +
                        "Set every value in output_template as a string.\n" +
                        "Do not return booleans, numbers, or null.\n" +
                        "Return \"true\" or \"false\" as strings, not boolean values.\n" +
                        "Return the completed result object.\n" +
                        "Do not return task, context, response_contract, or instructions.\n" +
                        "Return exactly one JSON object.\n" +
                        "No text before or after JSON.\n\n" +
                        "INPUT:\n" +
                        "{" +
                        "\"task\":{" +
                        "\"description\":\"Validate that the selected flight has valid departure and arrival airport codes based on the airport nodes in the graph. A valid flight must have: (1) from matching one Airport:* code, (2) to matching one Airport:* code, (3) from != to.\"," +
                        "\"factId\":\"Flight:F100\"," +
                        "\"relatedFactIds\":[\"Airport:AUS\",\"Airport:DFW\"]" +
                        "}," +
                        "\"context\":{" +
                        "\"nodes\":{" +
                        "\"Flight:F100\":{" +
                        "\"id\":\"Flight:F100\"," +
                        "\"text\":\"{\\\"id\\\":\\\"F100\\\",\\\"kind\\\":\\\"Flight\\\",\\\"mode\\\":\\\"relational\\\",\\\"from\\\":\\\"AUS\\\",\\\"to\\\":\\\"DFW\\\"}\"," +
                        "\"attributes\":[]," +
                        "\"mode\":\"RELATIONAL\"" +
                        "}," +
                        "\"Airport:AUS\":{" +
                        "\"id\":\"Airport:AUS\"," +
                        "\"text\":\"{\\\"id\\\":\\\"AUS\\\",\\\"kind\\\":\\\"Airport\\\",\\\"code\\\":\\\"AUS\\\"}\"," +
                        "\"attributes\":[]," +
                        "\"mode\":\"ATOMIC\"" +
                        "}," +
                        "\"Airport:DFW\":{" +
                        "\"id\":\"Airport:DFW\"," +
                        "\"text\":\"{\\\"id\\\":\\\"DFW\\\",\\\"kind\\\":\\\"Airport\\\",\\\"code\\\":\\\"DFW\\\"}\"," +
                        "\"attributes\":[]," +
                        "\"mode\":\"ATOMIC\"" +
                        "}" +
                        "}" +
                        "}," +
                        "\"response_contract\":{" +
                        "\"type\":\"validation_result\"," +
                        "\"description\":\"Deterministic response contract derived from selected fields.\"," +
                        "\"schema\":{" +
                        "\"result\":{" +
                        "\"fields\":{" +
                        "\"ok\":\"string\"," +
                        "\"code\":\"string\"," +
                        "\"message\":\"string\"," +
                        "\"anchorId\":\"string\"" +
                        "}" +
                        "}" +
                        "}" +
                        "}," +
                        "\"output_template\":{" +
                        "\"result\":{" +
                        "\"ok\":\"\"," +
                        "\"code\":\"\"," +
                        "\"message\":\"\"," +
                        "\"anchorId\":\"\"" +
                        "}" +
                        "}," +
                        "\"instructions\":[" +
                        "\"Return ONLY the output_template with values filled.\"," +
                        "\"Do NOT return task, context, response_contract, or instructions.\"," +
                        "\"Do NOT describe or explain anything.\"," +
                        "\"Replace all empty values in output_template.\"," +
                        "\"Do NOT copy the task description into result.message.\"," +
                        "\"Set result.message to one short outcome sentence.\"," +
                        "\"Set result.code to one short outcome code.\"," +
                        "\"Set result.anchorId to task.factId.\"," +
                        "\"Set every value as a string.\"," +
                        "\"Return exactly one JSON object.\"," +
                        "\"No text before or after JSON.\"" +
                        "]" +
                        "}";

        JsonObject llmPayload = new JsonObject();
        llmPayload.addProperty("model", "llama3");
        llmPayload.addProperty("prompt", prompt);
        llmPayload.addProperty("stream", false);

        OpenAILlmAdapter adapter = new OpenAILlmAdapter();
        String response = adapter.invokeLlm(queryRequest, llmPayload);

        JsonObject root = JsonParser.parseString(response).getAsJsonObject();

        String modelOutput = root.getAsJsonObject("rawResponse").get("response").getAsString().trim();

        // eyeball focus: still executor mode + contract presence should not break behavior
        Assertions.assertTrue(modelOutput.startsWith("{"));
    }

    //-----trace reintroduction---------------
    @Test
    void invokeLlm_executor_mode_should_return_result_and_trace_objects() {

        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setMeta(new Meta("v1", "validate_flight_airports", "test"));

        String expectedRequestId = queryRequest.generateRequestId();

        String prompt =
                "You are a task execution engine.\n\n" +
                        "Return only JSON.\n" +
                        "Do not explain anything.\n" +
                        "Do not describe the input.\n" +
                        "Do not summarize the task.\n" +
                        "Use the provided output_template as the final answer format.\n" +
                        "Replace every empty value in output_template if the value can be determined from the input.\n" +
                        "Synthesize result values from the task and context.\n" +
                        "Do not copy the task description into result.message.\n" +
                        "Set result.message to one short outcome sentence.\n" +
                        "Set result.code to one short outcome code.\n" +
                        "Set result.anchorId to task.factId.\n" +
                        "Set every value in output_template as a string.\n" +
                        "Do not return booleans, numbers, or null.\n" +
                        "Return \"true\" or \"false\" as strings, not boolean values.\n" +
                        "Fill trace fields only if the value can be determined from the input or runtime context.\n" +
                        "If a trace field cannot be determined, leave it as an empty string.\n" +
                        "Return the completed result and trace objects.\n" +
                        "Do not return task, context, or instructions.\n" +
                        "Return exactly one JSON object.\n" +
                        "No text before or after JSON.\n\n" +
                        "INPUT:\n" +
                        "{\"task\":{\"description\":\"Validate that the selected flight has valid departure and arrival airport codes based on the airport nodes in the graph. A valid flight must have: (1) from matching one Airport:* code, (2) to matching one Airport:* code, (3) from != to.\",\"factId\":\"Flight:F100\",\"relatedFactIds\":[\"Airport:AUS\",\"Airport:DFW\"]},\"context\":{\"nodes\":{\"Flight:F100\":{\"id\":\"Flight:F100\",\"text\":\"{\\\"id\\\":\\\"F100\\\",\\\"kind\\\":\\\"Flight\\\",\\\"mode\\\":\\\"relational\\\",\\\"from\\\":\\\"AUS\\\",\\\"to\\\":\\\"DFW\\\"}\",\"attributes\":[],\"mode\":\"RELATIONAL\"},\"Airport:AUS\":{\"id\":\"Airport:AUS\",\"text\":\"{\\\"id\\\":\\\"AUS\\\",\\\"kind\\\":\\\"Airport\\\",\\\"code\\\":\\\"AUS\\\"}\",\"attributes\":[],\"mode\":\"ATOMIC\"},\"Airport:DFW\":{\"id\":\"Airport:DFW\",\"text\":\"{\\\"id\\\":\\\"DFW\\\",\\\"kind\\\":\\\"Airport\\\",\\\"code\\\":\\\"DFW\\\"}\",\"attributes\":[],\"mode\":\"ATOMIC\"}}},\"output_template\":{\"result\":{\"ok\":\"\",\"code\":\"\",\"message\":\"\",\"anchorId\":\"\"},\"trace\":{\"finish_reason\":\"\",\"prompt_tokens\":\"\",\"response_tokens\":\"\",\"total_tokens\":\"\",\"latency_ms\":\"\",\"model_fingerprint\":\"\"}},\"instructions\":[\"Return ONLY the output_template with values filled.\",\"Do NOT return task, context, or instructions.\",\"Do NOT describe or explain anything.\",\"Replace all empty values in output_template when determinable.\",\"Do NOT copy the task description into result.message.\",\"Set result.message to one short outcome sentence.\",\"Set result.code to one short outcome code.\",\"Set result.anchorId to task.factId.\",\"Set every value as a string.\",\"If a trace field cannot be determined, leave it as an empty string.\",\"Return exactly one JSON object.\",\"No text before or after JSON.\"]}";

        JsonObject llmPayload = new JsonObject();
        llmPayload.addProperty("model", "llama3");
        llmPayload.addProperty("prompt", prompt);
        llmPayload.addProperty("stream", false);

        OpenAILlmAdapter adapter = new OpenAILlmAdapter();
        String response = adapter.invokeLlm(queryRequest, llmPayload);

        JsonObject root = JsonParser.parseString(response).getAsJsonObject();

        Assertions.assertEquals(expectedRequestId, root.getAsJsonObject("llmRequest").get("requestId").getAsString());
        Assertions.assertEquals("validate_flight_airports", root.getAsJsonObject("llmRequest").get("queryKind").getAsString());
        Assertions.assertTrue(root.get("success").getAsBoolean());
        Assertions.assertFalse(root.get("rawResponse").isJsonNull());

        String modelOutput = root.getAsJsonObject("rawResponse").get("response").getAsString().trim();

        // eyeball focus: result quality must remain stable when trace is reintroduced
        Assertions.assertTrue(modelOutput.startsWith("{"));
    }

    @Test
    void invokeLlm_executor_mode_should_populate_determinable_trace_fields_only() {

        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setMeta(new Meta("v1", "validate_flight_airports", "test"));

        String expectedRequestId = queryRequest.generateRequestId();

        String prompt =
                "You are a task execution engine.\n\n" +
                        "Return only JSON.\n" +
                        "Do not explain anything.\n" +
                        "Do not describe the input.\n" +
                        "Do not summarize the task.\n" +
                        "Use the provided output_template as the final answer format.\n" +
                        "Replace every empty value in output_template if the value can be determined from the input.\n" +
                        "Synthesize result values from the task and context.\n" +
                        "Do not copy the task description into result.message.\n" +
                        "Set result.message to one short outcome sentence.\n" +
                        "Set result.code to one short outcome code.\n" +
                        "Set result.anchorId to task.factId.\n" +
                        "Set every value in output_template as a string.\n" +
                        "Do not return booleans, numbers, or null.\n" +
                        "Return \"true\" or \"false\" as strings, not boolean values.\n" +
                        "Set trace.finish_reason to \"stop\".\n" +
                        "If any other trace field cannot be determined from the input, leave it as an empty string.\n" +
                        "Return the completed result and trace objects.\n" +
                        "Do not return task, context, or instructions.\n" +
                        "Return exactly one JSON object.\n" +
                        "No text before or after JSON.\n\n" +
                        "INPUT:\n" +
                        "{\"task\":{\"description\":\"Validate that the selected flight has valid departure and arrival airport codes based on the airport nodes in the graph. A valid flight must have: (1) from matching one Airport:* code, (2) to matching one Airport:* code, (3) from != to.\",\"factId\":\"Flight:F100\",\"relatedFactIds\":[\"Airport:AUS\",\"Airport:DFW\"]},\"context\":{\"nodes\":{\"Flight:F100\":{\"id\":\"Flight:F100\",\"text\":\"{\\\"id\\\":\\\"F100\\\",\\\"kind\\\":\\\"Flight\\\",\\\"mode\\\":\\\"relational\\\",\\\"from\\\":\\\"AUS\\\",\\\"to\\\":\\\"DFW\\\"}\",\"attributes\":[],\"mode\":\"RELATIONAL\"},\"Airport:AUS\":{\"id\":\"Airport:AUS\",\"text\":\"{\\\"id\\\":\\\"AUS\\\",\\\"kind\\\":\\\"Airport\\\",\\\"code\\\":\\\"AUS\\\"}\",\"attributes\":[],\"mode\":\"ATOMIC\"},\"Airport:DFW\":{\"id\":\"Airport:DFW\",\"text\":\"{\\\"id\\\":\\\"DFW\\\",\\\"kind\\\":\\\"Airport\\\",\\\"code\\\":\\\"DFW\\\"}\",\"attributes\":[],\"mode\":\"ATOMIC\"}}},\"output_template\":{\"result\":{\"ok\":\"\",\"code\":\"\",\"message\":\"\",\"anchorId\":\"\"},\"trace\":{\"finish_reason\":\"\",\"prompt_tokens\":\"\",\"response_tokens\":\"\",\"total_tokens\":\"\",\"latency_ms\":\"\",\"model_fingerprint\":\"\"}},\"instructions\":[\"Return ONLY the output_template with values filled.\",\"Do NOT return task, context, or instructions.\",\"Do NOT describe or explain anything.\",\"Replace all empty values in output_template when determinable.\",\"Do NOT copy the task description into result.message.\",\"Set result.message to one short outcome sentence.\",\"Set result.code to one short outcome code.\",\"Set result.anchorId to task.factId.\",\"Set every value as a string.\",\"Set trace.finish_reason to stop.\",\"If any other trace field cannot be determined, leave it as an empty string.\",\"Return exactly one JSON object.\",\"No text before or after JSON.\"]}";

        JsonObject llmPayload = new JsonObject();
        llmPayload.addProperty("model", "llama3");
        llmPayload.addProperty("prompt", prompt);
        llmPayload.addProperty("stream", false);

        OpenAILlmAdapter adapter = new OpenAILlmAdapter();
        String response = adapter.invokeLlm(queryRequest, llmPayload);

        JsonObject root = JsonParser.parseString(response).getAsJsonObject();

        Assertions.assertEquals(expectedRequestId, root.getAsJsonObject("llmRequest").get("requestId").getAsString());
        Assertions.assertEquals("validate_flight_airports", root.getAsJsonObject("llmRequest").get("queryKind").getAsString());
        Assertions.assertTrue(root.get("success").getAsBoolean());
        Assertions.assertFalse(root.get("rawResponse").isJsonNull());

        String modelOutput = root.getAsJsonObject("rawResponse").get("response").getAsString().trim();

        // eyeball focus: result must remain stable while one trace field is populated
        Assertions.assertTrue(modelOutput.startsWith("{"));
    }

    @Test
    void invokeLlm_executor_mode_should_fully_populate_trace_fields() {

        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setMeta(new Meta("v1", "validate_flight_airports", "test"));

        String expectedRequestId = queryRequest.generateRequestId();

        String prompt =
                "You are a task execution engine.\n\n" +
                        "Return only JSON.\n" +
                        "Do not explain anything.\n" +
                        "Do not describe the input.\n" +
                        "Do not summarize the task.\n" +
                        "Use the provided output_template as the final answer format.\n" +
                        "Replace every empty value in output_template if the value can be determined from the input.\n" +
                        "Synthesize result values from the task and context.\n" +
                        "Do not copy the task description into result.message.\n" +
                        "Set result.message to one short outcome sentence.\n" +
                        "Set result.code to one short outcome code.\n" +
                        "Set result.anchorId to task.factId.\n" +
                        "Set every value in output_template as a string.\n" +
                        "Do not return booleans, numbers, or null.\n" +
                        "Return \"true\" or \"false\" as strings, not boolean values.\n" +
                        "Populate all trace fields as strings.\n" +
                        "Set trace.finish_reason to \"stop\".\n" +
                        "Set trace.prompt_tokens to a short numeric string estimate.\n" +
                        "Set trace.response_tokens to a short numeric string estimate.\n" +
                        "Set trace.total_tokens to the sum of prompt_tokens and response_tokens as a string.\n" +
                        "Set trace.latency_ms to a short numeric string estimate.\n" +
                        "Set trace.model_fingerprint to a short stable model identifier string.\n" +
                        "Return the completed result and trace objects.\n" +
                        "Do not return task, context, or instructions.\n" +
                        "Return exactly one JSON object.\n" +
                        "No text before or after JSON.\n\n" +
                        "INPUT:\n" +
                        "{\"task\":{\"description\":\"Validate that the selected flight has valid departure and arrival airport codes based on the airport nodes in the graph. A valid flight must have: (1) from matching one Airport:* code, (2) to matching one Airport:* code, (3) from != to.\",\"factId\":\"Flight:F100\",\"relatedFactIds\":[\"Airport:AUS\",\"Airport:DFW\"]},\"context\":{\"nodes\":{\"Flight:F100\":{\"id\":\"Flight:F100\",\"text\":\"{\\\"id\\\":\\\"F100\\\",\\\"kind\\\":\\\"Flight\\\",\\\"mode\\\":\\\"relational\\\",\\\"from\\\":\\\"AUS\\\",\\\"to\\\":\\\"DFW\\\"}\",\"attributes\":[],\"mode\":\"RELATIONAL\"},\"Airport:AUS\":{\"id\":\"Airport:AUS\",\"text\":\"{\\\"id\\\":\\\"AUS\\\",\\\"kind\\\":\\\"Airport\\\",\\\"code\\\":\\\"AUS\\\"}\",\"attributes\":[],\"mode\":\"ATOMIC\"},\"Airport:DFW\":{\"id\":\"Airport:DFW\",\"text\":\"{\\\"id\\\":\\\"DFW\\\",\\\"kind\\\":\\\"Airport\\\",\\\"code\\\":\\\"DFW\\\"}\",\"attributes\":[],\"mode\":\"ATOMIC\"}}},\"output_template\":{\"result\":{\"ok\":\"\",\"code\":\"\",\"message\":\"\",\"anchorId\":\"\"},\"trace\":{\"finish_reason\":\"\",\"prompt_tokens\":\"\",\"response_tokens\":\"\",\"total_tokens\":\"\",\"latency_ms\":\"\",\"model_fingerprint\":\"\"}},\"instructions\":[\"Return ONLY the output_template with values filled.\",\"Do NOT return task, context, or instructions.\",\"Do NOT describe or explain anything.\",\"Replace all empty values in output_template when determinable.\",\"Do NOT copy the task description into result.message.\",\"Set result.message to one short outcome sentence.\",\"Set result.code to one short outcome code.\",\"Set result.anchorId to task.factId.\",\"Set every value as a string.\",\"Populate all trace fields as strings.\",\"Set trace.finish_reason to stop.\",\"Set trace.prompt_tokens to a short numeric string estimate.\",\"Set trace.response_tokens to a short numeric string estimate.\",\"Set trace.total_tokens to the sum of prompt_tokens and response_tokens as a string.\",\"Set trace.latency_ms to a short numeric string estimate.\",\"Set trace.model_fingerprint to a short stable model identifier string.\",\"Return exactly one JSON object.\",\"No text before or after JSON.\"]}";

        JsonObject llmPayload = new JsonObject();
        llmPayload.addProperty("model", "llama3");
        llmPayload.addProperty("prompt", prompt);
        llmPayload.addProperty("stream", false);

        OpenAILlmAdapter adapter = new OpenAILlmAdapter();
        String response = adapter.invokeLlm(queryRequest, llmPayload);

        JsonObject root = JsonParser.parseString(response).getAsJsonObject();

        Assertions.assertEquals(expectedRequestId, root.getAsJsonObject("llmRequest").get("requestId").getAsString());
        Assertions.assertEquals("validate_flight_airports", root.getAsJsonObject("llmRequest").get("queryKind").getAsString());
        Assertions.assertTrue(root.get("success").getAsBoolean());
        Assertions.assertFalse(root.get("rawResponse").isJsonNull());

        String modelOutput = root.getAsJsonObject("rawResponse").get("response").getAsString().trim();

        // eyeball focus: full result + full trace population
        Assertions.assertTrue(modelOutput.startsWith("{"));
    }

    //---minimum_sematic_instruction_set----
    @Test
    void invokeLlm_executor_mode_should_hold_with_reduced_instruction_set_v3_code_ok_consistency() {

        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setMeta(new Meta("v1", "validate_flight_airports", "test"));

        String expectedRequestId = queryRequest.generateRequestId();

        String prompt =
                "You are a task execution engine.\n\n" +
                        "Return only JSON.\n" +
                        "Use the provided output_template as the final answer format.\n" +
                        "Set result.ok to \"true\" if the validation passes, otherwise \"false\".\n" +
                        "Set result.message to one short outcome sentence.\n" +
                        "Set result.code consistently with result.ok:\n" +
                        "- \"OK\" when result.ok is \"true\"\n" +
                        "- \"FAIL\" when result.ok is \"false\"\n" +
                        "- \"WARN\" only when the outcome is partial or uncertain\n" +
                        "Set result.anchorId to task.factId.\n" +
                        "Set every value in output_template as a string.\n" +
                        "Return \"true\" or \"false\" as strings, not boolean values.\n" +
                        "Return exactly one JSON object.\n\n" +
                        "INPUT:\n" +
                        "{\"task\":{\"description\":\"Validate that the selected flight has valid departure and arrival airport codes based on the airport nodes in the graph. A valid flight must have: (1) from matching one Airport:* code, (2) to matching one Airport:* code, (3) from != to.\",\"factId\":\"Flight:F100\",\"relatedFactIds\":[\"Airport:AUS\",\"Airport:DFW\"]},\"context\":{\"nodes\":{\"Flight:F100\":{\"id\":\"Flight:F100\",\"text\":\"{\\\"id\\\":\\\"F100\\\",\\\"kind\\\":\\\"Flight\\\",\\\"mode\\\":\\\"relational\\\",\\\"from\\\":\\\"AUS\\\",\\\"to\\\":\\\"DFW\\\"}\",\"attributes\":[],\"mode\":\"RELATIONAL\"},\"Airport:AUS\":{\"id\":\"Airport:AUS\",\"text\":\"{\\\"id\\\":\\\"AUS\\\",\\\"kind\\\":\\\"Airport\\\",\\\"code\\\":\\\"AUS\\\"}\",\"attributes\":[],\"mode\":\"ATOMIC\"},\"Airport:DFW\":{\"id\":\"Airport:DFW\",\"text\":\"{\\\"id\\\":\\\"DFW\\\",\\\"kind\\\":\\\"Airport\\\",\\\"code\\\":\\\"DFW\\\"}\",\"attributes\":[],\"mode\":\"ATOMIC\"}}},\"output_template\":{\"result\":{\"ok\":\"\",\"code\":\"\",\"message\":\"\",\"anchorId\":\"\"}},\"instructions\":[\"Return ONLY the output_template with values filled.\",\"Set result.ok to true if validation passes, otherwise false.\",\"Set result.message to one short outcome sentence.\",\"Set result.code consistently with result.ok: OK when ok is true, FAIL when ok is false, WARN only when the outcome is partial or uncertain.\",\"Set result.anchorId to task.factId.\",\"Set every value as a string.\",\"Return exactly one JSON object.\"]}";

        JsonObject llmPayload = new JsonObject();
        llmPayload.addProperty("model", "llama3");
        llmPayload.addProperty("prompt", prompt);
        llmPayload.addProperty("stream", false);

        OpenAILlmAdapter adapter = new OpenAILlmAdapter();
        String response = adapter.invokeLlm(queryRequest, llmPayload);

        JsonObject root = JsonParser.parseString(response).getAsJsonObject();

        Assertions.assertEquals(expectedRequestId, root.getAsJsonObject("llmRequest").get("requestId").getAsString());
        Assertions.assertEquals("validate_flight_airports", root.getAsJsonObject("llmRequest").get("queryKind").getAsString());
        Assertions.assertTrue(root.get("success").getAsBoolean());
        Assertions.assertFalse(root.get("rawResponse").isJsonNull());

        String modelOutput = root.getAsJsonObject("rawResponse").get("response").getAsString().trim();

        // eyeball focus: reduced instruction set + code/ok semantic consistency
        Assertions.assertTrue(modelOutput.startsWith("{"));
    }
}
