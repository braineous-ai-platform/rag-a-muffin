package ai.braineous.rag.prompt.cgo.api;

import ai.braineous.rag.prompt.cgo.query.Node;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PromptInputTests {
    @Test
    void generate_fromJson_shouldPopulatePromptInput() {
        System.out.println("console.log -> test start: generate_fromJson_shouldPopulatePromptInput");

        String json = "{\n" +
                "  \"meta\": {\n" +
                "    \"version\": \"v1\",\n" +
                "    \"query_kind\": \"validate_flight_airports\",\n" +
                "    \"description\": \"Validate that the selected flight fact has valid departure and arrival airport codes using graph context.\"\n" +
                "  },\n" +
                "  \"context\": {\n" +
                "    \"nodes\": {\n" +
                "      \"Flight:F100\": {\n" +
                "        \"id\": \"Flight:F100\",\n" +
                "        \"text\": \"{\\\"id\\\":\\\"F100\\\",\\\"kind\\\":\\\"Flight\\\",\\\"mode\\\":\\\"relational\\\",\\\"from\\\":\\\"AUS\\\",\\\"to\\\":\\\"DFW\\\"}\",\n" +
                "        \"attributes\": [],\n" +
                "        \"mode\": \"relational\"\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"task\": {\n" +
                "    \"description\": \"Validate that the selected flight has valid departure and arrival airport codes based on the airport nodes in the graph. A valid flight must have: (1) 'from' matching one Airport:* code, (2) 'to' matching one Airport:* code, (3) 'from' != 'to'.\",\n" +
                "    \"factId\": \"Flight:F100\"\n" +
                "  },\n" +
                "  \"response_contract\": {\n" +
                "    \"type\": \"validation_result\",\n" +
                "    \"description\": \"Standard response for fact-level validation over a graph context.\",\n" +
                "    \"schema\": { \"result\": { \"type\": \"object\" } }\n" +
                "  },\n" +
                "  \"instructions\": [\n" +
                "    \"Return a single JSON object that strictly follows this schema.\",\n" +
                "    \"Do not include any fields not listed in this schema.\",\n" +
                "    \"Do not add natural language outside of JSON.\"\n" +
                "  ],\n" +
                "  \"llm_instructions\": [\n" +
                "    \"You are given a JSON object with 'meta', 'context', 'task', 'response_contract', and 'llm_instructions' fields.\",\n" +
                "    \"Use only the 'context' and 'task' to understand what needs to be computed.\",\n" +
                "    \"Locate the node in 'context.nodes' whose 'id' matches 'task.factId'.\",\n" +
                "    \"Treat that node's 'text' field as JSON-encoded data and parse fields like 'id', 'kind', 'from', 'to'.\",\n" +
                "    \"Use the 'response_contract' as the single source of truth for the shape of your reply.\",\n" +
                "    \"Your answer must be a single JSON object that matches the 'result' schema in response_contract.\",\n" +
                "    \"Do not include any keys that are not defined in the response_contract schema.\",\n" +
                "    \"Do not include any natural language outside of JSON.\"\n" +
                "  ]\n" +
                "}";

        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        System.out.println("console.log -> parsed json keys: " + root.keySet());

        PromptInput parsed = new PromptInput().generate(root);
        assertNotNull(parsed);

        // meta
        assertNotNull(parsed.getMeta());
        assertEquals("v1", parsed.getMeta().getVersion());
        assertEquals("validate_flight_airports", parsed.getMeta().getQueryKind());
        assertNotNull(parsed.getMeta().getDescription());
        System.out.println("console.log -> meta ok");

        // context.nodes
        assertNotNull(parsed.getGraphContext());
        assertNotNull(parsed.getGraphContext().getNodes());
        assertEquals(1, parsed.getGraphContext().getNodes().size());
        assertTrue(parsed.getGraphContext().getNodes().containsKey("Flight:F100"));

        Node node = parsed.getGraphContext().getNodes().get("Flight:F100");
        assertNotNull(node);
        assertEquals("Flight:F100", node.getId());
        assertNotNull(node.getText());
        assertEquals(Node.Mode.RELATIONAL, node.getMode());
        System.out.println("console.log -> node ok: " + node.getId());

        // task
        assertNotNull(parsed.getTask());
        assertEquals("Flight:F100", ((ValidateTask) parsed.getTask()).getFactId());
        assertNotNull(parsed.getTask().getDescription());
        System.out.println("console.log -> task ok");

        // response contract
        assertNotNull(parsed.getResponseContract());
        assertEquals("validation_result", parsed.getResponseContract().getType());
        assertNotNull(parsed.getResponseContract().getSchema());
        assertTrue(parsed.getResponseContract().getSchema().has("result"));
        System.out.println("console.log -> response_contract ok");

        // instructions
        assertNotNull(parsed.getInstructions());
        assertEquals(3, parsed.getInstructions().size());
        assertEquals(
                "Return a single JSON object that strictly follows this schema.",
                parsed.getInstructions().get(0).getText()
        );

        // llm instructions
        assertNotNull(parsed.getLlmInstructions());
        assertTrue(parsed.getLlmInstructions().size() >= 5);

        System.out.println("console.log -> test end: PASS");
    }

}
